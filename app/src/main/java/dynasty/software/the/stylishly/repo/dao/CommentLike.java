package dynasty.software.the.stylishly.repo.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import dynasty.software.the.stylishly.models.Comment;

/**
 * Author : Aduraline.
 */

@Dao
public interface CommentLike {

    @Insert
    long likeComment(Comment comment);

    @Query("SELECT * FROM Comment WHERE postId = :id")
    Comment liked(String id);

    @Query("DELETE FROM Comment WHERE postId = :id")
    void unLike(String id);
}
