package dynasty.software.the.stylishly.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import dynasty.software.the.stylishly.R;
import dynasty.software.the.stylishly.StylishlyApplication;
import dynasty.software.the.stylishly.models.Post;
import dynasty.software.the.stylishly.models.PostLike;
import dynasty.software.the.stylishly.repo.RepositoryManager;
import dynasty.software.the.stylishly.repo.dao.PostDao;
import dynasty.software.the.stylishly.ui.activities.CommentActivity;
import dynasty.software.the.stylishly.ui.activities.MainActivity;
import dynasty.software.the.stylishly.ui.activities.PhotoPreviewActivity;
import dynasty.software.the.stylishly.ui.activities.ProfileActivity;
import dynasty.software.the.stylishly.ui.activities.TagSearchActivity;
import dynasty.software.the.stylishly.ui.adapters.PostAdapter;
import dynasty.software.the.stylishly.ui.base.BaseFragment;
import dynasty.software.the.stylishly.ui.base.EndLessScrollListener;
import dynasty.software.the.stylishly.utils.KEYS;
import dynasty.software.the.stylishly.utils.L;
import dynasty.software.the.stylishly.utils.Util;

/**
 * Author : Aduraline.
 */

public class MainFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener{

    private List<Post> postList = new ArrayList<>();
    private PostAdapter postAdapter;
    public static final int PER_PAGE = 20;
    private volatile boolean mLoading = false;
    private List<ParseObject> originalParseObjects = new ArrayList<>();
    private List<ParseObject> likedParseObjects = new ArrayList<>();
    private Handler handler = new Handler(Looper.getMainLooper());
    private ActionBar mActionBar;

    @BindView(R.id.rv_feeds_fragment_main)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipe_refresh_main_fragment)
    SwipeRefreshLayout swipeRefreshLayout;

    public static MainFragment newInstance() {

        Bundle args = new Bundle();

        MainFragment fragment = new MainFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            mActionBar = activity.getSupportActionBar();
            if (mActionBar != null)
                mActionBar.setTitle(getString(R.string.app_name));
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(linearLayoutManager);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        postAdapter = new PostAdapter(getActivity(), postList);
        postAdapter.setPostInteractionListener(postInteractionListener);
        mRecyclerView.setAdapter(postAdapter);

        EndLessScrollListener endLessScrollListener = new EndLessScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                paginate(page);
            }
        };
        mRecyclerView.addOnScrollListener(endLessScrollListener);
        loadPosts();
    }

    private void paginate(final int page) {

        if(mLoading) return;

        if (!Util.isOnline()) return;

        mLoading = true;
        swipeRefreshLayout.setRefreshing(true);

        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery(KEYS.Objects.POSTS);
        parseQuery.setLimit(PER_PAGE);
        parseQuery.setSkip(PER_PAGE * page);
        parseQuery.orderByDescending("createdAt");
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {

                swipeRefreshLayout.setRefreshing(false);
                mLoading = false;
                if (e != null) {
                    L.wtf(e);
                    return;
                }

                for (ParseObject parseObject : list) {
                    Post post = new Post(parseObject);
                    postList.add(post);
                    originalParseObjects.add(parseObject);
                }

                if (postAdapter != null) {
                    int count = postAdapter.getItemCount();
                    int newCount = postList.size() - 1;

                    postAdapter.notifyItemRangeChanged(count, newCount);
                }
            }
        });
    }

    private PostAdapter.IPostInteractionListener postInteractionListener = new PostAdapter.IPostInteractionListener() {
        @Override
        public void onLike(int position, boolean liked) {

            ParseObject parseObject = originalParseObjects.get(position);
            Post post = postList.get(position);

            if (!post.getId().trim().equalsIgnoreCase(parseObject.getObjectId().trim())) {

                /*
                * Object mismatch, should never happen anyway...but we never know...
                *
                * */
                return;
            }

            PostLike postLike = new PostLike();
            postLike.likedPostId = parseObject.getObjectId();
            if (liked) {
                parseObject.increment("like_count");
                parseObject.saveInBackground();

                RepositoryManager.manager().database().likes().newLike(postLike);
                likedParseObjects.add(parseObject);
            }else {

                int likeCount = post.getLikeCount();
                likeCount -= 1;
                parseObject.put("like_count", likeCount);
                parseObject.saveInBackground();

                ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery(KEYS.Objects.LIKES);
                parseQuery.whereEqualTo("user", ParseUser.getCurrentUser());
                parseQuery.whereEqualTo("post", parseObject);
                parseQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> list, ParseException e) {
                        if (e != null) {
                            L.wtf(e);
                            return;
                        }

                        if (list.size() <= 0)
                            return;

                        ParseObject.deleteAllInBackground(list);
                    }
                });

                RepositoryManager.manager().database().likes().unLike(postLike.likedPostId);
                likedParseObjects.remove(parseObject);

            }
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
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            intent.putExtra(ProfileActivity.KEY_USERNAME, username.trim());
            startActivity(intent);
        }

        @Override
        public void onBookmark(int position, Post post) {

            PostDao dao = RepositoryManager.manager().database().bookmarks();
            ParseObject parseObject = originalParseObjects.get(position);

            if (post.bookmarked) {
                dao.newBookmark(post);
                ParseObject bookmark = new ParseObject(KEYS.Objects.BOOKMARKS);
                bookmark.put("user", ParseUser.getCurrentUser());
                bookmark.put("post", parseObject);
                bookmark.saveEventually();

            }else {
                dao.remove(post);

                ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery(KEYS.Objects.BOOKMARKS);
                parseQuery.whereEqualTo("user", ParseUser.getCurrentUser());
                parseQuery.whereEqualTo("post", parseObject);
                parseQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> list, ParseException e) {
                        if (e != null) {
                            L.wtf(e);
                            return;
                        }

                        L.fine("Gotten ==> " + list.size());
                        if (list.size() <= 0)
                            return;

                        ParseObject.deleteAllInBackground(list);
                    }
                });

            }

        }
    };
    private void loadPosts() {

        swipeRefreshLayout.setRefreshing(true);

        if (!Util.isOnline()) {
            swipeRefreshLayout.setRefreshing(false);
            snack("Device is offline! Turn on your mobile data and retry");
            return;
        }

        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery(KEYS.Objects.POSTS);
        parseQuery.setLimit(PER_PAGE);
        parseQuery.orderByDescending("createdAt");
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {

                if (e != null) {
                    L.wtf(e);
                    snack("Network response unknown. Please retry.");
                    return;
                }

                postList.clear();
                originalParseObjects.clear();
                swipeRefreshLayout.setRefreshing(false);
                L.fine("Total ==> " + objects.size());
                for (ParseObject parseObject : objects) {

                    Post post = new Post(parseObject);
                    postList.add(post);
                    originalParseObjects.add(parseObject);
                }

                StylishlyApplication
                        .getApplication()
                        .getExecutorService()
                        .execute(new TransformPostTask());
            }
        });
    }

    private class TransformPostTask implements Runnable {

        @Override
        public void run() {

            List<Post> result = new ArrayList<>();
            for (Post next : postList) {

                Post bookmarked = RepositoryManager.manager().database().bookmarks().hasBookmarked(next.getId());
                PostLike like = RepositoryManager.manager().database().likes().liked(next.getId());
                next.bookmarked = bookmarked != null;
                next.liked = like != null;

                result.add(next);
            }

            postList.clear();
            postList.addAll(result);

            handler.post(new Runnable() {
                @Override
                public void run() {

                    if (postAdapter != null)
                        postAdapter.notifyDataSetChanged();

                }
            });
        }
    }

    @Override
    public void onRefresh() {

        loadPosts();
    }

    @Override
    public void onPause() {

        StylishlyApplication
                .getApplication()
                .saveLikes(likedParseObjects);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mActionBar != null)
            mActionBar.setTitle(getString(R.string.app_name));
    }
}
