package dynasty.software.the.stylishly.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
import com.pnikosis.materialishprogress.ProgressWheel;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import dynasty.software.the.stylishly.R;
import dynasty.software.the.stylishly.models.Post;
import dynasty.software.the.stylishly.models.PostLike;
import dynasty.software.the.stylishly.repo.RepositoryManager;
import dynasty.software.the.stylishly.ui.activities.CommentActivity;
import dynasty.software.the.stylishly.ui.activities.CreateNewPostActivity;
import dynasty.software.the.stylishly.ui.activities.EditProfileActivity;
import dynasty.software.the.stylishly.ui.activities.HomeActivity;
import dynasty.software.the.stylishly.ui.activities.MyRelationActivity;
import dynasty.software.the.stylishly.ui.activities.PhotoPreviewActivity;
import dynasty.software.the.stylishly.ui.activities.ProfileActivity;
import dynasty.software.the.stylishly.ui.activities.TagSearchActivity;
import dynasty.software.the.stylishly.ui.adapters.PostAdapter;
import dynasty.software.the.stylishly.ui.base.BaseFragment;
import dynasty.software.the.stylishly.utils.KEYS;
import dynasty.software.the.stylishly.utils.L;
import dynasty.software.the.stylishly.utils.Util;

/**
 * Author : Aduraline.
 */

public class AccountFragment extends BaseFragment {

    @BindView(R.id.rv_user_posts_account_fragment)
    RecyclerView mRecyclerView;
    @BindView(R.id.layout_no_post_yet_account)
    LinearLayout noPostLayout;
    @BindView(R.id.pw_fragment_account)
    ProgressWheel progressWheel;
    @BindView(R.id.tv_username_account_fragment)
    TextView usernameTextView;
    @BindView(R.id.tv_time_joined_account_fragment)
    TextView timeJoinedTextView;
    @BindView(R.id.tv_bio_account_fragment)
    TextView bioTextView;
    @BindView(R.id.tv_follower_count_account_fragment)
    TextView followerCountTextView;
    @BindView(R.id.tv_following_count_account_fragment)
    TextView followingCountTextView;
    @BindView(R.id.tv_post_count_account_fragment)
    TextView postCountTextView;
    @BindView(R.id.toolbar_account_fragment)
    Toolbar mToolbar;
    @BindView(R.id.iv_user_account_fragment)
    CircleImageView circleImageView;


    private PostAdapter postAdapter;
    private List<Post> postList = new ArrayList<>();
    public static final String TAG = "AccountFragmentTAG";
    private HomeActivity homeActivity;
    private ParseUser parseUser;

    public static AccountFragment newInstance() {

        Bundle args = new Bundle();

        AccountFragment fragment = new AccountFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        homeActivity = (HomeActivity) getActivity();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setNestedScrollingEnabled(false);
        mToolbar.setTitle("Account");

        parseUser = ParseUser.getCurrentUser();

        postAdapter = new PostAdapter(getActivity(), postList);
        postAdapter.setPostInteractionListener(iPostInteractionListener);
        mRecyclerView.setAdapter(postAdapter);

        renderUser();
        findUsersPost();
    }

    private void renderUser() {

        ParseUser currentUser = ParseUser.getCurrentUser();
        followingCountTextView.setText(String.valueOf(currentUser.getInt("following_count")));
        followerCountTextView.setText(String.valueOf(currentUser.getInt("follower_count")));
        postCountTextView.setText(String.valueOf(currentUser.getInt("post_count")));

        if (currentUser.getString("user_bio") == null ||
                currentUser.getString("user_bio").isEmpty()) {
            currentUser.put("user_bio", getString(R.string.default_bio));
            currentUser.saveEventually();
        }
        usernameTextView.setText(currentUser.getUsername());
        bioTextView.setText(currentUser.getString("user_bio"));

        String timeJoined = TimeAgo.using(currentUser.getCreatedAt().getTime());
        timeJoinedTextView.setText(String.valueOf("Joined " + timeJoined));

        ParseFile photoFile = currentUser.getParseFile("photo_uri");
        if (photoFile != null) {
            String photo = photoFile.getUrl();
            if (photo != null && !photo.isEmpty()) {
                Glide.with(this).load(photo).apply(Util.requestOptions()).into(circleImageView);
            }
        }
    }

    private void findUsersPost() {

        ParseUser parseUser = ParseUser.getCurrentUser();
        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery(KEYS.Objects.POSTS);
        parseQuery.whereEqualTo("user", parseUser);

        progressWheel.setVisibility(View.VISIBLE);

        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {

                progressWheel.setVisibility(View.GONE);
                if (e != null) {
                    L.wtf(e);
                    snack("Error response from network. Please retry.");
                    return;
                }

                for (ParseObject parseObject : list) {
                    Post post = new Post(parseObject);
                    postList.add(post);
                }

                if (postAdapter != null)
                    postAdapter.notifyDataSetChanged();

                if (postList.size() <= 0) {
                    noPostLayout.setVisibility(View.VISIBLE);
                }else {
                    noPostLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    @OnClick(R.id.fab_add_new_post_account_fragment) public void onAddNewPostClick() {

        Intent intent = new Intent(getActivity(), CreateNewPostActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.btn_edit_profile_account_fragment) public void onEditProfileClick() {
        startActivity(new Intent(getActivity(), EditProfileActivity.class));
    }

    @Override
    public void onResume() {
        super.onResume();

        if (homeActivity != null)
            homeActivity.showSystemUI();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        homeActivity = null;
    }

    private PostAdapter.IPostInteractionListener iPostInteractionListener = new PostAdapter.IPostInteractionListener() {
        @Override
        public void onLike(int position, boolean liked) {

            if (parseUser == null) return;

            Post post = postList.get(position);
            ParseRelation<ParseObject> likedParseRelation = parseUser.getRelation(KEYS.Objects.LIKES);
            PostLike postLike = new PostLike();
            postLike.likedPostId = post.getId();

            if (liked) {
                likedParseRelation.add(ParseObject.createWithoutData(KEYS.Objects.POSTS, post.getId()));
                RepositoryManager.manager().database().likes().newLike(postLike);
            }else {
                likedParseRelation.remove(ParseObject.createWithoutData(KEYS.Objects.POSTS, post.getId()));
                RepositoryManager.manager().database().likes().unLike(postLike.likedPostId);
            }

            parseUser.saveInBackground();
        }

        @Override
        public void onClick(Post clicked) {

            Intent intent = new Intent(getActivity(), PhotoPreviewActivity.class);
            intent.putExtra(Post.KEY, Parcels.wrap(clicked));
            startActivity(intent);

        }

        @Override
        public void onCommentClick(Post clicked) {

            Intent intent = new Intent(getActivity(), CommentActivity.class);
            intent.putExtra(Post.KEY, Parcels.wrap(clicked));
            startActivity(intent);
        }

        @Override
        public void onHashTagClick(String name) {
            Intent intent = new Intent(getActivity(), TagSearchActivity.class);
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

    @OnClick({R.id.btn_load_followers_account_fragment, R.id.btn_load_following_account})
    public void onSeeFollowerOrFollowing(View view) {

        Intent intent = new Intent(getActivity(), MyRelationActivity.class);
        switch (view.getId()) {
            case R.id.btn_load_followers_account_fragment:
                intent.putExtra(MyRelationActivity.KEY, MyRelationActivity.ACTION_SHOW_FOLLOWERS);
                break;
            case R.id.btn_load_following_account:
                intent.putExtra(MyRelationActivity.KEY, MyRelationActivity.ACTION_SHOW_FOLLOWING);
                break;
        }

        startActivity(intent);
    }
}
