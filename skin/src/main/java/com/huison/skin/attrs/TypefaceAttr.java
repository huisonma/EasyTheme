package com.huison.skin.attrs;

import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import com.huison.skin.kind.ISkin;
import com.huison.skin.kind.ZipSkin;

/**
 * Created by huisonma on 2019/5/7.
 */
public class TypefaceAttr extends BaseAttr {

    @Override
    public void apply(ISkin skin, View view) {
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            if (skin instanceof ZipSkin) {
                applyZipSkin((ZipSkin) skin, textView);
            } else {
                int style = textView.getTypeface().getStyle();
                Typeface typeface = skin.getTypeface(entryName, style);
                if (typeface != null) {
                    textView.setTypeface(typeface);
                }
            }
        }
    }

    private void applyZipSkin(ZipSkin zipSkin, final TextView textView) {
        int style = textView.getTypeface().getStyle();
        zipSkin.asyncGetTypeface(entryName, style, new ZipSkin.AsyncGetResAdapter() {
            @Override
            public void getTypeface(Typeface typeface) {
                super.getTypeface(typeface);
                if (typeface != null) {
                    textView.setTypeface(typeface);
                }
            }
        });
    }
}
