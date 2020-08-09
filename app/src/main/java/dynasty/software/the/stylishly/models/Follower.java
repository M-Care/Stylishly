package dynasty.software.the.stylishly.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Author : Aduraline.
 */

@Entity
public class Follower {

    @PrimaryKey(autoGenerate = true)
    public long id = 0;
    public String username = "";
    public long dateTime = 0;

}
