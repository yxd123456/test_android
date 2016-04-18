package com.hz.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.FloatRange;
import android.util.AttributeSet;
import android.view.View;

import com.hz.R;
import com.hz.util.DensityUtil;

/**
 * 项目状态视图
 */
public class ProjectStatusView extends View {
    public static final String TAG = ProjectStatusView.class.getSimpleName();
    private int default_width;
    private int default_height;
    private int background_color;
    private int border_color;
    private int fill_color;
    private static final int borderWidth = 4;
    private RectF borderRectf;
    private Paint borderPaint;
    private Paint fillPaint;
    private RectF fillRectf;
    private float fillPrecent = 0;

    public ProjectStatusView(Context context) {
        super(context);
        initComponents();
    }

    public ProjectStatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initComponents();
    }

    public ProjectStatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initComponents();
    }

    /**
     * 初始化系统组件
     **/
    private void initComponents() {
        Context context = this.getContext();
        default_width = DensityUtil.dip2px(context, 30);
        default_height = DensityUtil.dip2px(context, 8);
        background_color = getColor(R.color.white);

        //边框
        border_color = getColor(R.color.red);
        borderRectf = new RectF(0, 0, default_width, default_height);
        borderPaint = new Paint();
        borderPaint.setStrokeWidth(borderWidth);
        borderPaint.setColor(border_color);
        borderPaint.setStyle(Paint.Style.STROKE);

        //填充
        fill_color = getColor(R.color.green);
        fillPaint = new Paint();
        fillPaint.setColor(fill_color);
        fillPaint.setStyle(Paint.Style.FILL);
        fillRectf = new RectF();
        fillRectf.set(0, 0, 1, default_height);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(widthMode == MeasureSpec.EXACTLY ? widthSize : default_width, heightMode == MeasureSpec.EXACTLY ? heightSize : default_height);

        borderRectf.set(0, 0, this.getMeasuredWidth(), this.getMeasuredHeight());
    }

    //设置背景颜色
    public void setBackGroundColor(@ColorRes int backGroundColor) {
        this.background_color = getColor(backGroundColor);
        this.invalidate();
    }

    //设置边框颜色
    public void setBorderColor(@ColorRes int borderColor) {
        this.border_color = getColor(borderColor);
        this.invalidate();
    }

    //设置填充颜色
    public void setFillColor(@ColorRes int fillColor) {
        this.fill_color = getColor(fillColor);
        this.invalidate();
    }

    private int getColor(@ColorRes int colorRes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getResources().getColor(colorRes, null);
        } else {
            return getResources().getColor(colorRes);
        }
    }

    //设置填充颜色百分比
    public void setFillPercent(@FloatRange(from = 0.0, to = 1.0) float fillPrecent) {
        this.fillPrecent = fillPrecent;
        this.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //背景颜色
        canvas.drawColor(background_color);

        //填充
        fillRectf.set(0, 0, this.getMeasuredWidth() * fillPrecent, this.getMeasuredHeight());
        fillPaint.setColor(fill_color);
        canvas.drawRect(fillRectf, fillPaint);

        //边框
        borderPaint.setColor(border_color);
        canvas.drawRect(borderRectf, borderPaint);
    }
}
