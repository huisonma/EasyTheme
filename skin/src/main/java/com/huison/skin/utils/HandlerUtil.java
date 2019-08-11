package com.huison.skin.utils;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by huisonma on 2019/5/7.
 */
public class HandlerUtil {

    private static final Handler sHandler = new Handler(Looper.getMainLooper());

    public static void postOnUiThread(Runnable runnable) {
        postOnUiThreadDelay(runnable, 0);
    }

    public static void postOnUiThreadDelay(Runnable runnable, long delayMillis) {
        sHandler.postDelayed(runnable, delayMillis);
    }

    public static void remove(Runnable runnable) {
        sHandler.removeCallbacks(runnable);
    }
}
