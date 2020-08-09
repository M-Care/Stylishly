package dynasty.software.the.stylishly.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.parse.ParseUser;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;
import dynasty.software.the.stylishly.R;
import dynasty.software.the.stylishly.models.ChatMessage;
import dynasty.software.the.stylishly.ui.base.BaseViewHolder;
import dynasty.software.the.stylishly.utils.Util;

/**
 * Author : Aduraline.
 */

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatMessage> mChatMessages = new ArrayList<>();
    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private String username = "";
    public static final SimpleDateFormat SDF = new SimpleDateFormat("hh:mm", Locale.getDefault());

    public static final int CHAT_IN = 1, CHAT_OUT = 2, PHOTO_IN = 3, PHOTO_OUT = 4;

    public ChatAdapter() {}

    public ChatAdapter(Context context, List<ChatMessage> messages) {
        mChatMessages = messages;
        mContext = context;
        ParseUser parseUser = ParseUser.getCurrentUser();
        if (parseUser != null)
            username = parseUser.getUsername();

        if (mContext != null)
            mLayoutInflater = LayoutInflater.from(mContext);
    }
    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (mContext == null) {
            mContext = parent.getContext();
            mLayoutInflater = LayoutInflater.from(mContext);
        }

        switch (viewType) {
            case CHAT_IN:
                return new ChatInViewHolder(view(R.layout.layout_chat_in, parent));
            case CHAT_OUT:
                return new ChatOutViewHolder(view(R.layout.layout_chat_out, parent));
            case PHOTO_IN:
                return new PhotoInViewHolder(view(R.layout.layout_photo_in, parent));
            case PHOTO_OUT:
                return new PhotoOutViewHolder(view(R.layout.layout_photo_out, parent));
        }
        //??WTF?
        return null;
    }

    private View view(int layoutId, ViewGroup viewGroup) {
        return mLayoutInflater.inflate(layoutId, viewGroup, false);
    }

    @Override
    public int getItemViewType(int position) {

        ChatMessage message = mChatMessages.get(position);

        if (message.from.trim().equalsIgnoreCase(username) &&
                message.isAttachment) {
            return PHOTO_OUT;
        }else if (message.from.trim().equalsIgnoreCase(username) &&
                !message.isAttachment) {
            return CHAT_OUT;
        }else if (!message.from.trim().equalsIgnoreCase(username) &&
                !message.isAttachment) {
            return CHAT_IN;
        }else {
            return CHAT_OUT;
        }
    }

    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position) {

        final int idx = position;
        ChatMessage chatMessage = mChatMessages.get(idx);

        if (holder instanceof ChatInViewHolder) {
            ChatInViewHolder chatInViewHolder = (ChatInViewHolder) holder;
            chatInViewHolder.messateTextView.setText(chatMessage.text);

            chatInViewHolder.timeReceivedChatIn.setText(SDF.format(new Date(chatMessage.dateTimeLong)));

        }else if (holder instanceof ChatOutViewHolder) {
            ChatOutViewHolder chatOutViewHolder = (ChatOutViewHolder) holder;
            chatOutViewHolder.messageTextView.setText(chatMessage.text);

            if (chatMessage.sending) {
                chatOutViewHolder.statusTextView.setVisibility(View.VISIBLE);
                chatOutViewHolder.statusTextView.setText("Sending...");
            } else {
                try {
                    chatOutViewHolder.statusTextView.setText(SDF.format(new Date(chatMessage.dateTimeLong)));
                }catch (Exception e) {
                    chatOutViewHolder.statusTextView.setText("Sent");
                }
            }

        }else if (holder instanceof PhotoInViewHolder) {
            String path = chatMessage.attachmentPath;
            PhotoInViewHolder photoInViewHolder = (PhotoInViewHolder) holder;
            if (path != null && !path.isEmpty()) {
                Glide.with(mContext)
                        .load(path)
                        .apply(Util.requestOptions())
                        .into(photoInViewHolder.imageView);
            }else {
                //??
            }
        }else {
            PhotoOutViewHolder photoOutViewHolder = (PhotoOutViewHolder) holder;
            String path = chatMessage.attachmentPath;
            if (path != null && !path.isEmpty()) {
                Glide.with(mContext)
                        .load(new File(path))
                        .apply(Util.requestOptions())
                        .into(photoOutViewHolder.imageView);
            }

            if (chatMessage.sending) {
                photoOutViewHolder.progressLayout.setVisibility(View.VISIBLE);
                photoOutViewHolder.timeSentTextView.setVisibility(View.GONE);
            }else {
                photoOutViewHolder.timeSentTextView.setText(SDF.format(new Date(chatMessage.dateTimeLong)));
                photoOutViewHolder.timeSentTextView.setVisibility(View.VISIBLE);
                photoOutViewHolder.progressLayout.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mChatMessages.size();
    }

    class ChatViewHolder extends BaseViewHolder {

        public ChatViewHolder(View itemView) {
            super(itemView);
        }
    }

    class ChatInViewHolder extends ChatViewHolder {

        @BindView(R.id.tv_text_chat_in)
        TextView messateTextView;
        @BindView(R.id.iv_user_chat_in)
        CircleImageView circleImageView;
        @BindView(R.id.tv_time_receieved_chat_in)
        TextView timeReceivedChatIn;

        public ChatInViewHolder(View itemView) {
            super(itemView);
        }
    }

    class ChatOutViewHolder extends ChatViewHolder {

        @BindView(R.id.tv_text_chat_out)
        TextView messageTextView;
        @BindView(R.id.tv_status_chat_out)
        TextView statusTextView;

        public ChatOutViewHolder(View itemView) {
            super(itemView);
        }
    }

    class PhotoInViewHolder extends ChatViewHolder {

        @BindView(R.id.tv_label_photo_in)
        TextView labelTextView;
        @BindView(R.id.iv_layout_photo_in)
        ImageView imageView;
        @BindView(R.id.pw_photo_in)
        ProgressWheel progressWheel;

        public PhotoInViewHolder(View itemView) {
            super(itemView);
        }
    }

    class PhotoOutViewHolder extends ChatViewHolder {

        @BindView(R.id.iv_layout_photo_out)
        ImageView imageView;
        @BindView(R.id.layout_progress_photo_out)
        LinearLayout progressLayout;
        @BindView(R.id.tv_photo_time_sent_photo_out)
        TextView timeSentTextView;

        public PhotoOutViewHolder(View itemView) {
            super(itemView);
        }
    }
}
