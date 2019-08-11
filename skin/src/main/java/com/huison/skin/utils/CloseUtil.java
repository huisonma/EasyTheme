package com.huison.skin.utils;

import android.os.Build;

import java.io.Closeable;
import java.io.IOException;
import java.util.zip.ZipFile;

/**
 * Created by huisonma on 2019/5/7.
 */
public class CloseUtil {

    public static void close(Closeable cloneable) {
        if (cloneable != null) {
            try {
                cloneable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(ZipFile zipFile) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
