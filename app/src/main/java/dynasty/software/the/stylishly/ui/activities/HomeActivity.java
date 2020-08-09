package dynasty.software.the.stylishly.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import net.steamcrafted.materialiconlib.MaterialIconView;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.OnClick;
import dynasty.software.the.stylishly.R;
import dynasty.software.the.stylishly.ui.base.BaseActivity;
import dynasty.software.the.stylishly.ui.fragments.AccountFragment;
import dynasty.software.the.stylishly.ui.fragments.DirectMessagesFragment;
import dynasty.software.the.stylishly.ui.fragments.FeedsFragment;
import dynasty.software.the.stylishly.ui.fragments.NotificationFragment;
import dynasty.software.the.stylishly.ui.fragments.SearchFragment;
import dynasty.software.the.stylishly.utils.KEYS;
import dynasty.software.the.stylishly.utils.L;

/**
 * Author : Aduraline.
 */

public class HomeActivity extends BaseActivity {

    @BindView(R.id.iv_home_home_activity)
    MaterialIconView homeIconView;
    @BindView(R.id.iv_search_home_activity)
    MaterialIconView searchIconView;
    @BindView(R.id.iv_notifications_home_activity)
    MaterialIconView notificationIconView;
    @BindView(R.id.iv_messages_home_activity)
    MaterialIconView messagesIconView;
    @BindView(R.id.iv_account_home_activity)
    MaterialIconView accountIconView;
    @BindView(R.id.layout_bottom_bar_background)
    LinearLayout mBottomBarBackground;
    @BindView(R.id.layout_feeds_fragment_container)
    FrameLayout feedsFragmentContainer;
    @BindView(R.id.fragment_container_home_activity)
    FrameLayout mainFragmentContainer;

    private View mDecorView;

    public static void start(Context context) {

        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_activity_home);

        createFollowerParseObject();

        mDecorView = getWindow().getDecorView();

        swapMain();
    }
    private void swap(Fragment mainFragment, String tag) {

        showSystemUI();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container_home_activity, mainFragment)
                    .commit();
        }else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container_home_activity, mainFragment
                            , tag)
                    .commit();
        }
    }

    private void swapMain() {

        hideSystemUI();
        FeedsFragment feedsFragment = (FeedsFragment) getSupportFragmentManager().findFragmentByTag(FeedsFragment.class.getSimpleName());
        if (feedsFragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.layout_feeds_fragment_container, feedsFragment)
                    .commit();
        }else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.layout_feeds_fragment_container,
                            FeedsFragment.newInstance(),
                            FeedsFragment.class.getSimpleName())
                    .commit();
        }

        mainFragmentContainer.setVisibility(View.GONE);
        feedsFragmentContainer.setVisibility(View.VISIBLE);
    }

    @OnClick({R.id.layout_tab_home, R.id.layout_tab_account,
            R.id.layout_tab_search, R.id.layout_tab_message,
            R.id.layout_tab_notification}) public void onDrawerItemSelected(View view) {

        int id = view.getId();

        if (id == R.id.layout_tab_home) {
            mBottomBarBackground.setBackground(null);
            swapMain();
        }else {
            mBottomBarBackground.setBackground(ContextCompat.getDrawable(this, R.color.white));
            mainFragmentContainer.setVisibility(View.VISIBLE);
            feedsFragmentContainer.setVisibility(View.GONE);
        }


        switch (id) {
            case R.id.layout_tab_home:
                select(homeIconView, accountIconView, notificationIconView, messagesIconView, searchIconView);
                homeIconView.setColorFilter(ContextCompat.getColor(this, R.color.transparent_white));
                break;
            case R.id.layout_tab_search:
                select(searchIconView, homeIconView, notificationIconView, messagesIconView, accountIconView);
                swap(SearchFragment.newInstance(), SearchFragment.TAG);
                break;
            case R.id.layout_tab_notification:
                select(notificationIconView, accountIconView, homeIconView, messagesIconView, searchIconView);
                swap(NotificationFragment.newInstance(), NotificationFragment.TAG);
                break;
            case R.id.layout_tab_account:
                select(accountIconView, homeIconView, notificationIconView, messagesIconView, searchIconView);
                swap(AccountFragment.newInstance(), AccountFragment.TAG);
                break;
            case R.id.layout_tab_message:
                select(messagesIconView, accountIconView, notificationIconView, homeIconView, searchIconView);
                swap(DirectMessagesFragment.newInstance(), DirectMessagesFragment.TAG);
                break;
        }
    }

    private void select(MaterialIconView iconView, MaterialIconView... others) {

        iconView.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary));
        for (MaterialIconView view : others) {
            if (view != null)
                view.setColorFilter(ContextCompat.getColor(this, R.color.primary_light));
        }
    }
    public void hideSystemUI() {

        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    public void showSystemUI() {
        mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @OnClick(R.id.btn_add_friends_activity_home) public void onAddFriendsClick() {

        startActivity(new Intent(this, FriendsSuggestionActivity.class));
    }

    private void createFollowerParseObject() {

        ParseUser parseUser = ParseUser.getCurrentUser();
        if (parseUser == null) {
            L.fine("No Parse user");
            return;
        }

        ParseObject parseObject = new ParseObject(KEYS.Objects.FOLLOWERS_RELATION);
        parseObject.put("userId", parseUser.getObjectId());
        parseObject.saveEventually(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    L.wtf(e);
                    return;
                }

                L.fine("Data Saved.");
            }
        });
    }
}
