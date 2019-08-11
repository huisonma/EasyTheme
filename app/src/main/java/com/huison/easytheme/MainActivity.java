package com.huison.easytheme;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.PermissionChecker;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.huison.skin.SkinManager;
import com.huison.skin.digger.ApkSkinDigger;
import com.huison.skin.kind.ISkin;
import com.huison.skin.kind.ZipSkin;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements ApkSkinDigger.OnApkSkinRefreshListener {

    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 0x100;

    SkinSelectorAdapter adapter;

    List<ISkin> skins = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.rv_main);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapter = new SkinSelectorAdapter();
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        skins = SkinManager.getInstance().getAllApkSkins();
        SkinManager.getInstance().addOnApkSkinRefreshListener(this);
        startPermissionCheck();
    }

    @Override
    public void onInstalledApkSkin(ISkin skin) {
        skins.add(skin);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onUninstalledApkSkin(ISkin skin) {
        SkinManager.getInstance().applyDefaultSkin();
        skins.remove(skin);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onUpdatedApkSkin(ISkin skin) {

    }

    @Override
    protected void onDestroy() {
        SkinManager.getInstance().removeOnApkSkinRefreshListener(this);
        super.onDestroy();
    }

    private void startPermissionCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_READ_EXTERNAL_STORAGE);
            } else {
                loadZipSkins();
            }
        }
    }

    private void loadZipSkins() {
        List<ISkin> zipSkins = SkinManager.getInstance().getAllZipSkins();
        skins.addAll(zipSkins);
        adapter.notifySkins(skins);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PermissionChecker.PERMISSION_GRANTED) {
            switch (requestCode) {
                case REQUEST_CODE_READ_EXTERNAL_STORAGE:
                    loadZipSkins();
                    break;
                default:
                    break;
            }
        }
    }

    static class SkinSelectorAdapter extends RecyclerView.Adapter<SkinSelectorViewHolder> {

        List<ISkin> skins;

        SkinSelectorAdapter() {
        }

        void notifySkins(List<ISkin> skins) {
            this.skins = skins;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public SkinSelectorViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_skin_selector, viewGroup, false);
            return new SkinSelectorViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SkinSelectorViewHolder viewHolder, int i) {
            if (skins != null) {
                viewHolder.bindData(skins.get(i));
            }
        }

        @Override
        public int getItemCount() {
            return skins != null ? skins.size() : 0;
        }
    }

    static class SkinSelectorViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        ISkin skin;

        SkinSelectorViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_view_holder);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SkinManager.getInstance().applySkin(skin);
                }
            });
        }

        void bindData(ISkin skin) {
            this.skin = skin;
            String skinCover = "skin_cover";
            if (skin instanceof ZipSkin) {
                Drawable drawable = skin.getDrawable(skinCover);
                if (drawable == null) {
                    ((ZipSkin) skin).asyncGetDrawable(skinCover, new ZipSkin.AsyncGetResAdapter() {
                        @Override
                        public void getDrawable(Drawable drawable) {
                            super.getDrawable(drawable);
                            Glide.with(itemView.getContext()).load(drawable).centerCrop().into(imageView);
                        }
                    });
                }
            } else {
                Glide.with(itemView.getContext()).load(skin.getDrawable(skinCover)).centerCrop().into(imageView);
            }
        }
    }
}
