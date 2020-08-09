package dynasty.software.the.stylishly.repo.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import dynasty.software.the.stylishly.models.Follower;

/**
 * Author : Aduraline.
 */
@Dao
public interface FollowerDao {

    @Insert
    long newFollower(Follower follower);

    @Query("DELETE FROM FOLLOWER WHERE username = :name")
    void removeFollower(String name);

    @Delete
    void unFollow(Follower follower);

    @Query("SELECT * FROM Follower")
    List<Follower> all();

    @Query("SELECT * FROM Follower WHERE username = :user")
    Follower isFollowing(String user);

}
