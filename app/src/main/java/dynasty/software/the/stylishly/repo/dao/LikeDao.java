package dynasty.software.the.stylishly.repo.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import dynasty.software.the.stylishly.models.Post;
import dynasty.software.the.stylishly.models.PostLike;

/**
 * Author : Aduraline.
 */

@Dao
public interface LikeDao {

    @Insert
    long newLike(PostLike post);

    @Query("SELECT * FROM PostLike WHERE likedPostId = :pid")
    PostLike liked(String pid);

    @Query("DELETE FROM PostLike WHERE likedPostId = :pid")
    void unLike(String pid);

    @Query("SELECT * FROM PostLike")
    List<PostLike> all();

    @Insert
    void newLikes(List<PostLike> postLikes);

}
