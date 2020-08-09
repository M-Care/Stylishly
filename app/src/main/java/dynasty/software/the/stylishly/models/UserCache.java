package dynasty.software.the.stylishly.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Author : Aduraline.
 */

@Entity
public class UserCache {

    @PrimaryKey(autoGenerate = true)
    public long id = 0;
    public String json = "";

    /*
    * Flag used to drop all elements inside the database, refer to CacheDao.clean()
    * */
    public boolean go = true;

}
