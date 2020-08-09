package dynasty.software.the.stylishly.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

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
import dynasty.software.the.stylishly.StylishlyApplication;
import dynasty.software.the.stylishly.async.PostCallback;
import dynasty.software.the.stylishly.async.TransformPostTask;
import dynasty.software.the.stylishly.models.Post;
import dynasty.software.the.stylishly.models.PostLike;
import dynasty.software.the.stylishly.repo.RepositoryManager;
import dynasty.software.the.stylishly.ui.activities.CommentActivity;
import dynasty.software.the.stylishly.ui.activities.PhotoPreviewActivity;
import dynasty.software.the.stylishly.ui.activities.ProfileActivity;
import dynasty.software.the.stylishly.ui.activities.SearchActivity;
import dynasty.software.the.stylishly.ui.activities.TagSearchActivity;
import dynasty.software.the.stylishly.ui.adapters.PostAdapter;
import dynasty.software.the.stylishly.ui.base.BaseFragment;
import dynasty.software.the.stylishly.utils.KEYS;
import dynasty.software.the.stylishly.utils.L;

/**
 * Author : Aduraline.
 */

public class PostSearchFragment extends BaseFragment implements PostAdapter.IPostInteractionListener {

    @BindView(R.id.rv_posts_search_fragment)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipe_layout_post_search_fragment)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.layout_no_post_post_search_fragment)
    LinearLayout emptyResultLinearLayout;

    private List<Post> postList = new ArrayList<>();
    private PostAdapter postAdapter;
    private SearchActivity searchActivity;

    public static PostSearchFragment newInstance() {

        Bundle args = new Bundle();

        PostSearchFragment fragment = new PostSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_posts_search_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchActivity = (SearchActivity) getActivity();
        registerListener();

        prepareRecyclerView();
    }

    private void prepareRecyclerView() {

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        postAdapter = new PostAdapter(getActivity(), postList);
        postAdapter.setPostInteractionListener(this);
        mRecyclerView.setAdapter(postAdapter);

    }

    private void registerListener() {

        if (searchActivity != null) {
            searchActivity.addListener(new SearchActivity.OnQuerySubmittedListener() {
                @Override
                public void onSubmit(String query) {
                    search(query);
                }
            });
        }
    }

    private void search(String query) {
        L.fine("Query ==> " + query);

        swipeRefreshLayout.setRefreshing(true);
        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery(KEYS.Objects.POSTS);
        parseQuery.whereContains("caption", query);
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {

                swipeRefreshLayout.setRefreshing(false);
                if (e != null) {
                    L.wtf(e);
                    return;
                }

                List<Post> posts = new ArrayList<>();
                for (ParseObject parseObject : list) {
                    Post post = new Post(parseObject);
                    posts.add(post);
                }

                if (posts.size() <= 0) {
                    emptyResultLinearLayout.setVisibility(View.VISIBLE);
                }else {
                    emptyResultLinearLayout.setVisibility(View.GONE);
                }

                StylishlyApplication.getApplication().getExecutorService().execute(new TransformPostTask(posts, postCallback));
            }
        });
    }
    private PostCallback postCallback = new PostCallback() {
        @Override
        public void call(List<Post> posts) {

            postList.addAll(posts);
            L.fine("Post ==> " + postList.size());
            if (postAdapter != null)
                postAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onLike(int position, boolean liked) {

        ParseObject parseObject = ParseObject.createWithoutData(KEYS.Objects.POSTS, postList.get(position).getId());
        ParseRelation<ParseObject> parseRelation = ParseUser.getCurrentUser().getRelation(KEYS.Objects.LIKES);

        PostLike postLike = new PostLike();
        postLike.likedPostId = postList.get(position).getId();

        if (liked) {
            parseRelation.add(parseObject);
            RepositoryManager.manager().database().likes().newLike(postLike);
        }else {
            parseRelation.remove(parseObject);
            RepositoryManager.manager().database().likes().unLike(postLike.likedPostId);
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
            RepositoryManager.manager().database().bookmarks().newBookmark(post);
            parseRelation.add(parseObject);
        }else {
            RepositoryManager.manager().database().bookmarks().remove(post);
            parseRelation.remove(parseObject);
        }

        ParseUser.getCurrentUser().saveInBackground();
    }
}
