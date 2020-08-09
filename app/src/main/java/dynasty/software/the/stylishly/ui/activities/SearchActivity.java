package dynasty.software.the.stylishly.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.miguelcatalan.materialsearchview.SearchAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import dynasty.software.the.stylishly.R;
import dynasty.software.the.stylishly.ui.adapters.SearchTabAdapter;
import dynasty.software.the.stylishly.ui.base.BaseActivity;
import dynasty.software.the.stylishly.utils.Util;

/**
 * Author : Aduraline.
 */

public class SearchActivity extends BaseActivity {

    @BindView(R.id.search_view_activity_search)
    MaterialSearchView materialSearchView;
    @BindView(R.id.view_pager_search_activity)
    ViewPager mViewPager;
    @BindView(R.id.toolbar_search_activity)
    Toolbar mToolbar;
    @BindView(R.id.tablayout_search_activity)
    TabLayout mTabLayout;

    private List<OnQuerySubmittedListener> querySubmittedListeners = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_search_activity);

        setSupportActionBar(mToolbar);

        materialSearchView.setHintTextColor(ContextCompat.getColor(this, R.color.white));
        materialSearchView.setOnSearchViewListener(searchViewListener);
        materialSearchView.setBackIcon(getResources().getDrawable(R.drawable.ic_action_navigation_arrow_back_inverted));
        materialSearchView.setHint("Search posts, people, #tags...");

        materialSearchView.setOnQueryTextListener(onQueryTextListener);
        prepareTabs();
    }

    private MaterialSearchView.SearchViewListener searchViewListener = new MaterialSearchView.SearchViewListener() {
        @Override
        public void onSearchViewShown() {
            mViewPager.setVisibility(View.GONE);
            mTabLayout.setVisibility(View.GONE);
        }

        @Override
        public void onSearchViewClosed() {

        }
    };

    private void prepareTabs() {

        SearchTabAdapter searchTabAdapter = new SearchTabAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(searchTabAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private MaterialSearchView.OnQueryTextListener onQueryTextListener = new MaterialSearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {

            mViewPager.setVisibility(View.VISIBLE);
            mTabLayout.setVisibility(View.VISIBLE);

            if (query == null || query.trim().length() <= 0) return false;

            Util.hideKeyboard(SearchActivity.this);
            for (OnQuerySubmittedListener listener : querySubmittedListeners) {
                listener.onSubmit(query.trim());
            }
            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
        }
    };

    public void addListener(OnQuerySubmittedListener submittedListener) {
        querySubmittedListeners.add(submittedListener);
    }

    public interface OnQuerySubmittedListener {
        void onSubmit(String query);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_item_search);

        materialSearchView.setMenuItem(menuItem);
        try {
            materialSearchView.showSearch();
        }catch (Exception e) {}

        return super.onCreateOptionsMenu(menu);
    }
}
