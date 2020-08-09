package dynasty.software.the.stylishly.ui.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hendraanggrian.widget.SocialTextView;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import dynasty.software.the.stylishly.R;
import dynasty.software.the.stylishly.models.Comment;
import dynasty.software.the.stylishly.repo.RepositoryManager;
import dynasty.software.the.stylishly.ui.base.BaseViewHolder;
import dynasty.software.the.stylishly.utils.KEYS;

/**
 * Author : Aduraline.
 */

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private List<Comment> commentList = new ArrayList<>();


    public CommentAdapter(Context context, List<Comment> comments) {
        commentList = comments;
        mContext = context;
        if (mContext != null)
            mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (mLayoutInflater == null)
            mLayoutInflater = LayoutInflater.from(parent.getContext());

        return new CommentViewHolder(mLayoutInflater.inflate(R.layout.layout_comment, parent, false));
    }

    @Override
    public void onBindViewHolder(CommentViewHolder holder, int position) {

        final int idx = position;
        final Comment comment = commentList.get(idx);
        holder.commentCountTextView.setText(String.valueOf(comment.likeCount));
        holder.commentTextView.setText(comment.text);
        holder.usernameTextView.setText(comment.username);
        holder.commentTimePostedTextView.setText(comment.timePosted);

        if (comment.liked) {
            holder.like.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_favorite_black_24dp));
            holder.like.setColorFilter(ContextCompat.getColor(mContext, R.color.colorAccent));
        }else {
            holder.like.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_favorite_border_black_24dp));
            holder.like.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary));
        }

        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                comment.liked = !comment.liked;
                if (comment.liked) {
                    comment.likeCount += 1;
                    RepositoryManager.manager().database().commentLike().likeComment(comment);
                }else {
                    comment.likeCount -= 1;
                    RepositoryManager.manager().database().commentLike().unLike(comment.id);
                }
                processComment(comment);
                notifyItemChanged(idx);
            }
        });

    }

    private void processComment(final Comment comment) {

        if (!comment.liked) return;

        final ParseRelation<ParseObject> parseRelation = ParseUser.getCurrentUser().getRelation(KEYS.Objects.COMMENT_LIKES);
        ParseQuery<ParseObject> parseQuery = parseRelation.getQuery();
        parseQuery.whereEqualTo("postId", comment.postId);
        parseQuery.whereEqualTo("commentId", comment.id);
        parseQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {

                if (e != null)
                    return;

                if (parseObject == null) {

                    ParseObject object = new ParseObject(KEYS.Objects.COMMENT_LIKES);
                    object.put("postId", comment.postId);
                    object.put("commentId", comment.id);
                    parseRelation.add(object);
                    ParseUser.getCurrentUser().saveInBackground();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    class CommentViewHolder extends BaseViewHolder {

        @BindView(R.id.tv_comment_text_layout_comment)
        SocialTextView commentTextView;
        @BindView(R.id.tv_username_layout_comment)
        TextView usernameTextView;
        @BindView(R.id.tv_comment_like_counts_layout_comment)
        TextView commentCountTextView;
        @BindView(R.id.tv_comment_time_posted)
        TextView commentTimePostedTextView;
        @BindView(R.id.iv_like_comment)
        ImageView like;

        public CommentViewHolder(View itemView) {
            super(itemView);
        }
    }
}
