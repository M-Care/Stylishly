package dynasty.software.the.stylishly.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SendCallback;
import com.zxy.tiny.Tiny;
import com.zxy.tiny.callback.FileCallback;

import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import dynasty.software.the.stylishly.R;
import dynasty.software.the.stylishly.StylishlyApplication;
import dynasty.software.the.stylishly.models.ChatMessage;
import dynasty.software.the.stylishly.models.ChatUser;
import dynasty.software.the.stylishly.models.Conversation;
import dynasty.software.the.stylishly.reciever.StylishlyPushReceiver;
import dynasty.software.the.stylishly.repo.RepositoryManager;
import dynasty.software.the.stylishly.repo.dao.ChatMessageDao;
import dynasty.software.the.stylishly.repo.dao.ConversationDao;
import dynasty.software.the.stylishly.ui.adapters.ChatAdapter;
import dynasty.software.the.stylishly.ui.base.BaseActivity;
import dynasty.software.the.stylishly.utils.KEYS;
import dynasty.software.the.stylishly.utils.L;
import dynasty.software.the.stylishly.utils.Util;
import tgio.parselivequery.BaseQuery;
import tgio.parselivequery.LiveQueryEvent;
import tgio.parselivequery.Subscription;
import tgio.parselivequery.interfaces.OnListener;

/**
 * Author : Aduraline.
 */

public class ChatActivity extends BaseActivity {


    @BindView(R.id.edt_chat_activity_chat)
    EditText chatEditText;
    @BindView(R.id.rv_chats_activity_chat)
    RecyclerView mRecyclerView;


    private ParseUser parseUser;
    private Conversation mConversation;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> mChatMessages = new ArrayList<>();
    private ConversationDao conversationDao;
    private ChatUser chatUser;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Subscription mSubscription;
    private List<String> messagesStrings = new ArrayList<>();

    public static final int RC_PICK_PHOTO = 11;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_chat);
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        chatUser = Parcels.unwrap(intent.getParcelableExtra(ChatUser.KEY));
        parseUser = ParseUser.getCurrentUser();
        createConversation(chatUser);
        prepareRecyclerView();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(chatUser.username);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setSubtitle("Chatting with " + chatUser.username);
        }

        loadPreviousMessages();
    }

    private void loadPreviousMessages() {

        final Handler handler = new Handler(Looper.getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                final List<ChatMessage> chatMessages =
                        RepositoryManager.manager().database().messageDao().messagesWith(mConversation.conversationId);

                RepositoryManager.manager().database().messageDao().readMessages(mConversation.conversationId, true);

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        mChatMessages.clear();
                        mChatMessages.addAll(chatMessages);

                        for (ChatMessage chatMessage : mChatMessages) {
                            chatMessage.sending = false;
                            messagesStrings.add(chatMessage.text);
                        }

                        if (chatAdapter != null)
                            chatAdapter.notifyDataSetChanged();

                        if (!mChatMessages.isEmpty()) {
                            mRecyclerView.smoothScrollToPosition(mChatMessages.size() - 1);
                        }
                    }
                });
            }
        };

        StylishlyApplication.getApplication().getExecutorService().execute(runnable);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            try {
                JSONObject jsonObject = new JSONObject(intent.getStringExtra("message_data"));
                ChatMessage chatMessage = ChatMessage.from(jsonObject);
                chatMessage.read = true;

                RepositoryManager.manager().database().messageDao().newMessage(chatMessage);
                RepositoryManager.manager().database().conversationDao().updateConversation(chatMessage.text, chatMessage.conversationId);

                mChatMessages.add(chatMessage);

                if (chatAdapter != null)
                    chatAdapter.notifyDataSetChanged();

                if (mChatMessages.size() > 0)
                    mRecyclerView.smoothScrollToPosition(mChatMessages.size() - 1);

            }catch (Exception e) {}

        }
    };
    private void prepareRecyclerView() {

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(this, mChatMessages);
        mRecyclerView.setAdapter(chatAdapter);
    }

    private void createConversation(ChatUser chatUser) {

        conversationDao = RepositoryManager.manager().database().conversationDao();
        mConversation = conversationDao.getConversation(chatUser.username.trim());
        if (mConversation == null) {
            L.fine("Creating new conversation with " + chatUser.username);
            mConversation = new Conversation();
            mConversation.conversationId = chatUser.username;
            mConversation.lastMessage = "";
            mConversation.user = chatUser.json();
            mConversation.dateTimeLong = System.currentTimeMillis();

            conversationDao.createConversation(mConversation);
        }else {
            L.fine("Conversation exists ==> " + mConversation.user);
        }
    }

    @OnClick(R.id.btn_send_message_activity_chat) public void onSendMessageClick() {

        final String messageText = Util.textOf(chatEditText);
        if (messageText.isEmpty())
            return;

        ChatMessageDao chatMessageDao = RepositoryManager.manager().database().messageDao();
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.isAttachment = false;
        chatMessage.from = parseUser.getUsername();
        chatMessage.text = messageText;
        chatMessage.conversationId = chatUser.username;
        chatMessage.to = chatUser.username;
        chatMessage.dateTimeLong = System.currentTimeMillis();
        chatMessage.senderPhoto = parseUser.getString("photo_uri");
        chatMessage.sending = true;
        chatMessage.read = true;


        sendMessage(chatMessage);
        conversationDao.updateConversation(messageText, mConversation.conversationId);
        chatMessageDao.newMessage(chatMessage);

        mChatMessages.add(chatMessage);
        chatEditText.setText("");

        if (chatAdapter != null)
            chatAdapter.notifyDataSetChanged();

        int pos = mChatMessages.size() - 1;
        mRecyclerView.smoothScrollToPosition(pos);
    }

    private void subscribeToLiveQuery() {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

               try {
                   if (mSubscription == null || !mSubscription.isSubscribed()) {
                       L.fine("Subscribing to LiveQuery");
                       mSubscription = new BaseQuery.Builder(KEYS.Objects.CHATS)
                               .where("to", parseUser.getUsername().trim())
                               .build()
                               .subscribe();

                       //mSubscription.on(LiveQueryEvent.CREATE, onListener);
                   }
               }catch (Exception e) {
                   L.wtf(e);
               }
            }
        };

        StylishlyApplication.getApplication().getExecutorService().execute(runnable);
    }


    private OnListener onListener = new OnListener() {
        @Override
        public void on(JSONObject object) {

            if (object == null) return;

            L.fine("New Message " + object.toString());

            try {

                JSONObject jsonObject = object.getJSONObject("object");

                final String from = jsonObject.getString("from");
                L.fine("Message From " + from);

                if (!from.trim().equalsIgnoreCase(chatUser.username.trim())) {
                    L.fine("Invalid message!");
                    return;
                }

                ChatMessage chatMessage = ChatMessage.from(jsonObject);
                chatMessage.conversationId = mConversation.conversationId;
                mChatMessages.add(chatMessage);

                conversationDao.updateConversation(chatMessage.text, mConversation.conversationId);
                RepositoryManager.manager().database().messageDao().newMessage(chatMessage);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (chatAdapter != null)
                            chatAdapter.notifyDataSetChanged();

                        int pos = mChatMessages.size() - 1;
                        if (pos > 0)
                            mRecyclerView.smoothScrollToPosition(pos);
                    }
                });

            }catch (Exception e) {
                L.wtf("Error parsing json " + object.toString() + " ==> " +e);
            }
        }
    };

    private void sendMessage(final ChatMessage chatMessage) {

        ParseObject parseObject = chatMessage.toParseObject();
        parseObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {

                if (e != null) {
                    L.wtf("Failed to send message " + e);
                    return;
                }

                int idx = mChatMessages.indexOf(chatMessage);
                mChatMessages.remove(idx);
                chatMessage.sending = false;
                mChatMessages.add(idx, chatMessage);

                if (chatAdapter != null)
                    chatAdapter.notifyDataSetChanged();


                int count = mChatMessages.size();
                if (count > 0)
                    mRecyclerView.smoothScrollToPosition(count - 1);
                L.fine("Message Sent!");

                sendPush(chatMessage);
            }
        });
    }

    @Override
    protected void onPause() {

        if (mSubscription != null && mSubscription.isSubscribed()) {
            unSubscribeToLiveQuery();
        }

        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        subscribeToLiveQuery();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(StylishlyPushReceiver.LOCAL_PUSH_RECEIVED));
    }

    /*
    * UnSubscribe from LiveQuery, must be done on background thread.
    * */
    private void unSubscribeToLiveQuery() {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    mSubscription.unsubscribe();
                }catch (Exception e) {}
            }
        };

        StylishlyApplication.getApplication().getExecutorService().execute(runnable);
    }

    /*
    * Recursively send notification to the user on the other end of the chat.
    * */
    private void sendPush(final ChatMessage chatMessage) {

        try {
            JSONObject jsonObject = new JSONObject(chatMessage.toJson());
            ParsePush parsePush = new ParsePush();
            parsePush.setChannel(chatUser.username.trim());
            parsePush.setData(jsonObject);
            parsePush.sendInBackground(new SendCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        L.wtf("Error sending push ==> " + e);

                        sendPush(chatMessage); //Retry indefinitely
                        return;
                    }

                    L.fine("Push sent!");
                }
            });
        }catch (Exception e) {
            L.wtf(e);

        }
    }

    @OnClick(R.id.btn_add_photo_activity_chat) public void onAddPhotoClick() {

        ImagePicker.create(this)
                .folderMode(true)
                .single()
                .theme(R.style.ImagePickerTheme)
                .toolbarImageTitle("Send Photo")
                .toolbarFolderTitle("Send Photo")
                .start(RC_PICK_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {

            switch (requestCode) {
                case RC_PICK_PHOTO:
                    Image image = ImagePicker.getFirstImageOrNull(data);
                    resizePhoto(image);
                    break;
            }
        }
    }

    private void resizePhoto(Image image) {

        Tiny.getInstance().source(image.getPath())
                .asFile().compress(new FileCallback() {
            @Override
            public void callback(boolean isSuccess, String outfile, Throwable t) {

                if (t != null) {
                    toast("Failed to send photo. Unable to process photo.");
                    return;
                }

                sendPhoto(outfile);
            }
        });
    }

    private void sendPhoto(String outfile) {

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.isAttachment = true;
        chatMessage.attachmentPath = outfile;
        chatMessage.text = "";
        chatMessage.sending = true;
        chatMessage.dateTimeLong = System.currentTimeMillis();
        mChatMessages.add(chatMessage);

        if (chatAdapter != null)
            chatAdapter.notifyDataSetChanged();

        sendMessage(chatMessage);
    }
}
