package dynasty.software.the.stylishly.models;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.parse.ParseObject;

import java.util.Date;

import dynasty.software.the.stylishly.utils.L;

/**
 * Author : Aduraline.
 */

@Entity
public class Comment {

    @PrimaryKey(autoGenerate = true)
    public long commentId = 0;
    public String id = "";
    public String text = "";
    public String username = "";
    public String timePosted = "";
    public int likeCount = 0;
    public String userPhoto = "";
    public boolean liked = false;
    public String postId = "";


    public Comment() {}

    public Comment(ParseObject parseObject) {
        id = parseObject.getObjectId();
        text = parseObject.getString("comment_text");
        try {
            username = parseObject.getParseUser("user").fetchIfNeeded().getUsername();
            userPhoto = parseObject.getParseUser("user").fetchIfNeeded().getString("photo");
        }catch (Exception e) {
            username = parseObject.getString("username");
            userPhoto = parseObject.getString("user_photo");
        }
        timePosted = TimeAgo.using(parseObject.getLong("date_time"));
        L.fine("Time created " + timePosted);
        likeCount = parseObject.getInt("like_count");
        postId = parseObject.getString("post");
    }

}
