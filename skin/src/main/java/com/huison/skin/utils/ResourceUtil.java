package com.huison.skin.utils;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;

/**
 * Created by huisonma on 2019/5/7.
 */
public class ResourceUtil {

    public static int getResourceId(Context context, String name, String defType) {
        if (TextUtils.isEmpty(name)) {
            return 0;
        }
        final Resources res = context.getResources();
        final String packageName = context.getPackageName();
        int resId = res.getIdentifier(name, defType, packageName);
        return resId;
    }
}
