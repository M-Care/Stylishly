package dynasty.software.the.stylishly.ui.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
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

public class UserGridAdapter extends RecyclerView.Adapter<UserGridAdapter.UserGridViewHolder> {

    private List<User> users = new ArrayList<>();
    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private ParseUser parseUser;

    public UserGridAdapter(Context context, List<User> userList) {
        users = userList;
        mContext = context;
        parseUser = ParseUser.getCurrentUser();

        if (mContext != null)
            mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public UserGridViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (mContext == null)
            mContext = parent.getContext();

        return new UserGridViewHolder(mLayoutInflater.inflate(R.layout.layout_user_grid, parent, false));
    }

    @Override
    public void onBindViewHolder(UserGridViewHolder holder, int position) {

        final int idx = position;
        final User user = users.get(idx);
        holder.usernameTextView.setText(user.username);
        holder.followerCountTextView.setText(String.valueOf(user.followerCount + " followers"));

        String photo = user.photoUri;
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

        if (user.selected) {
            holder.follow.setVisibility(View.GONE);
            holder.following.setVisibility(View.VISIBLE);
        }else {
            holder.follow.setVisibility(View.VISIBLE);
            holder.following.setVisibility(View.GONE);
        }

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra(ProfileActivity.KEY_USERNAME, user.username);
                mContext.startActivity(intent);
            }
        });

        holder.follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                follow(user);
                user.selected = true;
                user.followerCount += 1;
                notifyItemChanged(idx);
            }
        });

        holder.following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(mContext)
                        .setMessage("Do you want to unFollow " + user.username + " ?")
                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                unFollow(user);
                                user.selected = false;
                                user.followerCount -= 1;
                                notifyItemChanged(idx);
                            }
                        }).setNegativeButton("NO", null)
                        .create().show();
            }
        });
    }

    private void follow(final User user) {

        if (parseUser == null) return;

        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery(KEYS.Objects.FOLLOWERS_RELATION);
        parseQuery.whereEqualTo("userId", user.original.getObjectId());
        parseQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {

                if (e != null) {L.wtf(e); return;}

                ParseRelation<ParseUser> parseRelation =
                        parseObject.getRelation(KEYS.Objects.FOLLOWERS_RELATION);
                parseRelation.add(parseUser);
                parseObject.saveEventually();
            }
        });

        ParseRelation<ParseUser> followingRelation = parseUser.getRelation(KEYS.Objects.RELATION_FOLLOWING);
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

    }

    private void unFollow(final User user) {

        if (parseUser == null) return;

        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery(KEYS.Objects.FOLLOWERS_RELATION);
        parseQuery.whereEqualTo("userId", user.original.getObjectId());
        parseQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {

                if (e != null) {
                    L.wtf(e);
                    return;
                }

                ParseRelation<ParseUser> parseRelation = parseObject.getRelation(KEYS.Objects.FOLLOWERS_RELATION);
                parseRelation.remove(user.original);
                parseObject.saveEventually();
            }
        });

        ParseRelation<ParseUser> followingRelation = parseUser.getRelation(KEYS.Objects.RELATION_FOLLOWING);
        followingRelation.remove(user.original);

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
        return users.size();
    }

    class UserGridViewHolder extends BaseViewHolder {

        @BindView(R.id.tv_username_user_grid)
        TextView usernameTextView;
        @BindView(R.id.tv_follower_count_user_grid)
        TextView followerCountTextView;
        @BindView(R.id.btn_follow_user_grid)
        Button follow;
        @BindView(R.id.btn_following_user_grid)
        Button following;
        @BindView(R.id.user_grid_root)
        CardView cardView;
        @BindView(R.id.iv_user_user_grid)
        CircleImageView circleImageView;

        public UserGridViewHolder(View itemView) {
            super(itemView);
        }
    }
}
