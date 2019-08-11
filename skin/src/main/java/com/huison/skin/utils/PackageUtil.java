package com.huison.skin.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huisonma on 2019/5/7.
 */
public class PackageUtil {

    public static List<PackageInfo> getInstallPackages(Context context) {
        List<PackageInfo> packageInfoList = null;
        if (context != null) {
            PackageManager packageManager = context.getPackageManager();
            if (packageManager != null) {
                packageInfoList = context.getPackageManager().getInstalledPackages(0);
            }
        }
        return packageInfoList == null ? new ArrayList<PackageInfo>() : packageInfoList;
    }
}
