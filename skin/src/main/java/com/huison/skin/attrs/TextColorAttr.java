package com.huison.skin.attrs;

import android.content.res.ColorStateList;
import android.view.View;
import android.widget.TextView;

import com.huison.skin.kind.ISkin;
import com.huison.skin.kind.ZipSkin;

/**
 * Created by huisonma on 2019/5/7.
 */
public class TextColorAttr extends BaseAttr {

    @Override
    public void apply(ISkin skin, View view) {
        if (view instanceof TextView) {
            if (skin instanceof ZipSkin) {
                applyZipSkin((ZipSkin) skin, view);
            } else {
                Integer color = skin.getColor(entryName);
                if (color != null) {
                    ((TextView) view).setTextColor(color);
                } else {
                    ColorStateList colorStateList = skin.getColorStateList(entryName);
                    if (colorStateList != null) {
                        ((TextView) view).setTextColor(colorStateList);
                    }
                }
            }
        }
    }

    private void applyZipSkin(ZipSkin zipSkin, final View view) {
        Integer color = zipSkin.getColor(entryName);
        if (color != null) {
            ((TextView) view).setTextColor(color);
        } else {
            zipSkin.asyncGetColorStateList(entryName, new ZipSkin.AsyncGetResAdapter() {
                @Override
                public void getColorStateList(ColorStateList colorStateList) {
                    super.getColorStateList(colorStateList);
                    if (colorStateList != null) {
                        ((TextView) view).setTextColor(colorStateList);
                    }
                }
            });
        }
    }
}
