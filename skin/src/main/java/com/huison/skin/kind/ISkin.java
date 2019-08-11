package com.huison.skin.kind;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

/**
 * Created by huisonma on 2019/5/7.
 */
public interface ISkin {

    void prepareInBackground();

    String getSkinName();

    Integer getColor(String name);

    ColorStateList getColorStateList(String name);

    Drawable getDrawable(String name);

    Float getTextSize(String name);

    Typeface getTypeface(String name, int style);

    void release();
}
