package dynasty.software.the.stylishly.async;

import android.os.Handler;
import android.os.Looper;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import dynasty.software.the.stylishly.StylishlyApplication;
import dynasty.software.the.stylishly.models.Follower;
import dynasty.software.the.stylishly.models.User;
import dynasty.software.the.stylishly.repo.RepositoryManager;
import dynasty.software.the.stylishly.utils.L;

/**
 * Author : Aduraline.
 */

public final class FriendSuggestionTask {

    private static int PER_LOAD = 20;

    /*
    * Recursively load users until there is enormous amount of unfollowed users.
    * */

    public static void perform(final int fromIndex, final Callback callback) {

        final int idx = fromIndex + 1;
        final Handler handler = new Handler(Looper.getMainLooper());

        L.fine("Going for Index " + idx);

        ParseQuery<ParseUser> parseQuery = ParseUser.getQuery();
        parseQuery.setLimit(PER_LOAD);
        parseQuery.setSkip(PER_LOAD * (idx - 1));
        parseQuery.orderByDescending("follower_count");
        parseQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> list, final ParseException e) {
                if (e != null) {
                    L.wtf("Failed to load ==> " + e);
                    callback.error();
                    return;
                }
                final List<ParseUser> copy = list;
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        final List<User> result = new ArrayList<>();
                        for (ParseUser parseUser : copy) {
                            Follower follower = RepositoryManager.manager().database().follower().isFollowing(parseUser.getUsername().trim());
                            if (follower == null)
                                result.add(new User(parseUser));
                        }

                        if (result.size() <= 5) {
                            if (result.isEmpty()) {

                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        callback.call(result);
                                    }
                                });

                            }else {
                                int newStartIndex = idx + 1;
                                perform(newStartIndex, callback);
                            }
                        }else {

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.call(result);
                                }
                            });
                        }
                    }
                };

                StylishlyApplication.getApplication().getExecutorService().execute(runnable);
            }
        });
    }


    public interface Callback {

        void call(List<User> users);
        void error();
    }
}
