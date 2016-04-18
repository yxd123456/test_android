package com.hz.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * package工具
 */
public class PackageUtil {

    public static final String TAG = "PackageUtil";

    /***
     * 获取app版本名称
     **/
    public static String getPackageVersionName(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            if (packageInfo != null) {
                return packageInfo.versionName;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, "onCreate: " + e.toString());
        }
        return "";
    }


    /**
     * 判断当前是否启用DEBUG
     **/
    public static boolean isDebugEnable(Context context) {
        try {
            ApplicationInfo info = context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {

        }
        return false;
    }
}
