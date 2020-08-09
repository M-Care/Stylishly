package dynasty.software.the.stylishly.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hendraanggrian.socialview.SocialView;
import com.hendraanggrian.widget.SocialTextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import dynasty.software.the.stylishly.R;
import dynasty.software.the.stylishly.models.Notification;
import dynasty.software.the.stylishly.models.Post;
import dynasty.software.the.stylishly.ui.activities.ProfileActivity;
import dynasty.software.the.stylishly.ui.base.BaseViewHolder;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;

/**
 * Author : Aduraline.
 */

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<Notification> notificationList = new ArrayList<>();
    private Context mContext;
    private LayoutInflater mLayoutInflater;

    public NotificationAdapter(Context context, List<Notification> notifications) {
        notificationList = notifications;
        mContext = context;
        if (mContext != null)
            mLayoutInflater = LayoutInflater.from(mContext);
    }
    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mLayoutInflater == null)
            mLayoutInflater = LayoutInflater.from(parent.getContext());

        return new NotificationViewHolder(mLayoutInflater.inflate(R.layout.layout_notification_type_new_follower, parent, false));
    }

    @Override
    public void onBindViewHolder(NotificationViewHolder holder, int position) {

        Notification notification = notificationList.get(position);
        String message = String.format("%s%s%s", "@" , notification.fromUsername, " started following you.");
        holder.socialTextView.setText(message);
        holder.timeTextView.setText(notification.dateTime);

        holder.socialTextView.setOnMentionClickListener(new Function2<SocialView, String, Unit>() {
            @Override
            public Unit invoke(SocialView socialView, String s) {

                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra(ProfileActivity.KEY_USERNAME, s.trim());
                mContext.startActivity(intent);
                return null;
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    class NotificationViewHolder extends BaseViewHolder {

        @BindView(R.id.tv_notification_message_new_follower_item)
        SocialTextView socialTextView;
        @BindView(R.id.tv_time_new_follower_notification_item)
        TextView timeTextView;

        public NotificationViewHolder(View itemView) {
            super(itemView);
        }
    }
}
