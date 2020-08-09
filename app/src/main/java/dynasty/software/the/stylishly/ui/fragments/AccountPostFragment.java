package dynasty.software.the.stylishly.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import dynasty.software.the.stylishly.R;
import dynasty.software.the.stylishly.models.Post;
import dynasty.software.the.stylishly.ui.adapters.PostAdapter;
import dynasty.software.the.stylishly.ui.base.BaseFragment;
import dynasty.software.the.stylishly.ui.base.EndLessScrollListener;
import dynasty.software.the.stylishly.utils.KEYS;
import dynasty.software.the.stylishly.utils.L;
import dynasty.software.the.stylishly.utils.Util;

/**
 * Author : Aduraline.
 */

public class AccountPostFragment extends BaseFragment {

    @BindView(R.id.rv_account_posts)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipe_layout_account_post_fragment)
    SwipeRefreshLayout swipeRefreshLayout;

    private PostAdapter postAdapter;
    private List<Post> postList = new ArrayList<>();
    public static final int PER_PAGE = 20;
    private volatile boolean mLoading = false;

    public static AccountPostFragment newInstance() {

        Bundle args = new Bundle();

        AccountPostFragment fragment = new AccountPostFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_account_post_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(linearLayoutManager);
        postAdapter = new PostAdapter(getActivity(), postList);
        mRecyclerView.setAdapter(postAdapter);

        EndLessScrollListener endLessScrollListener = new EndLessScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                paginate(page);
            }
        };
        mRecyclerView.addOnScrollListener(endLessScrollListener);

        findUsersPost();
    }

    private void paginate(final int page) {

        if (mLoading) return;

        if (!Util.isOnline()) return;

        swipeRefreshLayout.setRefreshing(true);
        mLoading = true;

        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery(KEYS.Objects.POSTS);
        ParseUser currentUser = ParseUser.getCurrentUser();
        parseQuery.whereEqualTo("user", currentUser);
        parseQuery.setLimit(PER_PAGE);
        parseQuery.setSkip(page * PER_PAGE);

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
                }

                if (postAdapter != null) {
                    int count = postAdapter.getItemCount();
                    int newCount = postList.size() - 1;

                    postAdapter.notifyItemRangeChanged(count, newCount);
                }
            }
        });
    }

    private void findUsersPost() {

        ParseUser parseUser = ParseUser.getCurrentUser();
        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery(KEYS.Objects.POSTS);
        parseQuery.whereEqualTo("user", parseUser);
        parseQuery.setLimit(PER_PAGE);

        swipeRefreshLayout.setRefreshing(true);

        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {

                swipeRefreshLayout.setRefreshing(false);
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
            }
        });
    }
}
