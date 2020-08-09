package dynasty.software.the.stylishly.models;

import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Author : Aduraline.
 */

public class Notification {

    public String text = "";
    public String fromUsername = "";
    public String toUsername = "";
    public int notificationType = 0;
    public String dateTime = "";

    public Notification() {}

    public Notification(ParseObject parseObject) {

        ParseUser from = parseObject.getParseUser("from");
        ParseUser to = ParseUser.getCurrentUser();
        fromUsername = from.getUsername();
        toUsername = to.getUsername();
        notificationType = parseObject.getInt("notification_type");
        dateTime = TimeAgo.using(parseObject.getLong("date_time"));
        text = parseObject.getString("notification_text");
    }
}
