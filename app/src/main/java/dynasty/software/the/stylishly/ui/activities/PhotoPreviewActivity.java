package dynasty.software.the.stylishly.ui.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.pnikosis.materialishprogress.ProgressWheel;

import org.parceler.Parcels;

import java.util.concurrent.atomic.AtomicBoolean;

import butterknife.BindView;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import dynasty.software.the.stylishly.R;
import dynasty.software.the.stylishly.models.Post;
import dynasty.software.the.stylishly.ui.base.BaseActivity;
import dynasty.software.the.stylishly.utils.Util;

/**
 * Author : Aduraline.
 */

public class PhotoPreviewActivity extends BaseActivity {

    @BindView(R.id.iv_photo_big_preview)
    ImageView imageView;
    @BindView(R.id.pw_big_photo_preview)
    ProgressWheel progressWheel;
    @BindView(R.id.tv_username_big_photo_preview)
    TextView usernameTextView;
    @BindView(R.id.tv_caption_photo_preview)
    TextView captionTextView;
    @BindView(R.id.tv_like_count_big_photo_preview)
    TextView likeCountTextView;
    @BindView(R.id.tv_comment_count_big_photo_preview)
    TextView commentCountTextView;
    @BindView(R.id.tv_time_big_photo_preview)
    TextView timeTextView;
    @BindView(R.id.iv_user_big_photo_preview)
    CircleImageView circleImageView;

    private Post post;
    private AtomicBoolean tap = new AtomicBoolean(false);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_big_photo_preview);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        post = Parcels.unwrap(intent.getParcelableExtra(Post.KEY));
        String photo = post.getPhotoUri();
        if (!photo.isEmpty()) {
            Glide.with(this)
                    .load(photo)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                            progressWheel.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            progressWheel.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .apply(Util.requestOptions())
                    .into(imageView);

        }

        render();
    }

    @OnClick(R.id.iv_photo_big_preview) public void onPhotoTap() {

        tap.set(!tap.get());

    }

    /*private void showBars() {

        TransitionManager.beginDelayedTransition(root);
        bottomLayout.setVisibility(View.VISIBLE);
        topLayout.setVisibility(View.VISIBLE);
    }

    private void hideBars() {
        TransitionManager.beginDelayedTransition(root);
        bottomLayout.setVisibility(View.GONE);
        topLayout.setVisibility(View.GONE);
    }*/

    private void render() {

        final String photo = post.userPhoto;
        if (photo != null && !photo.isEmpty()) {
            Glide.with(this)
                    .load(photo)
                    .apply(Util.requestOptions())
                    .into(circleImageView);
        }
        usernameTextView.setText(post.getUsername());
        captionTextView.setText(post.getCaption());
        timeTextView.setText(post.getDateTime());
        likeCountTextView.setText(post.getLikeCount() > 0 ? post.getLikeCount() + " likes" : post.getLikeCount() + " like");
        commentCountTextView.setText(post.getCommentCount() > 0 ? post.getCommentCount() + " Comments" : post.getCommentCount() + " Comment");
    }

    @OnClick(R.id.tv_comment_count_big_photo_preview) public void onCommentClick() {

        Intent intent = new Intent(this, CommentActivity.class);
        intent.putExtra(Post.KEY, Parcels.wrap(post));
        startActivity(intent);

    }
}
