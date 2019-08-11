package com.huison.skin.utils;

import android.util.Log;

/**
 * Created by huisonma on 2019/5/7.
 */
public class LogUtil {

    private static boolean DEBUG = false;

    public static void enableDebug() {
        DEBUG = true;
    }

    public static void d(String tag, String msg) {
        if (DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (DEBUG) {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (DEBUG) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (DEBUG) {
            Log.e(tag, msg);
        }
    }
}
