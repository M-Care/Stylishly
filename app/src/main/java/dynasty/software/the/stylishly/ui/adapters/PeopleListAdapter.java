package dynasty.software.the.stylishly.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;
import dynasty.software.the.stylishly.R;
import dynasty.software.the.stylishly.models.User;
import dynasty.software.the.stylishly.ui.activities.ProfileActivity;
import dynasty.software.the.stylishly.ui.base.BaseViewHolder;
import dynasty.software.the.stylishly.utils.KEYS;
import dynasty.software.the.stylishly.utils.L;
import dynasty.software.the.stylishly.utils.Util;

/**
 * Author : Aduraline.
 */

public class PeopleListAdapter extends RecyclerView.Adapter<PeopleListAdapter.PeopleListViewHolder> {

    private List<User> accounts = new ArrayList<>();
    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private IInterationListener iInterationListener;

    public PeopleListAdapter(Context context, List<User> users) {
        accounts = users;
        mContext = context;

        if (mContext != null)
            mLayoutInflater = LayoutInflater.from(mContext);
    }

    public void setiInterationListener(IInterationListener iInterationListener) {
        this.iInterationListener = iInterationListener;
    }

    @Override
    public PeopleListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mLayoutInflater == null)
            mLayoutInflater = LayoutInflater.from(parent.getContext());

        return new PeopleListViewHolder(mLayoutInflater.inflate(R.layout.layout_people, parent, false));
    }

    @Override
    public void onBindViewHolder(PeopleListViewHolder holder, int position) {

        final int idx = position;
        final User next = accounts.get(idx);
        holder.followerCountTextView.setText(String.valueOf(next.followerCount > 1 ? next.followerCount + " Followers" : next.followerCount + " Follower"));
        holder.usernameTextView.setText(next.username);

        String photo = next.photoUri;
        if (photo != null && !photo.isEmpty()) {
            Glide.with(mContext)
                    .load(photo)
                    .apply(Util.requestOptions())
                    .into(holder.circleImageView);
        }

        if (next.selected) {
            holder.follow.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_supervisor_account_black_24dp));
            holder.follow.setColorFilter(ContextCompat.getColor(mContext, R.color.colorAccent));
        }else {
            holder.follow.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_person_add_black_24dp));
            holder.follow.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary));
        }

        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra(ProfileActivity.KEY_USERNAME, next.username);
                mContext.startActivity(intent);
            }
        });

        holder.follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                next.selected = !next.selected;
                notifyItemChanged(idx);

                if (iInterationListener != null) {
                    if (next.selected) {
                        iInterationListener.onFollow(next);
                    }else {
                        iInterationListener.onUnFollow(next);
                    }
                }
            }
        });
    }

    private void unFollow(User next) {

        ParseUser user = next.original;
        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery(KEYS.Objects.RELATIONS);
        parseQuery.whereEqualTo("master", user);
        parseQuery.whereEqualTo("follower", ParseUser.getCurrentUser());
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {

                if (e != null) {
                    L.wtf(e);
                    return;
                }

                if (list.size() <= 0) return;

                ParseObject parseObject = list.get(0);
                parseObject.deleteEventually();
            }
        });

        int followerCount = user.getInt("follower_count");

        followerCount = followerCount <= 0 ? 0 : followerCount - 1;
        user.put("follower_count", followerCount);
        user.saveEventually();

        ParseUser currentUser = ParseUser.getCurrentUser();
        int fCount = currentUser.getInt("following_count") - 1;
        currentUser.put("following_count", fCount);
        currentUser.saveEventually();
    }

    private void follow(User next) {

        ParseUser user = next.original;
        ParseObject parseObject = new ParseObject(KEYS.Objects.RELATIONS);
        parseObject.put("master", user);
        parseObject.put("follower", ParseUser.getCurrentUser());
        parseObject.put("date_time", System.currentTimeMillis());

        parseObject.saveEventually();

        user.increment("follower_count");
        user.saveEventually();

        ParseUser currentUser = ParseUser.getCurrentUser();
        currentUser.increment("following_count");
        currentUser.saveEventually();
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    class PeopleListViewHolder extends BaseViewHolder {

        @BindView(R.id.iv_user_layout_people)
        CircleImageView circleImageView;
        @BindView(R.id.tv_username_layout_people)
        TextView usernameTextView;
        @BindView(R.id.tv_follower_count_layout_people)
        TextView followerCountTextView;
        @BindView(R.id.btn_follow_user_layout_people)
        ImageView follow;
        @BindView(R.id.layout_people_root)
        RelativeLayout root;

        public PeopleListViewHolder(View itemView) {
            super(itemView);
        }
    }

    public interface IInterationListener {

        void onFollow(User followed);
        void onUnFollow(User unFollowed);

    }
}
