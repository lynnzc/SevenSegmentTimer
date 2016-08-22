package com.lynn.code.sevensegmenttimer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * 七段数码管 view
 * Created by Lynn on 8/10/16.
 */

public class SevenSegmentDigitView extends View {
    private static final int DEFAULT_ON = Color.WHITE;
    private static final int DEFAULT_OFF = Color.GRAY;

    //显示的值
    private int mDigit;
    private int mRight;
    private int mLeft;

    private int mOn;
    private int mOff;
    //两个数字间的间隔
    private int mInset;

    private List<Segment> mSegments;

    private Matrix mMatrix;
    private RectF mBound;
    private Paint mPaint;
    private Path mCurrentPath;

    public SevenSegmentDigitView(Context context) {
        this(context, null);
    }

    public SevenSegmentDigitView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SevenSegmentDigitView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);

        mMatrix = new Matrix();
        mCurrentPath = new Path();

        mOn = DEFAULT_ON;
        mOff = DEFAULT_OFF;

        //默认全灭
        mDigit = -1;
        //pixel
        mInset = 10;

        mSegments = Segment.segments();
        if (mSegments == null || mSegments.isEmpty()) {
            return;
        }

        //计算 显示一个数字 需要的最小空间
        mBound = new RectF();
        //为了取并集, 先计算其中一个空间
        mSegments.get(0).path.computeBounds(mBound, true);
        for (int i = 1; i < mSegments.size(); i++) {
            //接着逐个计算
            RectF singleBound = new RectF();
            mSegments.get(i).path.computeBounds(singleBound, true);
            //取并集
            mBound.union(singleBound);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measure(widthMeasureSpec, mBound.width() * 2), measure(heightMeasureSpec, mBound.height()));
    }

    private int measure(int spec, float desiredSize) {
        int result;
        int mode = MeasureSpec.getMode(spec);
        int size = MeasureSpec.getSize(spec);

        switch (mode) {
            case MeasureSpec.AT_MOST:
                result = Math.min(size, (int) desiredSize);
                break;
            case MeasureSpec.EXACTLY:
                result = size;
                break;
            case MeasureSpec.UNSPECIFIED:
            default:
                result = (int) desiredSize;
                break;
        }

        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mMatrix.reset();
        // 移动到画布中央
        canvas.translate(canvas.getWidth() / 2, canvas.getHeight() / 2);

        if (mSegments == null || mSegments.isEmpty()) {
            return;
        }

        final int w = canvas.getWidth() - getPaddingLeft() - getPaddingRight();
        final int h = canvas.getHeight() - getPaddingTop() - getPaddingBottom();
        final float widthFactor = Math.min(w / mBound.width() / 2, h / mBound.height());
        //放大到接近宽, 高
        mMatrix.preScale(widthFactor, widthFactor);
        mPaint.setStrokeWidth(24);

        //十位数
        canvas.save();
        canvas.translate(-mBound.width() * widthFactor / 2 - mInset / 2, 0);
        drawSegment(canvas, mLeft);
        canvas.restore();

        //个位数
        canvas.save();
        canvas.translate(mBound.width() * widthFactor / 2 + mInset / 2, 0);
        drawSegment(canvas, mRight);
        canvas.restore();
    }

    /**
     * 绘制单个数字
     * 0 - 6
     * LT = 0
     * LB = 1
     * CT = 2
     * CC = 3
     * CB = 4
     * RT = 5
     * RB = 6
     **/
    private void drawSegment(Canvas canvas, int digit) {
        for (int i = 0; i < mSegments.size(); i++) {
            //变换到当前灯管, 并且按照实际绘制范围放大
            mSegments.get(i).path.transform(mMatrix, mCurrentPath);
            //这是为了保留一点间隔
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(Color.TRANSPARENT);
            canvas.drawPath(mCurrentPath, mPaint);

            mPaint.setStyle(Paint.Style.FILL);
            //TODO 根据对应数字设置灯管是否 发亮, 通过颜色区分
            mPaint.setColor(Digit.toDigit(digit).value(i) ? mOn : mOff);
            //TODO 绘制数字
            canvas.drawPath(mCurrentPath, mPaint);
            //重置
            mCurrentPath.reset();
        }
    }

    /**
     * 刷新数字
     */
    public void setDigit(int digit) {
        if (digit > 99 || digit < 0) {
            throw new IllegalArgumentException("数值不在显示范围");
        }
        this.mDigit = digit;

        //提取位数
        if (mDigit >= 10) {
            mLeft = mDigit / 10;
            mRight = mDigit % 10;
        } else if (mDigit > 0) {
            mLeft = 0;
            mRight = mDigit;
        } else {
            mLeft = 0;
            mRight = 0;
        }
        postInvalidate();
    }

    /**
     * 设置亮的颜色
     */
    public void setOnRes(@ColorInt int on) {
        this.mOn = on;
    }

    /**
     * 设置暗的颜色
     */
    public void setOffRes(@ColorInt int off) {
        this.mOff = off;
    }

    /**
     * 一个数字 一共 由7个灯管组成
     * <p>
     * 规则 on / off
     * 分别是 左上  左下  中上 中中 中下 右上 右下
     * 0 :   on   on   on  off  on  on  on
     * 1 :   off  off  off off  off on  on
     * 2 :   off  on   on  on   on  on  off
     * 3 :   off  off  on  on   on  on  on
     * 4 :   on   off  off on   off on  on
     * 5 :   on   off  on  on   on  off on
     * 6 :   on   on   on  on   on  off on
     * 7 :   off  off  on  off  off on  on
     * 8 :   on   on   on  on   on  on  on
     * 9 :   on   off  on  on   on  on  on
     * 默认状态
     * all:  off  off  off off  off off off
     */
    public static class Digit {
        private static final boolean ON = true;
        private static final boolean OFF = false;

        public static Digit DEFAULT = new Digit();
        /**
         * 表示数字 0 - 9
         */
        public static Digit ZERO = new Digit(ON, ON, ON, OFF, ON, ON, ON);
        public static Digit ONE = new Digit(OFF, OFF, OFF, OFF, OFF, ON, ON);
        public static Digit TWO = new Digit(OFF, ON, ON, ON, ON, ON);
        public static Digit THREE = new Digit(OFF, OFF, ON, ON, ON, ON, ON);
        public static Digit FOUR = new Digit(ON, OFF, OFF, ON, OFF, ON, ON);
        public static Digit FIVE = new Digit(ON, OFF, ON, ON, ON, OFF, ON);
        public static Digit SIX = new Digit(ON, ON, ON, ON, ON, OFF, ON);
        public static Digit SEVEN = new Digit(OFF, OFF, ON, OFF, OFF, ON, ON);
        public static Digit EIGHT = new Digit(ON, ON, ON, ON, ON, ON, ON);
        public static Digit NIGHT = new Digit(ON, OFF, ON, ON, ON, ON, ON);

        private boolean[] segments;

        public static Digit toDigit(int value) {
            switch (value) {
                case 0:
                    return ZERO;
                case 1:
                    return ONE;
                case 2:
                    return TWO;
                case 3:
                    return THREE;
                case 4:
                    return FOUR;
                case 5:
                    return FIVE;
                case 6:
                    return SIX;
                case 7:
                    return SEVEN;
                case 8:
                    return EIGHT;
                case 9:
                    return NIGHT;
                default:
                    return DEFAULT;
            }
        }

        Digit(boolean... isOns) {
            //最多只要 7个状态, 多传入忽视
            final int size = isOns.length > 7 ? 7 : isOns.length;
            segments = new boolean[7];
            //缺少的参数默认为 off
            for (int i = 0; i < size; i++) {
                segments[i] = isOns[i];
            }

            if (size < 7) {
                for (int i = size; i < 7; i++) {
                    segments[i] = OFF;
                }
            }
        }

        public boolean[] values() {
            return segments;
        }

        public boolean value(int i) {
            if (segments == null ||
                    segments.length <= i ||
                    i < 0) {
                return false;
            }
            return segments[i];
        }
    }

    /**
     * 表示其中一个数字管
     */
    public static class Segment {
        private static final int LT = 0;
        private static final int LB = 1;
        private static final int CT = 2;
        private static final int CC = 3;
        private static final int CB = 4;
        private static final int RT = 5;
        private static final int RB = 6;

        //一个灯管宽度
        private static final int WIDTH = 4;
        //一个灯管中两端突出的三角形的高 width / 2, 保证等腰三角形
        private static final int PEAK = WIDTH / 2;
        //一个灯管高度 10 + width / 2 * 2
        private static final int HEIGHT = 10 + PEAK * 2;

        private Path path;
        //用于作平移和旋转变换
        private Matrix matrix;

        //亮的规则
        public Segment(int offset) {
            matrix = new Matrix();
            path = new Path();

            //初始化中点
            //基准灯管, 位于7个当中的中间
            //使用图形的对称中心作为坐标原点
            initBase();
            final int gap = 1;
            switch (offset) {
                default:
                case LT:
                    //左上 相当于base 向左 向上平移, 旋转90度
                    path = translatePath(-(HEIGHT / 2) - gap, HEIGHT / 2 + gap, 90);
                    break;
                case LB:
                    //左下, 相当于base 向左 向下平移, 旋转90度
                    path = translatePath(-(HEIGHT / 2) - gap, -(HEIGHT / 2) - gap, 90);
                    break;
                case CT:
                    //中上, 相当于base 向上 平移, 旋转0度
                    path = translatePath(0, HEIGHT + gap * 2, 0);
                    break;
                case CC:
                    //中中, base
                    path = translatePath(0, 0, 0);
                    break;
                case CB:
                    //中下, 相当于base 向下平移, 旋转0度
                    path = translatePath(0, -HEIGHT - gap * 2, 0);
                    break;
                case RT:
                    //右上, 相当于base 向右 向上平移, 旋转90度
                    path = translatePath(HEIGHT / 2 + gap, HEIGHT / 2 + gap, 90);
                    break;
                case RB:
                    //右下, 相当于base 向右 向下平移, 旋转90度
                    path = translatePath(HEIGHT / 2 + gap, -(HEIGHT / 2) - gap, 90);
                    break;
            }
        }

        //helper
        public static List<Segment> segments() {
            List<Segment> segments = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                Segment segment = new Segment(i);
                segments.add(segment);
            }
            return segments;
        }

        /**
         * 初始化基准灯管
         */
        private void initBase() {
            List<Point> points = new ArrayList<>();
            //最顶点
            points.add(new Point(0, HEIGHT / 2));
            //左上
            points.add(new Point(-(WIDTH / 2), HEIGHT / 2 - PEAK));
            //左下
            points.add(new Point(-(WIDTH / 2), -(HEIGHT / 2 - PEAK)));
            //最下点
            points.add(new Point(0, -(HEIGHT / 2)));
            //右下
            points.add(new Point(WIDTH / 2, -(HEIGHT / 2 - PEAK)));
            //右上
            points.add(new Point(WIDTH / 2, HEIGHT / 2 - PEAK));
            //基准的灯管path
            makeBasePath(points);
        }

        /**
         * 代表中心的那个管
         *
         * @param points
         */
        private void makeBasePath(List<Point> points) {
            if (points == null || points.isEmpty()) {
                return;
            }
            final Path path = new Path();
            path.moveTo(points.get(0).x, points.get(0).y);
            for (int i = 1; i < points.size(); i++) {
                path.lineTo(points.get(i).x, points.get(i).y);
            }

            //闭合,形成多边形
            path.close();
            matrix.setRotate(90);
            path.transform(matrix, this.path);
        }

        /**
         * 基于base 变换
         *
         * @param translateX
         * @param translateY
         * @param rotate
         * @return
         */
        private Path translatePath(int translateX, int translateY, int rotate) {
            Path path = new Path();
            //每次使用setxxx, matrix会重置整个矩阵
            matrix.setTranslate(translateX, -translateY);
            matrix.preRotate(rotate);
            //此时还是base
            this.path.transform(matrix, path);
            return path;
        }
    }
}



