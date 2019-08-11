package com.huison.skin.attrs;

import android.view.View;
import android.widget.TextView;

import com.huison.skin.kind.ISkin;
import com.huison.skin.utils.DensityUtil;

/**
 * Created by huisonma on 2019/5/7.
 */
public class TextSizeAttr extends BaseAttr {

    @Override
    public void apply(ISkin skin, View view) {
        if (view instanceof TextView) {
            ((TextView) view).setTextSize(DensityUtil.px2dp(view.getContext(), skin.getTextSize(entryName)));
        }
    }
}