package dynasty.software.the.stylishly.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dynasty.software.the.stylishly.R;
import dynasty.software.the.stylishly.ui.activities.CreateNewPostActivity;
import dynasty.software.the.stylishly.ui.activities.HomeActivity;
import dynasty.software.the.stylishly.ui.activities.MainActivity;
import dynasty.software.the.stylishly.utils.KEYS;
import dynasty.software.the.stylishly.utils.L;

/**
 * Author : Aduraline.
 */

public class CreateNewPostService extends Service {

    public static final String INTENT_DATA = "dynasty.software.stylish.EVENT";
    public static final String KEY_MESSAGE = INTENT_DATA + ".MESSGAE";
    public static final String KEY_STATUS = "key_status";
    public static final int NOTIFICATION_ID = 4;

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder notification;

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }

        String json = intent.getStringExtra(CreateNewPostActivity.EXTRA_POST_PAYLOAD);
        L.fine("Service Started ==> " + json);

        showProgressNotification();

        try {
            final JSONObject jsonObject = new JSONObject(json);
            final ParseFile toUpload = new ParseFile(new File(jsonObject.getString("photo_uri")));
            toUpload.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        L.wtf(e);

                        String message = "Failed to upload post due to network error. Please retry";
                        Intent in = new Intent(INTENT_DATA);
                        in.putExtra(KEY_MESSAGE, message);
                        LocalBroadcastManager.getInstance(CreateNewPostService.this)
                                .sendBroadcast(in);

                        showErroredNotification();
                        stopSelf();
                        return;
                    }

                    uploadPost(toUpload, jsonObject);
                }
            });

        }catch (Exception e) {
            //will never happen!
        }
        return START_STICKY;
    }

    private void uploadPost(final ParseFile toUpload, JSONObject jsonObject) {

        try {
            ParseObject parseObject = new ParseObject(KEYS.Objects.POSTS);
            ParseUser parseUser = ParseUser.getCurrentUser();
            parseObject.put("caption", jsonObject.getString("caption"));
            parseObject.put("photo_uri", toUpload);
            parseObject.put("user", parseUser);
            parseObject.put("username", parseUser.getUsername());
            parseObject.put("date_time", System.currentTimeMillis());
            parseObject.put("like_count", 0);
            parseObject.put("comment_count", 0);

            String tagString = jsonObject.getString("tag_string");
            tagString = tagString.replace(",", "");
            parseObject.put("tag_string", tagString);

            String[] tags = jsonObject.getString("tags").split(",");
            List<String> tagList = new ArrayList<>();
            for (String s : tags) {
                tagList.add(s.trim());
            }

            parseObject.put("tags", tagList);
            parseObject.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {

                    if (e != null) {
                        L.wtf(e);
                        String message = "Failed to upload post due to network error. Please retry";
                        Intent in = new Intent(INTENT_DATA);
                        in.putExtra(KEY_MESSAGE, message);
                        LocalBroadcastManager.getInstance(CreateNewPostService.this)
                                .sendBroadcast(in);

                        showErroredNotification();
                        stopSelf();
                        return;

                    }

                    showFinishedNotification();
                    String message = "Post has been uploaded successfully";
                    Intent in = new Intent(INTENT_DATA);
                    in.putExtra(KEY_MESSAGE, message);
                    in.putExtra(KEY_STATUS, "success");
                    LocalBroadcastManager.getInstance(CreateNewPostService.this)
                            .sendBroadcast(in);
                    L.fine("Post Saved successfully. Photo ==> " + toUpload.getUrl());

                    countPosts();
                }
            });
        }catch (Exception e) {
            L.wtf(e);
        }
    }

    private void countPosts() {

        ParseUser parseUser = ParseUser.getCurrentUser();
        parseUser.increment("post_count");
        parseUser.saveEventually();

    }

    private void showProgressNotification() {

        notification = new NotificationCompat.Builder(this)
                .setContentTitle("Creating Post")
                .setContentText("Please wait...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setProgress(0, 0, true);

        mNotificationManager.notify(NOTIFICATION_ID, notification.build());
    }

    private void showErroredNotification() {

        notification = new NotificationCompat.Builder(this)
                .setContentTitle("Operation Failed")
                .setAutoCancel(true)
                .setContentText("Failed to publish post. Please retry")
                .setSmallIcon(R.mipmap.ic_launcher);

        mNotificationManager.notify(NOTIFICATION_ID, notification.build());
    }

    private void showFinishedNotification() {

        Intent intent = new Intent(this, HomeActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 18, intent, 0);

        notification = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setContentTitle("Post Published")
                .setContentText("Your post has been published successfully.")
                .setSmallIcon(R.mipmap.ic_launcher);

        mNotificationManager.notify(NOTIFICATION_ID, notification.build());
    }
}
