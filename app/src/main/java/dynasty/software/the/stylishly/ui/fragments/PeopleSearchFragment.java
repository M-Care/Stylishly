package dynasty.software.the.stylishly.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import dynasty.software.the.stylishly.R;
import dynasty.software.the.stylishly.StylishlyApplication;
import dynasty.software.the.stylishly.models.Follower;
import dynasty.software.the.stylishly.models.User;
import dynasty.software.the.stylishly.repo.RepositoryManager;
import dynasty.software.the.stylishly.ui.activities.SearchActivity;
import dynasty.software.the.stylishly.ui.adapters.FriendSuggestionAdapter;
import dynasty.software.the.stylishly.ui.adapters.PeopleListAdapter;
import dynasty.software.the.stylishly.ui.base.BaseFragment;
import dynasty.software.the.stylishly.utils.KEYS;
import dynasty.software.the.stylishly.utils.L;

/**
 * Author : Aduraline.
 */

public class PeopleSearchFragment extends BaseFragment {

    @BindView(R.id.swipe_layout_people_search_fragment)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.rv_people_search_fragments)
    RecyclerView mRecyclerView;
    @BindView(R.id.layout_people_not_found_tag_search)
    LinearLayout notFoundLayout;

    private FriendSuggestionAdapter peopleListAdapter;
    private List<User> accounts = new ArrayList<>();
    private SearchActivity searchActivity;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public static PeopleSearchFragment newInstance() {

        Bundle args = new Bundle();

        PeopleSearchFragment fragment = new PeopleSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_people_search_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchActivity = (SearchActivity) getActivity();
        registerListener();

        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        peopleListAdapter = new FriendSuggestionAdapter(getActivity(), accounts);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(peopleListAdapter);
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


    public void search(String query) {


        L.fine("Search: People " + query);
        swipeRefreshLayout.setRefreshing(true);

        ParseQuery<ParseUser> parseQuery = ParseUser.getQuery();
        parseQuery.whereStartsWith("username", query.trim());
        parseQuery.setLimit(20);
        parseQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> list, ParseException e) {

                swipeRefreshLayout.setRefreshing(false);
                if (e != null) {
                    L.wtf(e);
                    return;
                }

                accounts.clear();
                for (ParseUser parseUser : list) {
                    User user = new User(parseUser);
                    accounts.add(user);
                }

                L.fine("Found " + accounts.size());

                if (list.size() <= 0) {
                    notFoundLayout.setVisibility(View.VISIBLE);
                }else {
                    StylishlyApplication.getApplication().getExecutorService().execute(runnable);
                    notFoundLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {

            for (User user : accounts) {

                Follower follower =
                        RepositoryManager.manager().database().follower().isFollowing(user.username);
                user.selected = follower != null;
            }

            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (peopleListAdapter != null)
                        peopleListAdapter.notifyDataSetChanged();
                }
            });
        }
    };
}
