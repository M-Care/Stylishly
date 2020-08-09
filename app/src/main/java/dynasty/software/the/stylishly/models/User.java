package dynasty.software.the.stylishly.models;

import com.google.gson.Gson;
import com.parse.ParseFile;
import com.parse.ParseUser;

/**
 * Author : Aduraline.
 */

public class User {

    public String id = "";
    public String username = "";
    public String photoUri = "";
    public int followerCount = 0;
    public boolean selected = false;
    public ParseUser original;
    public String bio = "";

    public User() {}

    public User(ParseUser parseObject) {

        id = parseObject.getObjectId();
        username = parseObject.getUsername();
        ParseFile parseFile = parseObject.getParseFile("photo_uri");
        photoUri = parseFile == null ? "" : parseFile.getUrl();
        followerCount = parseObject.getInt("follower_count");
        bio = parseObject.getString("user_bio");
        selected = false;
        original = parseObject;
    }

    public String json() {
        return new Gson().toJson(this);
    }
}
