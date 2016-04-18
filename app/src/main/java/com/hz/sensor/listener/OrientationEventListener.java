package com.hz.sensor.listener;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Gravity;

import com.hz.view.PopupToast;

/**
 * 手机方向改变事件监听
 */
public class OrientationEventListener implements SensorEventListener {

    private Context context;
    private SensorManager sensorManager;
    private float lastX;
    private onOrientationChangeListener onOrientationChangeListener;

    public OrientationEventListener(Context context) {
        this.context = context;
    }

    /**
     * 启动
     **/
    public void start() {
        Log.d("Test",getClass().getSimpleName()+"被调用了");
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            if (sensor != null) {
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
            } else {
                PopupToast.show(context, Gravity.BOTTOM, "不支持方向感应器", PopupToast.CUSTOME);
            }
        }
    }

    //停止
    public void stop() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // 接受方向感应器的类型
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            // 这里我们可以得到数据，然后根据需要来处理
            float x = event.values[SensorManager.DATA_X];
            if (Math.abs(x - lastX) > 1) {
                if (onOrientationChangeListener != null) {
                    onOrientationChangeListener.onOrientationChange(x);
                }
            }
            lastX = x;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    public void setOnOrientationChangeListener(OrientationEventListener.onOrientationChangeListener onOrientationChangeListener) {
        this.onOrientationChangeListener = onOrientationChangeListener;
    }

    public interface onOrientationChangeListener {
        void onOrientationChange(float x);
    }
}
