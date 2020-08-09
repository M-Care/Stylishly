package dynasty.software.the.stylishly.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import butterknife.BindView;
import butterknife.OnClick;
import dynasty.software.the.stylishly.R;
import dynasty.software.the.stylishly.models.Post;
import dynasty.software.the.stylishly.ui.base.BaseActivity;
import dynasty.software.the.stylishly.ui.fragments.AccountFragment;
import dynasty.software.the.stylishly.ui.fragments.FeedsFragment;
import dynasty.software.the.stylishly.ui.fragments.MainFragment;
import dynasty.software.the.stylishly.ui.fragments.NotificationFragment;
import dynasty.software.the.stylishly.ui.fragments.SearchFragment;

/**
 * Author : Aduraline.
 */

public class MainActivity extends BaseActivity {

    @BindView(R.id.bottom_nav_main)
    AHBottomNavigation mAhBottomNavigation;
    @BindView(R.id.fab_new_feed_item)
    FloatingActionButton mFloatingActionButton;
    /*@BindView(R.id.search_view_main)
    MaterialSearchView mMaterialSearchView;*/

    public static void start(Context context) {

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_activity_main);
        swap(MainFragment.newInstance());

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {

        mAhBottomNavigation.addItem(new AHBottomNavigationItem("Explore", R.drawable.ic_explore_black_24dp));
        mAhBottomNavigation.addItem(new AHBottomNavigationItem("Search", R.drawable.ic_search_black_24dp));
        mAhBottomNavigation.addItem(new AHBottomNavigationItem("Messages", R.drawable.ic_mail_black_24dp));
        mAhBottomNavigation.addItem(new AHBottomNavigationItem("Notifications", R.drawable.ic_notifications_black_24dp));
        mAhBottomNavigation.addItem(new AHBottomNavigationItem("Account", R.drawable.ic_account_box_black_24dp));

        mAhBottomNavigation.setBehaviorTranslationEnabled(false);
        mAhBottomNavigation.manageFloatingActionButtonBehavior(mFloatingActionButton);
        mAhBottomNavigation.setInactiveColor(ContextCompat.getColor(this, R.color.primary_light));
        mAhBottomNavigation.setAccentColor(ContextCompat.getColor(this, R.color.colorAccent));
        mAhBottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_HIDE);
        mAhBottomNavigation.setCurrentItem(0);

        mAhBottomNavigation.setOnTabSelectedListener(selectedListener);
        swap(FeedsFragment.newInstance());
    }

    private AHBottomNavigation.OnTabSelectedListener selectedListener = new AHBottomNavigation.OnTabSelectedListener() {
        @Override
        public boolean onTabSelected(int position, boolean wasSelected) {

            switch (position) {
                case 0:
                    if (!wasSelected)
                        swap(FeedsFragment.newInstance());
                    return true;
                case 1:
                    if (!wasSelected)
                        swap(SearchFragment.newInstance());
                    return true;
                case 3:
                    if (!wasSelected)
                        swap(NotificationFragment.newInstance());
                    return true;
                case 4:
                    if (!wasSelected)
                        swap(AccountFragment.newInstance());
                    return true;

            }
            return false;
        }
    };

    /*public MaterialSearchView getMaterialSearchView() {
        return mMaterialSearchView;
    }

    public void setMenuItem(MenuItem menuItem) {
        mMaterialSearchView.setMenuItem(menuItem);
    }*/

    private void swap(Fragment mainFragment) {

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container_main_activity, mainFragment)
                .commit();
    }

    @OnClick(R.id.fab_new_feed_item) public void onNewPostClick() {

        startActivity(new Intent(this, CreateNewPostActivity.class));
    }
}
