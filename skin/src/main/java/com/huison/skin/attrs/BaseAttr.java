package com.huison.skin.attrs;

import android.view.View;

import com.huison.skin.kind.ISkin;

/**
 * Created by huisonma on 2019/5/7.
 */
public abstract class BaseAttr {

    public static final String BACKGROUND = "background";
    public static final String TEXT_COLOR = "textColor";
    public static final String TEXT_SIZE = "textSize";
    public static final String FONT_FAMILY = "font_family";

    public String attrName;
    public String attrValue;
    public int resId;
    public String entryName;
    public String entryType;

    public abstract void apply(ISkin skin, View view);
}
