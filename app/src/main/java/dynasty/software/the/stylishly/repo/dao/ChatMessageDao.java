package dynasty.software.the.stylishly.repo.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import dynasty.software.the.stylishly.models.ChatMessage;

/**
 * Author : Aduraline.
 */

@Dao
public interface ChatMessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long newMessage(ChatMessage message);

    @Query("SELECT * FROM ChatMessage WHERE conversationId = :id")
    List<ChatMessage> messagesWith(String id);

    @Query("SELECT * FROM ChatMessage WHERE conversationId = :id AND read = :value")
    List<ChatMessage> unReadMessages(String id, boolean value);

    @Query("UPDATE ChatMessage SET read = :value WHERE conversationId = :id")
    void readMessages(String id, boolean value);

}
