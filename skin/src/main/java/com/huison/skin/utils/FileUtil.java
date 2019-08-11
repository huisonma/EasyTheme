package com.huison.skin.utils;

import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Created by huisonma on 2019/5/7.
 */
public class FileUtil {

    public static List<ZipEntry> readZipFile(String zipFilePath) {
        List<ZipEntry> zipEntries = new ArrayList<>();
        ZipFile zipFile = null;
        InputStream in = null;
        ZipInputStream zin = null;
        try {
            zipFile = new ZipFile(zipFilePath);

            in = new BufferedInputStream(new FileInputStream(zipFilePath));
            zin = new ZipInputStream(in);
            ZipEntry zipEntry;
            while ((zipEntry = zin.getNextEntry()) != null) {
                if (!zipEntry.isDirectory() && !zipEntry.getName().startsWith("__MACOSX")) {
                    zipEntries.add(zipEntry);
                }
            }
            zin.closeEntry();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (zin != null) {
                try {
                    zin.closeEntry();
                    zin.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            CloseUtil.close(in);
            CloseUtil.close(zipFile);
        }
        return zipEntries;
    }

    public static String readZipEntryContent(String zipFilePath, String zipEntryName) {
        return readZipEntryContent(zipFilePath, new ZipEntry(zipEntryName));
    }

    public static String readZipEntryContent(String zipFilePath, ZipEntry zipEntry) {
        ZipFile zipFile = null;
        String content = null;
        try {
            zipFile = new ZipFile(zipFilePath);
            content = readZipEntryContent(zipFile, zipEntry);
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            CloseUtil.close(zipFile);
        }
        return content;
    }

    public static String readZipEntryContent(ZipFile zipFile, ZipEntry zipEntry) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(zipEntry)));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    public static void saveInputStream(String path, InputStream is) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        OutputStream os = null;
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(is);
            os = new FileOutputStream(file);
            byte[] buf = new byte[1024 * 16];
            while (bis.read(buf) != -1) {
                os.write(buf);
            }
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CloseUtil.close(bis);
            CloseUtil.close(os);
        }
    }

    public static void unzip(String src, String des) throws IOException {
        if (TextUtils.isEmpty(src) || TextUtils.isEmpty(des)) {
            throw new IOException("src is empty or des is empty !");
        }

        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(src));
        ZipEntry zipEntry;
        String zipEntryName;
        File desFile = new File(des);
        desFile.mkdirs();
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            zipEntryName = zipEntry.getName();
            if (zipEntry.isDirectory()) {
                zipEntryName = zipEntryName.substring(0, zipEntryName.length() - 1);
                File folder = new File(des + File.separator + zipEntryName);
                folder.mkdirs();
            } else {
                File file = new File(des + File.separator + zipEntryName);
                file.createNewFile();
                FileOutputStream out = new FileOutputStream(file);
                int len;
                byte[] buffer = new byte[1024 * 16];
                while ((len = zipInputStream.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                    out.flush();
                }
                out.close();
            }
        }
        zipInputStream.close();
    }

    public static boolean checkFileExists(String path) {
        File file = new File(path);
        return file.exists() && file.isFile();
    }
}
