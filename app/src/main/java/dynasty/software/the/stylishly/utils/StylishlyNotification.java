package dynasty.software.the.stylishly.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import org.parceler.Parcels;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import dynasty.software.the.stylishly.R;
import dynasty.software.the.stylishly.StylishlyApplication;
import dynasty.software.the.stylishly.models.ChatMessage;
import dynasty.software.the.stylishly.models.ChatUser;
import dynasty.software.the.stylishly.reciever.StylishlyPushReceiver;
import dynasty.software.the.stylishly.repo.RepositoryManager;
import dynasty.software.the.stylishly.ui.activities.ChatActivity;

/**
 * Author : Aduraline.
 */

public class StylishlyNotification {

    private NotificationManager mNotificationManager;
    private static StylishlyNotification sInstance;
    private Context mContext;
    public static final int NOTIFICATION_ID = 10;
    private AtomicInteger atomicInteger = new AtomicInteger(0);

    private StylishlyNotification() {

        mContext = StylishlyApplication.getApplication().getApplicationContext();
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public Context getContext() {
        return mContext;
    }

    public static synchronized StylishlyNotification getInstance() {

        if (sInstance == null)
            sInstance = new StylishlyNotification();
        return sInstance;
    }

    public void messageNotification(ChatMessage chatMessage) {

        if (StylishlyApplication.isChatVisible) {

            Intent intent = new Intent(StylishlyPushReceiver.LOCAL_PUSH_RECEIVED);
            intent.putExtra("message_data", chatMessage.toJson());
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
            return;
        }

        int notifCount = atomicInteger.incrementAndGet();
        Intent activityIntent = new Intent(getContext(), ChatActivity.class);
        ChatUser chatUser = new ChatUser();
        chatUser.username = chatMessage.from;
        chatUser.photoUri = chatMessage.senderPhoto;
        activityIntent.putExtra(ChatUser.KEY, Parcels.wrap(chatUser));

        PendingIntent pendingIntent =
                PendingIntent.getActivity(getContext(), 0, activityIntent,0);

        final String text = "New message from " + chatMessage.from + " : " + chatMessage.text;
        NotificationCompat.BigTextStyle bigTextStyle =
                new NotificationCompat.BigTextStyle().bigText(text);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext())
                .setContentTitle("Stylish.ly")
                .setContentText(text)
                .setChannelId("StylishlyChat")
                .setNumber(notifCount)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.ic_launcher))
                .setVibrate(new long[] {1000, 1000})
                .setContentIntent(pendingIntent)
                .setStyle(bigTextStyle);

        chatMessage.conversationId = chatUser.username;
        RepositoryManager.manager().database().messageDao().newMessage(chatMessage);
        RepositoryManager.manager().database().conversationDao().updateConversation(chatMessage.text, chatMessage.conversationId);

        int notificatonId = new Random().nextInt(1000);
        mNotificationManager.notify(notificatonId, builder.build());
    }
}
