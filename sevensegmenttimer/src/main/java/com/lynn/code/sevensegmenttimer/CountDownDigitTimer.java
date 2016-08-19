package com.lynn.code.sevensegmenttimer;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.LinearLayout;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 七段数码管 样式的 倒计时view
 * Created by Lynn on 8/11/16.
 */

public class CountDownDigitTimer extends LinearLayout {
    private ScheduledExecutorService mTimer;

    private int mHour;
    private int mMinute;
    private int mSecond;

    private int mCurrentHour;
    private int mCurrentMinute;
    private int mCurrentSecond;

    //only two tag visible if hour is zero
    private boolean mIsPartialVisibleIfLessThanAnHour;
    //only one tag visible if minute is zero
    private boolean mIsPartialVisibleIfLessThanAMinute;

    private TimeSeparatorView mLeftSeparator;
    private TimeSeparatorView mRightSeparator;

    private SevenSegmentDigitView mHourView;
    private SevenSegmentDigitView mMinuteView;
    private SevenSegmentDigitView mSecondView;

    private Runnable mTask;

    private OnCoundDownCallback mCallback;

    public CountDownDigitTimer(Context context) {
        this(context, null);
    }

    public CountDownDigitTimer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CountDownDigitTimer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        //default background
        setOrientation(LinearLayout.HORIZONTAL);

        mIsPartialVisibleIfLessThanAnHour = false;
        mIsPartialVisibleIfLessThanAMinute = false;

        mHour = 0;
        mMinute = 0;
        mSecond = 0;

        mLeftSeparator = new TimeSeparatorView(getContext());
        mRightSeparator = new TimeSeparatorView(getContext());
        mLeftSeparator.setStyle(TimeSeparatorView.SQUARE);
        mRightSeparator.setStyle(TimeSeparatorView.SQUARE);

        mLeftSeparator.setBackgroundColor(Color.WHITE);
        mRightSeparator.setBackgroundColor(Color.WHITE);

        mHourView = new SevenSegmentDigitView(getContext());
        mMinuteView = new SevenSegmentDigitView(getContext());
        mSecondView = new SevenSegmentDigitView(getContext());

        mHourView.setBackgroundColor(Color.BLACK);
        mMinuteView.setBackgroundColor(Color.BLACK);
        mSecondView.setBackgroundColor(Color.BLACK);

        addView(mHourView);
        addView(mLeftSeparator);
        addView(mMinuteView);
        addView(mRightSeparator);
        addView(mSecondView);

        setWeight(mHourView, 3);
        setWeight(mLeftSeparator, 1);
        setWeight(mMinuteView, 3);
        setWeight(mRightSeparator, 1);
        setWeight(mSecondView, 3);

        final int padding = dpToPx(8);
        mHourView.setPadding(padding, padding, padding, padding);
        mMinuteView.setPadding(padding, padding, padding, padding);
        mSecondView.setPadding(padding, padding, padding, padding);

        initTask();
    }

    private void setWeight(View v, int weight) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT);
        lp.weight = weight;
        v.setLayoutParams(lp);
    }

    /**
     * 根据传入的时间millis, 分解成时分秒
     */
    private void divideTime(int timestamp) {
        int second = timestamp / 1000;
        int minute = 0;
        int hour = 0;

        if (second > 60) {
            minute = second / 60;
            //更新剩下的秒
            second %= 60;
        }

        if (minute > 60) {
            hour = minute / 60;
            //更新分钟
            minute %= 60;
        }

        if (hour > 99) {
            //最多计算 100小时 99:60:59
            hour = 99;
            //TODO 可以提醒输入超过99会被舍去
        }

        mHour = hour;
        mMinute = minute;
        mSecond = second;
    }

    public void setCountDownTimeStamp(int millis) {
        divideTime(millis);
        startTask();
    }

    /**
     * 直接设置 时间
     */
    public void setCountDownTime(int second) {
        setCountDownTime(0, 0, second);
    }

    public void setCountDownTime(int minute, int second) {
        setCountDownTime(0, minute, second);
    }

    public void setCountDownTime(int hour, int minute, int second) {
        if (hour > 99) {
            //最多计算 100小时 99:60:59
            hour = 99;
            //TODO 可以提醒输入超过99会被舍去
        }
        this.mHour = hour;
        this.mMinute = minute;
        this.mSecond = second;

        startTask();
    }

    public void setIsPartialVisibleIfLessThanAnHour(boolean isPartialVisibleIfLessThanAnHour) {
        this.mIsPartialVisibleIfLessThanAnHour = isPartialVisibleIfLessThanAnHour;
    }

    public void setIsPartialVisibleIfLessThanAMinute(boolean isPartialVisibleIfLessThanAMinute) {
        this.mIsPartialVisibleIfLessThanAMinute = isPartialVisibleIfLessThanAMinute;
    }

    private void initTask() {
        mTask = new Runnable() {
            @Override
            public void run() {
                if (mIsPartialVisibleIfLessThanAMinute) {
                    mHourView.setVisibility(View.GONE);
                    mLeftSeparator.setVisibility(View.GONE);
                    mMinuteView.setVisibility(View.GONE);
                    mRightSeparator.setVisibility(View.GONE);
                } else if (mIsPartialVisibleIfLessThanAnHour) {
                    mHourView.setVisibility(View.GONE);
                    mLeftSeparator.setVisibility(View.GONE);
                } else {
                    mHourView.setVisibility(View.VISIBLE);
                    mLeftSeparator.setVisibility(View.VISIBLE);
                    mMinuteView.setVisibility(View.VISIBLE);
                    mRightSeparator.setVisibility(View.VISIBLE);
                }

                if (mCurrentSecond > 0) {
                    mCurrentSecond -= 1;
                } else if (mCurrentMinute > 0) {
                    mCurrentMinute -= 1;
                    mCurrentSecond = 59;
                } else if (mCurrentHour > 0) {
                    mCurrentHour -= 1;
                    mCurrentMinute = 59;
                    mCurrentSecond = 59;
                }

                post(new Runnable() {
                    @Override
                    public void run() {
//                        Log.d("update time", mCurrentHour + " " + mCurrentMinute + " " + mCurrentSecond + " ");
                        //更新ui
                        mHourView.setDigit(mCurrentHour);
                        mMinuteView.setDigit(mCurrentMinute);
                        mSecondView.setDigit(mCurrentSecond);
                    }
                });

                //判断计时是否结束
                if (mCurrentSecond == 0 &&
                        (mIsPartialVisibleIfLessThanAMinute || mCurrentMinute == 0) &&
                        (mIsPartialVisibleIfLessThanAnHour || mCurrentHour == 0)) {
                    if (mCallback != null) {
                        mCallback.onFinish(CountDownDigitTimer.this);
                    }
                }
            }
        };
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startTask();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopTimer();
    }

    private void startTask() {
        mCurrentHour = mHour;
        mCurrentMinute = mMinute;
        mCurrentSecond = mSecond;

        //确保为空
        stopTimer();

        //初始化定时任务
        mTimer = Executors.newSingleThreadScheduledExecutor();
        //启动定时任务 1秒一次回调
        mTimer.scheduleAtFixedRate(mTask, 0, 1000, TimeUnit.MILLISECONDS);
    }

    private void stopTimer() {
        if (mTimer != null && !mTimer.isShutdown()) {
            mTimer.shutdown();
        }
        //置空
        mTimer = null;
    }

    private int dpToPx(int dp) {
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        return dp * (dm.densityDpi / 160);
    }

    public void setCountDownCallback(OnCoundDownCallback callback) {
        this.mCallback = callback;
    }

    public interface OnCoundDownCallback {
        void onFinish(View v);
    }
}

