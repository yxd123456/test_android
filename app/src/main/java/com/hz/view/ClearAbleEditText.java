package com.hz.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.hz.R;

import org.w3c.dom.Text;

/**
 * 可以清除的输入框
 */
public class ClearAbleEditText extends AppCompatEditText implements View.OnFocusChangeListener {

    public static final String TAG = ClearAbleEditText.class.getSimpleName();
    private Drawable rightDrawable = null;
    private  OnTextChangeListener onTextChangeListener;

    public ClearAbleEditText(Context context) {
        super(context);
        initComponents();
    }

    public ClearAbleEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initComponents();
    }

    public ClearAbleEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initComponents();
    }

    /**
     * 初始化系统组件
     * *
     */
    private void initComponents() {
        rightDrawable = getResources().getDrawable(R.drawable.cleartext_black);
        this.setSingleLine(true);
        this.setOnFocusChangeListener(this);
    }


    // focus 改变回调
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            showClearDrawable(!TextUtils.isEmpty(this.getText().toString()));
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (getCompoundDrawables()[2] != null) {
                boolean clear = event.getX() > (getWidth() - getTotalPaddingRight()) && event.getX() < (getWidth() - getPaddingRight());
                if (clear) {
                    this.setText("");
                }
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        showClearDrawable(!TextUtils.isEmpty(text));
        if(onTextChangeListener != null){
            onTextChangeListener.onTextChange(text.toString());
        }
    }

    private void showClearDrawable(boolean show) {
        if (rightDrawable != null) {
            int height = (int) (this.getMeasuredHeight() * 0.6);
            rightDrawable.setBounds(0, 0, height, height);
        }

        this.setCompoundDrawables(null, null, show ? rightDrawable : null, null);
    }

    public void setOnTextChangeListener(OnTextChangeListener onTextChangeListener) {
        this.onTextChangeListener = onTextChangeListener;
    }

    public interface OnTextChangeListener{
        void onTextChange(String newText);
    }
}
