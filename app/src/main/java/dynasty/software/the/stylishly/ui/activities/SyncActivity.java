package dynasty.software.the.stylishly.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import dynasty.software.the.stylishly.R;
import dynasty.software.the.stylishly.models.Comment;
import dynasty.software.the.stylishly.models.Follower;
import dynasty.software.the.stylishly.models.PostLike;
import dynasty.software.the.stylishly.models.User;
import dynasty.software.the.stylishly.repo.RepositoryManager;
import dynasty.software.the.stylishly.repo.dao.CommentLike;
import dynasty.software.the.stylishly.ui.base.BaseActivity;
import dynasty.software.the.stylishly.utils.KEYS;
import dynasty.software.the.stylishly.utils.L;

/**
 * Author : Aduraline.
 */

public class SyncActivity extends BaseActivity {

    private volatile AtomicInteger atomicInteger = new AtomicInteger(0);
    private static int TOTAL = 2;

    public static void start(Context context) {
        Intent intent = new Intent(context, SyncActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_sync_activity);
        syncFollowing();
        syncLikedPost();
    }


    private void syncLikedPost() {

        final ParseUser parseUser = ParseUser.getCurrentUser();
        if (parseUser == null) return;

        boolean hasSynced = RepositoryManager.manager().preferences().getBoolean("has_synced_liked", false);
        if (!hasSynced) {

            ParseRelation<ParseObject> parseRelation = parseUser.getRelation(KEYS.Objects.LIKES);
            parseRelation.getQuery().findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> list, ParseException e) {

                    if (e != null) {
                        L.wtf(e);
                        return;
                    }

                    L.fine("Post Synced " + list.size());
                    for (ParseObject parseObject : list) {
                        PostLike postLike = new PostLike();
                        postLike.likedPostId = parseObject.getObjectId();
                        RepositoryManager.manager().database().likes().newLike(postLike);
                    }

                    RepositoryManager.manager().preferences().edit().putBoolean("has_synced_liked", true).apply();
                    atomicInteger.incrementAndGet();
                    if (TOTAL == atomicInteger.get()) {
                        HomeActivity.start(SyncActivity.this);
                    }
                }
            });

        }
    }

    private void syncFollowing() {

        ParseUser parseUser = ParseUser.getCurrentUser();
        if (parseUser == null) return;

        ParseRelation<ParseUser> parseRelation = parseUser.getRelation(KEYS.Objects.RELATION_FOLLOWING);
        parseRelation.getQuery().findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> list, ParseException e) {

                if (e != null) return;

                List<User> users = new ArrayList<>();

                for (ParseUser parseObject : list) {
                    users.add(new User(parseObject));
                }

                for (User user : users) {

                    Follower follower = new Follower();
                    follower.username = user.username;
                    RepositoryManager.manager().database().follower().newFollower(follower);
                }

                if (TOTAL == atomicInteger.incrementAndGet()) {
                    HomeActivity.start(SyncActivity.this);
                }
            }
        });
    }

    private void syncCommentLikes() {

        ParseUser parseUser = ParseUser.getCurrentUser();
        if (parseUser == null) return;

        ParseRelation<ParseObject> parseRelation = parseUser.getRelation(KEYS.Objects.COMMENT_LIKES);
        parseRelation.getQuery().findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {
                if (e != null) return;

                L.fine("Comment Likes => " + list.size());
                for (ParseObject parseObject : list) {

                    Comment comment = new Comment();
                }
            }
        });
    }

}
