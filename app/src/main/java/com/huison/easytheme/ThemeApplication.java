package com.huison.easytheme;

import android.app.Application;

import com.huison.skin.SkinManager;
import com.huison.skin.utils.LogUtil;

/**
 * Created by huisonma on 2019/5/7.
 */
public class ThemeApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.enableDebug();
        SkinManager.init(this);
    }
}
