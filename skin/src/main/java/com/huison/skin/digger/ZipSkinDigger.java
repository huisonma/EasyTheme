package com.huison.skin.digger;

import android.content.Context;
import android.text.TextUtils;

import com.huison.skin.kind.ISkin;
import com.huison.skin.kind.ZipSkin;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import bolts.Task;

/**
 * Created by huisonma on 2019/5/7.
 */
public class ZipSkinDigger implements ISkinDigger {

    private Map<String, ISkin> skins = new ConcurrentHashMap<>();

    private Context context;

    public ZipSkinDigger(Context context) {
        this.context = context.getApplicationContext();

        loadSkinsBackground();
    }

    private void loadSkinsBackground() {
        Task.callInBackground(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                File dir = context.getExternalFilesDir(null);
                String zipSkinDirPath;
                if (dir != null) {
                    zipSkinDirPath = dir.getAbsolutePath() + "/skin";
                } else {
                    zipSkinDirPath = context.getFilesDir().getAbsolutePath() + "/skin";
                }

                File zipSkinDir = new File(zipSkinDirPath);
                if (zipSkinDir.exists()) {
                    String[] list = zipSkinDir.list();
                    if (list != null && list.length > 0) {
                        for (String fileName : list) {
                            if (fileName.endsWith(".zip")) {
                                skins.put(fileName, new ZipSkin(context, zipSkinDirPath + "/" + fileName));
                            }
                        }
                    }
                } else {
                    zipSkinDir.mkdirs();
                }
                return null;
            }
        });
    }

    @Override
    public Map<String, ISkin> getSkins() {
        return skins;
    }

    @Override
    public boolean isSkinValid(String skinName) {
        if (TextUtils.isEmpty(skinName)) {
            return false;
        }
        File file = new File(skinName);
        return file.exists();
    }
}
