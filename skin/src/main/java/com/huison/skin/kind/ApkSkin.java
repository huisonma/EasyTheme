package com.huison.skin.kind;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import com.huison.skin.Constants;
import com.huison.skin.utils.ResourceUtil;

/**
 * Created by huisonma on 2019/5/7.
 */
public class ApkSkin extends AbstractSkin {

    private String packageName;

    /**
     * Apk皮肤（其他App）上线文
     */
    private Context skinContext;

    public ApkSkin(Context context, String packageName) {
        super(context);
        this.packageName = packageName;
        if (context.getPackageName().equals(packageName)) {
            skinContext = context;
        } else {
            try {
                skinContext = context.createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (skinContext == null) {
            skinContext = context;
        }
    }

    @Override
    public void prepareInBackground() {

    }

    @Override
    public String getSkinName() {
        return packageName;
    }

    @Override
    public Integer getColor(String name) {
        int resId = ResourceUtil.getResourceId(skinContext, name, Constants.TAG_COLOR);
        if (resId > 0) {
            try {
                return skinContext.getResources().getColor(resId);
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
            }
        }
        return super.getColor(name);
    }

    @Override
    public ColorStateList getColorStateList(String name) {
        int resId = ResourceUtil.getResourceId(skinContext, name, Constants.TAG_COLOR);
        if (resId > 0) {
            try {
                return skinContext.getResources().getColorStateList(resId);
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
            }
        }
        return super.getColorStateList(name);
    }

    @Override
    public Drawable getDrawable(String name) {
        int resId = ResourceUtil.getResourceId(skinContext, name, Constants.TAG_DRAWABLE);
        if (resId > 0) {
            try {
                return skinContext.getResources().getDrawable(resId);
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
            }
        }
        return super.getDrawable(name);
    }

    @Override
    public Float getTextSize(String name) {
        int resId = ResourceUtil.getResourceId(skinContext, name, Constants.TAG_DIMEN);
        if (resId > 0) {
            try {
                return skinContext.getResources().getDimension(resId);
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
            }
        }
        return super.getTextSize(name);
    }

    @Override
    public Typeface getTypeface(String name, int style) {
        Typeface typeface = super.getTypeface(name, style);
        if (typeface == null) {
            try {
                typeface = Typeface.createFromAsset(skinContext.getResources().getAssets(), "fonts/" + name + ".ttf");
            } catch (Exception e1) {
                e1.printStackTrace();
                try {
                    typeface = Typeface.createFromAsset(skinContext.getResources().getAssets(), "fonts/" + name + ".otf");
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
        if (typeface != null) {
            typefaceCache.put(name, typeface);
        } else {
            typeface = Typeface.DEFAULT;
        }
        return typeface;
    }
}
