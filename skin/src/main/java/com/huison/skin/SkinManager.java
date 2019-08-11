package com.huison.skin;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v4.view.LayoutInflaterCompat;
import android.view.LayoutInflater;

import com.huison.skin.digger.ApkSkinDigger;
import com.huison.skin.digger.ISkinDigger;
import com.huison.skin.digger.ZipSkinDigger;
import com.huison.skin.kind.ApkSkin;
import com.huison.skin.kind.DefaultSkin;
import com.huison.skin.kind.ISkin;
import com.huison.skin.kind.ZipSkin;
import com.huison.skin.utils.HandlerUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import bolts.Task;

/**
 * Created by huisonma on 2019/5/7.
 */
public final class SkinManager implements ApkSkinDigger.OnApkSkinRefreshListener {

    /**
     * 默认皮肤，属于ApkSkin子类
     */
    public static final int TYPE_DEFAULT_SKIN = 0;

    /**
     * Apk皮肤，通过包名(packageName)匹配资源
     */
    public static final int TYPE_APK_SKIN = 1;

    /**
     * Zip皮肤，通过路径(path)匹配资源
     */
    public static final int TYPE_ZIP_SKIN = 2;

    @IntDef({
            TYPE_DEFAULT_SKIN,
            TYPE_APK_SKIN,
            TYPE_ZIP_SKIN,
    })
    public @interface SkinType {
    }

    public static SkinManager getInstance() {
        if (sInstance == null) {
            synchronized (SkinManager.class) {
                if (sInstance == null) {
                    sInstance = new SkinManager();
                }
            }
        }
        return sInstance;
    }

    private static SkinManager sInstance;

    private static SkinFactory2 sSkinFactory2;

    private static final Set<Activity> sActivities = new HashSet<>();

    private static Context sContext;

    private ISkin currentSkin;

    private int currentSkinType;

    private ISkinDigger apkSkinDigger;
    private ISkinDigger zipSkinDigger;

    private List<ApkSkinDigger.OnApkSkinRefreshListener> apkSkinRefreshListeners = new ArrayList<>();

    private SkinManager() {
        checkContext();

        apkSkinDigger = new ApkSkinDigger(sContext);
        ((ApkSkinDigger) apkSkinDigger).setOnApkSkinRefreshListener(this);
        zipSkinDigger = new ZipSkinDigger(sContext);

        currentSkin = apkSkinDigger.getSkins().get(Constants.DEFAULT_SKIN_PACKAGE);
        currentSkinType = TYPE_DEFAULT_SKIN;
    }

    public static void init(Application application) {
        if (application == null) {
            throw new NullPointerException("init SkinManager : application is null !");
        }
        sContext = application;
        application.registerActivityLifecycleCallbacks(sActivityLifecycleCallbacks);
    }

    private static void checkContext() {
        if (sContext == null) {
            throw new NullPointerException("sContext is null ! Your must call init() before use this class");
        }
    }

    public static Context context() {
        checkContext();
        return sContext;
    }

    /**
     * call on Activity onCreate() and before super.onCreate()
     */
    public void initSkinFactory2(LayoutInflater inflater) {
        sSkinFactory2 = new SkinFactory2();
        LayoutInflaterCompat.setFactory2(inflater, sSkinFactory2);
    }

    public void applyDefaultSkin() {
        applySkin(apkSkinDigger.getSkins().get(Constants.DEFAULT_SKIN_PACKAGE), true);
    }

    public boolean applySkin(ISkin newSkin) {
        return applySkin(newSkin, false);
    }

    public boolean applySkin(ISkin newSkin, boolean force) {
        if (!force && currentSkin == newSkin) {
            return false;
        }

        if (newSkin instanceof DefaultSkin) {
            currentSkinType = TYPE_APK_SKIN;
        } else if (newSkin instanceof ApkSkin) {
            currentSkinType = TYPE_APK_SKIN;
        } else if (newSkin instanceof ZipSkin) {
            currentSkinType = TYPE_ZIP_SKIN;
        }
        if (!checkSkinValid(newSkin.getSkinName(), currentSkinType)) {
            return false;
        }

        final ISkin lastSkin = currentSkin;
        currentSkin = newSkin;
        if (sSkinFactory2 != null) {
            Task.callInBackground(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    currentSkin.prepareInBackground();
                    HandlerUtil.postOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sSkinFactory2.apply(currentSkin);
                            if (lastSkin != null) {
                                lastSkin.release();
                            }
                        }
                    });
                    return null;
                }
            });
            return true;
        }
        return false;
    }

    /**
     * 确认即将使用的皮肤是否有效，路径是否合理，文件是否存在
     */
    public boolean checkSkinValid(String skinName, @SkinType int skinType) {
        boolean isSkinValid = false;
        if (skinType == TYPE_DEFAULT_SKIN) {
            isSkinValid = true;
        } else if (skinType == TYPE_ZIP_SKIN) {
            isSkinValid = zipSkinDigger.isSkinValid(skinName);
        } else if (skinType == TYPE_APK_SKIN) {
            isSkinValid = apkSkinDigger.isSkinValid(skinName);
        }
        return isSkinValid;
    }

    @Override
    public void onInstalledApkSkin(ISkin skin) {
        for (ApkSkinDigger.OnApkSkinRefreshListener listener : apkSkinRefreshListeners) {
            if (listener != null) {
                listener.onInstalledApkSkin(skin);
            }
        }
    }

    @Override
    public void onUninstalledApkSkin(ISkin skin) {
        for (ApkSkinDigger.OnApkSkinRefreshListener listener : apkSkinRefreshListeners) {
            if (listener != null) {
                listener.onUninstalledApkSkin(skin);
            }
        }
    }

    @Override
    public void onUpdatedApkSkin(ISkin skin) {
        for (ApkSkinDigger.OnApkSkinRefreshListener listener : apkSkinRefreshListeners) {
            if (listener != null) {
                listener.onUpdatedApkSkin(skin);
            }
        }
    }

    public boolean addOnApkSkinRefreshListener(ApkSkinDigger.OnApkSkinRefreshListener listener) {
        if (listener != null) {
            return apkSkinRefreshListeners.add(listener);
        }
        return false;
    }

    public boolean removeOnApkSkinRefreshListener(ApkSkinDigger.OnApkSkinRefreshListener listener) {
        if (listener != null) {
            return apkSkinRefreshListeners.remove(listener);
        }
        return false;
    }

    public List<ISkin> getAllSkins() {
        List<ISkin> allSkins = new ArrayList<>();
        Iterator<Map.Entry<String, ISkin>> apkSkinIterator = apkSkinDigger.getSkins().entrySet().iterator();
        while (apkSkinIterator.hasNext()) {
            Map.Entry<String, ISkin> entry = apkSkinIterator.next();
            allSkins.add(entry.getValue());
        }
        Iterator<Map.Entry<String, ISkin>> zipSkinIterator = zipSkinDigger.getSkins().entrySet().iterator();
        while (zipSkinIterator.hasNext()) {
            Map.Entry<String, ISkin> entry = zipSkinIterator.next();
            allSkins.add(entry.getValue());
        }
        return allSkins;
    }

    public List<ISkin> getAllApkSkins() {
        List<ISkin> allApkSkins = new ArrayList<>();
        Iterator<Map.Entry<String, ISkin>> apkSkinIterator = apkSkinDigger.getSkins().entrySet().iterator();
        while (apkSkinIterator.hasNext()) {
            Map.Entry<String, ISkin> entry = apkSkinIterator.next();
            allApkSkins.add(entry.getValue());
        }
        return allApkSkins;
    }

    public List<ISkin> getAllZipSkins() {
        List<ISkin> allZipSkins = new ArrayList<>();
        Iterator<Map.Entry<String, ISkin>> zipSkinIterator = zipSkinDigger.getSkins().entrySet().iterator();
        while (zipSkinIterator.hasNext()) {
            Map.Entry<String, ISkin> entry = zipSkinIterator.next();
            allZipSkins.add(entry.getValue());
        }
        return allZipSkins;
    }

    public ISkin getDefaultSkin() {
        return apkSkinDigger.getSkins().get(Constants.DEFAULT_SKIN_PACKAGE);
    }

    public ISkin getCurrentSkin() {
        return currentSkin == null ? getDefaultSkin() : currentSkin;
    }

    public int getCurrentSkinType() {
        return currentSkinType;
    }

    private static Application.ActivityLifecycleCallbacks sActivityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            if (activity != null) {
                sActivities.add(activity);
            }
        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            if (activity != null) {
                sActivities.remove(activity);
            }
            if (sActivities.isEmpty()) {
                if (sSkinFactory2 != null) {
                    sSkinFactory2.release();
                }
            }
        }
    };
}
