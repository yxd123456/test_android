package com.hz.upload;

import android.util.Log;

import com.hz.BuildConfig;
import com.hz.common.Constans;
import com.hz.entity.ResponseStateEntity;
import com.hz.util.EncryptUtil;
import com.hz.util.HttpManager;
import com.hz.util.JsonUtil;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;

import java.io.File;
import java.io.FilenameFilter;

/**
 * 将最新版本的apk上传到服务器
 */
public class ApkUploader {
    private static final String APK_DIR = System.getProperty("user.dir") + "\\app\\build\\outputs\\apk";
    private static final String APK_PREFIX = "edap-release";
    private static final Boolean isPublic = true;
    private static final String SERVER_URL = Constans.SERVER_ADDRESS + "/file/uploadMobileAppFile.html";
    public static final String TAG = "ApkUploader";
    private static String userName = "taol";
    private static String passwd = EncryptUtil.getHmacMD5String("123456", EncryptUtil.EDAP_SALT);//加密密码;
    private static String loginUrl = Constans.LOGIN_URL_LOGIN;

    public static void main(String[] args) {
        uploadNewVewsionApk();
    }

    /**
     * 上传最新版本的apk到服务器
     **/
    public static void uploadNewVewsionApk() {
        Log.d("Test","upload被调用了");
        File apkFile = getApkFile();
        if (apkFile == null) {
            System.err.println("apk File is null");
            return;
        }
        String updateDesc = getApkUpdateDesc();
        String versionName = getApkVersionName();

        System.out.println("******************************上传apk描述信息******************************");
        System.out.println("SERVER_URL : " + SERVER_URL);
        System.out.println("apkFile : " + apkFile.getAbsolutePath());
        System.out.println("updateDesc : " + updateDesc);
        System.out.println("versionName : " + versionName);
        System.out.println("isPublic : " + isPublic);

        if (checkLogin()) {
            uploadApk(apkFile, updateDesc, versionName, isPublic);
        } else {
            System.out.println("登陆失败");
        }

    }

    /**
     * 上传apk文件到服务器
     **/
    public static void uploadApk(File apkFile, String updateDesc, String versionName, Boolean ispublic) {
        MultipartBuilder multipartBuilder = new MultipartBuilder().type(MultipartBuilder.FORM);
        RequestBody fileRequestBody = RequestBody.create(MediaType.parse("application/vnd.android.package-archive"), apkFile);
        multipartBuilder.addFormDataPart("file", apkFile.getName(), fileRequestBody);//apk信息
        multipartBuilder.addFormDataPart("description", updateDesc);//apk更新描述
        multipartBuilder.addFormDataPart("isPublicAccess", ispublic.toString());//apk是否公开
        RequestBody requestBody = multipartBuilder.build();

        Request request = new Request.Builder().post(requestBody).url(SERVER_URL).build();
        HttpManager.getInstance().addAsyncHttpTask(request, new HttpManager.HttpTaskCallback() {

            @Override
            public void onFailure(Exception e) {
                System.err.println("上传apk失败 错误信息：" + e.toString());
            }

            @Override
            public void onSuccess(String responStr) {
                ResponseStateEntity responseStateEntity = JsonUtil.convertJsonToObj(responStr, ResponseStateEntity.class);
                if (responseStateEntity != null && responseStateEntity.isSuccess()) {
                    System.out.println("上传apk成功");
                } else {
                    System.err.println("上传apk失败");
                }
            }
        });
    }

    private static boolean checkLogin() {
        //1.登陆系统保存cookie
        RequestBody loginRequestBody = new FormEncodingBuilder()
                .add("username", userName)
                .add("password", passwd)
                .add("remember-me", "true")
                .build();

        Request loginRequest = new Request.Builder()
                .url(loginUrl)
                .addHeader("X-Requested-With", "XMLHttpRequest")
                .post(loginRequestBody)
                .build();

        String loginResult = HttpManager.getInstance().addSyncHttpTask(loginRequest);
        if (loginResult != null) {
            System.out.print(TAG + "run: " + loginResult);
            ResponseStateEntity loginRespState = JsonUtil.convertJsonToObj(loginResult, ResponseStateEntity.class);
            if (loginRespState != null && loginRespState.isSuccess()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取上级描述
     **/
    private static String getApkUpdateDesc() {
        StringBuffer desc = new StringBuffer();
        desc.append("1.退出登录功能移动至左侧菜单栏 \n");
        desc.append("2.移除左侧菜单栏测试页面 \n");
        desc.append("3.地图点位通过拖动修改经纬度");
        desc.append("4.杆编号类型验证");
        desc.append("5.地图点位选择时点位跳动");
        desc.append("6.移除弹出内存泄露检测提示");

        return desc.toString();
    }


    /**
     * 获取apk版本号
     **/
    private static String getApkVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    /**
     * 获取构建以后的apk文件路径
     **/
    private static File getApkFile() {
        File apkDir = new File(APK_DIR);
        if (apkDir.isDirectory()) {
            File[] apks = apkDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return filename.startsWith(APK_PREFIX);
                }
            });

            if (apks != null && apks.length > 0) {
                if (apks.length == 1) {
                    return apks[0];
                } else {
                    throw new RuntimeException("有多个apk,build之前 请先clear");
                }
            }
        }
        return null;
    }
}
