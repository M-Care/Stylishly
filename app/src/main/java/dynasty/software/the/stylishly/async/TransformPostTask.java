package dynasty.software.the.stylishly.async;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;

import dynasty.software.the.stylishly.models.Post;
import dynasty.software.the.stylishly.models.PostLike;
import dynasty.software.the.stylishly.repo.RepositoryManager;

/**
 * Author : Aduraline.
 */

public class TransformPostTask implements Runnable {

    private List<Post> postList;
    private PostCallback postCallback;

    private final Handler handler = new Handler(Looper.getMainLooper());

    public TransformPostTask(List<Post> toTransform, PostCallback callback) {

        postList = toTransform;
        postCallback = callback;

    }
    @Override
    public void run() {

        List<Post> result = new ArrayList<>();
        for (Post next : postList) {

            Post bookmarked = RepositoryManager.manager().database().bookmarks().hasBookmarked(next.getId());
            PostLike like = RepositoryManager.manager().database().likes().liked(next.getId());
            next.bookmarked = bookmarked != null;
            next.liked = like != null;

            result.add(next);
        }

        postList.clear();
        postList.addAll(result);

        handler.post(new Runnable() {
            @Override
            public void run() {

                if (postCallback != null)
                    postCallback.call(postList);
            }
        });
    }
}
