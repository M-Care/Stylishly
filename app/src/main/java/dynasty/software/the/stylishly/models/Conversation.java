package dynasty.software.the.stylishly.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Author : Aduraline.
 */

@Entity
public class Conversation {

    @PrimaryKey(autoGenerate = true)
    public long convoId = 0;
    public String lastMessage = "";
    public String user = "";
    public String conversationId = "";
    public long dateTimeLong = 0;
    public String dateTime = "";
    public int unReadCount = 0;

    public boolean hasUnReadMessages() {
        return unReadCount > 0;
    }
}
