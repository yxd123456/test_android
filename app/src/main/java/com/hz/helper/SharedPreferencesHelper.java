package com.hz.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.hz.activity.LoginActivity;
import com.hz.activity.MaterialHomeActivity;

/**
 * 系统参数操作工具
 */
public class SharedPreferencesHelper {

    public SharedPreferencesHelper() {
        Log.d("Test",getClass().getSimpleName()+"被调用了");
    }

    public static final String STR_DEFAULT = "";
    public static final int INT_DEFAULT = 0;
    public static final String NEED_UPDATE_PROJECT_MATERIAL_DATA = "NEED_UPDATE_PROJECT_MATERIAL_DATA";//标志是否需要更新项目关联材料库数据


    /**
     * 保存登陆信息到系统配置文件
     *
     * @param context  上下文
     * @param userId   用户ID
     * @param realName 用户真实姓名
     * @param userName 用户名
     * @param passWd   密码
     */
    public static void saveLoginInfoToPreferences(Context context, long userId, String realName, String userName, String passWd) {
        SharedPreferences preferences = getShardPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(LoginActivity.KEY_USERID, userId);
        editor.putString(LoginActivity.KEY_REALNAME, realName);
        editor.putString(LoginActivity.KEY_USERNAME, userName);
        editor.putString(LoginActivity.KEY_PASSWD, passWd);
        editor.apply();
    }

    /**
     * 清除用户登录保存的相关信息
     *
     * @param context 上下文
     */
    public static void clearPreferencesLoginInfo(Context context) {
        SharedPreferences preferences = getShardPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(LoginActivity.KEY_USERID);
        editor.remove(LoginActivity.KEY_USERNAME);
        editor.remove(LoginActivity.KEY_PASSWD);
        editor.remove(LoginActivity.KEY_REALNAME);
        editor.apply();
    }

    /**
     * @param context 上下文
     *                **
     */
    public static SharedPreferences getShardPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * 获取系统配置文件中保存的用户ID信息
     * *
     */
    public static long getUserId(Context context) {
        return getShardPreferences(context).getLong(LoginActivity.KEY_USERID, INT_DEFAULT);
    }

    /**
     * 获取系统配置文件中保存的用户名称信息
     * *
     */
    public static String getUserNameFrom(Context context) {
        return getShardPreferences(context).getString(LoginActivity.KEY_USERNAME, STR_DEFAULT);
    }

    /**
     * 获取系统配置文件中保存的用户名称信息
     * *
     */
    public static String getUserRealName(Context context) {
        return getShardPreferences(context).getString(LoginActivity.KEY_REALNAME, STR_DEFAULT);
    }

    /**
     * 获取系统配置文件中保存的用户密码信息
     * *
     */
    public static String getUserPassWd(Context context) {
        return getShardPreferences(context).getString(LoginActivity.KEY_PASSWD, STR_DEFAULT);
    }

    /**
     * 获取系统配置文件中保存的用户jpush注册ID信息
     * *
     */
    public static String getRegistrationId(Context context) {
        return getShardPreferences(context).getString(LoginActivity.KEY_REGISTRATIONID, STR_DEFAULT);
    }

    /**
     * 保存用户是否需要更新材料库数据的标志
     * *
     */
    public static void saveNeedToUpdateMaterialDataIdentifier(Context context, boolean needToUpdateMaterialData) {
        getShardPreferences(context).edit().putBoolean(NEED_UPDATE_PROJECT_MATERIAL_DATA, needToUpdateMaterialData).apply();
    }


    /**
     * 获取用户是否需要更新材料库数据的标志
     * *
     */
    public static boolean getNeedToUpdateMaterialDataIdentifier(Context context) {
        return getShardPreferences(context).getBoolean(NEED_UPDATE_PROJECT_MATERIAL_DATA, false);
    }

    /**
     * 保存用户jpush注册ID信息到系统配置文件
     * *
     */
    public static void saveRegistrationId(Context context, String regId) {
        getShardPreferences(context).edit().putString(LoginActivity.KEY_REGISTRATIONID, regId).apply();
    }


  /*  *//**
     * 获取用户是否已经注册
     * *
     *//*
    public static boolean getUserLoginIdentifier(Context context) {
        return getShardPreferences(context).getBoolean(MaterialHomeActivity.KEY_USER_HASLOGIN, false);
    }

    *//**
     * 保存用户用户是否已经注册到系统配置文件
     * *
     *//*
    public static void saveUserLoginIdentifier(Context context, boolean hasLogin) {
        getShardPreferences(context).edit().putBoolean(MaterialHomeActivity.KEY_USER_HASLOGIN, hasLogin).apply();
    }*/
}
