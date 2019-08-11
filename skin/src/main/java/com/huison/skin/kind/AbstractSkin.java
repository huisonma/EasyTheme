package com.huison.skin.kind;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.huison.skin.Constants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by huisonma on 2019/5/7.
 */
public abstract class AbstractSkin implements ISkin {

    private static final float DEFAULT_TEXT_SIZE = 16.0f;

    protected Context context;

    protected Map<String, Typeface> typefaceCache = new ConcurrentHashMap<>();

    AbstractSkin(Context context) {
        this.context = context;
    }

    @Override
    public void prepareInBackground() {

    }

    @Override
    public String getSkinName() {
        return null;
    }

    @Override
    public Integer getColor(String name) {
        return null;
    }

    @Override
    public ColorStateList getColorStateList(String name) {
        return null;
    }

    @Override
    public Drawable getDrawable(String name) {
        return null;
    }

    @Override
    public Float getTextSize(String name) {
        return DEFAULT_TEXT_SIZE;
    }

    @Override
    public Typeface getTypeface(String name, int style) {
        Typeface typeface = null;
        if (typefaceCache != null) {
            typeface = typefaceCache.get(name);
        }
        if (typeface == null) {
            if (TextUtils.equals(name, Constants.SYSTEM_TYPEFACE_SANS_SERIF)) {
                typeface = Typeface.create(Typeface.SANS_SERIF, style);
            } else if (TextUtils.equals(name, Constants.SYSTEM_TYPEFACE_SERIF)) {
                typeface = Typeface.create(Typeface.SERIF, style);
            } else if (TextUtils.equals(name, Constants.SYSTEM_TYPEFACE_MONOSPACE)) {
                typeface = Typeface.create(Typeface.MONOSPACE, style);
            }
        }
        return typeface;
    }

    @Override
    public void release() {
        if (typefaceCache != null) {
            typefaceCache.clear();
        }
    }
}
