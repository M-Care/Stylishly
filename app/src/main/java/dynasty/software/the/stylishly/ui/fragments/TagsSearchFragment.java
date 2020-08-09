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
import android.widget.LinearLayout;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import dynasty.software.the.stylishly.R;
import dynasty.software.the.stylishly.models.HashTag;
import dynasty.software.the.stylishly.ui.activities.SearchActivity;
import dynasty.software.the.stylishly.ui.adapters.HashTagsListAdapter;
import dynasty.software.the.stylishly.ui.base.BaseFragment;
import dynasty.software.the.stylishly.utils.KEYS;
import dynasty.software.the.stylishly.utils.L;

/**
 * Author : Aduraline.
 */

public class TagsSearchFragment extends BaseFragment {

    @BindView(R.id.swipe_layout_tag_search)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.rv_tag_search_tag_search_fragment)
    RecyclerView mRecyclerView;
    @BindView(R.id.layout_tag_not_found_tag_search)
    LinearLayout emptyResultLinearLayout;

    private SearchActivity activity;
    private List<HashTag> mHashTags = new ArrayList<>();
    private HashTagsListAdapter hashTagsListAdapter;

    public static TagsSearchFragment newInstance() {

        Bundle args = new Bundle();

        TagsSearchFragment fragment = new TagsSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_fragment_tag_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        activity = (SearchActivity) getActivity();
        registerListener();

        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        prepareRecyclerView();

    }

    private void prepareRecyclerView() {

        hashTagsListAdapter = new HashTagsListAdapter(getActivity(), mHashTags);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(hashTagsListAdapter);

    }

    private void loadTags(String query) {

        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery(KEYS.Objects.POSTS);
        parseQuery.whereContainedIn("tags", Arrays.asList(query));
        parseQuery.orderByDescending("like_count");
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {

                if (e != null) {
                    L.wtf(e);
                    snack("Failed to load tags. Please retry");
                    return;
                }

                mHashTags.clear();
                try {
                    for (ParseObject parseObject : list) {
                        JSONArray jsonArray = parseObject.getJSONArray("tags");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            HashTag hashTag = new HashTag(jsonArray.getString(i));
                            mHashTags.add(hashTag);
                        }
                    }
                }catch (Exception ex) {L.wtf(ex);}

                if (hashTagsListAdapter != null)
                    hashTagsListAdapter.notifyDataSetChanged();

                if (mHashTags.size() <= 0)
                    emptyResultLinearLayout.setVisibility(View.VISIBLE);
                else
                    emptyResultLinearLayout.setVisibility(View.GONE);

            }
        });
    }

    private void registerListener() {

        if (activity != null) {
            activity.addListener(new SearchActivity.OnQuerySubmittedListener() {
                @Override
                public void onSubmit(String query) {
                    search(query);
                }
            });
        }
    }

    public void search(String query) {
        loadTags(query);
    }
}
