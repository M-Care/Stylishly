package dynasty.software.the.stylishly.async;

import java.util.List;

import dynasty.software.the.stylishly.models.Post;

/**
 * Author : Aduraline.
 */

public interface PostCallback {

    void call(List<Post> posts);
}
