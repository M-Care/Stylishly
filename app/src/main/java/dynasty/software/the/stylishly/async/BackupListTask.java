package dynasty.software.the.stylishly.async;

import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

import dynasty.software.the.stylishly.models.Post;
import dynasty.software.the.stylishly.models.PostLike;
import dynasty.software.the.stylishly.repo.RepositoryManager;

/**
 * Author : Aduraline.
 */

public class BackupListTask implements Runnable {

    List<ParseObject> parseObjects;
    public BackupListTask(List<ParseObject> toBackup) {
        parseObjects = toBackup;
    }

    @Override
    public void run() {

        List<PostLike> postLikes = new ArrayList<>();

        for (ParseObject parseObject : parseObjects) {
            PostLike postLike = new PostLike();
            postLike.likedPostId = parseObject.getObjectId();
            postLikes.add(postLike);
        }

        RepositoryManager.manager().database().likes().newLikes(postLikes);
    }
}
