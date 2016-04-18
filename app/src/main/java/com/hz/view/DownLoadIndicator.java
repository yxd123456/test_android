package com.hz.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.hz.R;
import com.hz.util.DensityUtil;

/**
 * 地图下载时的标志
 */
public class DownLoadIndicator extends View {

    public static final String TAG = DownLoadIndicator.class.getSimpleName();
    private int default_size;
    private int circleSize = 0;
    private Paint circlePaint;
    private int circleInitColor;
    private int circleDownloadColor;
    private int circleOkColor;
    private RectF rectf;
    private Path clipPath;
    private PaintFlagsDrawFilter paintFlagsDrawFilter;

    private int downLoadStatus = DOWNLOAD_STATUS_INIT;
    public static final int DOWNLOAD_STATUS_INIT = 1001;//初始状态 灰色背景 对号
    public static final int DOWNLOAD_STATUS_DOWNLOADING = 1002;//蓝色背景 白色箭头上下运动
    public static final int DOWNLOAD_STATUS_PUSH = 1003;//蓝色背景 白色箭头不动
    public static final int DOWNLOAD_STATUS_OK = 1004;//下载完成 绿色背景 对号

    public static final int OK_IMAGE_RES = R.drawable.ic_check_circle_black_18dp;
    public static final int ARROR_IMAGE_RES = R.drawable.ic_keyboard_backspace_white_18dp;
    public static final int ARROR_PUSH_IMAGE_RES = R.drawable.ic_keyboard_backspace_black_18dp;

    private BitmapDrawable bitmapDrawableArror;
    private BitmapDrawable bitmapDrawableOk;
    private BitmapDrawable bitmapDrawableArrorPush;

    private PorterDuffColorFilter filterNormal;
    private PorterDuffColorFilter filterDownload;
    private PorterDuffColorFilter filterPush;
    private PorterDuffColorFilter filterComplete;

    private float translate_progress = 0;


    public DownLoadIndicator(Context context) {
        this(context, null);
    }

    public DownLoadIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DownLoadIndicator(Context context, AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);
        Log.d("Test",getClass().getSimpleName()+"被调用了");
        initComponents(context, attrs, defStyleAttr);
    }

    private void initComponents(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DownLoadIndicator);
        circleInitColor = typedArray.getColor(R.styleable.DownLoadIndicator_circleInitColor, getResources().getColor(R.color.darkgray));
        circleDownloadColor = typedArray.getColor(R.styleable.DownLoadIndicator_circleDownloadColor, getResources().getColor(R.color.seablue));
        circleOkColor = typedArray.getColor(R.styleable.DownLoadIndicator_circleOkColor, getResources().getColor(R.color.green));
        typedArray.recycle();

        default_size = DensityUtil.dip2px(this.getContext(), 12);

        rectf = new RectF();
        clipPath = new Path();
        paintFlagsDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

        bitmapDrawableArror = (BitmapDrawable) getResources().getDrawable(ARROR_IMAGE_RES);
        bitmapDrawableOk = (BitmapDrawable) getResources().getDrawable(OK_IMAGE_RES);
        bitmapDrawableArrorPush = (BitmapDrawable) getResources().getDrawable(ARROR_PUSH_IMAGE_RES);

        filterNormal = new PorterDuffColorFilter(circleInitColor, PorterDuff.Mode.SRC_IN);
        filterDownload = new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        filterPush = new PorterDuffColorFilter(circleDownloadColor, PorterDuff.Mode.SRC_IN);
        filterComplete = new PorterDuffColorFilter(circleOkColor, PorterDuff.Mode.SRC_IN);


    }

    /**
     * 设置下载状态
     **/
    public void setDownLoadStatus(int downLoadStatus) {
        this.downLoadStatus = downLoadStatus;
        this.invalidate();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        setMeasuredDimension(widthMode == MeasureSpec.EXACTLY ? widthSize : default_size, heightMode == MeasureSpec.EXACTLY ? heightSize : default_size);

        int width = this.getMeasuredWidth();
        int height = this.getMeasuredHeight();

        circleSize = Math.min(width, height);

        rectf.set(0, 0, circleSize, circleSize);

        clipPath.reset();
        clipPath.addCircle(circleSize / 2, circleSize / 2, circleSize / 2, Path.Direction.CCW);
        translate_progress = circleSize / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.clipPath(clipPath, Region.Op.REPLACE);
        canvas.setDrawFilter(paintFlagsDrawFilter);

        switch (downLoadStatus) {
            case DOWNLOAD_STATUS_INIT://初始状态 灰色背景 对号
                bitmapDrawableOk.getPaint().setColorFilter(filterNormal);
                canvas.drawBitmap(bitmapDrawableOk.getBitmap(), null, rectf, bitmapDrawableOk.getPaint());
                translate_progress = 0;
                break;
            case DOWNLOAD_STATUS_DOWNLOADING://蓝色背景 白色箭头上下运动
                canvas.drawColor(circleDownloadColor);
                canvas.rotate(-90, circleSize / 2, circleSize / 2);
                canvas.translate(translate_progress, 0);
                translate_progress = translate_progress - 1;

                if (translate_progress > circleSize / 2) {
                    translate_progress = -circleSize / 2;
                } else if (translate_progress < -circleSize / 2) {
                    translate_progress = circleSize / 2;
                }
                invalidate();
                bitmapDrawableArror.getPaint().setColorFilter(filterDownload);
                canvas.drawBitmap(bitmapDrawableArror.getBitmap(), null, rectf, bitmapDrawableArror.getPaint());
                break;
            case DOWNLOAD_STATUS_PUSH://蓝色背景 白色箭头不动
                canvas.rotate(-90, circleSize / 2, circleSize / 2);
                bitmapDrawableArrorPush.getPaint().setColorFilter(filterPush);
                canvas.drawBitmap(bitmapDrawableArrorPush.getBitmap(), null, rectf, bitmapDrawableArrorPush.getPaint());
                translate_progress = 0;
                break;
            case DOWNLOAD_STATUS_OK://下载完成 绿色背景 对号
                bitmapDrawableOk.getPaint().setColorFilter(filterComplete);
                canvas.drawBitmap(bitmapDrawableOk.getBitmap(), null, rectf, bitmapDrawableOk.getPaint());
                translate_progress = 0;
                break;
        }

        canvas.restore();
    }
}

