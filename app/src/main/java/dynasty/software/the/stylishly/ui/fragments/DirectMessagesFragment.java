package dynasty.software.the.stylishly.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import dynasty.software.the.stylishly.R;
import dynasty.software.the.stylishly.StylishlyApplication;
import dynasty.software.the.stylishly.models.ChatMessage;
import dynasty.software.the.stylishly.models.ChatUser;
import dynasty.software.the.stylishly.models.Conversation;
import dynasty.software.the.stylishly.repo.RepositoryManager;
import dynasty.software.the.stylishly.ui.activities.HomeActivity;
import dynasty.software.the.stylishly.ui.adapters.ConversationAdapter;
import dynasty.software.the.stylishly.ui.base.BaseFragment;
import dynasty.software.the.stylishly.utils.KEYS;
import dynasty.software.the.stylishly.utils.L;

/**
 * Author : Aduraline.
 */

public class DirectMessagesFragment extends BaseFragment {

    @BindView(R.id.swipe_layout_dm_fragment)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.rv_dm_fragment)
    RecyclerView mRecyclerView;
    @BindView(R.id.toolbar_dm)
    Toolbar mToolbar;
    @BindView(R.id.no_dm_fragment)
    LinearLayout noDmLinearLayout;

    public static final String TAG = "DirectMessagesTag";
    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy, hh:mm", Locale.getDefault());

    private ConversationAdapter conversationAdapter;
    private List<Conversation> mConversations = new ArrayList<>();
    private HomeActivity homeActivity;
    private volatile boolean shouldLoadOnResume = false;


    public static DirectMessagesFragment newInstance() {

        Bundle args = new Bundle();

        DirectMessagesFragment fragment = new DirectMessagesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_fragment_direct_messages, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        homeActivity = (HomeActivity) getActivity();
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        mToolbar.setTitle("Direct Messages");

        prepareRecyclerView();

        boolean hasSynced = RepositoryManager.manager().preferences().getBoolean("chat_synced", false);
        if (hasSynced) {
            loadConversations();
            L.fine("Loading Local conversations...");
        }else {
            L.fine("Sync started...");
            syncConversation();
        }
    }

    private void syncConversation() {

        ParseUser parseUser = ParseUser.getCurrentUser();
        if (parseUser == null) return;

        swipeRefreshLayout.setRefreshing(true);
        final Handler  handler = new Handler(Looper.getMainLooper());

        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery(KEYS.Objects.CHATS);
        parseQuery.whereEqualTo("from", parseUser.getUsername());
        parseQuery.whereEqualTo("to", parseUser.getUsername());
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {

                if (e != null) {
                    L.wtf(e);
                    snack("Failed to load conversation. Please retry");
                    return;
                }

                final List<ChatMessage> chatMessages = new ArrayList<>();
                for (ParseObject parseObject : list) {
                    ChatMessage chatMessage = new ChatMessage(parseObject);
                    chatMessages.add(chatMessage);
                }

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {

                        for (ChatMessage chatMessage : chatMessages) {

                            RepositoryManager.manager().database().messageDao().newMessage(chatMessage);
                            Conversation conversation =
                                    RepositoryManager.manager().database().conversationDao().getConversation(chatMessage.conversationId);
                            if (conversation == null) {
                                conversation = new Conversation();
                                conversation.conversationId = chatMessage.to;
                                ChatUser chatUser = new ChatUser();
                                chatUser.username = chatMessage.to;
                                chatUser.photoUri = "";
                                conversation.user = chatUser.json();
                                RepositoryManager.manager().database().conversationDao().createConversation(conversation);
                                RepositoryManager.manager().database().conversationDao().updateConversation(chatMessage.text,
                                        chatMessage.conversationId);
                            }else {
                                RepositoryManager.manager().database().conversationDao().updateConversation(chatMessage.text, chatMessage.conversationId);
                            }
                        }
                        RepositoryManager.manager().preferences().edit().putBoolean("chat_synced", true).apply();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                loadConversations();
                            }
                        });
                    }
                };

                StylishlyApplication.getApplication().getExecutorService().execute(runnable);
            }
        });
    }

    private void prepareRecyclerView() {

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        conversationAdapter = new ConversationAdapter(mConversations, getActivity());
        mRecyclerView.setAdapter(conversationAdapter);

    }

    private void loadConversations() {

        swipeRefreshLayout.setRefreshing(true);
        List<Conversation> conversations = RepositoryManager.manager().database().conversationDao().load();
        L.fine("Conv ==> " + conversations.size());

        for (int i = 0; i < conversations.size(); i++) {
            Conversation conversation = conversations.get(i);
            conversation.dateTime =
                    conversation.dateTimeLong == 0 ? SIMPLE_DATE_FORMAT.format(new Date(System.currentTimeMillis()))
                            : SIMPLE_DATE_FORMAT.format(new Date(conversation.dateTimeLong));

            conversation.unReadCount =
                    RepositoryManager.manager().database().messageDao().unReadMessages(conversation.conversationId, false).size();

            L.fine("Conversation with " + conversation.user + " Iteration => " + i);
            mConversations.add(conversation);
        }

        swipeRefreshLayout.setRefreshing(false);
        if (conversationAdapter != null)
            conversationAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (homeActivity == null)
            homeActivity = (HomeActivity) getActivity();

        if (homeActivity != null)
            homeActivity.showSystemUI();

        if (mConversations.size() > 0)
            mConversations.clear();
        loadConversations();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        homeActivity = null;
    }
}
