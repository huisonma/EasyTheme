package com.huison.skin.kind;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.huison.skin.parser.ColorStateListParser;
import com.huison.skin.parser.ResValuesXmlParser;
import com.huison.skin.parser.XmlDrawableParser;
import com.huison.skin.utils.CloseUtil;
import com.huison.skin.utils.ExternalStorage;
import com.huison.skin.utils.FileUtil;
import com.huison.skin.utils.HandlerUtil;
import com.huison.skin.utils.LogUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import bolts.Task;

/**
 * Created by huisonma on 2019/5/7.
 */
public class ZipSkin extends AbstractSkin {

    private String skinPath;
    private String skinName;

    private List<ZipEntry> zipEntries;

    private Map<String, Integer> colorCache = new ConcurrentHashMap<>();
    private Map<String, Float> dimenCache = new ConcurrentHashMap<>();
    private Map<String, String> stringCache = new ConcurrentHashMap<>();

    public ZipSkin(Context context, String skinPath) {
        super(context);
        this.skinPath = skinPath;
        File file = new File(skinPath);
        if (file.exists()) {
            skinName = file.getName().replace(".zip", "");
        }
    }

    @Override
    public void prepareInBackground() {
        if (FileUtil.checkFileExists(skinPath)) {
            try {
                zipEntries = FileUtil.readZipFile(skinPath);
                for (ZipEntry zipEntry : zipEntries) {
                    String name = zipEntry.getName();
                    LogUtil.e("ZipSkin", "zipEntry.name = " + name);

                    if (name.contains("colors.xml")) {
                        ResValuesXmlParser.parseColorXml(colorCache, skinPath, zipEntry);
                    } else if (name.contains("dimens.xml")) {
                        ResValuesXmlParser.parseDimenXml(dimenCache, skinPath, zipEntry);
                    } else if (name.contains("strings.xml")) {
                        ResValuesXmlParser.parseStringXml(stringCache, skinPath, zipEntry);
                    } else if (name.contains("fonts/")) {
                        String skinFontsDir = ExternalStorage.getZipSkinFontsDir(skinName);
                        String fontPath = skinFontsDir + File.separator + name.substring(name.lastIndexOf("/") + 1);
                        saveFontToSingleFile(fontPath, zipEntry);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void saveFontToSingleFile(String fontPath, ZipEntry zipEntry) {
        if (!FileUtil.checkFileExists(fontPath)) {
            ZipFile zipFile = null;
            try {
                zipFile = new ZipFile(skinPath);
                FileUtil.saveInputStream(fontPath, zipFile.getInputStream(zipEntry));
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                CloseUtil.close(zipFile);
            }
        }
    }

    public String getSkinName() {
        return skinPath;
    }

    @Override
    public Integer getColor(String name) {
        if (colorCache != null && colorCache.containsKey(name)) {
            return colorCache.get(name);
        }
        return super.getColor(name);
    }

    @Override
    public ColorStateList getColorStateList(String name) {
        for (ZipEntry zipEntry : zipEntries) {
            String zipEntryName = zipEntry.getName();
            if (zipEntryName.contains("color/")) {
                if (zipEntryName.endsWith(name + ".xml")) {
                    return ColorStateListParser.parseColorStateList(skinPath, zipEntry, colorCache);
                }
            }
        }
        return super.getColorStateList(name);
    }

    public void asyncGetColorStateList(final String name, final AsyncGetResCallback callback) {
        Task.callInBackground(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                final ColorStateList colorStateList = getColorStateList(name);
                HandlerUtil.postOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.getColorStateList(colorStateList);
                        }
                    }
                });
                return null;
            }
        });
    }

    @Override
    public Drawable getDrawable(String name) {
        return XmlDrawableParser.getXmlDrawable(context, skinPath, zipEntries, name);
    }

    public void asyncGetDrawable(final String name, final AsyncGetResCallback callback) {
        Task.callInBackground(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                if (zipEntries == null) {
                    zipEntries = FileUtil.readZipFile(skinPath);
                }
                final Drawable drawable = getDrawable(name);
                HandlerUtil.postOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.getDrawable(drawable);
                        }
                    }
                });
                return null;
            }
        });
    }

    @Override
    public Float getTextSize(String name) {
        if (dimenCache != null && dimenCache.containsKey(name)) {
            return dimenCache.get(name);
        }
        return super.getTextSize(name);
    }

    @Override
    public Typeface getTypeface(String name, int style) {
        Typeface typeface = super.getTypeface(name, style);
        if (typeface == null) {
            String fontFamily = stringCache.get(name);
            if (!TextUtils.isEmpty(fontFamily)) {
                String skinFontsDir = ExternalStorage.getZipSkinFontsDir(skinName);
                File fontFile = new File(skinFontsDir + File.separator + fontFamily);
                if (fontFile.exists() && fontFile.isFile()) {
                    typeface = Typeface.createFromFile(fontFile);
                }
            }
        }
        if (typeface != null) {
            typefaceCache.put(name, typeface);
        }
        return typeface;
    }

    public void asyncGetTypeface(final String name, final int type, final AsyncGetResCallback callback) {
        Task.callInBackground(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                final Typeface typeface = getTypeface(name, type);
                HandlerUtil.postOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            callback.getTypeface(typeface);
                        }
                    }
                });
                return null;
            }
        });
    }

    @Override
    public void release() {
        super.release();
        if (zipEntries != null) {
            zipEntries.clear();
        }
        if (colorCache != null) {
            colorCache.clear();
        }
        if (dimenCache != null) {
            dimenCache.clear();
        }
    }

    public interface AsyncGetResCallback {

        void getColorStateList(ColorStateList colorStateList);

        void getDrawable(Drawable drawable);

        void getTypeface(Typeface typeface);
    }

    public static class AsyncGetResAdapter implements AsyncGetResCallback {

        @Override
        public void getColorStateList(ColorStateList colorStateList) {
        }

        @Override
        public void getDrawable(Drawable drawable) {
        }

        @Override
        public void getTypeface(Typeface typeface) {
        }
    }
}
