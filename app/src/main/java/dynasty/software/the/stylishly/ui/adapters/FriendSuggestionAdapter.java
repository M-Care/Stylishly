package dynasty.software.the.stylishly.ui.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;
import dynasty.software.the.stylishly.R;
import dynasty.software.the.stylishly.models.Follower;
import dynasty.software.the.stylishly.models.User;
import dynasty.software.the.stylishly.repo.RepositoryManager;
import dynasty.software.the.stylishly.ui.activities.ProfileActivity;
import dynasty.software.the.stylishly.ui.base.BaseViewHolder;
import dynasty.software.the.stylishly.utils.KEYS;
import dynasty.software.the.stylishly.utils.L;
import dynasty.software.the.stylishly.utils.Util;

/**
 * Author : Aduraline.
 */

public class FriendSuggestionAdapter extends RecyclerView.Adapter<FriendSuggestionAdapter.FriendSuggestionViewHolder> {

    private List<User> mUsers = new ArrayList<>();
    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private ParseUser parseUser;

    public FriendSuggestionAdapter(Context context, List<User> users) {
        mContext = context;
        mUsers = users;
        if (mContext != null)
            mLayoutInflater = LayoutInflater.from(mContext);

        parseUser = ParseUser.getCurrentUser();
    }

    @Override
    public FriendSuggestionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (mContext == null)
            mContext = parent.getContext();

        mLayoutInflater = LayoutInflater.from(mContext);
        return new FriendSuggestionViewHolder(mLayoutInflater.inflate(R.layout.layout_people, parent, false));
    }

    @Override
    public void onBindViewHolder(FriendSuggestionViewHolder holder, int position) {

        final int idx = position;
        final User next = mUsers.get(idx);
        final String photo = next.photoUri;
        if (photo != null && !photo.isEmpty()) {
            Glide.with(mContext)
                    .load(photo)
                    .apply(Util.requestOptions())
                    .into(holder.circleImageView);
        }else {
            Glide.with(mContext)
                    .load(R.drawable.human)
                    .apply(Util.requestOptions())
                    .into(holder.circleImageView);
        }

        holder.usernameTextView.setText(next.username);
        holder.bioTextView.setText(next.bio);
        holder.followerCountTextView.setText(next.followerCount > 1 ? String.valueOf(next.followerCount + " Followers")
                : String.valueOf(next.followerCount + " Follower"));

        if (next.selected) {
            holder.follow.setVisibility(View.GONE);
            holder.unFollow.setVisibility(View.VISIBLE);
        }else {
            holder.follow.setVisibility(View.VISIBLE);
            holder.unFollow.setVisibility(View.GONE);
        }

        holder.follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                follow(next);
                next.followerCount++;
                next.selected = true;
                notifyItemChanged(idx);
            }
        });

        holder.unFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(mContext)
                        .setMessage("Do you want to unFollow " + next.username + " ?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                unFollow(next);
                                next.followerCount--;
                                next.selected = false;
                                notifyItemChanged(idx);
                            }
                        }).setNegativeButton("NO", null)
                        .create().show();
            }
        });

        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra(ProfileActivity.KEY_USERNAME, next.username.trim());
                mContext.startActivity(intent);

            }
        });
    }

    private void follow(final User user) {

        if (parseUser == null) return;

        ParseObject followerParseObject = ParseObject.createWithoutData(KEYS.Objects.FOLLOWERS_RELATION,
                user.original.getObjectId());
        ParseRelation<ParseUser> followersRelation = followerParseObject.getRelation(KEYS.Objects.RELATION_FOLLOWER);
        ParseRelation<ParseUser> followingRelation = parseUser.getRelation(KEYS.Objects.RELATION_FOLLOWING);
        followersRelation.add(parseUser);
        followingRelation.add(user.original);

        parseUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    L.wtf("Failed to follow " + e);
                    return;
                }
                L.fine(parseUser.getUsername() + " started following " + user.original.getUsername());
                Follower follower = new Follower();
                follower.username = user.username;
                follower.dateTime = System.currentTimeMillis();
                RepositoryManager.manager().database().follower().newFollower(follower);
            }
        });
        followerParseObject.saveInBackground();
    }

    private void unFollow(final User user) {

        if (parseUser == null) return;


        ParseObject followerParseObject = ParseObject.createWithoutData(KEYS.Objects.FOLLOWERS_RELATION,
                user.original.getObjectId());

        ParseRelation<ParseUser> followersRelation = followerParseObject.getRelation(KEYS.Objects.RELATION_FOLLOWER);
        ParseRelation<ParseUser> followingRelation = parseUser.getRelation(KEYS.Objects.RELATION_FOLLOWING);
        followersRelation.remove(parseUser);
        followingRelation.remove(user.original);

        followerParseObject.saveInBackground();
        parseUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    L.wtf("Failed to unfollow " + e);
                    return;
                }

                L.fine(parseUser.getUsername() + " UnFollowed " + user.username);
                Follower follower = new Follower();
                follower.username = user.username;
                RepositoryManager.manager().database().follower().removeFollower(follower.username);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    class FriendSuggestionViewHolder extends BaseViewHolder {

        @BindView(R.id.iv_user_layout_people)
        CircleImageView circleImageView;
        @BindView(R.id.tv_username_layout_people)
        TextView usernameTextView;
        @BindView(R.id.tv_follower_count_layout_people)
        TextView followerCountTextView;
        @BindView(R.id.btn_follow_user_layout_people)
        Button follow;
        @BindView(R.id.btn_unfollow_user_layout_people)
        Button unFollow;
        @BindView(R.id.tv_default_bio_layout_people)
        TextView bioTextView;
        @BindView(R.id.layout_people_root)
        RelativeLayout root;

        public FriendSuggestionViewHolder(View itemView) {
            super(itemView);
        }
    }
}
