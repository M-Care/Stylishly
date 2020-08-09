package dynasty.software.the.stylishly.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import dynasty.software.the.stylishly.models.Notification;
import dynasty.software.the.stylishly.ui.activities.HomeActivity;
import dynasty.software.the.stylishly.ui.adapters.NotificationAdapter;
import dynasty.software.the.stylishly.ui.base.BaseFragment;
import dynasty.software.the.stylishly.utils.KEYS;
import dynasty.software.the.stylishly.utils.L;

/**
 * Author : Aduraline.
 */

public class NotificationFragment extends BaseFragment {

    @BindView(R.id.rv_notifications)
    RecyclerView mRecyclerView;
    @BindView(R.id.layout_no_notification)
    LinearLayout emptyNotificationLinearLayout;
    @BindView(R.id.swipe_layout_notification)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.toolbar_notification)
    Toolbar mToolbar;

    private HomeActivity homeActivity;
    private List<Notification> notificationList = new ArrayList<>();
    private NotificationAdapter notificationAdapter;
    public static final String TAG = "NotificationFragmentTag";

    public static NotificationFragment newInstance() {

        Bundle args = new Bundle();

        NotificationFragment fragment = new NotificationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_notification_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        homeActivity = (HomeActivity) getActivity();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        notificationAdapter = new NotificationAdapter(getActivity(), notificationList);
        mRecyclerView.setAdapter(notificationAdapter);

        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        mToolbar.setTitle("Notifications");

        findNotifications();
    }

    private void findNotifications() {

        swipeRefreshLayout.setRefreshing(true);
        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery(KEYS.Objects.NOTIFICATIONS);
        parseQuery.whereEqualTo("user", ParseUser.getCurrentUser());
        parseQuery.include("user");
        parseQuery.include("from");
        parseQuery.setLimit(KEYS.PAGE);
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {

                swipeRefreshLayout.setRefreshing(false);
                if (e != null) {
                    L.wtf(e);
                    snack("Failed to load notification. Network failure.");
                    return;
                }

                for (ParseObject parseObject : list) {
                    Notification notification = new Notification(parseObject);
                    notificationList.add(notification);
                }

                if (notificationAdapter != null) {
                    notificationAdapter.notifyDataSetChanged();
                }

                if (notificationList.size() > 0) {
                    emptyNotificationLinearLayout.setVisibility(View.GONE);
                }else {
                    emptyNotificationLinearLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if (homeActivity == null)
            homeActivity = (HomeActivity) getActivity();

        if (homeActivity != null)
            homeActivity.showSystemUI();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        homeActivity = null;
    }
}
