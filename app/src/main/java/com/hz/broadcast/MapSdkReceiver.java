package com.hz.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;

/**
 * 百度地图broadreceiver
 */
public class MapSdkReceiver extends BroadcastReceiver {
    public static final String TAG = MapSdkReceiver.class.getSimpleName();
    private Handler handler = new Handler();

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d("Test",getClass().getSimpleName()+"被调用了");

        if (intent.getAction().equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
            final String errorMsg = "key 百度地图key验证失败," + intent.getExtras().toString();
            Log.e(TAG, errorMsg);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * 注册百度地图receiver
     **/
    public void registerMapSdkReceiver(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        intentFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
        context.registerReceiver(this, intentFilter);
    }


    public void unregisterReceiver(Context context){
        handler.removeCallbacksAndMessages(null);
        context.unregisterReceiver(this);
    }



}
