package com.hz.helper;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.hz.activity.LoginActivity;

/**
 * 公用的帮助类
 */
public class CommonHelper {

    public CommonHelper() {
        Log.d("Test",getClass().getSimpleName()+"被调用了");

    }

    /**
     * 跳转到登陆页面
     **/
    public static void toLoginActivity(Context context) {
        Intent loginIntent = new Intent(context, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(loginIntent);
    }
}
