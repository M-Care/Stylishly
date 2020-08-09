package dynasty.software.the.stylishly.repo.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import dynasty.software.the.stylishly.models.Post;

/**
 * Author : Aduraline.
 */

@Dao
public interface PostDao {

    @Insert
    long newBookmark(Post toBookmark);

    @Query("SELECT * FROM Post WHERE id = :postid")
    Post hasBookmarked(String postid);

    @Insert
    void bulkBookmark(List<Post> posts);

    @Delete
    void remove(Post post);

    @Query("SELECT * FROM Post")
    List<Post> all();

}
