package dynasty.software.the.stylishly.repo.room;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import dynasty.software.the.stylishly.models.ChatMessage;
import dynasty.software.the.stylishly.models.Comment;
import dynasty.software.the.stylishly.models.Conversation;
import dynasty.software.the.stylishly.models.Follower;
import dynasty.software.the.stylishly.models.Post;
import dynasty.software.the.stylishly.models.PostLike;
import dynasty.software.the.stylishly.models.UserCache;
import dynasty.software.the.stylishly.repo.dao.CacheDao;
import dynasty.software.the.stylishly.repo.dao.ChatMessageDao;
import dynasty.software.the.stylishly.repo.dao.CommentLike;
import dynasty.software.the.stylishly.repo.dao.ConversationDao;
import dynasty.software.the.stylishly.repo.dao.FollowerDao;
import dynasty.software.the.stylishly.repo.dao.LikeDao;
import dynasty.software.the.stylishly.repo.dao.PostDao;

/**
 * Author : Aduraline.
 */

@Database(entities = {Follower.class, Post.class,
        PostLike.class, ChatMessage.class,
        Conversation.class, UserCache.class, Comment.class}, version = 8, exportSchema = false)
public abstract class StylishlyDatabase extends RoomDatabase {

    public abstract FollowerDao follower();

    public abstract PostDao bookmarks();

    public abstract LikeDao likes();

    public abstract ConversationDao conversationDao();

    public abstract ChatMessageDao messageDao();

    public abstract CacheDao cache();

    public abstract CommentLike commentLike();

}
