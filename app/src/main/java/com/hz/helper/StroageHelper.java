package com.hz.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.hz.common.Constans;
import com.hz.util.ArrayUtil;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

/**
 * 文件操作工具
 */
public class StroageHelper {

    public StroageHelper() {
        Log.d("Test",getClass().getSimpleName()+"被调用了");
    }

    public static final String TAG = "StroageHelper";

    /**
     * @param context android上下文
     */
    public static File getEdapDirectory(Context context) {
        return StorageUtils.getOwnCacheDirectory(context, Constans.ROOT_DICTORY);
    }

    /**
     * @param context    android上下文
     * @param directorys 目录列表  new String[]{"1","2","3","4"} ==>  edap\1\2\3\4
     */
    public static File getOwnCacheDirectory(Context context, String... directorys) {
        return StorageUtils.getOwnCacheDirectory(context, Constans.ROOT_DICTORY + File.separator + ArrayUtil.join(directorys, File.separator));
    }

    /**
     * 获取项目图片存放地址
     *
     * @param context android上下文
     */
    public static File getProjectImagesCacheRootDir(Context context) {
        return getOwnCacheDirectory(context, Constans.PROJECT_IMAGE_DIR);
    }


    /**
     * 根据时间创建项目图片路径
     *
     * @param context android上下文
     */
    public static File getProjectImageFileByDate(Context context, String imageFolder, String imageName) {
        File folderPath = getOwnCacheDirectory(context, ArrayUtil.contact(Constans.PROJECT_IMAGE_DIR, imageFolder));
        return new File(folderPath, imageName);
    }

    /**
     * 获取项目okhttpclient缓存存放地址
     *
     * @param context android上下文
     */
    public static File getProjectOkHttpClientCacheDir(Context context) {
        return getOwnCacheDirectory(context, Constans.PROJECT_OKHTTPCLIENT_DIR);
    }

    /**
     * 获取项目图片加载缓存存放地址
     *
     * @param context android上下文
     */
    public static File getProjectImageLoaderCacheDir(Context context) {
        return getOwnCacheDirectory(context, Constans.PROJECT_IMAGELOADER_DIR);
    }

    /**
     * 获取项目错误日志存放地址
     *
     * @param context android上下文
     */
    public static File getProjectCrashDir(Context context) {
        return getOwnCacheDirectory(context, Constans.PROJECT_CRASH_DIR);
    }

    /**
     * 保存bitmap到硬盘
     *
     * @param bitmap 图片资源
     * @param path   图片保存路径位置
     */
    public static void saveBitmapToPath(Bitmap bitmap, String path) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path, false));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

}
