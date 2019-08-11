package com.huison.skin.parser;

import android.graphics.Color;
import android.text.TextUtils;
import android.util.Xml;

import com.huison.skin.Constants;
import com.huison.skin.SkinManager;
import com.huison.skin.utils.CloseUtil;
import com.huison.skin.utils.DensityUtil;
import com.huison.skin.utils.LogUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by huisonma on 2019/5/7.
 */
public class ResValuesXmlParser {

    private static final String TAG = "ResXmlParser";

    public static final Map<String, Integer> SUPPORT_STATE_LIST_MAP = new HashMap<>();

    /**
     * 暂时支持以下几种state
     */
    static {
        SUPPORT_STATE_LIST_MAP.put("state_selected", android.R.attr.state_selected);
        SUPPORT_STATE_LIST_MAP.put("state_pressed", android.R.attr.state_pressed);
        SUPPORT_STATE_LIST_MAP.put("state_checked", android.R.attr.state_checked);
    }

    public static void parseColorXml(Map<String, Integer> cache, String zipFilePath, ZipEntry zipEntry) {
        XmlPullParser parser = Xml.newPullParser();
        ZipFile zipFile = null;
        InputStream inputStream = null;
        try {
            zipFile = new ZipFile(zipFilePath);
            inputStream = zipFile.getInputStream(zipEntry);
            parser.setInput(inputStream, "utf-8");

            Map<String, String> quotedColors = new HashMap<>();
            boolean startTagResources = false;
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String name = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (TextUtils.equals(name, Constants.TAG_RESOURCES)) {
                            startTagResources = true;
                        } else if (TextUtils.equals(name, Constants.TAG_COLOR)) {
                            if (startTagResources) {
                                parseColorInternal(cache, parser, quotedColors);
                            }
                        }
                        break;
                    default:
                        break;
                }
                eventType = parser.next();
            }
            // 填充"@color/xxx"
            for (Map.Entry<String, String> entry : quotedColors.entrySet()) {
                if (entry != null) {
                    String colorName = entry.getValue();
                    if (cache.containsKey(colorName)) {
                        cache.put(entry.getKey(), cache.get(colorName));
                    } else {
                        throw new UnsupportedOperationException("could not identify color [@color/" + colorName + "]");
                    }
                }
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CloseUtil.close(inputStream);
            CloseUtil.close(zipFile);
        }
    }

    /**
     * <color name="background_color">#A0A0F0</color>
     */
    private static void parseColorInternal(Map<String, Integer> cache, XmlPullParser parser, Map<String, String> quotedColors) {
        try {
            String colorKey = parser.getAttributeValue(null, Constants.TAG_NAME);
            String colorStr = parser.nextText();
            LogUtil.d(TAG, "color : key = " + colorKey + ", value = " + colorStr);
            if (TextUtils.isEmpty(colorStr)) {
                return;
            }

            if (colorStr.startsWith("#")) {// "#FFFFFF"
                cache.put(colorKey, parseCompleteColor(colorStr));
            } else if (colorStr.startsWith(Constants.QUOTE_PREFIX_COLOR)) {// "@color/xxx"
                colorStr = colorStr.replace(Constants.QUOTE_PREFIX_COLOR, "");
                quotedColors.put(colorKey, colorStr);
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void parseDimenXml(Map<String, Float> cache, String zipFilePath, ZipEntry zipEntry) {
        XmlPullParser parser = Xml.newPullParser();
        ZipFile zipFile = null;
        InputStream inputStream = null;
        try {
            zipFile = new ZipFile(zipFilePath);
            inputStream = zipFile.getInputStream(zipEntry);
            parser.setInput(inputStream, "utf-8");

            Map<String, String> quotedDimens = new HashMap<>();
            boolean startTagResources = false;
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String name = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (TextUtils.equals(name, Constants.TAG_RESOURCES)) {
                            startTagResources = true;
                        } else if (TextUtils.equals(name, Constants.TAG_DIMEN)) {
                            if (startTagResources) {
                                parseDimenInternal(cache, parser, quotedDimens);
                            }
                        }
                        break;
                    default:
                        break;
                }
                eventType = parser.next();
            }
            // 填充"@dimen/xxx"
            for (Map.Entry<String, String> entry : quotedDimens.entrySet()) {
                if (entry != null) {
                    String dimenName = entry.getValue();
                    if (cache.containsKey(dimenName)) {
                        cache.put(entry.getKey(), cache.get(dimenName));
                    } else {
                        throw new UnsupportedOperationException("could not identify color [@dimen/" + dimenName + "]");
                    }
                }
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CloseUtil.close(inputStream);
            CloseUtil.close(zipFile);
        }
    }

    /**
     * <dimen name="theme_text_size">16sp</dimen>
     */
    private static void parseDimenInternal(Map<String, Float> cache, XmlPullParser parser, Map<String, String> quotedDimens) {
        try {
            String dimenKey = parser.getAttributeValue(null, Constants.TAG_NAME);
            String dimenStr = parser.nextText();
            LogUtil.d(TAG, "dimen : key = " + dimenKey + ", value = " + dimenStr);
            if (TextUtils.isEmpty(dimenStr)) {
                return;
            }

            if (dimenStr.startsWith(Constants.QUOTE_PREFIX_DIMEN)) {// "@dimen/xxx"
                dimenStr = dimenStr.replace(Constants.QUOTE_PREFIX_DIMEN, "");
                quotedDimens.put(dimenKey, dimenStr);
            } else {
                cache.put(dimenKey, formatDimenValueToPixel(dimenStr));
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public static void parseStringXml(Map<String, String> cache, String zipFilePath, ZipEntry zipEntry) {
        XmlPullParser parser = Xml.newPullParser();
        ZipFile zipFile = null;
        InputStream inputStream = null;
        try {
            zipFile = new ZipFile(zipFilePath);
            inputStream = zipFile.getInputStream(zipEntry);
            parser.setInput(inputStream, "utf-8");

            Map<String, String> quotedStrings = new HashMap<>();
            boolean startTagResources = false;
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String name = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (TextUtils.equals(name, Constants.TAG_RESOURCES)) {
                            startTagResources = true;
                        } else if (TextUtils.equals(name, Constants.TAG_STRING)) {
                            if (startTagResources) {
                                parseStringInternal(cache, parser, quotedStrings);
                            }
                        }
                        break;
                    default:
                        break;
                }
                eventType = parser.next();
            }
            // 填充"@string/xxx"
            for (Map.Entry<String, String> entry : quotedStrings.entrySet()) {
                if (entry != null) {
                    String stringName = entry.getValue();
                    if (cache.containsKey(stringName)) {
                        cache.put(entry.getKey(), cache.get(stringName));
                    } else {
                        throw new UnsupportedOperationException("could not identify color [@string/" + stringName + "]");
                    }
                }
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CloseUtil.close(inputStream);
            CloseUtil.close(zipFile);
        }
    }

    /**
     * <string name="font_family">Helvetica.ttf</string>
     */
    private static void parseStringInternal(Map<String, String> cache, XmlPullParser parser, Map<String, String> quotedStrings) {
        try {
            String stringKey = parser.getAttributeValue(null, Constants.TAG_NAME);
            String string = parser.nextText();
            LogUtil.d(TAG, "string : key = " + stringKey + ", value = " + string);
            if (TextUtils.isEmpty(string)) {
                return;
            }

            if (string.startsWith(Constants.QUOTE_PREFIX_STRING)) {// "@string/xxx"
                string = string.replace(Constants.QUOTE_PREFIX_STRING, "");
                quotedStrings.put(stringKey, string);
            } else {
                cache.put(stringKey, string);
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    static float formatDimenValueToPixel(String dimenValue) {
        float result = 0;
        try {
            if (!TextUtils.isEmpty(dimenValue)) {
                if (dimenValue.endsWith(Constants.TAG_DP)) {// 16dp
                    dimenValue = dimenValue.substring(0, dimenValue.indexOf(Constants.TAG_DP));
                    result = DensityUtil.dp2px(SkinManager.context(), Float.valueOf(dimenValue));
                } else if (dimenValue.endsWith(Constants.TAG_SP)) {// 16sp
                    dimenValue = dimenValue.substring(0, dimenValue.indexOf(Constants.TAG_SP));
                    result = DensityUtil.sp2px(SkinManager.context(), Float.valueOf(dimenValue));
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return result;
    }

    static int parseCompleteColor(String colorStr) {
        int color = 0;
        if (colorStr.length() == 7 || colorStr.length() == 9) {
            color = Color.parseColor(colorStr);
        } else if (colorStr.length() == 4) {
            // 补全颜色字符：#FAF => #F0A0F0
            char[] bytes = colorStr.toCharArray();
            char[] newBytes = new char[]{bytes[0], bytes[1], '0', bytes[2], '0', bytes[3], '0'};
            String newColorStr = new String(newBytes);
            try {
                LogUtil.d(TAG, "newColorStr = " + newColorStr);
                color = Color.parseColor(newColorStr);
            } catch (IllegalArgumentException e) {
                throw new UnsupportedOperationException("Unknown color ! oriColorStr = " + colorStr + ", newColorStr = " + newColorStr);
            }
        }
        return color;
    }
}
