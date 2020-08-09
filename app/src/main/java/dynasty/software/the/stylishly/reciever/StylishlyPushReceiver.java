package dynasty.software.the.stylishly.reciever;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.json.JSONObject;

import dynasty.software.the.stylishly.models.ChatMessage;
import dynasty.software.the.stylishly.utils.L;
import dynasty.software.the.stylishly.utils.StylishlyNotification;

/**
 * Author : Aduraline.
 */

public class StylishlyPushReceiver extends BroadcastReceiver {

    public static final String KEY_PUSH_DATA = "com.parse.Data";
    public static final String ACTION_PUSH_RECEIVED = "com.parse.push.intent.RECEIVE";
    public static final String LOCAL_PUSH_RECEIVED = "com.stylish.ly.PUSH";

    @Override
    public void onReceive(Context context, Intent intent) {

        L.fine("Push Recieved!");
        if (intent == null) {
            L.fine("Null intent");
            return;
        }
        final String action = intent.getAction();
        if (action == null) {
            L.fine("Null action");
            return;
        }
        switch (action) {
            case ACTION_PUSH_RECEIVED:

                try {
                    String json = intent.getStringExtra(KEY_PUSH_DATA);

                    L.fine("Json => " + json);
                    ChatMessage chatMessage = ChatMessage.from(new JSONObject(json));
                    StylishlyNotification.getInstance().messageNotification(chatMessage);
                }catch (Exception e) {
                    L.fine("Failed to parse Json ==> " + e);
                }
                break;
        }
    }
}
