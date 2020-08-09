package dynasty.software.the.stylishly.ui.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hendraanggrian.socialview.SocialView;
import com.hendraanggrian.widget.SocialTextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;
import dynasty.software.the.stylishly.R;
import dynasty.software.the.stylishly.models.Post;
import dynasty.software.the.stylishly.ui.base.BaseViewHolder;
import dynasty.software.the.stylishly.utils.L;
import dynasty.software.the.stylishly.utils.Util;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;

/**
 * Author : Aduraline.
 */

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostVewHolder> {

    private List<Post> postList = new ArrayList<>();
    private Context mContext;
    private IPostInteractionListener iPostInteractionListener;
    private LayoutInflater mLayoutInflater;

    public PostAdapter(Context context, List<Post> posts) {
        postList = posts;
        mContext = context;
        if (mContext != null)
            mLayoutInflater = LayoutInflater.from(mContext);
    }

    public void setPostInteractionListener(IPostInteractionListener iPostInteractionListener) {
        this.iPostInteractionListener = iPostInteractionListener;
    }

    @Override
    public PostVewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mLayoutInflater == null)
            mLayoutInflater = LayoutInflater.from(parent.getContext());

        return new PostVewHolder(mLayoutInflater.inflate(R.layout.layout_feed, parent, false));
    }

    @Override
    public void onBindViewHolder(PostVewHolder holder, int position) {

        final int idx = position;
        final Post post = postList.get(idx);

        final String userPhoto = post.userPhoto;
        if (userPhoto != null && !userPhoto.isEmpty()) {
            Glide.with(mContext)
                    .load(userPhoto)
                    .apply(Util.requestOptions())
                    .into(holder.circleImageView);
        }else {
            Glide.with(mContext)
                    .load(R.drawable.human)
                    .apply(Util.requestOptions())
                    .into(holder.circleImageView);
        }
        holder.captionTextView.setText(post.getCaption());
        holder.commentsCountTextView.setText(post.getCommentCount() > 0 ?
                post.getCommentCount() + " COMMENTS" : post.getCommentCount() + " COMMENT");
        holder.likesCountTextView.setText(post.getLikeCount() > 0 ?
                post.getLikeCount() + " likes" : post.getLikeCount() + " like");
        holder.timeTextView.setText(post.getDateTime());
        holder.usernameTextView.setText(post.getUsername());
        holder.tagsTextView.setText(post.tags());

        if (post.liked) {
            holder.like.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_favorite_black_24dp));
            holder.like.setColorFilter(ContextCompat.getColor(mContext, R.color.colorAccent));
        }else {
            holder.like.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_favorite_border_black_24dp));
            holder.like.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary));
        }

        if (post.bookmarked) {
            holder.bookmark.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_bookmark_black_24dp));
        }else {
            holder.bookmark.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_bookmark_border_black_24dp));
        }

        holder.commentsCountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (iPostInteractionListener != null)
                    iPostInteractionListener.onCommentClick(post);
            }
        });

        holder.tagsTextView.setOnHashtagClickListener(new Function2<SocialView, String, Unit>() {
            @Override
            public Unit invoke(SocialView socialView, String s) {

                if (iPostInteractionListener != null)
                    iPostInteractionListener.onHashTagClick(s);
                return null;
            }
        });

        holder.usernameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (iPostInteractionListener != null)
                    iPostInteractionListener.onViewProfileClick(post.getUsername());
            }
        });

        holder.circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (iPostInteractionListener != null)
                    iPostInteractionListener.onViewProfileClick(post.getUsername());
            }
        });

        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                post.liked = !post.liked;
                int likeCount = post.getLikeCount();
                if (post.liked) {
                    likeCount += 1;
                }else {
                    likeCount -= 1;
                }
                post.setLikeCount(likeCount);
                notifyItemChanged(idx);

                if (iPostInteractionListener != null)
                    iPostInteractionListener.onLike(idx, post.liked);
            }
        });

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (iPostInteractionListener != null)
                    iPostInteractionListener.onClick(post);
            }
        });

        holder.bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                post.bookmarked = !post.bookmarked;
                if (iPostInteractionListener != null)
                    iPostInteractionListener.onBookmark(idx, post);

                notifyItemChanged(idx);
            }
        });

        String photo = post.getPhotoUri();
        if (!photo.isEmpty()) {

            Glide.with(mContext)
                    .load(photo)
                    .apply(Util.requestOptions())
                    .into(holder.imageView);
        }else {
            L.fine(photo);
        }
    }

    public interface IPostInteractionListener {

        void onLike(int position, boolean liked);
        void onClick(Post clicked);
        void onCommentClick(Post clicked);
        void onHashTagClick(String name);
        void onViewProfileClick(String username);
        void onBookmark(int position, Post post);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    class PostVewHolder extends BaseViewHolder {

        @BindView(R.id.iv_photo_layout_feed)
        ImageView imageView;
        @BindView(R.id.tv_caption_layout_feed)
        TextView captionTextView;
        @BindView(R.id.tv_tags_layout_feed)
        SocialTextView tagsTextView;
        @BindView(R.id.tv_username_feeds_layout)
        TextView usernameTextView;
        @BindView(R.id.tv_time_layout_feed)
        TextView timeTextView;
        @BindView(R.id.tv_likes_count_layout_feed)
        TextView likesCountTextView;
        @BindView(R.id.tv_comment_count_layout_feed)
        TextView commentsCountTextView;
        @BindView(R.id.iv_like_layout_feed)
        ImageView like;
        @BindView(R.id.iv_user_feed_layout)
        CircleImageView circleImageView;
        @BindView(R.id.iv_bookmark)
        ImageView bookmark;

        public PostVewHolder(View itemView) {
            super(itemView);
        }
    }
}
