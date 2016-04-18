package com.hz.drawable;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.text.TextPaint;
import android.util.Log;

import java.io.Serializable;

/**
 * 自定义用户名称drawable
 */
public class UserNameDrawable extends Drawable {

    private String userName;
    private int width;
    private int height;
    private TextPaint textPaint = new TextPaint();


    public UserNameDrawable(String userName, int width, int height, float textSize) {
        Log.d("Test",getClass().getSimpleName()+"被调用了");

        this.userName = userName;
        this.width = width;
        this.height = height;

        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(textSize);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAlpha(120);
    }

    @Override

    public void draw(Canvas canvas) {
        canvas.drawColor(Color.GRAY);
        canvas.drawText(userName, width / 2, height / 2, textPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        textPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        textPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getIntrinsicHeight() {
        return height;
    }

    @Override
    public int getIntrinsicWidth() {
        return width;
    }

    @Override
    public int getOpacity() {
        return 0;
    }
}
