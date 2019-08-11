package com.huison.skin.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by huisonma on 2019/5/7.
 */
public class NinePatchChunk {

    private static final int NO_COLOR = 0x00000001;

    public static void readPaddingFromChunk(byte[] chunk, Rect paddingRect) {
        paddingRect.left = getInt(chunk, 12);
        paddingRect.right = getInt(chunk, 16);
        paddingRect.top = getInt(chunk, 20);
        paddingRect.bottom = getInt(chunk, 24);
    }

    public static byte[] readChunk(Bitmap resourceBmp) throws IOException {
        final int bitmapWidth = resourceBmp.getWidth();
        final int bitmapHeight = resourceBmp.getHeight();

        int xPointCount = 0;
        int yPointCount = 0;
        int xBlockCount = 0;
        int yBlockCount = 0;

        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        for (int i = 0; i < 32; i++) {
            bao.write(0);
        }

        {
            int[] pixelsTop = new int[bitmapWidth - 2];
            resourceBmp.getPixels(pixelsTop, 0, bitmapWidth, 1, 0, bitmapWidth - 2, 1);
            boolean topFirstPixelIsBlack = pixelsTop[0] == Color.BLACK;
            boolean topLastPixelIsBlack = pixelsTop[pixelsTop.length - 1] == Color.BLACK;
            int tmpLastColor = Color.TRANSPARENT;
            for (int i = 0, len = pixelsTop.length; i < len; i++) {
                if (tmpLastColor != pixelsTop[i]) {
                    xPointCount++;
                    writeInt(bao, i);
                    tmpLastColor = pixelsTop[i];
                }
            }
            if (topLastPixelIsBlack) {
                xPointCount++;
                writeInt(bao, pixelsTop.length);
            }
            xBlockCount = xPointCount + 1;
            if (topFirstPixelIsBlack) {
                xBlockCount--;
            }
            if (topLastPixelIsBlack) {
                xBlockCount--;
            }
        }

        {
            int[] pixelsLeft = new int[bitmapHeight - 2];
            resourceBmp.getPixels(pixelsLeft, 0, 1, 0, 1, 1, bitmapHeight - 2);
            boolean firstPixelIsBlack = pixelsLeft[0] == Color.BLACK;
            boolean lastPixelIsBlack = pixelsLeft[pixelsLeft.length - 1] == Color.BLACK;
            int tmpLastColor = Color.TRANSPARENT;
            for (int i = 0, len = pixelsLeft.length; i < len; i++) {
                if (tmpLastColor != pixelsLeft[i]) {
                    yPointCount++;
                    writeInt(bao, i);
                    tmpLastColor = pixelsLeft[i];
                }
            }
            if (lastPixelIsBlack) {
                yPointCount++;
                writeInt(bao, pixelsLeft.length);
            }
            yBlockCount = yPointCount + 1;
            if (firstPixelIsBlack) {
                yBlockCount--;
            }
            if (lastPixelIsBlack) {
                yBlockCount--;
            }
        }

        {
            for (int i = 0; i < xBlockCount * yBlockCount; i++) {
                writeInt(bao, NO_COLOR);
            }
        }

        byte[] data = bao.toByteArray();
        data[0] = 1;
        data[1] = (byte) xPointCount;
        data[2] = (byte) yPointCount;
        data[3] = (byte) (xBlockCount * yBlockCount);
        dealPaddingInfo(resourceBmp, data);
        CloseUtil.close(bao);
        return data;
    }

    private static void dealPaddingInfo(Bitmap bm, byte[] data) {
        {   // padding left & padding right
            int[] bottomPixels = new int[bm.getWidth() - 2];
            bm.getPixels(bottomPixels, 0, bottomPixels.length, 1,
                    bm.getHeight() - 1, bottomPixels.length, 1);
            for (int i = 0; i < bottomPixels.length; i++) {
                if (Color.BLACK == bottomPixels[i]) { // padding left
                    writeInt(data, 12, i);
                    break;
                }
            }
            for (int i = bottomPixels.length - 1; i >= 0; i--) {
                if (Color.BLACK == bottomPixels[i]) { // padding right
                    writeInt(data, 16, bottomPixels.length - i - 1);
                    break;
                }
            }
        }
        { // padding top & padding bottom
            int[] rightPixels = new int[bm.getHeight() - 2];
            bm.getPixels(rightPixels, 0, 1, bm.getWidth() - 1, 0, 1,
                    rightPixels.length);
            for (int i = 0; i < rightPixels.length; i++) {
                if (Color.BLACK == rightPixels[i]) { // padding top
                    writeInt(data, 20, i);
                    break;
                }
            }
            for (int i = rightPixels.length - 1; i >= 0; i--) {
                if (Color.BLACK == rightPixels[i]) { // padding bottom
                    writeInt(data, 24, rightPixels.length - i);
                    break;
                }
            }
        }
    }

    private static int getInt(byte[] bs, int from) {
        int b1 = bs[from + 0];
        int b2 = bs[from + 1];
        int b3 = bs[from + 2];
        int b4 = bs[from + 3];
        int i = b1 | (b2 << 8) | (b3 << 16) | b4 << 24;
        return i;
    }

    private static void writeInt(OutputStream out, int v) throws IOException {
        out.write((v >> 0) & 0xFF);
        out.write((v >> 8) & 0xFF);
        out.write((v >> 16) & 0xFF);
        out.write((v >> 24) & 0xFF);
    }

    private static void writeInt(byte[] b, int offset, int v) {
        b[offset + 0] = (byte) (v >> 0);
        b[offset + 1] = (byte) (v >> 8);
        b[offset + 2] = (byte) (v >> 16);
        b[offset + 3] = (byte) (v >> 24);
    }
}
