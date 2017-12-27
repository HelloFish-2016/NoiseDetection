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

    final String TAG = "NoiseboardView";

    private int mRadius; // 圆弧半径
    private int mBigSliceCount; // 大份数
    private int mScaleCountInOneBigScale; // 相邻两个大刻度之间的小刻度个数
    private int mScaleColor; // 刻度颜色
    private int mScaleTextSize; // 刻度字体大小
    private String mUnitText = ""; // 单位
    private int mUnitTextSize; // 单位字体大小
    private int mMinValue; // 最小值
    private int mMaxValue; // 最大值
    private int mRibbonWidth; // 色条宽

    private int mStartAngle; // 起始角度
    private int mSweepAngle; // 扫过角度

    private int mPointerRadius; // 三角形指针半径
    private int mCircleRadius; // 中心圆半径

    private float mRealTimeValue = 0.0f; // 实时值

    private int mBigScaleRadius; // 大刻度半径
    private int mSmallScaleRadius; // 小刻度半径
    private int mNumScaleRadius; // 数字刻度半径

    private int mViewColor_green; // 字体颜色
    private int mViewColor_yellow; // 字体颜色
    private int mViewColor_orange; // 字体颜色
    private int mViewColor_red; // 字体颜色

    private int mViewWidth; // 控件宽度
    private int mViewHeight; // 控件高度
    private float mCenterX;//中心点圆坐标x
    private float mCenterY;//中心点圆坐标y

    private Paint mPaintScale;//圆盘上大小刻度画笔
    private Paint mPaintScaleText;//圆盘上刻度值画笔
    private Paint mPaintCirclePointer;//绘制中心圆，指针
    private Paint mPaintValue;//绘制实时值
    private Paint mPaintRibbon;//绘制色带

    private RectF mRectRibbon;//存储色带的矩形数据
    private Rect mRectScaleText;//存储刻度值的矩形数据
    private Path path;//绘制指针的路径

    private int mSmallScaleCount; // 小刻度总数
    private float mBigScaleAngle; // 相邻两个大刻度之间的角度
    private float mSmallScaleAngle; // 相邻两个小刻度之间的角度

    private String[] mGraduations; // 每个大刻度的刻度值
    private float initAngle;//指针实时角度

    private SweepGradient mSweepGradient ;//设置渐变
    private int[] color = new int[7];//渐变颜色组

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
        mBigSliceCount = a.getInteger(R.styleable.NoiseboardView_bigSliceCount, 5);
        mScaleCountInOneBigScale = a.getInteger(R.styleable.NoiseboardView_sliceCountInOneBigSlice, 5);
        mScaleColor = a.getColor(R.styleable.NoiseboardView_scaleColor, Color.WHITE);
        mScaleTextSize = a.getDimensionPixelSize(R.styleable.NoiseboardView_scaleTextSize, spToPx(12));
        mUnitText = a.getString(R.styleable.NoiseboardView_unitText);
        mUnitTextSize = a.getDimensionPixelSize(R.styleable.NoiseboardView_unitTextSize, spToPx(14));
        mMinValue = a.getInteger(R.styleable.NoiseboardView_minValue, 0);
        mMaxValue = a.getInteger(R.styleable.NoiseboardView_maxValue, 150);
        mRibbonWidth = a.getDimensionPixelSize(R.styleable.NoiseboardView_ribbonWidth, 0);

        a.recycle();
        init();
    }

    private void init() {

        //起始角度是从水平正方向即（钟表3点钟方向）开始从0算的，扫过的角度是按顺时针方向算
        mStartAngle = 175;
        mSweepAngle = 190;

        mPointerRadius = mRadius / 3 * 2;
        mCircleRadius = mRadius / 17;

        mSmallScaleRadius = mRadius - dpToPx(10);
        mBigScaleRadius = mRadius - dpToPx(18);
        mNumScaleRadius = mRadius - dpToPx(20);

        mSmallScaleCount = mBigSliceCount * 5;
        mBigScaleAngle = mSweepAngle / (float) mBigSliceCount;
        mSmallScaleAngle = mBigScaleAngle / mScaleCountInOneBigScale;
        mGraduations = getMeasureNumbers();

        //确定控件的宽度 padding值，在构造方法执行完就被赋值
        mViewWidth = getPaddingLeft() + mRadius * 2 + getPaddingRight() + dpToPx(4);
        mViewHeight = mViewWidth;
        mCenterX = mViewWidth / 2.0f;
        mCenterY = mViewHeight / 2.0f;

        mPaintScale = new Paint();
        mPaintScale.setAntiAlias(true);
        mPaintScale.setColor(mScaleColor);
        mPaintScale.setStyle(Paint.Style.STROKE);
        mPaintScale.setStrokeCap(Paint.Cap.ROUND);

        mPaintScaleText = new Paint();
        mPaintScaleText.setAntiAlias(true);
        mPaintScaleText.setColor(mScaleColor);
        mPaintScaleText.setStyle(Paint.Style.STROKE);

        mPaintCirclePointer = new Paint();
        mPaintCirclePointer.setAntiAlias(true);

        mRectScaleText = new Rect();
        path = new Path();

        mPaintValue = new Paint();
        mPaintValue.setAntiAlias(true);
        mPaintValue.setStyle(Paint.Style.STROKE);
        mPaintValue.setTextAlign(Paint.Align.CENTER);
        mPaintValue.setTextSize(mUnitTextSize);

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
        mPaintRibbon = new Paint();
        mPaintRibbon.setAntiAlias(true);
        mPaintRibbon.setStyle(Paint.Style.STROKE);
        mPaintRibbon.setStrokeWidth(mRibbonWidth);
        mSweepGradient = new SweepGradient(mCenterX, mCenterY,color,null);
        mPaintRibbon.setShader(mSweepGradient);//设置渐变 从X轴正方向取color数组颜色开始渐变

        if (mRibbonWidth > 0) {
            int r  = mRadius - mRibbonWidth / 2 + dpToPx(1) ;
            mRectRibbon = new RectF(mCenterX - r, mCenterY - r, mCenterX + r, mCenterY + r);
        }
    }

    /**
     * 确定每个大刻度的值
     * @return
     */
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

    /**
     * <dt>UNSPECIFIED :  0 << 30 = 0</dt>
     * <dd>
     *     父控件没有对子控件做限制，子控件可以是自己想要的尺寸
     *     其实就是子空间在布局里没有设置宽高，但布局里添加控件都要设置宽高，所以这种情况暂时没碰到
     * </dd>
     *
     * <dt>EXACTLY : 1 << 30 = 1073741824</dt>
     * <dd>
     *      父控件给子控件决定了确切大小，子控件将被限定在给定的边界里。
     *      如果是填充父窗体(match_parent)，说明父控件已经明确知道子控件想要多大的尺寸了，也是这种模式
     * </dd>
     *
     * <dt>AT_MOST : 2 << 30 = -2147483648</dt>
     * <dd>
     *      在布局设置wrap_content，父控件并不知道子控件到底需要多大尺寸（具体值），
     *      需要子控件在onMeasure测量之后再让父控件给他一个尽可能大的尺寸以便让内容全部显示
     *      如果在onMeasure没有指定控件大小，默认会填充父窗体，因为在view的onMeasure源码中，
     *      AT_MOST（相当于wrap_content ）和EXACTLY （相当于match_parent ）两种情况返回的测量宽高都是specSize，
     *      而这个specSize正是父控件剩余的宽高，所以默认onMeasure方法中wrap_content 和match_parent 的效果是一样的，都是填充剩余的空间。
     * </dd>
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);//从约束规范中获取模式
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);//从约束规范中获取尺寸
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        //在布局中设置了具体值
        if (widthMode == MeasureSpec.EXACTLY)
            mViewWidth = widthSize;

        //在布局中设置 wrap_content，控件就取能完全展示内容的宽度（同时需要考虑屏幕的宽度）
        if (widthMode == MeasureSpec.AT_MOST)
            mViewWidth = Math.min(mViewWidth, widthSize);

        if (heightMode == MeasureSpec.EXACTLY) {
            mViewHeight = heightSize;
        } else {

            float[] point1 = getCoordinatePoint(mRadius, mStartAngle);
            float[] point2 = getCoordinatePoint(mRadius, mStartAngle + mSweepAngle);
            float maxY = Math.max(Math.abs(point1[1]) - mCenterY, Math.abs(point2[1]) - mCenterY);
            float f = mCircleRadius + dpToPx(2) + dpToPx(25) ;
            float max = Math.max(maxY, f);
            mViewHeight = (int) (max + mRadius + getPaddingTop() + getPaddingBottom() + dpToPx(2) * 2);

            if (heightMode == MeasureSpec.AT_MOST)
                mViewHeight = Math.min(mViewHeight, heightSize);
        }

        //保存测量宽度和测量高度
        setMeasuredDimension(mViewWidth, mViewHeight);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        // 绘制色带
        canvas.drawArc(mRectRibbon, 170, 199, false, mPaintRibbon);

        mPaintScale.setStrokeWidth(dpToPx(2));
        for (int i = 0; i <= mBigSliceCount; i++) {
            //绘制大刻度
            float angle = i * mBigScaleAngle + mStartAngle;
            float[] point1 = getCoordinatePoint(mRadius, angle);
            float[] point2 = getCoordinatePoint(mBigScaleRadius, angle);
            canvas.drawLine(point1[0], point1[1], point2[0], point2[1], mPaintScale);

            //绘制圆盘上的数字
            mPaintScaleText.setTextSize(mScaleTextSize);
            String number = mGraduations[i];
            mPaintScaleText.getTextBounds(number, 0, number.length(), mRectScaleText);
            if (angle % 360 > 135 && angle % 360 < 215) {
                mPaintScaleText.setTextAlign(Paint.Align.LEFT);
            } else if ((angle % 360 >= 0 && angle % 360 < 45) || (angle % 360 > 325 && angle % 360 <= 360)) {
                mPaintScaleText.setTextAlign(Paint.Align.RIGHT);
            } else {
                mPaintScaleText.setTextAlign(Paint.Align.CENTER);
            }
            float[] numberPoint = getCoordinatePoint(mNumScaleRadius, angle);
            if (i == 0 || i == mBigSliceCount) {
                canvas.drawText(number, numberPoint[0], numberPoint[1] + (mRectScaleText.height() / 2), mPaintScaleText);
            } else {
                canvas.drawText(number, numberPoint[0], numberPoint[1] + mRectScaleText.height(), mPaintScaleText);
            }
        }

        //绘制小的子刻度
        mPaintScale.setStrokeWidth(dpToPx(1));
        for (int i = 0; i < mSmallScaleCount; i++) {
            if (i % mScaleCountInOneBigScale != 0) {
                float angle = i * mSmallScaleAngle + mStartAngle;
                float[] point1 = getCoordinatePoint(mRadius, angle);
                float[] point2 = getCoordinatePoint(mSmallScaleRadius, angle);

                mPaintScale.setStrokeWidth(dpToPx(1));
                canvas.drawLine(point1[0], point1[1], point2[0], point2[1], mPaintScale);
            }
        }

        if (mRealTimeValue <= 40) {
            mPaintValue.setColor(mViewColor_green);
            mPaintCirclePointer.setColor(mViewColor_green);
        } else if (mRealTimeValue > 40 && mRealTimeValue <= 90) {
            mPaintValue.setColor(mViewColor_yellow);
            mPaintCirclePointer.setColor(mViewColor_yellow);
        } else if (mRealTimeValue > 90 && mRealTimeValue <= 120) {
            mPaintValue.setColor(mViewColor_orange);
            mPaintCirclePointer.setColor(mViewColor_orange);
        } else {
            mPaintValue.setColor(mViewColor_red);
            mPaintCirclePointer.setColor(mViewColor_red);
        }

        //绘制中心点的圆
        mPaintCirclePointer.setStyle(Paint.Style.STROKE);
        mPaintCirclePointer.setStrokeWidth(dpToPx(4));
        canvas.drawCircle(mCenterX, mCenterY, mCircleRadius + dpToPx(3), mPaintCirclePointer);

        //绘制三角形指针
        path.reset();
        mPaintCirclePointer.setStyle(Paint.Style.FILL);
        float[] point1 = getCoordinatePoint(mCircleRadius / 2, initAngle + 90);
        path.moveTo(point1[0], point1[1]);
        float[] point2 = getCoordinatePoint(mCircleRadius / 2, initAngle - 90);
        path.lineTo(point2[0], point2[1]);
        float[] point3 = getCoordinatePoint(mPointerRadius, initAngle);
        path.lineTo(point3[0], point3[1]);
        path.close();
        canvas.drawPath(path, mPaintCirclePointer);

        // 绘制三角形指针底部的圆弧效果
        canvas.drawCircle((point1[0] + point2[0]) / 2, (point1[1] + point2[1]) / 2, mCircleRadius / 2, mPaintCirclePointer);

        //绘制实时值
        canvas.drawText(trimFloat(mRealTimeValue)+" "+ mUnitText, mCenterX, mCenterY - mRadius / 3 , mPaintValue);
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

        Log.e("getCoordinatePoint","radius="+radius+",cirAngle="+cirAngle+",point[0]="+point[0]+",point[1]="+point[1]);
        return point;
    }

    /**
     * 通过实时数值得到指针角度
     */
    private float getAngleFromResult(float result) {
        if (result > mMaxValue)
            return 360.0f;
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