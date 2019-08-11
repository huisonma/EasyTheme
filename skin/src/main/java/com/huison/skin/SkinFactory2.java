package com.huison.skin;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.huison.skin.attrs.BackgroundAttr;
import com.huison.skin.attrs.BaseAttr;
import com.huison.skin.attrs.TextColorAttr;
import com.huison.skin.attrs.TextSizeAttr;
import com.huison.skin.attrs.TypefaceAttr;
import com.huison.skin.holder.AttrHolder;
import com.huison.skin.kind.ISkin;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huisonma on 2019/5/7.
 */
public final class SkinFactory2 implements LayoutInflater.Factory2 {

    private static final String TAG = "SkinFactory2";

    private static final String DEFAULT_NAMESPACE = "http://schemas.android.com/apk/res-dynamic";

    private static final String ATTRIBUTE_NAME_DYNAMIC_THEMES = "dynamic_themes";

    private static final List<String> SUPPORT_ATTRS = new ArrayList<>();

    static {
        SUPPORT_ATTRS.add(BaseAttr.BACKGROUND);
        SUPPORT_ATTRS.add(BaseAttr.TEXT_COLOR);
        SUPPORT_ATTRS.add(BaseAttr.TEXT_SIZE);
        SUPPORT_ATTRS.add(BaseAttr.FONT_FAMILY);
    }

    private List<AttrHolder> themeHolders;

    public SkinFactory2() {
        themeHolders = new ArrayList<>();
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return onCreateView(null, name, context, attrs);
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        View view = null;
        boolean dynamicTheme = attrs.getAttributeBooleanValue(DEFAULT_NAMESPACE, ATTRIBUTE_NAME_DYNAMIC_THEMES, false);
        if (dynamicTheme) {
            view = createView(parent, name, context, attrs);
            if (view != null) {
                parseAttributeSet(view, context, attrs);
            }
        }
        return view;
    }

    private static View createView(View parent, String name, Context context, AttributeSet attrs) {
        if (TextUtils.isEmpty(name)) {
            return null;
        }
        View view;
        if (name.contains(".")) {
            view = createView(parent, name, context, attrs, null);
        } else {
            view = createView(parent, name, context, attrs, "android.view.");

            if (view == null) {
                view = createView(parent, name, context, attrs, "android.widget.");
            }

            if (view == null) {
                view = createView(parent, name, context, attrs, "android.webkit.");
            }
        }
        return view;
    }

    private static View createView(View parent, String name, Context context, AttributeSet attrs, String prefix) {
        Log.d(TAG, (prefix == null ? "" : prefix) + name);
        View view = null;
        try {
            view = LayoutInflater.from(context).createView(name, prefix, attrs);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return view;
    }

    private void parseAttributeSet(View view, Context context, AttributeSet attrs) {
        final Resources res = context.getResources();
        final int attrCount = attrs.getAttributeCount();
        List<BaseAttr> baseAttrs = null;
        for (int i = 0; i < attrCount; i++) {
            final String attrName = attrs.getAttributeName(i);
            if (isSupportAttr(attrName)) {
                final String attrValue = attrs.getAttributeValue(i);
                if (TextUtils.isEmpty(attrValue)) {
                    continue;
                }
                if (attrValue.startsWith("@")) {
                    int resId = Integer.valueOf(attrValue.substring(1));
                    final String entryName = res.getResourceEntryName(resId);
                    final String entryType = res.getResourceTypeName(resId);

                    BaseAttr baseAttr = createAttr(attrName, attrValue, resId, entryName, entryType);
                    if (baseAttr != null) {
                        if (baseAttrs == null) {
                            baseAttrs = new ArrayList<>();
                        }
                        baseAttrs.add(baseAttr);
                    }

                    Log.d(TAG, new StringBuilder()
                            .append(view.getClass().getSimpleName())
                            .append(" : attrName = ").append(attrName)
                            .append(", attrValue = ").append(attrValue)
                            .append(", resId = ").append(resId)
                            .append(", entryName = ").append(entryName)
                            .append(", entryType = ").append(entryType)
                            .toString());
                } else {
                    BaseAttr baseAttr = createAttr(attrName, attrValue, 0, null, null);
                    if (baseAttr != null) {
                        if (baseAttrs == null) {
                            baseAttrs = new ArrayList<>();
                        }
                        baseAttrs.add(baseAttr);
                    }

                    Log.d(TAG, new StringBuilder()
                            .append(view.getClass().getSimpleName())
                            .append(" : attrName = ").append(attrName)
                            .append(", resId = ").append(attrValue)
                            .toString());
                }
            }
        }
        if (baseAttrs != null && !baseAttrs.isEmpty()) {
            AttrHolder themeHolder = new AttrHolder(view, baseAttrs);
            themeHolders.add(themeHolder);
        }
    }

    /**
     * 例：<TextView android:textColor="@color/colorAccent" />
     * attrName = textColor, resId = @2131034154, resId = 2131034154, entryName = colorAccent, entryType = color
     *
     * @param attrName  textColor
     * @param attrValue @2131034154
     * @param id        2131034154
     * @param entryName colorAccent
     * @param entryType color
     * @return
     */
    private BaseAttr createAttr(String attrName, String attrValue, int id, String entryName, String entryType) {
        BaseAttr baseAttr = null;
        switch (attrName) {
            case BaseAttr.BACKGROUND:
                baseAttr = new BackgroundAttr();
                break;
            case BaseAttr.TEXT_COLOR:
                baseAttr = new TextColorAttr();
                break;
            case BaseAttr.TEXT_SIZE:
                baseAttr = new TextSizeAttr();
                break;
            case BaseAttr.FONT_FAMILY:
                baseAttr = new TypefaceAttr();
                break;
            default:
                break;
        }
        if (baseAttr != null) {
            baseAttr.attrName = attrName;
            baseAttr.attrValue = attrValue;
            baseAttr.resId = id;
            baseAttr.entryName = entryName;
            baseAttr.entryType = entryType;
        }

        return baseAttr;
    }

    public void apply(ISkin skin) {
        if (themeHolders != null) {
            for (AttrHolder themeHolder : themeHolders) {
                themeHolder.apply(skin);
            }
        }
    }

    public void release() {
        if (themeHolders != null) {
            for (AttrHolder themeHolder : themeHolders) {
                themeHolder.release();
            }
            themeHolders.clear();
            themeHolders = null;
        }
    }

    private static boolean isSupportAttr(String attrName) {
        return SUPPORT_ATTRS.contains(attrName);
    }
}
