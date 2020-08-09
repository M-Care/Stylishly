package dynasty.software.the.stylishly.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

import dynasty.software.the.stylishly.utils.L;

/**
 * Author : Aduraline.
 */

@Parcel
@Entity
public class Post {


    public static final String KEY = "key_post";

    @PrimaryKey(autoGenerate = true)
    public long postId = 0;
    public String id = "";
    public String caption = "";

    @Ignore
    List<String> tags;
    public String userId = "";
    public String username = "";
    public String photoUri = "";
    public String dateTime = "";
    public int commentCount = 0;
    public int likeCount = 0;
    public boolean liked = false;
    public boolean bookmarked = false;
    public String mTags;
    public String userPhoto = "";

    public Post() {}

    public Post(ParseObject parseObject) {
        id = parseObject.getObjectId();
        caption = parseObject.getString("caption");
        photoUri = parseObject.getParseFile("photo_uri").getUrl();
        username = parseObject.getString("username");

        try {

            ParseFile photoFile = parseObject.getParseObject("user").fetchIfNeeded().getParseFile("photo_uri");
            if (photoFile != null) {
                userPhoto = photoFile.getUrl();
            }
        }catch (Exception e) {
            L.wtf(e);
        }
        if (username == null || username.isEmpty()) {
            try {
                username = parseObject.getParseUser("user").fetchIfNeeded().getUsername();
            }catch (Exception e) {
                username = "";
            }
        }

        try {
            userId = parseObject.getParseUser("user").fetchIfNeeded().getObjectId();
        }catch (Exception e) {}
        dateTime = TimeAgo.using(parseObject.getLong("date_time"));
        commentCount = parseObject.getInt("comment_count");
        likeCount = parseObject.getInt("like_count");
        mTags = parseObject.getString("tag_string");
    }

    public static Post fromJson(String json) {

        Post post = new Post();
        try {
            JSONObject jsonObject = new JSONObject(json);
            post.username = jsonObject.getString("username");
            post.caption = jsonObject.getString("caption");
            post.photoUri = jsonObject.getString("photo_uri");
            post.dateTime = jsonObject.getString("date_time");
        }catch (Exception e) {}

        return post;
    }

    public String toJson() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("caption", getCaption());
            jsonObject.put("photo_uri", getPhotoUri());
            jsonObject.put("username", getUsername());
            jsonObject.put("date_time", getDateTime());
        }catch (Exception e) {}

        return jsonObject.toString();
    }

    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String tags() {
        return mTags;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }
}
