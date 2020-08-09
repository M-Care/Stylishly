package dynasty.software.the.stylishly.models;

import com.google.gson.Gson;

import org.parceler.Parcel;

/**
 * Author : Aduraline.
 */

@Parcel
public class ChatUser {

    public String username = "";
    public String photoUri = "";

    public static final String KEY = "ChatUser";

    public String json() {
        return new Gson().toJson(this);
    }
}
