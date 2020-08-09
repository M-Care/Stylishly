package dynasty.software.the.stylishly.repo.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import dynasty.software.the.stylishly.models.Conversation;

/**
 * Author : Aduraline.
 */

@Dao
public interface ConversationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long createConversation(Conversation conversation);

    @Query("SELECT * FROM Conversation WHERE conversationId = :id")
    Conversation getConversation(String id);

    @Query("UPDATE Conversation SET lastMessage = :msg WHERE conversationId = :id")
    void updateConversation(String msg, String id);

    @Query("SELECT * FROM Conversation")
    List<Conversation> load();
}
