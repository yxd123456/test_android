package com.hz.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.FloatRange;
import android.util.AttributeSet;
import android.view.View;

import com.hz.R;
import com.hz.util.DensityUtil;

/**
 * 项目图片上传状态imageview
 *
 * @author long
 */
public class UploadStatusView extends View {
    private static final String TAG = UploadStatusView.class.getSimpleName();
    private static final int BACKGROUND_DRAWABLE_RESID = R.drawable.cloud_ok_64;
    private static final int MAX_PROGRESS = 100;
    private static final int MIN_PROGRESS = 0;
    private int progress = 40;
    private BitmapDrawable mBackBitmapDrawable;
    private int default_size;
    private RectF borderRectf;
    private Paint borderPaint;
    private Paint backGroundPaint;
    private Paint arcPaint;
    public static final int ARC_BORDER_WIDTH = 5;
    public int ARC_COLOR_GOING;
    public int ARC_COLOR_BACKGROUND;
    private boolean useDebug = false;
    private RectF smallImageRectf;
    private PorterDuffColorFilter filterNormal;
    private PorterDuffColorFilter filterProgress;
    private PorterDuffColorFilter filterComplete;

    public UploadStatusView(Context context) {
        this(context, null);
    }

    public UploadStatusView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UploadStatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initComponent(context, attrs, defStyleAttr);
    }

    private void initComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        mBackBitmapDrawable = (BitmapDrawable) getResources().getDrawable(BACKGROUND_DRAWABLE_RESID);
        default_size = DensityUtil.dip2px(this.getContext(), 20);

        borderRectf = new RectF();
        borderPaint = new Paint();
        borderPaint.setColor(getResources().getColor(R.color.red));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setAntiAlias(true);
        borderPaint.setStrokeWidth(2);

        backGroundPaint = mBackBitmapDrawable.getPaint();


        ARC_COLOR_GOING = getResources().getColor(R.color.green);
        ARC_COLOR_BACKGROUND = getResources().getColor(R.color.whitegray);
        arcPaint = new Paint();
        arcPaint.setAntiAlias(true);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setColor(ARC_COLOR_BACKGROUND);
        arcPaint.setStrokeWidth(ARC_BORDER_WIDTH);

        smallImageRectf = new RectF();

        filterNormal = new PorterDuffColorFilter(getResources().getColor(R.color.darkgray), PorterDuff.Mode.SRC_IN);
        filterProgress = new PorterDuffColorFilter(getResources().getColor(R.color.green), PorterDuff.Mode.SRC_IN);
        filterComplete = new PorterDuffColorFilter(getResources().getColor(R.color.seablue), PorterDuff.Mode.SRC_IN);
    }

    /**
     * 设置上传进度
     **/
    public void setProgress(@FloatRange(from = MIN_PROGRESS, to = MAX_PROGRESS) int progress) {
        this.progress = progress;
        invalidate();
    }

    /**
     * 设置初始值
     **/
    public void setProgressToStart() {
        setProgress(MIN_PROGRESS);
    }


    /**
     * 设置最大值
     **/
    public void setProgressToEnd() {
        setProgress(MAX_PROGRESS);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        setMeasuredDimension(widthMode == MeasureSpec.EXACTLY ? widthSize : default_size, heightMode == MeasureSpec.EXACTLY ? heightSize : default_size);

        borderRectf.set(0, 0, this.getMeasuredWidth(), this.getMeasuredHeight());
        borderRectf.inset(ARC_BORDER_WIDTH / 2, ARC_BORDER_WIDTH / 2);

        smallImageRectf.set(borderRectf);
        smallImageRectf.inset(ARC_BORDER_WIDTH, ARC_BORDER_WIDTH);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (useDebug) {
            canvas.drawRect(borderRectf, borderPaint);
        }

        switch (progress) {
            case 0://默认状态
                mBackBitmapDrawable.setColorFilter(filterNormal);
                canvas.drawBitmap(mBackBitmapDrawable.getBitmap(), null, borderRectf, backGroundPaint);
                break;
            case 100://上传完成
                mBackBitmapDrawable.setColorFilter(filterComplete);
                canvas.drawBitmap(mBackBitmapDrawable.getBitmap(), null, borderRectf, backGroundPaint);
                break;
            default://上传中
                //缩小背景
                mBackBitmapDrawable.setColorFilter(filterProgress);
                canvas.drawBitmap(mBackBitmapDrawable.getBitmap(), null, smallImageRectf, backGroundPaint);

                //进度条背景
                arcPaint.setColor(ARC_COLOR_BACKGROUND);
                canvas.drawArc(borderRectf, 0, 360, false, arcPaint);

                //背景外旋转进度条 进行中进度
                arcPaint.setColor(ARC_COLOR_GOING);
                canvas.drawArc(borderRectf, 0, 360 * progress / MAX_PROGRESS, false, arcPaint);
                break;
        }
    }
}
