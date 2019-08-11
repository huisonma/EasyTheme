package com.huison.skin.parser;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.NinePatch;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.StateListDrawable;
import android.text.TextUtils;
import android.util.Xml;

import com.huison.skin.Constants;
import com.huison.skin.utils.BitmapUtil;
import com.huison.skin.utils.CloseUtil;
import com.huison.skin.utils.NinePatchChunk;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by huisonma on 2019/5/7.
 */
public class XmlDrawableParser {

    public static Drawable getXmlDrawable(Context context, String zipFilePath, List<ZipEntry> zipEntries, String fileName) {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(zipFilePath);
            return getXmlDrawableInternal(context, zipFile, zipEntries, fileName);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CloseUtil.close(zipFile);
        }
        return null;
    }

    private static Drawable getXmlDrawableInternal(Context context, ZipFile zipFile, List<ZipEntry> zipEntries, String fileName) {
        for (ZipEntry zipEntry : zipEntries) {
            String zipEntryName = zipEntry.getName();
            if (zipEntryName.contains("drawable/")) {
                if (zipEntryName.endsWith(fileName + ".9.png") || zipEntryName.endsWith(fileName + ".9")) {
                    return createNinePatchDrawable(context, zipFile, zipEntry);
                } else if (zipEntryName.endsWith(fileName + ".xml")) {
                    return createXmlDrawable(context, zipFile, zipEntries, fileName, zipEntry);
                } else if (zipEntryName.endsWith(fileName + ".jpg")
                        || zipEntryName.endsWith(fileName + ".png")
                        || zipEntryName.endsWith(fileName + ".jpeg")) {
                    return createBitmapDrawable(context, zipFile, zipEntry);
                }
            }
        }
        return null;
    }

    private static Drawable createNinePatchDrawable(Context context, ZipFile zipFile, ZipEntry zipEntry) {
        Drawable drawable = null;
        InputStream inputStream = null;
        try {
            inputStream = zipFile.getInputStream(zipEntry);
            Bitmap bitmap = BitmapUtil.decode9PatchBitmap(inputStream);
            byte[] chunk = bitmap.getNinePatchChunk();
            Rect padding = new Rect();
            NinePatchChunk.readPaddingFromChunk(chunk, padding);
            if (NinePatch.isNinePatchChunk(chunk)) {
                bitmap.setDensity(480);
                drawable = new NinePatchDrawable(context.getResources(), bitmap, chunk, padding, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CloseUtil.close(inputStream);
        }
        return drawable;
    }

    private static Drawable createXmlDrawable(Context context, ZipFile zipFile, List<ZipEntry> zipEntries, String fileName, ZipEntry zipEntry) {
        Drawable drawable = null;
        XmlPullParser parser = Xml.newPullParser();
        InputStream inputStream = null;
        try {
            inputStream = zipFile.getInputStream(zipEntry);
            parser.setInput(inputStream, "utf-8");

            boolean startTagSelector = false;
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String parserName = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (TextUtils.equals(parserName, Constants.TAG_SELECTOR)) {
                            startTagSelector = true;
                            drawable = new StateListDrawable();
                        } else {
                            if (startTagSelector) {
                                if (TextUtils.equals(parserName, Constants.TAG_ITEM)) {
                                    parseDrawableSelectorItem(context, zipFile, zipEntries, parser, (StateListDrawable) drawable);
                                }
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (TextUtils.equals(parserName, Constants.TAG_SELECTOR) || TextUtils.equals(parserName, Constants.TAG_SHAPE)) {
                            return drawable;
                        }
                        break;
                    default:
                        break;
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CloseUtil.close(inputStream);
        }
        return null;
    }

    private static void parseDrawableSelectorItem(Context context, ZipFile zipFile, List<ZipEntry> zipEntries,
                                                  XmlPullParser parser, StateListDrawable stateListDrawable) {
        List<Integer> tempStates = new ArrayList<>();
        Drawable drawable = null;

        for (int i = 0, count = parser.getAttributeCount(); i < count; i++) {
            String name = parser.getAttributeName(i);
            String value = parser.getAttributeValue(i);

            if (TextUtils.equals(name, Constants.TAG_DRAWABLE)) {
                if (value.startsWith("#")) {
                    drawable = new ColorDrawable(Color.parseColor(value));
                } else if (value.startsWith(Constants.QUOTE_PREFIX_DRAWABLE)) {
                    value = value.replace(Constants.QUOTE_PREFIX_DRAWABLE, "");
                    drawable = getXmlDrawableInternal(context, zipFile, zipEntries, value);
                }
            } else {
                if (ResValuesXmlParser.SUPPORT_STATE_LIST_MAP.containsKey(name) && Boolean.parseBoolean(value)) {
                    tempStates.add(ResValuesXmlParser.SUPPORT_STATE_LIST_MAP.get(name));
                }
            }
        }
        int len = tempStates.size();
        int[] states = new int[len];
        for (int i = 0; i < len; i++) {
            states[i] = tempStates.get(i);
        }
        stateListDrawable.addState(states, drawable);
    }

    private static Drawable createBitmapDrawable(Context context, ZipFile zipFile, ZipEntry zipEntry) {
        Drawable drawable = null;
        InputStream inputStream = null;
        try {
            inputStream = zipFile.getInputStream(zipEntry);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            drawable = new BitmapDrawable(context.getResources(), bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CloseUtil.close(inputStream);
        }
        return drawable;
    }
}
