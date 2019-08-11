package com.huison.skin.holder;

import android.view.View;

import com.huison.skin.kind.ISkin;
import com.huison.skin.attrs.BaseAttr;

import java.util.List;

/**
 * Created by huisonma on 2019/5/7.
 */
public final class AttrHolder {

    private View view;

    private List<BaseAttr> attrs;

    public AttrHolder(View view, List<BaseAttr> attrs) {
        this.view = view;
        this.attrs = attrs;
    }

    public void apply(ISkin skin) {
        if (attrs != null && view != null) {
            for (BaseAttr attr : attrs) {
                attr.apply(skin, view);
            }
        }
    }

    public void release() {
        if (view != null) {
            view = null;
        }
        if (attrs != null) {
            attrs.clear();
        }
        attrs = null;
    }
}
