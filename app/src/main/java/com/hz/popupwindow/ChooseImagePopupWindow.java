package com.hz.popupwindow;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;

import com.hz.R;

/**
 * 准备选择图片
 */
public class ChooseImagePopupWindow extends PopupWindow implements View.OnTouchListener, View.OnClickListener {



    public static final String TAG = ChooseImagePopupWindow.class.getSimpleName();
    private View rootView;
    public static final int VIEW_TYPE_IMAGES = 1001;
    public static final int VIEW_TYPE_CAMERAS = 1002;
    public static final int VIEW_TYPE_CANCEL = 1003;
    private onButtonClickListener onButtonClickListener;

    public ChooseImagePopupWindow(Context context) {
        super(context);
        Log.d("Test",getClass().getSimpleName()+"被调用了");
        LayoutInflater inflater = LayoutInflater.from(context);
        rootView = inflater.inflate(R.layout.popupwindow_preparetochooseimage, null);
        rootView.setOnTouchListener(this);
        setContentView(rootView);
        getCameraButton().setOnClickListener(this);
        getImagesButton().setOnClickListener(this);
        getCancelButton().setOnClickListener(this);

        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setFocusable(true);
        this.setAnimationStyle(R.style.popupwindow_animation);
        this.setBackgroundDrawable(new ColorDrawable(0xb0000000)); //设置背景为透明，如果不设置的话默认值有边框
    }

    private Button getCameraButton() {
        return (Button) rootView.findViewById(R.id.id_popupwindow_tocamera);
    }

    private Button getImagesButton() {
        return (Button) rootView.findViewById(R.id.id_popupwindow_images);
    }

    private Button getCancelButton() {
        return (Button) rootView.findViewById(R.id.id_popupwindow_cancal);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        View containerView = rootView.findViewById(R.id.id_popupwindow_container);
        int height = containerView.getTop();
        int y = (int) event.getY();
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (y < height) {
                dismiss();
            }
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        if (onButtonClickListener == null) {
            return;
        }

        switch (v.getId()) {
            case R.id.id_popupwindow_cancal:
                onButtonClickListener.onButtonClick(v, VIEW_TYPE_CANCEL);
                break;
            case R.id.id_popupwindow_tocamera:
                onButtonClickListener.onButtonClick(v, VIEW_TYPE_CAMERAS);
                break;
            case R.id.id_popupwindow_images:
                onButtonClickListener.onButtonClick(v, VIEW_TYPE_IMAGES);
                break;
        }
    }

    public interface onButtonClickListener {
        void onButtonClick(View view, int viewType);
    }

    public void setOnButtonClickListener(ChooseImagePopupWindow.onButtonClickListener onButtonClickListener) {
        this.onButtonClickListener = onButtonClickListener;
    }
}
