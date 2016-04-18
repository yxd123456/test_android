package com.hz.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hz.R;

/**
 * 自定义居中或者据底显示Toast
 */
public class PopupToast {

    //pop 提示信息类型
    public static final int OK = 1;
    public static final int ERROR = 2;
    public static final int CUSTOME = 3;

    public static final int WHAT_CLOSE = 1;
    public static final int SHOW_DURING = 700;//toast显示多少时间
    private static Toast toast;
    private static Handler mHandler = new MyHandler(Looper.getMainLooper());

    static class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            if (toast != null) {
                toast.cancel();
            }
        }
    }

    /**
     * 显示错误信息
     * *
     */
    public static void showOk(Context context, String title) {
        PopupToast.show(context, Gravity.CENTER, title, OK);
    }

    /**
     * 显示成功信息
     * *
     */
    public static void showError(Context context, String title) {
        PopupToast.show(context, Gravity.CENTER, title, ERROR);
    }

    public static void show(Context context, int gravity, String title, int type) {
        context = context.getApplicationContext();
        if (toast != null) {
            toast.cancel();
        }

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.pop_toast, null);
        TextView textView = (TextView) view.findViewById(R.id.message);
        ImageView imageView = (ImageView) view.findViewById(R.id.spinnerImageView);
        textView.setText(title);


        int imageResourceId = R.drawable.ok;
        switch (type) {
            case OK:
                imageResourceId = R.drawable.ok;
                break;
            case ERROR:
                imageResourceId = R.drawable.x;
                break;
            case CUSTOME:
                imageView.setVisibility(View.GONE);
                break;
        }
        imageView.setImageResource(imageResourceId);

        toast = Toast.makeText(context, title, Toast.LENGTH_SHORT);
        toast.setGravity(gravity, 0, 0);
        toast.setView(view);
        toast.show();

        mHandler.sendEmptyMessageDelayed(WHAT_CLOSE, SHOW_DURING);
    }
}
