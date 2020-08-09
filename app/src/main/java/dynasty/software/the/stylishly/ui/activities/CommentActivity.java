package dynasty.software.the.stylishly.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.pnikosis.materialishprogress.ProgressWheel;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import dynasty.software.the.stylishly.R;
import dynasty.software.the.stylishly.StylishlyApplication;
import dynasty.software.the.stylishly.models.Comment;
import dynasty.software.the.stylishly.models.Post;
import dynasty.software.the.stylishly.repo.RepositoryManager;
import dynasty.software.the.stylishly.repo.dao.CommentLike;
import dynasty.software.the.stylishly.ui.adapters.CommentAdapter;
import dynasty.software.the.stylishly.ui.base.BaseActivity;
import dynasty.software.the.stylishly.utils.KEYS;
import dynasty.software.the.stylishly.utils.L;
import dynasty.software.the.stylishly.utils.Util;

/**
 * Author : Aduraline.
 */

public class CommentActivity extends BaseActivity {

    @BindView(R.id.rv_comments)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipe_layout_comment_activity)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.layout_no_comment_comment_activity)
    LinearLayout noCommentLayout;
    @BindView(R.id.edt_comment_activity)
    EditText commentEditText;
    @BindView(R.id.pw_comment_activity)
    ProgressWheel progressWheel;
    @BindView(R.id.btn_post_comment_comment_activity)
    Button postCommentButton;

    private List<Comment> commentList = new ArrayList<>();
    private CommentAdapter commentAdapter;
    private Post currentPost;
    private ParseObject postParseObject;
    private Handler handler = new Handler(Looper.getMainLooper());


    public static final int PER_LOAD = 20;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_comment_activity);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Comments");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        currentPost = Parcels.unwrap(intent.getParcelableExtra(Post.KEY));
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);

        fetchComment();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentAdapter = new CommentAdapter(this, commentList);
        mRecyclerView.setAdapter(commentAdapter);
    }

    private void fetchComment() {

        swipeRefreshLayout.setRefreshing(true);

        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery(KEYS.Objects.COMMENTS);
        parseQuery.whereEqualTo("post", currentPost.getId());
        parseQuery.setLimit(PER_LOAD);

        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {

                swipeRefreshLayout.setRefreshing(false);
                if (e != null) {
                    L.wtf(e);
                    snack("Failed to fetch comment. Swipe down to retry.");
                    return;
                }

                for (ParseObject object : list) {
                    Comment comment = new Comment(object);
                    commentList.add(comment);
                }

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {

                        for (Comment comment : commentList) {
                            Comment cm =
                                    RepositoryManager.manager().database().commentLike().liked(comment.id);
                            comment.liked = cm != null;
                        }

                        handler.post(new Runnable() {
                            @Override
                            public void run() {

                                if (commentAdapter != null)
                                    commentAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                };

                StylishlyApplication.getApplication().getExecutorService().execute(runnable);
                if (commentList.size() <= 0)
                    noCommentLayout.setVisibility(View.VISIBLE);
                else
                    noCommentLayout.setVisibility(View.GONE);
            }
        });
    }

    @OnClick(R.id.btn_post_comment_comment_activity) public void onPostCommentClick() {

        final String text = Util.textOf(commentEditText);
        Util.hideKeyboard(this);
        if (text.isEmpty()) {
            snack("Write something first!");
            return;
        }

        ParseUser parseUser = ParseUser.getCurrentUser();

        final ParseObject parseObject = new ParseObject(KEYS.Objects.COMMENTS);
        parseObject.put("post", currentPost.getId());
        parseObject.put("comment_text", text);
        parseObject.put("user", parseUser);
        parseObject.put("username", parseUser.getUsername());
        parseObject.put("user_photo", parseUser.getString("photo") == null ? "" : parseUser.getString("photo"));
        parseObject.put("like_count", 0);
        parseObject.put("date_time", System.currentTimeMillis());

        final Comment comment = new Comment(parseObject);

        postCommentButton.setVisibility(View.GONE);
        progressWheel.setVisibility(View.VISIBLE);
        parseObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {

                if (e != null) {
                    L.wtf(e);
                    progressWheel.setVisibility(View.GONE);
                    postCommentButton.setVisibility(View.VISIBLE);
                    snack("Failed to post comment. Please retry");
                    return;
                }
                commentList.add(comment);
                int count = commentList.size() - 1;
                if (commentAdapter != null)
                    commentAdapter.notifyDataSetChanged();

                mRecyclerView.smoothScrollToPosition(count);
                commentEditText.setText("");
                toast("Comment Posted!");
                progressWheel.setVisibility(View.GONE);
                postCommentButton.setVisibility(View.VISIBLE);
                noCommentLayout.setVisibility(View.GONE);

                incrementCommentCount();
                createNotification();
            }
        });
    }

    private void createNotification() {

        ParseUser parseUser = ParseUser.getCurrentUser();

        String notificationMessageText = "@" + parseUser.getUsername() + " commented on your post";
        ParseObject parseObject = new ParseObject(KEYS.Objects.NOTIFICATIONS);
        parseObject.put("user", currentPost.userId);
        parseObject.put("from", parseUser);
        parseObject.put("notification_text", notificationMessageText);
        parseObject.put("date_time", System.currentTimeMillis());
        parseObject.put("notification_type", KEYS.NotificationType.NEW_COMMENT);
        parseObject.put("seen", false);

        parseObject.saveEventually();
    }

    private void incrementCommentCount() {

        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery(KEYS.Objects.POSTS);
        parseQuery.getInBackground(currentPost.getId(), new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {

                if (e != null || parseObject == null) {
                    L.wtf("Error " + e);
                    return;
                }

                L.fine("Data, Comment Count ==> " + parseObject.getInt("comment_count"));
                parseObject.increment("comment_count");
                parseObject.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {

                        L.fine("Saved " + e);
                    }
                });
            }
        });
    }
}
