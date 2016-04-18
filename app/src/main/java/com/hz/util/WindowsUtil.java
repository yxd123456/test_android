package com.hz.util;


import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.view.WindowManager;

import com.hz.R;

public class WindowsUtil {

    /**
     * 设置透明状态栏和透明导航栏
     * *
     */
    public static void setTranslucentStatusAndNavigation(Activity activity) {
        //设置version >= 4.4 支持沉浸式 透明状态栏，透明导航栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

}
