package dynasty.software.the.stylishly.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import dynasty.software.the.stylishly.R;
import dynasty.software.the.stylishly.models.User;
import dynasty.software.the.stylishly.ui.adapters.FriendSuggestionAdapter;
import dynasty.software.the.stylishly.ui.base.BaseActivity;
import dynasty.software.the.stylishly.utils.KEYS;
import dynasty.software.the.stylishly.utils.L;

/**
 * Author : Aduraline.
 */

public class MyRelationActivity extends BaseActivity {


    @BindView(R.id.swipe_layout_my_relation)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.rv_my_relations)
    RecyclerView mRecyclerView;
    @BindView(R.id.layout_no_record_my_relation)
    LinearLayout emptyResultLayout;

    private FriendSuggestionAdapter friendSuggestionAdapter;
    private List<User> mAccounts = new ArrayList<>();

    public static final String KEY = "Action";
    public static final int ACTION_SHOW_FOLLOWERS = 1, ACTION_SHOW_FOLLOWING = 2;
    public int mAction = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_my_relation);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        prepareRecyclerView();
        mAction = intent.getIntExtra(KEY, -1);
        Load();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(mAction == ACTION_SHOW_FOLLOWERS ? "Followers" : "Following");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void Load() {
        if (mAction == ACTION_SHOW_FOLLOWERS) {
            loadFollowers();
        }else {
            loadFollowing();
        }
    }

    private void prepareRecyclerView() {

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        friendSuggestionAdapter = new FriendSuggestionAdapter(this, mAccounts);
        mRecyclerView.setAdapter(friendSuggestionAdapter);

        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
    }

    private void loadFollowers() {

        swipeRefreshLayout.setRefreshing(true);
        ParseUser parseUser = ParseUser.getCurrentUser();
        if (parseUser == null) {
            finish();
            return;
        }

        ParseRelation<ParseUser> parseRelation = parseUser.getRelation(KEYS.Objects.RELATION_FOLLOWER);
        parseRelation.getQuery().findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> list, ParseException e) {

                swipeRefreshLayout.setRefreshing(false);
                if (e != null) {
                    L.wtf(e);
                    toast("Network response unknown");
                    return;
                }

                for (ParseUser parseObject : list) {
                    User user = new User(parseObject);
                    user.selected = mAction == ACTION_SHOW_FOLLOWING;
                    mAccounts.add(user);
                }

                if (friendSuggestionAdapter != null)
                    friendSuggestionAdapter.notifyDataSetChanged();

                if (mAccounts.isEmpty())
                    emptyResultLayout.setVisibility(View.VISIBLE);
                else
                    emptyResultLayout.setVisibility(View.GONE);
            }
        });
    }

    private void loadFollowing() {

        swipeRefreshLayout.setRefreshing(true);
        ParseUser parseUser = ParseUser.getCurrentUser();
        if (parseUser == null) {
            finish();
            return;
        }

        ParseRelation<ParseUser> parseRelation = parseUser.getRelation(KEYS.Objects.RELATION_FOLLOWING);
        parseRelation.getQuery().findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> list, ParseException e) {

                swipeRefreshLayout.setRefreshing(false);
                if (e != null) {
                    L.wtf(e);
                    toast("Network response unknown");
                    return;
                }

                for (ParseUser parseObject : list) {
                    User user = new User(parseObject);
                    user.selected = mAction == ACTION_SHOW_FOLLOWING;
                    mAccounts.add(user);
                }

                if (friendSuggestionAdapter != null)
                    friendSuggestionAdapter.notifyDataSetChanged();

                if (mAccounts.isEmpty())
                    emptyResultLayout.setVisibility(View.VISIBLE);
                else
                    emptyResultLayout.setVisibility(View.GONE);
            }
        });
    }
}
