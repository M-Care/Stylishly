package dynasty.software.the.stylishly.ui.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.pnikosis.materialishprogress.ProgressWheel;

import net.steamcrafted.materialiconlib.MaterialIconView;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import dynasty.software.the.stylishly.R;
import dynasty.software.the.stylishly.StylishlyApplication;
import dynasty.software.the.stylishly.async.PostCallback;
import dynasty.software.the.stylishly.async.TransformPostTask;
import dynasty.software.the.stylishly.models.ChatUser;
import dynasty.software.the.stylishly.models.Follower;
import dynasty.software.the.stylishly.models.Post;
import dynasty.software.the.stylishly.models.PostLike;
import dynasty.software.the.stylishly.repo.RepositoryManager;
import dynasty.software.the.stylishly.repo.dao.FollowerDao;
import dynasty.software.the.stylishly.ui.adapters.PostAdapter;
import dynasty.software.the.stylishly.ui.base.BaseActivity;
import dynasty.software.the.stylishly.ui.base.EndLessScrollListener;
import dynasty.software.the.stylishly.utils.KEYS;
import dynasty.software.the.stylishly.utils.L;
import dynasty.software.the.stylishly.utils.Util;

/**
 * Author : Aduraline.
 */

public class ProfileActivity extends BaseActivity {

    @BindView(R.id.loading_layout_user_profile)
    RelativeLayout loadingLayout;
    @BindView(R.id.swipe_layout_user_profile)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.tv_username_user_profile)
    TextView usernameTextView;
    @BindView(R.id.tv_post_count_user_profile)
    TextView postCountTextView;
    @BindView(R.id.tv_followers_count_user_profile)
    TextView followerCountTextView;
    @BindView(R.id.tv_following_count_user_profile)
    TextView followingCountTextView;
    @BindView(R.id.tv_bio_user_profile)
    TextView bioTextView;
    @BindView(R.id.tv_time_joined_user_profile)
    TextView timeJoinedTextView;
    @BindView(R.id.layout_follow_container_user_profile)
    RelativeLayout followButtonContainerLayout;
    @BindView(R.id.layout_message_container_user_profile)
    RelativeLayout messageButtonContainerLayout;
    @BindView(R.id.pw_following_user_profile)
    ProgressWheel followProgressWheel;
    @BindView(R.id.btn_follow_user_profile)
    Button followButton;
    @BindView(R.id.pw_user_profile)
    ProgressWheel postProgressWheel;
    @BindView(R.id.no_post_layout_user_profile)
    LinearLayout zeroPostLinearLayout;
    @BindView(R.id.rv_recents_post_user_profile)
    RecyclerView mRecyclerView;
    @BindView(R.id.pw_unfollow_user)
    ProgressWheel unFollowProgressWheel;
    @BindView(R.id.btn_unfollow_user_profile)
    MaterialIconView unFollowButton;
    @BindView(R.id.iv_user_user_profile)
    CircleImageView circleImageView;

    public static final String KEY_USERNAME = "key_username";

    private String username = "";
    private ParseUser currentUser;
    private List<Post> userPostList = new ArrayList<>();
    private PostAdapter postAdapter;
    private List<ParseObject> originalParseObjects = new ArrayList<>();
    private EndLessScrollListener endLessScrollListener;
    private LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_user_profile);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Profile");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        username = intent.getStringExtra(KEY_USERNAME);
        if (username == null || username.isEmpty()) {
            finish();
            return;
        }
        linearLayoutManager = new LinearLayoutManager(this);
        endLessScrollListener = new EndLessScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {

                L.fine("Load More " + page + "; Count => " + totalItemsCount);
            }
        };
        mRecyclerView.addOnScrollListener(endLessScrollListener);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setNestedScrollingEnabled(false);

        postAdapter = new PostAdapter(this, userPostList);
        postAdapter.setPostInteractionListener(postInteractionListener);
        mRecyclerView.setAdapter(postAdapter);

        loadUser();
        loadPosts();
    }

    private void loadPosts() {

        postProgressWheel.setVisibility(View.VISIBLE);
        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery(KEYS.Objects.POSTS);
        parseQuery.whereEqualTo("username", username.trim());
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {

                postProgressWheel.setVisibility(View.GONE);
                if (e != null) {
                    snack("Failed to obtain users post. Please swipe down to refresh");
                    L.wtf(e);
                    return;
                }

                userPostList.clear();
                for (ParseObject parseObject : list) {
                    Post post = new Post(parseObject);
                    userPostList.add(post);

                    originalParseObjects.add(parseObject);
                }

                StylishlyApplication.getApplication()
                        .getExecutorService()
                        .execute(new TransformPostTask(userPostList, callback));


            }
        });
    }

    private PostCallback callback = new PostCallback() {
        @Override
        public void call(List<Post> posts) {

            userPostList = posts;

            if (postAdapter != null)
                postAdapter.notifyDataSetChanged();

            if (userPostList.size() <= 0) {
                zeroPostLinearLayout.setVisibility(View.VISIBLE);
            }else {
                zeroPostLinearLayout.setVisibility(View.GONE);
            }
        }
    };

    @OnClick(R.id.btn_message_user_profile) public void onMessageClick() {

        ChatUser chatUser = new ChatUser();
        chatUser.username = currentUser.getUsername();
        chatUser.photoUri = currentUser.getString("photo_uri");

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatUser.KEY, Parcels.wrap(chatUser));
        startActivity(intent);

    }
    private void loadUser() {

        loadingLayout.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setRefreshing(true);
        ParseQuery<ParseUser> parseQuery = ParseUser.getQuery();
        parseQuery.whereEqualTo("username", username.trim());

        parseQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> list, ParseException e) {

                if (e != null) {
                    toast("Failed to find user. Please retry");
                    L.wtf(e);
                    finish();
                    return;
                }
                L.fine("Size " + list.size());
                if (list.size() <= 0) {
                    toast("This user does not exists. Please retry");
                    finish();
                    return;
                }

                currentUser = list.get(0);
                render();
            }
        });
    }

    private void render() {

        loadingLayout.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);

        ParseFile parseFile = currentUser.getParseFile("photo_uri");
        String photo = "";
        if (parseFile != null) {
            photo = parseFile.getUrl();
        }

        if (photo != null && !photo.isEmpty()) {
            Glide.with(this)
                    .load(photo)
                    .apply(Util.requestOptions())
                    .into(circleImageView);
        }

        followingCountTextView.setText(String.valueOf(currentUser.getInt("following_count")));
        followerCountTextView.setText(String.valueOf(currentUser.getInt("follower_count")));
        postCountTextView.setText(String.valueOf(currentUser.getInt("post_count")));

        String bio = currentUser.getString("user_bio");
        if (bio == null || bio.isEmpty()) {
            bio = getString(R.string.default_bio);
            currentUser.put("user_bio", bio);
            currentUser.saveEventually();
        }
        usernameTextView.setText(currentUser.getUsername());
        bioTextView.setText(bio);

        String timeJoined = TimeAgo.using(currentUser.getCreatedAt().getTime());
        timeJoinedTextView.setText(String.valueOf("Joined " + timeJoined));

        Follower follower = RepositoryManager.manager().database().follower().isFollowing(username);
        if (follower != null) {
            messageButtonContainerLayout.setVisibility(View.VISIBLE);
            followButtonContainerLayout.setVisibility(View.GONE);
        }else {
            messageButtonContainerLayout.setVisibility(View.GONE);
            followButtonContainerLayout.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.btn_follow_user_profile) public void onFollowClick() {
        addFollower();
    }

    private void createNotification() {

        ParseUser user = ParseUser.getCurrentUser();
        String notificationMessageText = "@" + user.getUsername() + " started following you.";
        ParseObject parseObject = new ParseObject(KEYS.Objects.NOTIFICATIONS);
        parseObject.put("user", currentUser);
        parseObject.put("from", ParseUser.getCurrentUser());
        parseObject.put("notification_text", notificationMessageText);
        parseObject.put("date_time", System.currentTimeMillis());
        parseObject.put("notification_type", KEYS.NotificationType.NEW_FOLLOWER);
        parseObject.put("seen", false);

        parseObject.saveEventually(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    L.wtf("Failed to create notification " + e);
                    return;
                }

                L.fine("Notification Created");
            }
        });
    }

    @OnClick(R.id.btn_unfollow_user_profile) public void onUnFollowClick() {

        new AlertDialog.Builder(this)
                .setTitle(null)
                .setMessage("UnFollow " + currentUser.getUsername())
                .setPositiveButton("UNFOLLOW", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        unFollow();
                    }
                })
                .setNegativeButton("CANCEL", null)
                .create().show();
    }

    private void unFollow() {
        ParseUser parseUser = ParseUser.getCurrentUser();
        ParseRelation<ParseObject> parseRelation = currentUser.getRelation(KEYS.Objects.RELATION_FOLLOWER);
        ParseRelation<ParseObject> currentUserRelation = parseUser.getRelation(KEYS.Objects.RELATION_FOLLOWING);

        parseRelation.remove(parseUser);
        currentUserRelation.remove(currentUser);

        parseUser.saveEventually();
        currentUser.saveEventually();

        Follower follower = RepositoryManager.manager().database().follower().isFollowing(username);
        if (follower != null)
            RepositoryManager.manager().database().follower().removeFollower(username);

        messageButtonContainerLayout.setVisibility(View.GONE);
        followButtonContainerLayout.setVisibility(View.VISIBLE);

        int fCount = currentUser.getInt("follower_count");
        if (fCount > 0) {
            fCount -= 1;
            currentUser.put("follower_count", fCount);
            followerCountTextView.setText(String.valueOf(fCount));
        }

        StylishlyApplication.getApplication().updateFollowerCount();
    }

    private void addFollower() {

        ParseUser parseUser = ParseUser.getCurrentUser();

        ParseObject userFollowerParseObject = ParseObject.createWithoutData(KEYS.Objects.FOLLOWERS_RELATION,
                currentUser.getObjectId());

        ParseRelation<ParseObject> parseRelation = userFollowerParseObject.getRelation(KEYS.Objects.RELATION_FOLLOWER);
        ParseRelation<ParseObject> currentUserRelation = parseUser.getRelation(KEYS.Objects.RELATION_FOLLOWING);

        parseRelation.add(parseUser);
        currentUserRelation.add(currentUser);

        userFollowerParseObject.saveEventually(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    L.wtf(e);
                    return;
                }

                L.fine("Followed");
            }
        });

        final Follower follower = new Follower();
        follower.dateTime = System.currentTimeMillis();
        follower.username = currentUser.getUsername();
        followProgressWheel.setVisibility(View.VISIBLE);
        followButton.setAlpha(0.4f);
        followButton.setText("FOLLOWING...");

        parseUser.saveEventually(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    L.wtf(e);

                    followButton.setText("FOLLOW");
                    followButton.setAlpha(1.0f);
                    followProgressWheel.setVisibility(View.GONE);


                    snack("Failed to follow " + username + ". Please retry.");
                    return;
                }

                RepositoryManager
                        .manager()
                        .database()
                        .follower()
                        .newFollower(follower);

                createNotification();

                messageButtonContainerLayout.setVisibility(View.VISIBLE);
                followButtonContainerLayout.setVisibility(View.GONE);
                unFollowButton.setVisibility(View.VISIBLE);
                followButton.setText("MESSAGE");
                followButton.setAlpha(1.0f);
                snack("You started following " + currentUser.getUsername());
                currentUser.increment("follower_count");
                followerCountTextView.setText(String.valueOf(currentUser.getInt("follower_count")));

                StylishlyApplication.getApplication().updateFollowerCount();
            }
        });
    }

    private PostAdapter.IPostInteractionListener postInteractionListener = new PostAdapter.IPostInteractionListener() {
        @Override
        public void onLike(int position, boolean liked) {

            if (currentUser == null) return;

            Post post = userPostList.get(position);
            ParseRelation<ParseObject> parseRelation = currentUser.getRelation(KEYS.Objects.LIKES);
            PostLike postLike = new PostLike();
            postLike.likedPostId = post.getId();
            ParseObject parseObject = ParseObject.createWithoutData(KEYS.Objects.POSTS, post.getId());

            if (liked) {
                parseRelation.add(parseObject);
                RepositoryManager.manager().database().likes().newLike(postLike);
            }else {
                parseRelation.remove(parseObject);
                RepositoryManager.manager().database().likes().unLike(postLike.likedPostId);
            }

            currentUser.saveInBackground();
        }

        @Override
        public void onClick(Post clicked) {

            Intent intent = new Intent(ProfileActivity.this, PhotoPreviewActivity.class);
            intent.putExtra(Post.KEY, Parcels.wrap(clicked));
            startActivity(intent);
        }

        @Override
        public void onCommentClick(Post clicked) {

            Intent intent = new Intent(ProfileActivity.this, CommentActivity.class);
            intent.putExtra(Post.KEY, Parcels.wrap(clicked));
            startActivity(intent);
        }

        @Override
        public void onHashTagClick(String name) {

            Intent intent = new Intent(ProfileActivity.this, TagSearchActivity.class);
            intent.putExtra(TagSearchActivity.TAG_KEY, name);
            startActivity(intent);

        }

        @Override
        public void onViewProfileClick(String username) {
        }

        @Override
        public void onBookmark(int position, Post post) {

        }
    };
}
