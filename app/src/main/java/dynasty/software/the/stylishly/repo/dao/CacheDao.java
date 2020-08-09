package dynasty.software.the.stylishly.repo.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import dynasty.software.the.stylishly.models.UserCache;

/**
 * Author : Aduraline.
 */

@Dao
public interface CacheDao {

    @Insert
    long cache(UserCache cache);

    @Insert
    void cacheAll(List<UserCache> caches);

    @Query("SELECT * FROM UserCache")
    List<UserCache> all();

    @Query("DELETE FROM UserCache WHERE go = :goValue")
    void clean(boolean goValue);

}
