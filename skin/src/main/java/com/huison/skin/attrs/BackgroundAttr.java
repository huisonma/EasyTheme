package com.huison.skin.attrs;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;

import com.huison.skin.kind.ISkin;
import com.huison.skin.Constants;
import com.huison.skin.kind.ZipSkin;

/**
 * Created by huisonma on 2019/5/7.
 */
public class BackgroundAttr extends BaseAttr {

    @Override
    public void apply(ISkin skin, final View view) {
        if (skin instanceof ZipSkin) {
            applyZipSkin((ZipSkin) skin, view);
        } else {
            if (TextUtils.equals(Constants.TAG_COLOR, entryType)) {
                Integer color = skin.getColor(entryName);
                if (color != null) {
                    view.setBackgroundColor(color);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ColorStateList colorStateList = skin.getColorStateList(entryName);
                        if (colorStateList != null) {
                            view.setBackgroundTintList(colorStateList);
                        }
                    }
                }
            } else if (TextUtils.equals(Constants.TAG_DRAWABLE, entryType)) {
                view.setBackground(skin.getDrawable(entryName));
            }
        }
    }

    private void applyZipSkin(ZipSkin zipSkin, final View view) {
        if (TextUtils.equals(Constants.TAG_COLOR, entryType)) {
            Integer color = zipSkin.getColor(entryName);
            if (color != null) {
                view.setBackgroundColor(color);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    zipSkin.asyncGetColorStateList(entryName, new ZipSkin.AsyncGetResAdapter() {
                        @Override
                        public void getColorStateList(ColorStateList colorStateList) {
                            super.getColorStateList(colorStateList);
                            if (colorStateList != null) {
                                view.setBackgroundTintList(colorStateList);
                            }
                        }
                    });
                }
            }
        } else if (TextUtils.equals(Constants.TAG_DRAWABLE, entryType)) {
            zipSkin.asyncGetDrawable(entryName, new ZipSkin.AsyncGetResAdapter() {
                @Override
                public void getDrawable(Drawable drawable) {
                    super.getDrawable(drawable);
                    view.setBackground(drawable);
                }
            });
        }
    }
}
