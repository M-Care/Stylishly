package dynasty.software.the.stylishly.utils;

import android.util.Log;

/**
 * Author : Aduraline.
 */

public final class L {

    private static final String TAG = "Stylish.ly";

    public static void fine(String m) {
        Log.d(TAG, m + "");
    }
    public static void wtf(String m) {
        fine(m);
    }
    public static void wtf(String message, Throwable throwable) {
        Log.d(TAG, message, throwable);
    }
    public static void wtf(Throwable throwable) {
        wtf("", throwable);
    }
}
