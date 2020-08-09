package dynasty.software.the.stylishly.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import dynasty.software.the.stylishly.R;
import dynasty.software.the.stylishly.async.FriendSuggestionTask;
import dynasty.software.the.stylishly.models.User;
import dynasty.software.the.stylishly.ui.adapters.FriendSuggestionAdapter;
import dynasty.software.the.stylishly.ui.base.BaseActivity;

/**
 * Author : Aduraline.
 */

public class FriendsSuggestionActivity extends BaseActivity {

    @BindView(R.id.rv_friends_suggestion)
    RecyclerView mRecyclerView;
    @BindView(R.id.no_result_layout)
    LinearLayout noResultLayout;
    @BindView(R.id.swipe_layout_friends_suggestion)
    SwipeRefreshLayout swipeRefreshLayout;

    private List<User> mSuggestions = new ArrayList<>();
    private FriendSuggestionAdapter friendSuggestionAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_friend_suggestion);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Friends Suggestion");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.red);
        prepareRecyclerView();
        loadData();
    }

    private void loadData() {

        swipeRefreshLayout.setRefreshing(true);
        FriendSuggestionTask.perform(0, new FriendSuggestionTask.Callback() {
            @Override
            public void call(List<User> users) {

                swipeRefreshLayout.setRefreshing(false);
                mSuggestions.clear();
                mSuggestions.addAll(users);

                if (friendSuggestionAdapter != null)
                    friendSuggestionAdapter.notifyDataSetChanged();

                if (mSuggestions.size() <= 0)
                    noResultLayout.setVisibility(View.VISIBLE);
                else
                    noResultLayout.setVisibility(View.GONE);
            }

            @Override
            public void error() {
                swipeRefreshLayout.setRefreshing(false);
                snack("Failed to perform network operation. Please retry");
            }
        });
    }

    private void prepareRecyclerView() {

        friendSuggestionAdapter = new FriendSuggestionAdapter(this, mSuggestions);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(friendSuggestionAdapter);

    }
}
