package com.shijingfeng.library.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.AnyThread;
import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.Nullable;

import com.shijingfeng.library.R;
import com.shijingfeng.library.util.ThreadUtil;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static android.util.Log.e;

/**
 * Function:
 * Date: 2020/8/18 17:53
 * Description:
 *
 * @author ShiJingFeng
 */
public class ProgressRing extends View {

    public static final String PROGRESS_VALUE = "%progress";

    /**
     * 圆环 Paint
     */
    private Paint mRingPaint;
    /**
     * 文本 Paint
     */
    private Paint mTextPaint;
    /**
     * 圆环 Path
     */
    private Path mRingPath;

    /**
     * 进度环颜色 默认纯红色
     */
    @ColorInt
    private int mRingColor = Color.RED;
    /**
     * 背景环颜色 默认浅灰色
     */
    @ColorInt
    private int mBackgroundRingColor = Color.parseColor("#D3D3D3");

    /**
     * 进度环厚度
     */
    private int mThickness = 0;

    /**
     * 文本颜色 默认纯红色
     */
    @ColorInt
    private int mTextColor = Color.RED;

    /**
     * 自定义文本 (如果为 null, 那么就显示默认进度文本)
     * 注: 文本中的 {@link ProgressRing#PROGRESS_VALUE} 代表当前进度(百分比值进度 或 数值进度)
     */
    private String mCustomText = null;

    /**
     * 文本大小
     */
    private int mTextSize = 0;

    /**
     * 文本是否可见  true 可见  false 不可见
     */
    private boolean mTextVisible = true;
    /**
     * 设置文本是否加粗  true 加粗  false 不加粗
     */
    private boolean mTextBold = false;

    /**
     * 进度环总进度
     */
    private float mTotalProgress = 100F;
    /**
     * 进度环当前进度
     */
    private float mCurProgress = 0F;

    /**
     * 进度类型: 默认百分比
     */
    @ProgressType
    private int mProgressType = ProgressType.PROGRESS_TYPE_PERCENT;

    /**
     * 进度方向: 默认顺时针
     */
    @ProgressDirection
    private int mProgressDirection = ProgressDirection.PROGRESS_DIRECTION_CW;

    /**
     * 起点位置: 顶部
     */
    @StartPosition
    private int mStartPosition = StartPosition.START_POSITION_TOP;

    public ProgressRing(Context context) {
        this(context, null);
    }

    public ProgressRing(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressRing(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ProgressRing(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        mRingPaint = new Paint();
        mRingPaint.setStyle(Paint.Style.STROKE);
        mTextPaint = new Paint();
        mRingPath = new Path();

        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ProgressRing);

        mRingColor = typedArray.getColor(R.styleable.ProgressRing_ringColor, Color.RED);
        mBackgroundRingColor = typedArray.getColor(R.styleable.ProgressRing_backgroundRingColor, Color.parseColor("#D3D3D3"));
        mThickness = typedArray.getDimensionPixelSize(R.styleable.ProgressRing_thickness, 0);
        mCustomText = typedArray.getString(R.styleable.ProgressRing_customText);
        mTextColor = typedArray.getColor(R.styleable.ProgressRing_textColor, Color.RED);
        mTextSize = typedArray.getDimensionPixelSize(R.styleable.ProgressRing_textSize, 0);
        mTextVisible = typedArray.getBoolean(R.styleable.ProgressRing_textVisible, true);
        mTextBold = typedArray.getBoolean(R.styleable.ProgressRing_textBold, false);
        mCurProgress = typedArray.getFloat(R.styleable.ProgressRing_curProgress, 0F);
        mTotalProgress = typedArray.getFloat(R.styleable.ProgressRing_totalProgress, 100F);
        mProgressType = typedArray.getInt(R.styleable.ProgressRing_progressType, ProgressType.PROGRESS_TYPE_PERCENT);
        mProgressDirection = typedArray.getInt(R.styleable.ProgressRing_progressDirection, ProgressDirection.PROGRESS_DIRECTION_CW);
        mStartPosition = typedArray.getInt(R.styleable.ProgressRing_startPosition, StartPosition.START_POSITION_TOP);
        typedArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 测试使用
//        mCurProgress = 360F;
//        mTotalProgress = 360F;

        final float size = Math.min(getWidth(), getHeight());
        final float thickness = mThickness != 0 ? mThickness : size / 15F;
        final float halfThickness = thickness / 2F;
        final float textSize = mTextSize != 0 ? mTextSize : size / 2.2F;

        // 绘制背景圆环
        final float centerPoint = size / 2F;

        mRingPaint.setColor(mBackgroundRingColor);
        mRingPaint.setStrokeWidth(thickness);
        mRingPath.reset();
        mRingPath.moveTo(centerPoint, centerPoint);
        mRingPath.addCircle(centerPoint, centerPoint, centerPoint - halfThickness, Path.Direction.CW);
        canvas.drawPath(mRingPath, mRingPaint);

        // 绘制进度圆环
        // 扫描角度 (顺时针正值, 逆时针负值)
        float sweepAngle = 0F;
        // 进度方向
        @ProgressDirection final int progressDirection = mProgressDirection;
        // 开始位置
        @StartPosition final int startPosition = mStartPosition;

        switch (progressDirection) {
            // 进度方向: 顺时针
            case ProgressDirection.PROGRESS_DIRECTION_CW:
                sweepAngle = mCurProgress * 360F / mTotalProgress;
                break;
            // 进度方向: 逆时针
            case ProgressDirection.PROGRESS_DIRECTION_CCW:
                sweepAngle = - (mCurProgress * 360F / mTotalProgress);
                break;
            default:
                break;
        }
        mRingPaint.setColor(mRingColor);
        mRingPath.reset();
        switch (startPosition) {
            case StartPosition.START_POSITION_TOP:
                mRingPath.moveTo(size / 2F, halfThickness);
                mRingPath.addArc(halfThickness, halfThickness, size - halfThickness, size - halfThickness, -90F, sweepAngle);
                break;
            case StartPosition.START_POSITION_BOTTOM:
                mRingPath.moveTo(size / 2F, size - halfThickness);
                mRingPath.addArc(halfThickness, halfThickness, size - halfThickness, size - halfThickness, 90F, sweepAngle);
                break;
            case StartPosition.START_POSITION_LEFT:
                mRingPath.moveTo(halfThickness, size / 2F);
                mRingPath.addArc(halfThickness, halfThickness, size - halfThickness, size - halfThickness, 180F, sweepAngle);
                break;
            case StartPosition.START_POSITION_RIGHT:
                mRingPath.moveTo(size - halfThickness, size / 2F);
                mRingPath.addArc(halfThickness, halfThickness, size - halfThickness, size - halfThickness, 0F, sweepAngle);
                break;
            default:
                break;
        }
        canvas.drawPath(mRingPath, mRingPaint);

        if (mTextVisible) {
            // 绘制文本
            mTextPaint.setTextSize(textSize);
            mTextPaint.setColor(mTextColor);
            mTextPaint.setFakeBoldText(mTextBold);

            String progressText;

            switch (mProgressType) {
                // 百分比类型
                case ProgressType.PROGRESS_TYPE_PERCENT:
                    progressText = String.valueOf((int) (mCurProgress * 100F / mTotalProgress));
                    break;
                // 数值类型
                case ProgressType.PROGRESS_TYPE_VALUE:
                    progressText = String.valueOf((int) mCurProgress);
                    break;
                default:
                    progressText = "";
                    break;
            }
            if (mCustomText != null) {
                progressText = mCustomText.replace(PROGRESS_VALUE, progressText);
            }

            float textWidth = mTextPaint.measureText(progressText);
            float textHeight = measureTextHeight(mTextPaint);

            canvas.drawText(progressText, centerPoint - textWidth / 2F, centerPoint + textHeight / 3F, mTextPaint);
        }
    }

    /**
     * 测量文字的高度
     * --经测试后发现，采用另一种带Rect的方式，获得的数据并不准确。
     * 特别是在一些对文字有一些倾斜处理的时候
     *
     * @param paint Paint
     * @return 文本高度
     */
    private float measureTextHeight(Paint paint) {
        if (paint == null) {
            return 0F;
        }
        return paint.getFontMetrics().descent - paint.getFontMetrics().ascent;
    }

    /**
     * 设置进度环颜色
     *
     * @param color 进度环颜色
     * @return ProgressRing
     */
    public ProgressRing setRingColor(@ColorInt int color) {
        this.mRingColor = color;
        return this;
    }

    /**
     * 设置背景环颜色
     *
     * @param color 背景环颜色
     * @return ProgressRing
     */
    public ProgressRing setBackgroundRingColor(@ColorInt int color) {
        this.mBackgroundRingColor = color;
        return this;
    }

    /**
     * 设置进度环厚度
     *
     * @param thickness 进度环厚度
     * @return ProgressRing
     */
    public ProgressRing setThickness(int thickness) {
        this.mThickness = thickness;
        return this;
    }

    /**
     * 设置进度环总进度
     *
     * @param totalProgress 进度环总进度
     * @return ProgressRing
     */
    public ProgressRing setTotalProgress(float totalProgress) {
        this.mTotalProgress = totalProgress;
        return this;
    }

    /**
     * 获取进度环总进度
     * @return 进度环总进度
     */
    public float getTotalProgress() {
        return this.mTotalProgress;
    }

    /**
     * 设置进度环当前进度
     *
     * @param curProgress 进度环当前进度
     * @return ProgressRing
     */
    public ProgressRing setCurProgress(float curProgress) {
        this.mCurProgress = curProgress;
        return this;
    }

    /**
     * 获取进度环当前进度
     * @return 进度环当前进度
     */
    public float getCurProgress() {
        return this.mCurProgress;
    }

    /**
     * 设置自定义文本 (如果为 null, 那么就显示默认进度文本)
     * 注: 文本中的 {@link ProgressRing#PROGRESS_VALUE} 代表当前进度(百分比值进度 或 数值进度)
     *
     * @param customText 自定义文本
     * @return ProgressRing
     */
    public ProgressRing setCustomText(@Nullable String customText) {
        this.mCustomText = customText;
        return this;
    }

    /**
     * 设置文本颜色
     *
     * @param color 文本颜色
     * @return ProgressRing
     */
    public ProgressRing setTextColor(@ColorInt int color) {
        this.mTextColor = color;
        return this;
    }

    /**
     * 设置文本大小
     *
     * @param textSize 文本大小
     * @return ProgressRing
     */
    public ProgressRing setTextSize(int textSize) {
        this.mTextSize = textSize;
        return this;
    }

    /**
     * 设置文本是否可见
     *
     * @param isVisible 文本是否可见
     * @return ProgressRing
     */
    public ProgressRing setTextVisible(boolean isVisible) {
        this.mTextVisible = isVisible;
        return this;
    }

    /**
     * 设置文本是否加粗
     *
     * @param isTextBold 文本是否加粗
     * @return ProgressRing
     */
    public ProgressRing setTextBold(boolean isTextBold) {
        this.mTextBold = isTextBold;
        return this;
    }

    /**
     * 设置进度类型
     *
     * @param progressType 进度类型
     * @return ProgressRing
     */
    public ProgressRing setProgressType(@ProgressType int progressType) {
        this.mProgressType = progressType;
        return this;
    }

    /**
     * 设置进度方向
     *
     * @param progressDirection 进度方向
     * @return ProgressRing
     */
    public ProgressRing setProgressDirection(@ProgressDirection int progressDirection) {
        this.mProgressDirection = progressDirection;
        return this;
    }

    /**
     * 设置开始位置
     *
     * @param startPosition 开始位置
     * @return ProgressRing
     */
    public ProgressRing setStartPosition(@StartPosition int startPosition) {
        this.mStartPosition = startPosition;
        return this;
    }

    /**
     * 刷新
     */
    @AnyThread
    public void refresh() {
        ThreadUtil.runOnUiThread(() -> {
            invalidate();
            return null;
        });
    }

    @Override
    public void invalidate() {
        // 保证当前进度永远小于等于总进度
        if (mCurProgress > mTotalProgress) {
            mCurProgress = mTotalProgress;
        }
        super.invalidate();
    }

    /**
     * 进度类型 限制注解
     */
    @IntDef({ProgressType.PROGRESS_TYPE_PERCENT, ProgressType.PROGRESS_TYPE_VALUE})
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.LOCAL_VARIABLE})
    @interface ProgressType {

        /**
         * 进度类型: 百分比
         */
        int PROGRESS_TYPE_PERCENT = 0;

        /**
         * 进度类型: 值
         */
        int PROGRESS_TYPE_VALUE = 1;

    }

    /**
     * 进度方向 限制注解
     */
    @IntDef({ProgressDirection.PROGRESS_DIRECTION_CW, ProgressDirection.PROGRESS_DIRECTION_CCW})
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.LOCAL_VARIABLE})
    @interface ProgressDirection {

        /**
         * 进度方向: 顺时针
         */
        int PROGRESS_DIRECTION_CW = 0;

        /**
         * 进度方向: 逆时针
         */
        int PROGRESS_DIRECTION_CCW = 1;

    }

    /**
     * 起点位置 限制注解
     */
    @IntDef({
            StartPosition.START_POSITION_TOP,
            StartPosition.START_POSITION_BOTTOM,
            StartPosition.START_POSITION_LEFT,
            StartPosition.START_POSITION_RIGHT,
    })
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.LOCAL_VARIABLE})
    @interface StartPosition {

        /**
         * 起点位置: 顶部
         */
        int START_POSITION_TOP = 0;

        /**
         * 起点位置: 底部
         */
        int START_POSITION_BOTTOM = 1;

        /**
         * 起点位置: 左部
         */
        int START_POSITION_LEFT = 2;

        /**
         * 起点位置: 右部
         */
        int START_POSITION_RIGHT = 3;

    }

}
