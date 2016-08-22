package com.lynn.code.sevensegmenttimer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 分割符号 view
 * Created by Lynn on 8/12/16.
 */

public class TimeSeparatorView extends View {
    public static final int CIRCLE = 0;
    public static final int SQUARE = 1;
    private static final int DEFAULT_COLOR = Color.BLACK;

    @SeparatorStyle
    private int mStyle;

    private double mSideFactor;

    private double mSeparatorInsetFactor;

    private Paint mPaint;

    private int mSeparatorColor;

    public TimeSeparatorView(Context context) {
        this(context, null);
    }

    public TimeSeparatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimeSeparatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setBackgroundColor(Color.TRANSPARENT);
        mStyle = CIRCLE;

        mSideFactor = 0.3;

        mSeparatorInsetFactor = 0.15;

        mSeparatorColor = DEFAULT_COLOR;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
//        mPaint.setColor(DEFAULT_COLOR);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setColor(mSeparatorColor);
        //移动到画布中心
        canvas.translate(canvas.getWidth() / 2, canvas.getHeight() / 2);

        final int w = canvas.getWidth() - getPaddingLeft() - getPaddingRight();
        final int h = canvas.getHeight() - getPaddingTop() - getPaddingBottom();

        final float inset = h * (float) mSeparatorInsetFactor;

        if (mStyle == CIRCLE) {
            final float radius = Math.min(w, h) * (float) mSideFactor;
            //上
            drawCircleWithOffset(canvas, -inset, radius);
            //下
            drawCircleWithOffset(canvas, inset, radius);
        } else {
            //mStyle == SQUARE
            //TODO 可以扩展支持矩形
            final float side = w * (float) mSideFactor;
            //上
            drawRectWithOffset(canvas, -inset, side, side);
            //下
            drawRectWithOffset(canvas, inset, side, side);
        }
    }

    private void drawRectWithOffset(Canvas canvas, float offset, float width, float height) {
        canvas.save();
        canvas.translate(0, offset);
        canvas.drawRect(-width / 2, -height / 2, width / 2, height / 2, mPaint);
        canvas.restore();
    }

    private void drawCircleWithOffset(Canvas canvas, float offset, float radius) {
        canvas.save();
        canvas.translate(0, offset);
        canvas.drawCircle(0, 0, radius, mPaint);
        canvas.restore();
    }

    public void setStyle(@SeparatorStyle int style) {
        mStyle = style;
    }

    public void setSeparatorColor(@ColorInt int color) {
        mSeparatorColor = color;
    }

    @IntDef(value = {CIRCLE, SQUARE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SeparatorStyle {
    }
}

