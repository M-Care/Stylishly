package dynasty.software.the.stylishly.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hendraanggrian.socialview.SocialView;
import com.hendraanggrian.widget.SocialTextView;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import dynasty.software.the.stylishly.R;
import dynasty.software.the.stylishly.models.Post;
import dynasty.software.the.stylishly.models.PostLike;
import dynasty.software.the.stylishly.repo.RepositoryManager;
import dynasty.software.the.stylishly.ui.activities.CommentActivity;
import dynasty.software.the.stylishly.ui.activities.ProfileActivity;
import dynasty.software.the.stylishly.ui.activities.TagSearchActivity;
import dynasty.software.the.stylishly.utils.KEYS;
import dynasty.software.the.stylishly.utils.Util;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;

/**
 * Author : Aduraline.
 */

public class VerticalFeedAdapter extends PagerAdapter {

    private List<Post> postList = new ArrayList<>();
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private InteractionListener interactionListener;


    public VerticalFeedAdapter(Context context, List<Post> posts) {
        postList = posts;
        mContext = context;

        if (mContext != null)
            mLayoutInflater = LayoutInflater.from(mContext);
    }

    public void setInteractionListener(InteractionListener interactionListener) {
        this.interactionListener = interactionListener;
    }

    @Override
    public int getCount() {
        return postList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        if (mLayoutInflater == null)
            mLayoutInflater = LayoutInflater.from(container.getContext());

        final Post next = postList.get(position);
        View view = mLayoutInflater.inflate(R.layout.layout_vertical_feed, container, false);
        ImageView imageView = view.findViewById(R.id.iv_vertical_feed);
        CircleImageView circleImageView = view.findViewById(R.id.iv_user_vertical_feed);
        final ImageView like = view.findViewById(R.id.iv_like_vertical_feed);
        ImageView comment = view.findViewById(R.id.iv_comment_vertical_feed);
        TextView caption = view.findViewById(R.id.tv_caption_vertical_feed);
        SocialTextView hashTags = view.findViewById(R.id.tv_hash_tags_vertical_feed);
        TextView username = view.findViewById(R.id.tv_username_vertical_feed);
        like.setColorFilter(ContextCompat.getColor(mContext, R.color.white));
        TextView commentCount = view.findViewById(R.id.tv_comment_count_vertical_feed);
        final TextView likeCount = view.findViewById(R.id.tv_like_count_vertical_feed);
        RelativeLayout commentButton = view.findViewById(R.id.btn_comment_vertical_feed);
        RelativeLayout likeButton = view.findViewById(R.id.btn_like_vertical_feed);
        comment.setColorFilter(ContextCompat.getColor(mContext, R.color.white));

        String userPhoto = next.userPhoto;
        if (userPhoto != null && !userPhoto.isEmpty()) {
            Glide.with(mContext)
                    .load(userPhoto)
                    .apply(Util.requestOptions())
                    .into(circleImageView);
        }
        caption.setText(next.caption);
        hashTags.setText(next.tags());
        username.setText(next.getUsername());
        commentCount.setText(String.valueOf(next.commentCount));
        likeCount.setText(String.valueOf(next.likeCount));
        if (next.liked) {
            like.setColorFilter(ContextCompat.getColor(mContext, R.color.colorAccent));
        }else {
            like.setColorFilter(ContextCompat.getColor(mContext, R.color.white));
        }

        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                next.liked = !next.liked;
                if (next.liked) {
                    next.likeCount += 1;
                    like.setColorFilter(ContextCompat.getColor(mContext, R.color.colorAccent));

                }else {
                    next.likeCount -= 1;
                    like.setColorFilter(ContextCompat.getColor(mContext, R.color.white));

                }

                likePost(next);
                likeCount.setText(String.valueOf(next.likeCount));
                updateLikeCount(next);

                if (interactionListener != null)
                    interactionListener.onPostLiked(next);
            }
        });
        final String photo = next.getPhotoUri();
        if (!photo.isEmpty()) {
            Glide.with(mContext)
                    .load(photo)
                    .apply(new RequestOptions().centerInside().error(R.color.colorPrimary).placeholder(R.color.colorPrimary))
                    .into(imageView);
        }

        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(mContext, CommentActivity.class);
                intent.putExtra(Post.KEY, Parcels.wrap(next));
                mContext.startActivity(intent);
            }
        });

        hashTags.setOnHashtagClickListener(new Function2<SocialView, String, Unit>() {
            @Override
            public Unit invoke(SocialView socialView, String s) {

                s = s.trim();
                Intent intent = new Intent(mContext, TagSearchActivity.class);
                intent.putExtra(TagSearchActivity.TAG_KEY, s);
                mContext.startActivity(intent);

                return null;
            }
        });

        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra(ProfileActivity.KEY_USERNAME, next.username.trim());
                mContext.startActivity(intent);
            }
        });

        container.addView(view);
        return view;
    }

    private void updateLikeCount(Post next) {

        ParseObject parseObject = ParseObject.createWithoutData(KEYS.Objects.POSTS, next.getId());
        parseObject.put("like_count", next.likeCount);
        parseObject.saveEventually();
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    private void likePost(Post toLike) {

        ParseUser parseUser = ParseUser.getCurrentUser();
        ParseRelation<ParseObject> parseRelation = parseUser.getRelation(KEYS.Objects.LIKES);
        ParseObject parseObject = ParseObject.createWithoutData(KEYS.Objects.POSTS, toLike.getId());

        PostLike postLike = new PostLike();
        postLike.likedPostId = toLike.getId();

        if (toLike.liked) {
            parseRelation.add(parseObject);
            RepositoryManager.manager().database().likes().newLike(postLike);

            createNotification(toLike);
        }
        else {
            parseRelation.remove(parseObject);
            RepositoryManager.manager().database().likes().unLike(postLike.likedPostId);
        }

        parseUser.saveEventually();
    }

    private void createNotification(final Post post) {

        if (!post.liked) return;

        ParseUser parseUser = ParseUser.getCurrentUser();
        if (parseUser == null || post.getUsername().trim().equalsIgnoreCase(parseUser.getUsername().trim())) return;


        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery(KEYS.Objects.NOTIFICATIONS);
        parseQuery.whereEqualTo("from", ParseUser.getCurrentUser());
        parseQuery.whereEqualTo("post", post.getId());
        parseQuery.whereEqualTo("notification_type", KEYS.NotificationType.POST_LIKE);
        parseQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {

                if (e != null) return;

                if (parseObject != null) return; //Already logged a notification

                ParseObject notificationParseObject = new ParseObject(KEYS.Objects.NOTIFICATIONS);
                notificationParseObject.put("from", ParseUser.getCurrentUser());
                notificationParseObject.put("post", post.getId());
                notificationParseObject.put("notification_type", KEYS.NotificationType.POST_LIKE);
                String text = "@" + ParseUser.getCurrentUser().getUsername() + " liked your post'" + post.getCaption() + "'";
                notificationParseObject.put("notification_text", text);
                notificationParseObject.put("date_time", System.currentTimeMillis());
                notificationParseObject.saveEventually();
            }
        });
    }

    public interface InteractionListener {
        void onPostLiked(Post post);
    }
}
