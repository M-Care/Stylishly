package dynasty.software.the.stylishly.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;
import dynasty.software.the.stylishly.R;
import dynasty.software.the.stylishly.models.ChatUser;
import dynasty.software.the.stylishly.models.Conversation;
import dynasty.software.the.stylishly.ui.activities.ChatActivity;
import dynasty.software.the.stylishly.ui.base.BaseViewHolder;
import dynasty.software.the.stylishly.utils.L;
import dynasty.software.the.stylishly.utils.Util;

/**
 * Author : Aduraline.
 */

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {


    private List<Conversation> conversationList = new ArrayList<>();
    private LayoutInflater mLayoutInflater;
    private Context mContext;

    public ConversationAdapter(List<Conversation> conversationList, Context mContext) {
        this.conversationList = conversationList;
        this.mContext = mContext;

        if (mContext != null)
            mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public ConversationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (mLayoutInflater == null)
            mLayoutInflater = LayoutInflater.from(parent.getContext());

        return new ConversationViewHolder(mLayoutInflater.inflate(R.layout.layout_conversation, parent, false));
    }

    @Override
    public void onBindViewHolder(ConversationViewHolder holder, int position) {

        final Conversation conversation = conversationList.get(position);
        final ChatUser chatUser = new Gson().fromJson(conversation.user, ChatUser.class);
        holder.lastMessageTextView.setText(conversation.lastMessage);
        holder.usernameTextView.setText(chatUser.username);
        holder.timeSentTextView.setText(conversation.dateTime);

        final String photo = chatUser.photoUri;
        if (!photo.isEmpty()) {
            Glide.with(mContext)
                    .load(photo)
                    .apply(Util.requestOptions())
                    .into(holder.circleImageView);
        }

        String unReadCountText = String.valueOf(conversation.unReadCount).length() == 1
                ? String.valueOf(conversation.unReadCount + " ") : String.valueOf(conversation.unReadCount);
        if (conversation.hasUnReadMessages()) {
            holder.unReadMessagesCountTextView.setText(unReadCountText);
            holder.unReadMessagesCountTextView.setVisibility(View.VISIBLE);
        }else {
            holder.unReadMessagesCountTextView.setVisibility(View.GONE);
        }

        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra(ChatUser.KEY, Parcels.wrap(chatUser));
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    class ConversationViewHolder extends BaseViewHolder {

        @BindView(R.id.tv_username_conversation)
        TextView usernameTextView;
        @BindView(R.id.tv_last_message_conversation)
        TextView lastMessageTextView;
        @BindView(R.id.tv_time_sent_conversation)
        TextView timeSentTextView;
        @BindView(R.id.iv_photo_conversation)
        CircleImageView circleImageView;
        @BindView(R.id.tv_unread_messages_count_layout_conversation)
        TextView unReadMessagesCountTextView;
        @BindView(R.id.conversation_root)
        RelativeLayout root;

        public ConversationViewHolder(View itemView) {
            super(itemView);
        }
    }
}
