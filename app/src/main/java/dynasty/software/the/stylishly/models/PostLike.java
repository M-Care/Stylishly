package dynasty.software.the.stylishly.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Author : Aduraline.
 */

@Entity
public class PostLike {

    @PrimaryKey(autoGenerate = true)
    public long likeId = 0;

    public String likedPostId = "";
}
