package com.huison.easytheme;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import com.huison.skin.SkinManager;

/**
 * Created by huisonma on 2019/5/7.
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // must call before super.onCreate() !
        SkinManager.getInstance().initSkinFactory2(getLayoutInflater());
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        applySkin();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        applySkin();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        applySkin();
    }

    protected void applySkin() {
        SkinManager.getInstance().applyDefaultSkin();
    }
}