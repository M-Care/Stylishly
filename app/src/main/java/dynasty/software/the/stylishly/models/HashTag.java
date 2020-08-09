package dynasty.software.the.stylishly.models;

/**
 * Author : Aduraline.
 */

public class HashTag {

    public String name = "";

    public HashTag(String s) {
        name = s;
    }

    public String string() {
        return "#" + name;
    }
}
