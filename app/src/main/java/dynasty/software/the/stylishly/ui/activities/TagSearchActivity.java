package dynasty.software.the.stylishly.ui.activities;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import dynasty.software.the.stylishly.R;
import dynasty.software.the.stylishly.models.Post;
import dynasty.software.the.stylishly.repo.RepositoryManager;
import dynasty.software.the.stylishly.ui.adapters.PostAdapter;
import dynasty.software.the.stylishly.ui.base.BaseActivity;
import dynasty.software.the.stylishly.utils.KEYS;
import dynasty.software.the.stylishly.utils.L;

/**
 * Author : Aduraline.
 */

public class TagSearchActivity extends BaseActivity
        implements SwipeRefreshLayout.OnRefreshListener, PostAdapter.IPostInteractionListener {

    @BindView(R.id.rv_tag_search)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipe_layout_tag_search_activity)
    SwipeRefreshLayout swipeRefreshLayout;

    private PostAdapter postAdapter;
    private List<Post> mMatchedPosts = new ArrayList<>();
    private String tagToSearch = "";
    public static final int PER_PAGE = 20;

    public static final String TAG_KEY = "tag_key";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_tag_search);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        tagToSearch = intent.getStringExtra(TAG_KEY);
        if (actionBar != null) {
            actionBar.setTitle("Tag #" + tagToSearch);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setup();
        search();
    }

    private void setup() {

        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        postAdapter = new PostAdapter(this, mMatchedPosts);
        postAdapter.setPostInteractionListener(this);
        mRecyclerView.setAdapter(postAdapter);
    }

    private void search() {

        swipeRefreshLayout.setRefreshing(true);
        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery(KEYS.Objects.POSTS);
        parseQuery.whereEqualTo("tags", tagToSearch.trim());
        parseQuery.setLimit(PER_PAGE);
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {

                swipeRefreshLayout.setRefreshing(false);
                if (e != null) {
                    L.wtf(e);
                    snack("Failed to complete request. Network response unknown. Please retry");
                    return;
                }

                for (ParseObject parseObject : list) {
                    Post post = new Post(parseObject);
                    mMatchedPosts.add(post);
                }

                if (postAdapter != null)
                    postAdapter.notifyDataSetChanged();

            }
        });
    }

    @Override
    public void onRefresh() {
        search();
    }


    @Override
    public void onLike(int position, boolean liked) {

        ParseObject parseObject = ParseObject.createWithoutData(KEYS.Objects.POSTS, mMatchedPosts.get(position).getId());
        ParseRelation<ParseObject> parseRelation = ParseUser.getCurrentUser().getRelation(KEYS.Objects.LIKES);
        if (liked) {
            parseRelation.add(parseObject);
        }else {
            parseRelation.remove(parseObject);
        }

        ParseUser.getCurrentUser().saveInBackground();
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

        Intent intent = new Intent(getActivity(), PhotoPreviewActivity.class);
        intent.putExtra(ProfileActivity.KEY_USERNAME, username);
        startActivity(intent);
    }

    @Override
    public void onBookmark(int position, Post post) {

        ParseRelation<ParseObject> parseRelation = ParseUser.getCurrentUser().getRelation(KEYS.Objects.BOOKMARKS);
        ParseObject parseObject = ParseObject.createWithoutData(KEYS.Objects.POSTS, post.getId());

        if (post.bookmarked) {
            parseRelation.add(parseObject);
            RepositoryManager.manager().database().bookmarks().newBookmark(post);
        }else {
            RepositoryManager.manager().database().bookmarks().remove(post);
            parseRelation.remove(parseObject);
        }

        ParseUser.getCurrentUser().saveInBackground();
    }

    public Context getActivity() {
        return this;
    }
}
