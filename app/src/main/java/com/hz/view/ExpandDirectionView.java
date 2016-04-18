package com.hz.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

import com.hz.R;

/**
 * EXPAND视图上下视图
 */
public class ExpandDirectionView extends View {

    public static final String TAG = ExpandDirectionView.class.getSimpleName();
    public static final int EXPAND_DIRECTION_DOWN = 1;
    public static final int EXPAND_DIRECTION_UP = 2;
    public BitmapDrawable bitmapDrawable;
    public RectF rectF;
    public int expand_direction = EXPAND_DIRECTION_DOWN;
    public Paint bitmaPaint;

    public ExpandDirectionView(Context context) {
        this(context, null);
    }

    public ExpandDirectionView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExpandDirectionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.ic_expand_less_black_48dp);
        if (bitmapDrawable != null) {
            bitmaPaint = bitmapDrawable.getPaint();
        }
        rectF = new RectF();
    }

    /**
     * 设置方向
     **/
    public ExpandDirectionView setDirection(int expand_direction) {
        this.expand_direction = expand_direction;
        invalidate();
        return this;
    }

    public ExpandDirectionView setViewAlpha(float alpha) {
        bitmaPaint.setAlpha((int) (alpha * 255));
        return this;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        rectF.set(this.getPaddingLeft(), this.getPaddingTop(), this.getMeasuredWidth() - this.getPaddingLeft(), this.getMeasuredHeight() - this.getPaddingTop());
    }

    @Override
    protected void onDraw(Canvas canvas) {

        switch (expand_direction) {
            case EXPAND_DIRECTION_DOWN:
                canvas.rotate(180, rectF.centerX(), rectF.centerY());
                canvas.drawBitmap(bitmapDrawable.getBitmap(), null, rectF, bitmaPaint);
                break;
            case EXPAND_DIRECTION_UP:
                canvas.drawBitmap(bitmapDrawable.getBitmap(), null, rectF, bitmaPaint);
                break;
        }

    }
}
