package dynasty.software.the.stylishly;

import android.app.Activity;
import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.multidex.MultiDexApplication;
import android.util.Base64;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import dynasty.software.the.stylishly.async.BackupListTask;
import dynasty.software.the.stylishly.async.FriendSuggestionTask;
import dynasty.software.the.stylishly.models.Post;
import dynasty.software.the.stylishly.models.PostLike;
import dynasty.software.the.stylishly.models.User;
import dynasty.software.the.stylishly.models.UserCache;
import dynasty.software.the.stylishly.repo.RepositoryManager;
import dynasty.software.the.stylishly.repo.dao.CacheDao;
import dynasty.software.the.stylishly.ui.activities.ChatActivity;
import dynasty.software.the.stylishly.utils.KEYS;
import dynasty.software.the.stylishly.utils.L;
import okhttp3.OkHttpClient;
import tgio.parselivequery.LiveQueryClient;
import tgio.parselivequery.LiveQueryEvent;
import tgio.parselivequery.interfaces.OnListener;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Author : Aduraline.
 */

public class StylishlyApplication extends MultiDexApplication implements Application.ActivityLifecycleCallbacks {

    private static StylishlyApplication application;
    private ExecutorService executorService = Executors.newCachedThreadPool();
    public static volatile boolean isChatVisible = false;

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(
                new Parse.Configuration.Builder(this)
                        .applicationId(getString(R.string.back_4_app_application_id))
                        .clientKey(getString(R.string.back_4_app_client_key))
                        .server("https://parseapi.back4app.com")
                        .enableLocalDataStore()
                        .build());
        ParseTwitterUtils.initialize("INCLUDE_CUSTOMER_KEY",
                "INCLUDE_CUSTOMER_SECRET_KEY");
        FacebookSdk.sdkInitialize(getApplicationContext());
        ParseFacebookUtils.initialize(this);

        LiveQueryClient.init("wss://##INCLUDE_BACK4APP_LIVE_QUERY_APP_NAME##.back4app.io", getString(R.string.back_4_app_application_id), true);
        LiveQueryClient.connect();


        LiveQueryClient.on(LiveQueryEvent.ALL, new OnListener() {
            @Override
            public void on(JSONObject object) {
                L.fine("All Event ==> " + object.toString());
            }
        });

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("fonts/Lato-Medium.ttf")
                .setFontAttrId(R.attr.fontPath).build());

        application = this;

        getKeyHash();

        initInstallation();
        subscribeToChannel();
        updateFollowerCount();
        syncLikedPost();

        registerActivityLifecycleCallbacks(this);
    }

    private void getKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo("dynasty.software.the.stylishly",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());

                L.fine("Hash ==> " + Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            //something
        } catch (NoSuchAlgorithmException e) {
            //something
        }
    }

    private void subscribeToChannel() {

        ParseUser parseUser = ParseUser.getCurrentUser();
        if (parseUser != null) {
            ParsePush.subscribeInBackground(parseUser.getUsername(), new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        L.wtf(e);
                        return;
                    }

                    L.fine("Subscribed.");
                }
            });
        }
    }

    private void syncLikedPost() {

        final ParseUser parseUser = ParseUser.getCurrentUser();
        if (parseUser == null) return;

        boolean hasSynced = RepositoryManager.manager().preferences().getBoolean("has_synced_liked", false);
        if (!hasSynced) {

            ParseRelation<ParseObject> parseRelation = parseUser.getRelation(KEYS.Objects.LIKES);
            parseRelation.getQuery().findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> list, ParseException e) {

                    if (e != null) {
                        L.wtf(e);
                        return;
                    }

                    L.fine("Post Synced " + list.size());
                    for (ParseObject parseObject : list) {
                        PostLike postLike = new PostLike();
                        postLike.likedPostId = parseObject.getObjectId();
                        RepositoryManager.manager().database().likes().newLike(postLike);
                    }

                    RepositoryManager.manager().preferences().edit().putBoolean("has_synced_liked", true).apply();
                }
            });

        }
    }

    public void updateFollowerCount() {

        final ParseUser parseUser = ParseUser.getCurrentUser();

        if (parseUser == null) return;

        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery(KEYS.Objects.FOLLOWERS_RELATION);
        parseQuery.whereEqualTo("userId", parseUser.getObjectId());
        parseQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {

                if (e != null) {
                    return;
                }

                if (parseObject == null) return;

                ParseRelation<ParseUser> parseRelation = parseObject.getRelation(KEYS.Objects.FOLLOWERS_RELATION);
                parseRelation.getQuery().countInBackground(new CountCallback() {
                    @Override
                    public void done(int i, ParseException e) {

                        L.fine("Follower Count ==> " + i);
                        parseUser.put("follower_count", i);
                        parseUser.saveEventually();
                    }
                });
            }
        });
        ParseRelation<ParseUser> followingRelation = parseUser.getRelation(KEYS.Objects.RELATION_FOLLOWING);
        followingRelation.getQuery().countInBackground(new CountCallback() {
            @Override
            public void done(int i, ParseException e) {
                if (e != null) {
                    L.wtf(e);
                    return;
                }

                L.fine("Following Count ==> " + i);
                parseUser.put("following_count", i);
                parseUser.saveEventually();
            }
        });
    }

    private OkHttpClient.Builder httpClient() {
        return new OkHttpClient.Builder()
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String s, SSLSession sslSession) {
                            return true;
                        }
                    });
    }

    private void initInstallation() {

        ParseInstallation parseInstallation = ParseInstallation.getCurrentInstallation();
        parseInstallation.put("GCMSenderId", "INPUT_YOUR_GCM_SENDER_ID");
        parseInstallation.saveInBackground();

    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public static StylishlyApplication getApplication() {
        return application;
    }

    public void saveLikes(List<ParseObject> likedParseObjects) {

        if (likedParseObjects.size() <= 0) return;

        List<ParseObject> parseObjects = new ArrayList<>();
        for (int i = 0; i < likedParseObjects.size(); i++) {

            ParseObject next = likedParseObjects.get(i);
            ParseObject parseObject = new ParseObject(KEYS.Objects.LIKES);
            parseObject.put("user", ParseUser.getCurrentUser());
            parseObject.put("post", next);
            parseObjects.add(parseObject);
        }

        ParseObject.saveAllInBackground(parseObjects, new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    L.wtf(e);
                    return;
                }

                L.fine("Saved Likes");
            }
        });
    }

    private void likeCounts() {

        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery(KEYS.Objects.LIKES);
        parseQuery.countInBackground(new CountCallback() {
            @Override
            public void done(int i, ParseException e) {

                if (e != null) {
                    L.wtf(e);
                    return;
                }
                int currentLikeCount = RepositoryManager.manager().database().likes().all().size();

                if (i > currentLikeCount) {
                    backupLikes();
                }

            }
        });
    }

    private void backupLikes() {

        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery(KEYS.Objects.LIKES);
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> list, ParseException e) {

                if (e != null) {
                    L.wtf(e);
                    return;
                }

                getExecutorService()
                        .execute(new BackupListTask(list));

            }
        });
    }

    public void follow(List<ParseObject> parseObjects) {
        ParseObject.saveAllInBackground(parseObjects);
    }

    public void unFollow(List<ParseObject> parseObjects) {
        ParseObject.deleteAllInBackground(parseObjects);
    }

    public void pinLikes(final List<Post> likedPostsList) {

    }



    @Override
    public void onTerminate() {
        super.onTerminate();

        executorService = null;
        unregisterActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

        if (activity instanceof ChatActivity) {
            isChatVisible = true;
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {

        if (activity instanceof ChatActivity) {
            isChatVisible = false;
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

}
