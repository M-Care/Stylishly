package dynasty.software.the.stylishly.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import net.steamcrafted.materialiconlib.MaterialMenuInflater;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import dynasty.software.the.stylishly.R;
import dynasty.software.the.stylishly.async.FriendSuggestionTask;
import dynasty.software.the.stylishly.models.HashTag;
import dynasty.software.the.stylishly.models.User;
import dynasty.software.the.stylishly.ui.activities.HomeActivity;
import dynasty.software.the.stylishly.ui.activities.MainActivity;
import dynasty.software.the.stylishly.ui.activities.SearchActivity;
import dynasty.software.the.stylishly.ui.adapters.HashTagsListAdapter;
import dynasty.software.the.stylishly.ui.adapters.SearchTabAdapter;
import dynasty.software.the.stylishly.ui.adapters.UserGridAdapter;
import dynasty.software.the.stylishly.ui.base.BaseFragment;
import dynasty.software.the.stylishly.utils.KEYS;
import dynasty.software.the.stylishly.utils.L;

/**
 * Author : Aduraline.
 */

public class SearchFragment extends BaseFragment {


    public static final String TAG = "SearchFragmentTAG";

    @BindView(R.id.toolbar_search_fragment)
    Toolbar mToolbar;
    @BindView(R.id.rv_user_suggestion_search_fragment)
    RecyclerView mSuggestionRecyclerView;
    @BindView(R.id.rv_top_tags_search_fragment)
    RecyclerView mTopTagsRecyclerView;
    @BindView(R.id.swipe_layout_search_frag)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.main_container_search_fragment)
    NestedScrollView nestedScrollView;
    @BindView(R.id.top_members_container_search_fragment)
    LinearLayout topMembersContainerLayout;
    @BindView(R.id.layout_tags_container_fragment_search)
    LinearLayout tagsContainerLayout;

    private UserGridAdapter userGridAdapter;
    private HashTagsListAdapter hashTagsListAdapter;
    private HomeActivity homeActivity;

    private List<User> suggestionList = new ArrayList<>();
    private List<HashTag> hashTagsList = new ArrayList<>();

    public static SearchFragment newInstance() {

        Bundle args = new Bundle();

        SearchFragment fragment = new SearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_search_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(true);
        homeActivity = (HomeActivity) getActivity();
        if (homeActivity != null) {
            homeActivity.setSupportActionBar(mToolbar);
            ActionBar actionBar = homeActivity.getSupportActionBar();
            if (actionBar != null)
                actionBar.setTitle("Search");
        }

        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        mSuggestionRecyclerView.setNestedScrollingEnabled(false);
        mTopTagsRecyclerView.setNestedScrollingEnabled(false);
        mSuggestionRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        mTopTagsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        init();

        userGridAdapter = new UserGridAdapter(getActivity(), suggestionList);
        hashTagsListAdapter = new HashTagsListAdapter(getActivity(), hashTagsList);
        mSuggestionRecyclerView.setAdapter(userGridAdapter);
        mTopTagsRecyclerView.setAdapter(hashTagsListAdapter);

    }

    private void init() {

        swipeRefreshLayout.setRefreshing(true);
        userGridAdapter = new UserGridAdapter(getActivity(), suggestionList);
        hashTagsListAdapter = new HashTagsListAdapter(getActivity(), hashTagsList);


        FriendSuggestionTask.perform(0, new FriendSuggestionTask.Callback() {
            @Override
            public void call(List<User> users) {

                swipeRefreshLayout.setRefreshing(false);
                nestedScrollView.setVisibility(View.VISIBLE);
                suggestionList.clear();
                suggestionList.addAll(users);

                if (userGridAdapter != null)
                    userGridAdapter.notifyDataSetChanged();

            }

            @Override
            public void error() {
                topMembersContainerLayout.setVisibility(View.GONE);
            }
        });
        loadTags();
    }

    private void loadTags() {

        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery(KEYS.Objects.POSTS);
        parseQuery.orderByDescending("like_count");

        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {

                swipeRefreshLayout.setRefreshing(false);
                if (e != null) {
                    L.wtf(e);
                    return;
                }

                nestedScrollView.setVisibility(View.VISIBLE);
                try {
                    for (ParseObject parseObject : list) {

                        JSONArray jsonArray = parseObject.getJSONArray("tags");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            hashTagsList.add(new HashTag(jsonArray.getString(i)));
                        }
                    }
                }catch (Exception ex) {L.wtf(ex);}

                if (hashTagsListAdapter != null)
                    hashTagsListAdapter.notifyDataSetChanged();

            }
        });
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_item_search:
                startActivity(new Intent(getActivity(), SearchActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (homeActivity == null)
            return;

        ActionBar actionBar = homeActivity.getSupportActionBar();
        if (actionBar != null)
            actionBar.hide();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (homeActivity == null)
            homeActivity = (HomeActivity) getActivity();

        if (homeActivity == null) return;

        ActionBar actionBar = homeActivity.getSupportActionBar();
        homeActivity.showSystemUI();
        if (actionBar != null)
            actionBar.show();

    }
}
