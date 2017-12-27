package com.mangoer.noisedetection.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import com.mangoer.noisedetection.R;

/**
 * @ClassName NoiseboardView
 * @Description TODO(刻度盘)
 * @author Mangoer
 * @Date 2017/12/22 11:18
 */
public class NoiseboardView extends View {

    private int mRadius; // 圆弧半径
    private int mStartAngle; // 起始角度
    private int mSweepAngle; // 扫过角度
    private int mBigSliceCount; // 大份数
    private int mSliceCountInOneBigSlice; // 划分一大份长的小份数

    private int mArcColor; // 刻度颜色
    private int mMeasureTextSize; // 刻度字体大小

    private String mUnitText = ""; // 单位
    private int mUnitTextSize; // 单位字体大小

    private int mMinValue; // 最小值
    private int mMaxValue; // 最大值
    private int mStripeWidth; // 色条宽

    private int mPointerRadius; // 三角形指针半径
    private int mCircleRadius; // 中心圆半径

    private float mRealTimeValue = 0.0f; // 实时值

    private int mBigSliceRadius; // 大刻度半径
    private int mSmallSliceRadius; // 小刻度半径
    private int mNumMeaRadius; // 数字刻度半径

    private int mViewColor_green; // 字体颜色
    private int mViewColor_yellow; // 字体颜色
    private int mViewColor_orange; // 字体颜色
    private int mViewColor_red; // 字体颜色

    private int mViewWidth; // 控件宽度
    private int mViewHeight; // 控件高度
    private float mCenterX;
    private float mCenterY;

    private Paint mPaintArc;//圆盘上大小刻度画笔
    private Paint mPaintText;//圆盘上刻度值画笔
    private Paint mPaintPointer;//绘制中心圆，指针
    private Paint mPaintValue;//绘制实时值
    private Paint mPaintStripe;//绘制色带

    private RectF mRectStripe;
    private Rect mRectMeasures;
    private Rect mRectRealText;
    private Path path;

    private int mSmallSliceCount; // 短刻度个数
    private float mBigSliceAngle; // 大刻度等分角度
    private float mSmallSliceAngle; // 小刻度等分角度

    private String[] mGraduations; // 等分的刻度值
    private float initAngle;

    private SweepGradient mSweepGradient ;//设置渐变
    private int[] color = new int[7];

    public NoiseboardView(Context context) {
        this(context, null);
    }

    public NoiseboardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NoiseboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //自定义属性
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NoiseboardView, defStyleAttr, 0);

        mRadius = a.getDimensionPixelSize(R.styleable.NoiseboardView_radius, dpToPx(80));
        mStartAngle = a.getInteger(R.styleable.NoiseboardView_startAngle, 180);
        mSweepAngle = a.getInteger(R.styleable.NoiseboardView_sweepAngle, 200);
        mBigSliceCount = a.getInteger(R.styleable.NoiseboardView_bigSliceCount, 5);
        mSliceCountInOneBigSlice = a.getInteger(R.styleable.NoiseboardView_sliceCountInOneBigSlice, 5);

        mArcColor = a.getColor(R.styleable.NoiseboardView_arcColor, Color.WHITE);
        mMeasureTextSize = a.getDimensionPixelSize(R.styleable.NoiseboardView_measureTextSize, spToPx(12));

        mUnitText = a.getString(R.styleable.NoiseboardView_unitText);
        mUnitTextSize = a.getDimensionPixelSize(R.styleable.NoiseboardView_unitTextSize, spToPx(14));

        mMinValue = a.getInteger(R.styleable.NoiseboardView_minValue, 0);
        mMaxValue = a.getInteger(R.styleable.NoiseboardView_maxValue, 100);
        mStripeWidth = a.getDimensionPixelSize(R.styleable.NoiseboardView_stripeWidth, 0);

        a.recycle();

        init();
    }

    private void init() {

        mPointerRadius = mRadius / 3 * 2;
        mCircleRadius = mRadius / 17;

        mSmallSliceRadius = mRadius - dpToPx(10);
        mBigSliceRadius = mSmallSliceRadius - dpToPx(8);
        mNumMeaRadius = mBigSliceRadius - dpToPx(2);

        mSmallSliceCount = mBigSliceCount * 5;
        mBigSliceAngle = mSweepAngle / (float) mBigSliceCount;
        mSmallSliceAngle = mBigSliceAngle / mSliceCountInOneBigSlice;
        mGraduations = getMeasureNumbers();

        int totalRadius;
        totalRadius = mRadius;

        mCenterX = mCenterY = 0.0f;
        if (mStartAngle <= 180 && mStartAngle + mSweepAngle >= 180) {
            mViewWidth = totalRadius * 2 + getPaddingLeft() + getPaddingRight() + dpToPx(2) * 2;
        } else {
            float[] point1 = getCoordinatePoint(totalRadius, mStartAngle);
            float[] point2 = getCoordinatePoint(totalRadius, mStartAngle + mSweepAngle);
            float max = Math.max(Math.abs(point1[0]), Math.abs(point2[0]));
            mViewWidth = (int) (max * 2 + getPaddingLeft() + getPaddingRight() + dpToPx(2) * 2);
        }
        if ((mStartAngle <= 90 && mStartAngle + mSweepAngle >= 90) ||
                (mStartAngle <= 270 && mStartAngle + mSweepAngle >= 270)) {
            mViewHeight = totalRadius * 2 + getPaddingLeft() + getPaddingRight() + dpToPx(2) * 2;
        } else {
            float[] point1 = getCoordinatePoint(totalRadius, mStartAngle);
            float[] point2 = getCoordinatePoint(totalRadius, mStartAngle + mSweepAngle);
            float max = Math.max(Math.abs(point1[1]), Math.abs(point2[1]));
            mViewHeight = (int) (max * 2 + getPaddingLeft() + getPaddingRight() + dpToPx(2) * 2);
        }

        mCenterX = mViewWidth / 2.0f;
        mCenterY = mViewHeight / 2.0f;

        if (mPaintArc == null) {
            mPaintArc = new Paint();
        }
        mPaintArc.setAntiAlias(true);
        mPaintArc.setColor(mArcColor);
        mPaintArc.setStyle(Paint.Style.STROKE);
        mPaintArc.setStrokeCap(Paint.Cap.ROUND);

        if (mPaintText == null) {
            mPaintText = new Paint();
        }
        mPaintText.setAntiAlias(true);
        mPaintText.setColor(mArcColor);
        mPaintText.setStyle(Paint.Style.STROKE);

        if (mPaintPointer == null) {
            mPaintPointer = new Paint();
        }
        mPaintPointer.setAntiAlias(true);

        mRectMeasures = new Rect();
        mRectRealText = new Rect();
        path = new Path();

        if (mPaintValue == null) {
            mPaintValue = new Paint();
        }
        mPaintValue.setAntiAlias(true);
        mPaintValue.setStyle(Paint.Style.STROKE);
        mPaintValue.setTextAlign(Paint.Align.CENTER);
        mPaintValue.setTextSize(mUnitTextSize);
        mPaintValue.getTextBounds(trimFloat(mRealTimeValue), 0, trimFloat(mRealTimeValue).length(), mRectRealText);

        initAngle = getAngleFromResult(mRealTimeValue);

        mViewColor_green = getResources().getColor(R.color.green_value);
        mViewColor_yellow = getResources().getColor(R.color.yellow_value);
        mViewColor_orange = getResources().getColor(R.color.orange_value);
        mViewColor_red = getResources().getColor(R.color.red_value);
        color[0] = mViewColor_red;
        color[1] = mViewColor_red;
        color[2] = mViewColor_green;
        color[3] = mViewColor_green;
        color[4] = mViewColor_yellow;
        color[5] = mViewColor_orange;
        color[6] = mViewColor_red;
        //色带画笔
        if (mPaintStripe == null) {
            mPaintStripe = new Paint();
        }
        mPaintStripe.setAntiAlias(true);
        mPaintStripe.setStyle(Paint.Style.STROKE);
        mPaintStripe.setStrokeWidth(mStripeWidth);
        int r ;
        if (mStripeWidth > 0) {
            r = mRadius + dpToPx(1) - mStripeWidth / 2;
            mRectStripe = new RectF(mCenterX - r, mCenterY - r, mCenterX + r, mCenterY + r);
        }
        mSweepGradient = new SweepGradient(mCenterX, mCenterY,color,null);
        mPaintStripe.setShader(mSweepGradient);//设置渐变 从X轴正方向取color数组颜色开始渐变
    }

    private String[] getMeasureNumbers() {
        String[] strings = new String[mBigSliceCount + 1];
        for (int i = 0; i <= mBigSliceCount; i++) {
            if (i == 0) {
                strings[i] = String.valueOf(mMinValue);
            } else if (i == mBigSliceCount) {
                strings[i] = String.valueOf(mMaxValue);
            } else {
                strings[i] = String.valueOf(((mMaxValue - mMinValue) / mBigSliceCount) * i);
            }
        }
        return strings;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY) {
            mViewWidth = dpToPx(widthSize);
        } else {
            if (widthMode == MeasureSpec.AT_MOST)
                mViewWidth = Math.min(mViewWidth, widthSize);
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            mViewHeight = dpToPx(heightSize);
        } else {
            int totalRadius;
            totalRadius = mRadius;
            if (mStartAngle >= 180 && mStartAngle + mSweepAngle <= 360) {
                mViewHeight = totalRadius + mCircleRadius + dpToPx(2) + dpToPx(25) +
                        getPaddingTop() + getPaddingBottom() + mRectRealText.height();
            } else {
                float[] point1 = getCoordinatePoint(totalRadius, mStartAngle);
                float[] point2 = getCoordinatePoint(totalRadius, mStartAngle + mSweepAngle);
                float maxY = Math.max(Math.abs(point1[1]) - mCenterY, Math.abs(point2[1]) - mCenterY);
                float f = mCircleRadius + dpToPx(2) + dpToPx(25) + mRectRealText.height();
                float max = Math.max(maxY, f);
                mViewHeight = (int) (max + totalRadius + getPaddingTop() + getPaddingBottom() + dpToPx(2) * 2);
            }
            if (widthMode == MeasureSpec.AT_MOST)
                mViewHeight = Math.min(mViewHeight, widthSize);
        }

        Log.e("onMeasure","widthSize="+widthSize+",heightSize="+heightSize);
        setMeasuredDimension(mViewWidth, mViewHeight);
    }


    @Override
    protected void onDraw(Canvas canvas) {

        // 绘制色带
        canvas.drawArc(mRectStripe, 170, 200, false, mPaintStripe);

        mPaintArc.setStrokeWidth(dpToPx(2));
        for (int i = 0; i <= mBigSliceCount; i++) {
            //绘制大刻度
            float angle = i * mBigSliceAngle + mStartAngle;
            float[] point1 = getCoordinatePoint(mRadius, angle);
            float[] point2 = getCoordinatePoint(mBigSliceRadius, angle);
            canvas.drawLine(point1[0], point1[1], point2[0], point2[1], mPaintArc);

            //绘制圆盘上的数字
            mPaintText.setTextSize(mMeasureTextSize);
            String number = mGraduations[i];
            mPaintText.getTextBounds(number, 0, number.length(), mRectMeasures);
            if (angle % 360 > 135 && angle % 360 < 215) {
                mPaintText.setTextAlign(Paint.Align.LEFT);
            } else if ((angle % 360 >= 0 && angle % 360 < 45) || (angle % 360 > 325 && angle % 360 <= 360)) {
                mPaintText.setTextAlign(Paint.Align.RIGHT);
            } else {
                mPaintText.setTextAlign(Paint.Align.CENTER);
            }
            float[] numberPoint = getCoordinatePoint(mNumMeaRadius, angle);
            if (i == 0 || i == mBigSliceCount) {
                canvas.drawText(number, numberPoint[0], numberPoint[1] + (mRectMeasures.height() / 2), mPaintText);
            } else {
                canvas.drawText(number, numberPoint[0], numberPoint[1] + mRectMeasures.height(), mPaintText);
            }
        }

        //绘制小的子刻度
        mPaintArc.setStrokeWidth(dpToPx(1));
        for (int i = 0; i < mSmallSliceCount; i++) {
            if (i % mSliceCountInOneBigSlice != 0) {
                float angle = i * mSmallSliceAngle + mStartAngle;
                float[] point1 = getCoordinatePoint(mRadius, angle);
                float[] point2 = getCoordinatePoint(mSmallSliceRadius, angle);

                mPaintArc.setStrokeWidth(dpToPx(1));
                canvas.drawLine(point1[0], point1[1], point2[0], point2[1], mPaintArc);
            }
        }

        if (mRealTimeValue <= 40) {
            mPaintValue.setColor(mViewColor_green);
            mPaintPointer.setColor(mViewColor_green);
        } else if (mRealTimeValue > 40 && mRealTimeValue <= 90) {
            mPaintValue.setColor(mViewColor_yellow);
            mPaintPointer.setColor(mViewColor_yellow);
        } else if (mRealTimeValue > 90 && mRealTimeValue <= 120) {
            mPaintValue.setColor(mViewColor_orange);
            mPaintPointer.setColor(mViewColor_orange);
        } else {
            mPaintValue.setColor(mViewColor_red);
            mPaintPointer.setColor(mViewColor_red);
        }

        //绘制实时值
        canvas.drawText(trimFloat(mRealTimeValue)+" "+ mUnitText, mCenterX, mCenterY - mRadius / 3 , mPaintValue);

        //绘制中心点的圆
        mPaintPointer.setStyle(Paint.Style.STROKE);
        mPaintPointer.setStrokeWidth(dpToPx(4));
        canvas.drawCircle(mCenterX, mCenterY, mCircleRadius + dpToPx(3), mPaintPointer);

        //绘制三角形指针
        path.reset();
        mPaintPointer.setStyle(Paint.Style.FILL);
        float[] point1 = getCoordinatePoint(mCircleRadius / 2, initAngle + 90);
        path.moveTo(point1[0], point1[1]);
        float[] point2 = getCoordinatePoint(mCircleRadius / 2, initAngle - 90);
        path.lineTo(point2[0], point2[1]);
        float[] point3 = getCoordinatePoint(mPointerRadius, initAngle);
        path.lineTo(point3[0], point3[1]);
        path.close();
        canvas.drawPath(path, mPaintPointer);

        // 绘制三角形指针底部的圆弧效果
        canvas.drawCircle((point1[0] + point2[0]) / 2, (point1[1] + point2[1]) / 2, mCircleRadius / 2, mPaintPointer);
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private int spToPx(int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }

    /**
     * 依圆心坐标，半径，扇形角度，计算出扇形终射线与圆弧交叉点的xy坐标
     */
    public float[] getCoordinatePoint(int radius, float cirAngle) {
        float[] point = new float[2];

        double arcAngle = Math.toRadians(cirAngle); //将角度转换为弧度
        if (cirAngle < 90) {
            point[0] = (float) (mCenterX + Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY + Math.sin(arcAngle) * radius);
        } else if (cirAngle == 90) {
            point[0] = mCenterX;
            point[1] = mCenterY + radius;
        } else if (cirAngle > 90 && cirAngle < 180) {
            arcAngle = Math.PI * (180 - cirAngle) / 180.0;
            point[0] = (float) (mCenterX - Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY + Math.sin(arcAngle) * radius);
        } else if (cirAngle == 180) {
            point[0] = mCenterX - radius;
            point[1] = mCenterY;
        } else if (cirAngle > 180 && cirAngle < 270) {
            arcAngle = Math.PI * (cirAngle - 180) / 180.0;
            point[0] = (float) (mCenterX - Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY - Math.sin(arcAngle) * radius);
        } else if (cirAngle == 270) {
            point[0] = mCenterX;
            point[1] = mCenterY - radius;
        } else {
            arcAngle = Math.PI * (360 - cirAngle) / 180.0;
            point[0] = (float) (mCenterX + Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY - Math.sin(arcAngle) * radius);
        }

        return point;
    }

    /**
     * 通过数值得到角度位置
     */
    private float getAngleFromResult(float result) {
        if (result > mMaxValue)
            return mMaxValue;
        return mSweepAngle * (result - mMinValue) / (mMaxValue - mMinValue) + mStartAngle;
    }

    /**
     * float类型如果小数点后为零则显示整数否则保留
     */
    public static String trimFloat(float value) {
        if (Math.round(value) - value == 0) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }


    public float getRealTimeValue() {
        return mRealTimeValue;
    }

    /**
     * 实时设置读数值
     * @param realTimeValue
     */
    public void setRealTimeValue(float realTimeValue) {
        if (realTimeValue > mMaxValue) return;
        mRealTimeValue = realTimeValue;
        initAngle = getAngleFromResult(mRealTimeValue);
        invalidate();
    }

}