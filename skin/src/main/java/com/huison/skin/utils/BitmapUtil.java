package com.huison.skin.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.NinePatch;

import java.io.InputStream;
import java.lang.reflect.Field;

/**
 * Created by huisonma on 2019/5/7.
 */
public class BitmapUtil {

    public static Bitmap decode9PatchBitmap(InputStream in) throws Exception {
        Bitmap bitmap = BitmapFactory.decodeStream(in);
        if (bitmap.getNinePatchChunk() != null) {
            return bitmap;
        }

        byte[] chunk = NinePatchChunk.readChunk(bitmap);
        boolean isNinePatchChunk = NinePatch.isNinePatchChunk(chunk);
        if (isNinePatchChunk) {
            Bitmap NinePatchBitmap = Bitmap.createBitmap(bitmap, 1, 1, bitmap.getWidth() - 2, bitmap.getHeight() - 2);
            Field filed = NinePatchBitmap.getClass().getDeclaredField("mNinePatchChunk");
            filed.setAccessible(true);
            filed.set(NinePatchBitmap, chunk);
            bitmap.recycle();
            return NinePatchBitmap;
        } else {
            return bitmap;
        }
    }
}
