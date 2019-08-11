package com.huison.skin.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * Created by huisonma on 2019/5/7.
 */
public class DensityUtil {

    public static int dp2px(Context context, float dpValue) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * density + 0.5f);
    }

    public static int px2dp(Context context, float pxValue) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / density + 0.5f);
    }

    public static int sp2px(Context context, float spValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float dimension = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, metrics);
        return (int) (dimension + 0.5f);
    }

    public static float px2sp(Context context, float dpValue) {
        return (dpValue / context.getResources().getDisplayMetrics().scaledDensity);
    }
}
