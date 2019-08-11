package com.huison.skin.digger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.text.TextUtils;

import com.huison.skin.kind.ApkSkin;
import com.huison.skin.Constants;
import com.huison.skin.kind.DefaultSkin;
import com.huison.skin.kind.ISkin;
import com.huison.skin.utils.LogUtil;
import com.huison.skin.utils.PackageUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import bolts.Task;

/**
 * Created by huisonma on 2019/5/7.
 */
public class ApkSkinDigger extends BroadcastReceiver implements ISkinDigger {

    private static final String TAG = "ApkSkinDigger";

    private static final String DATA_SCHEMA = "package";

    private static final int DATA_SCHEMA_LENGTH = (DATA_SCHEMA + ":").length();

    private Context context;
    private Map<String, ISkin> skins = new ConcurrentHashMap<>();

    private OnApkSkinRefreshListener onApkSkinRefreshListener;

    public ApkSkinDigger(Context context) {
        this.context = context.getApplicationContext();
        skins.put(Constants.DEFAULT_SKIN_PACKAGE, new DefaultSkin(context));
        loadSkinsBackground();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter.addDataScheme(DATA_SCHEMA);
        this.context.registerReceiver(this, intentFilter);
    }

    private void loadSkinsBackground() {
        Task.callInBackground(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                List<PackageInfo> infoList = PackageUtil.getInstallPackages(context);
                for (PackageInfo packageInfo : infoList) {
                    if (packageInfo != null
                            && !TextUtils.equals(Constants.DEFAULT_SKIN_PACKAGE, packageInfo.packageName)
                            && packageInfo.packageName.startsWith(Constants.APK_SKIN_PACKAGE_NAME_PREFIX)) {

                        skins.put(packageInfo.packageName, new ApkSkin(context, packageInfo.packageName));
                    }
                }
                return null;
            }
        });
    }

    public void setOnApkSkinRefreshListener(OnApkSkinRefreshListener listener) {
        onApkSkinRefreshListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            final String action = intent.getAction();
            final String dataString = intent.getDataString();
            if (action != null && dataString != null) {
                String packageName;
                switch (action) {
                    case Intent.ACTION_PACKAGE_ADDED:
                        packageName = dataString.substring(DATA_SCHEMA_LENGTH);
                        if (packageName.startsWith(Constants.APK_SKIN_PACKAGE_NAME_PREFIX)) {
                            onSkinInstalled(packageName);
                        }
                        break;
                    case Intent.ACTION_PACKAGE_FULLY_REMOVED:
                        packageName = dataString.substring(DATA_SCHEMA_LENGTH);
                        if (packageName.startsWith(Constants.APK_SKIN_PACKAGE_NAME_PREFIX)) {
                            onSkinUninstalled(packageName);
                        }
                        break;
                    case Intent.ACTION_PACKAGE_REPLACED:
                        packageName = dataString.substring(DATA_SCHEMA_LENGTH);
                        if (packageName.startsWith(Constants.APK_SKIN_PACKAGE_NAME_PREFIX)) {
                            onSkinReplaced(packageName);
                        }
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "onReceive Error : " + e.getMessage());
        }
    }

    private void onSkinInstalled(String packageName) {
        ISkin skin = new ApkSkin(context, packageName);
        skins.put(packageName, skin);
        if (onApkSkinRefreshListener != null) {
            onApkSkinRefreshListener.onInstalledApkSkin(skin);
        }
    }

    private void onSkinUninstalled(String packageName) {
        ISkin skin = skins.remove(packageName);
        if (onApkSkinRefreshListener != null) {
            onApkSkinRefreshListener.onUninstalledApkSkin(skin);
        }
    }

    private void onSkinReplaced(String packageName) {
        ISkin skin = skins.remove(packageName);
        if (onApkSkinRefreshListener != null) {
            onApkSkinRefreshListener.onUpdatedApkSkin(skin);
        }
    }

    @Override
    public Map<String, ISkin> getSkins() {
        return skins;
    }

    @Override
    public boolean isSkinValid(String skinName) {
        if (skins != null) {
            return skins.containsKey(skinName);
        }
        return false;
    }

    public interface OnApkSkinRefreshListener {

        void onInstalledApkSkin(ISkin skin);

        void onUninstalledApkSkin(ISkin skin);

        void onUpdatedApkSkin(ISkin skin);
    }
}
