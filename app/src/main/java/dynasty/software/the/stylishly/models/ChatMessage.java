package dynasty.software.the.stylishly.models;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.gson.Gson;
import com.parse.ParseFile;
import com.parse.ParseObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.sql.Time;

import dynasty.software.the.stylishly.utils.KEYS;

/**
 * Author : Aduraline.
 */

@Entity
public class ChatMessage {

    @PrimaryKey(autoGenerate = true)
    public long chatId = 0;
    public String from = "";
    public String to = "";
    public String text = "";
    public boolean isAttachment = false;
    public String conversationId = "";
    public String dateTime = "";
    public String attachmentPath = "";
    public long dateTimeLong = 0;
    public boolean sending = false;
    public String senderPhoto = "";
    public boolean read = false;


    public ParseObject toParseObject() {

        ParseObject parseObject = new ParseObject(KEYS.Objects.CHATS);
        parseObject.put("from", from);
        parseObject.put("to", to);
        if (isAttachment)
            parseObject.put("attachment", new ParseFile(new File(attachmentPath)));
        parseObject.put("date_time", dateTimeLong);
        parseObject.put("message_text", text);

        dateTime = TimeAgo.using(parseObject.getLong("date_time"));
        return parseObject;
    }

    public ChatMessage() {}

    public ChatMessage(ParseObject parseObject) {

        from = parseObject.getString("from");
        to = parseObject.getString("to");
        ParseFile parseFile = parseObject.getParseFile("attachment");
        isAttachment = parseFile != null;
        if (isAttachment)
            attachmentPath = parseFile.getUrl();
        dateTime = TimeAgo.using(parseObject.getCreatedAt().getTime());
        conversationId = to;
        text = parseObject.getString("message_text");
    }


    public static ChatMessage from(JSONObject object) throws JSONException {

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.from = object.getString("from");
        chatMessage.to = object.getString("to");
        chatMessage.text = object.has("text") ? object.getString("text") : object.getString("message_text");
        chatMessage.isAttachment = (object.has("attachment_path") || object.has("attachment")) && !object.getString("attachment_path").isEmpty();
        chatMessage.senderPhoto = object.has("sender_photo") ? object.getString("sender_photo") : "";
        chatMessage.dateTime = object.has("date_time") ? TimeAgo.using(object.getLong("date_time")) : TimeAgo.using(object.getLong("dateTimeLong"));
        return chatMessage;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }
}
