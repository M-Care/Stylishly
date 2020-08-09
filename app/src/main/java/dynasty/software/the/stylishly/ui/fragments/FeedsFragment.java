package dynasty.software.the.stylishly.ui.fragments;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import dynasty.software.the.stylishly.R;
import dynasty.software.the.stylishly.StylishlyApplication;
import dynasty.software.the.stylishly.async.PostCallback;
import dynasty.software.the.stylishly.async.TransformPostTask;
import dynasty.software.the.stylishly.models.Post;
import dynasty.software.the.stylishly.models.PostLike;
import dynasty.software.the.stylishly.repo.RepositoryManager;
import dynasty.software.the.stylishly.repo.dao.LikeDao;
import dynasty.software.the.stylishly.ui.activities.HomeActivity;
import dynasty.software.the.stylishly.ui.adapters.VerticalFeedAdapter;
import dynasty.software.the.stylishly.ui.base.BaseFragment;
import dynasty.software.the.stylishly.utils.KEYS;
import dynasty.software.the.stylishly.utils.L;
import dynasty.software.the.stylishly.utils.Util;
import me.kaelaela.verticalviewpager.VerticalViewPager;

import static dynasty.software.the.stylishly.ui.fragments.MainFragment.PER_PAGE;

/**
 * Author : Aduraline.
 */

public class FeedsFragment extends BaseFragment {

    private List<ParseObject> originalParseObjects = new ArrayList<>();
    private List<Post> postList = new ArrayList<>();
    private VerticalFeedAdapter verticalFeedAdapter;
    private Handler handler = new Handler(Looper.getMainLooper());
    private HomeActivity homeActivity;

    @BindView(R.id.vertical_viewpager_feeds_fragment)
    VerticalViewPager mVerticalViewPager;
    @BindView(R.id.pw_feeds_fragment)
    ProgressWheel progressWheel;


    public static FeedsFragment newInstance() {

        Bundle args = new Bundle();

        FeedsFragment fragment = new FeedsFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_feeds_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        homeActivity = (HomeActivity) getActivity();

        verticalFeedAdapter = new VerticalFeedAdapter(getActivity(), postList);
        mVerticalViewPager.setAdapter(verticalFeedAdapter);
        verticalFeedAdapter.setInteractionListener(interactionListener);

        loadPosts();
    }

    private void loadPosts() {

        if (!Util.isOnline()) {
            snack("Device is offline! Turn on your mobile data and retry");
            return;
        }

        progressWheel.setVisibility(View.VISIBLE);
        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery(KEYS.Objects.POSTS);
        parseQuery.include("user");
        parseQuery.setLimit(PER_PAGE);
        parseQuery.orderByDescending("createdAt");
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {

                if (e != null) {
                    L.wtf(e);
                    snack("Network response unknown. Please retry.");
                    return;
                }

                postList.clear();
                originalParseObjects.clear();
                progressWheel.setVisibility(View.GONE);
                L.fine("Total ==> " + objects.size());
                for (ParseObject parseObject : objects) {

                    Post post = new Post(parseObject);
                    postList.add(post);
                    originalParseObjects.add(parseObject);
                }

                StylishlyApplication
                        .getApplication()
                        .getExecutorService()
                        .execute(new TransformPostTask(postList, postCallback));
            }
        });
    }

    private PostCallback postCallback = new PostCallback() {
        @Override
        public void call(List<Post> posts) {

            L.fine("Post returned => " + posts.size());
            postList = posts;
            L.fine("Posts ==> " + postList.size());

            try {
                if (verticalFeedAdapter != null)
                    verticalFeedAdapter.notifyDataSetChanged();
            }catch (Exception e) {}
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (homeActivity == null)
            homeActivity = (HomeActivity) getActivity();

        if (homeActivity != null)
            homeActivity.hideSystemUI();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (homeActivity != null)
            homeActivity.showSystemUI();

    }

    private VerticalFeedAdapter.InteractionListener interactionListener = new VerticalFeedAdapter.InteractionListener() {
        @Override
        public void onPostLiked(Post post) {

            LikeDao likeDao = RepositoryManager.manager().database().likes();
            PostLike postLike = new PostLike();
            postLike.likedPostId = post.getId();
            if (post.liked) {
                likeDao.newLike(postLike);
            }else {
                likeDao.unLike(postLike.likedPostId);
            }
        }
    };
}
