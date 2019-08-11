package com.huison.skin.utils;

import android.text.TextUtils;

import com.huison.skin.SkinManager;

import java.io.File;

/**
 * Created by maxiaozeng on 2019/8/10.
 */
public class ExternalStorage {

    public static String getExternalDir() {
        File dir = SkinManager.context().getExternalFilesDir(null);
        if (dir != null) {
            return dir.getAbsolutePath();
        } else {
            return SkinManager.context().getFilesDir().getAbsolutePath();
        }
    }

    public static String getSkinDir() {
        String dir = getExternalDir() + "/skin";
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

    public static String getZipSkinFontsDir(String zipSkinName) {
        if (TextUtils.isEmpty(zipSkinName)) {
            return getSkinDir();
        }
        String dir = getSkinDir() + File.separator + zipSkinName + "/fonts";
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }
}
