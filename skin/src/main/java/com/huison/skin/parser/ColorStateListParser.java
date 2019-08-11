package com.huison.skin.parser;

import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.util.Xml;

import com.huison.skin.Constants;
import com.huison.skin.utils.CloseUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by huisonma on 2019/5/7.
 */
public class ColorStateListParser {

    public static ColorStateList parseColorStateList(String zipFilePath, ZipEntry zipEntry, Map<String, Integer> colorCache) {
        XmlPullParser parser = Xml.newPullParser();
        ZipFile zipFile = null;
        InputStream inputStream = null;
        ColorStateList colorStateList = null;
        try {
            zipFile = new ZipFile(zipFilePath);
            inputStream = zipFile.getInputStream(zipEntry);
            parser.setInput(inputStream, "utf-8");

            boolean startTagSelector = false;
            final List<int[]> stateList = new LinkedList<>();
            final List<Integer> colorList = new LinkedList<>();

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String name = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (TextUtils.equals(name, Constants.TAG_SELECTOR)) {
                            startTagSelector = true;
                        } else if (TextUtils.equals(name, Constants.TAG_ITEM)) {
                            if (startTagSelector) {
                                parseColorSelectorItem(parser, stateList, colorList, colorCache);
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (TextUtils.equals(name, Constants.TAG_SELECTOR)) {
                            int len = stateList.size();
                            int[][] states = new int[len][];
                            int[] colors = new int[len];
                            for (int i = 0; i < len; i++) {
                                states[i] = stateList.get(i);
                                colors[i] = colorList.get(i);
                            }
                            colorStateList = new ColorStateList(states, colors);
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
            CloseUtil.close(zipFile);
        }
        return colorStateList;
    }

    private static void parseColorSelectorItem(XmlPullParser parser, List<int[]> stateList, List<Integer> colorList, Map<String, Integer> colorCache) {
        List<Integer> tempStates = new ArrayList<>();
        int color = 0;

        for (int i = 0, count = parser.getAttributeCount(); i < count; i++) {
            String name = parser.getAttributeName(i);
            String value = parser.getAttributeValue(i);

            if (TextUtils.equals(name, Constants.TAG_COLOR)) {
                if (value.startsWith("#")) {// "#FFFFFF"
                    color = ResValuesXmlParser.parseCompleteColor(value);
                } else if (value.startsWith(Constants.QUOTE_PREFIX_COLOR)) {// "@color/xxx"
                    value = value.replace(Constants.QUOTE_PREFIX_COLOR, "");
                    if (colorCache.containsKey(value)) {
                        color = colorCache.get(value);
                    } else {
                        throw new UnsupportedOperationException("could not identify color [@color/" + value + "]");
                    }
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
        stateList.add(states);
        colorList.add(color);
    }
}
