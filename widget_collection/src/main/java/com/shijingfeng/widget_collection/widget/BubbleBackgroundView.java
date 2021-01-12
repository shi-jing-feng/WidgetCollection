package com.shijingfeng.widget_collection.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

import com.shijingfeng.widget_collection.R;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static android.util.Log.e;
import static com.shijingfeng.widget_collection.widget.BubbleBackgroundView.Direction.BOTTOM;
import static com.shijingfeng.widget_collection.widget.BubbleBackgroundView.Direction.LEFT;
import static com.shijingfeng.widget_collection.widget.BubbleBackgroundView.Direction.RIGHT;
import static com.shijingfeng.widget_collection.widget.BubbleBackgroundView.Direction.TOP;

/**
 * Function:
 * Date: 2020/8/25 15:59
 * Description:
 *
 * @author ShiJingFeng
 */
public class BubbleBackgroundView extends FrameLayout {

    /** 默认箭头宽比例值 */
    private static final float DEFAULT_ARROW_WIDTH = 2F / 9F;
    /** 默认箭头高比例值 */
    private static final float DEFAULT_ARROW_HEIGHT = 2F / 9F;
    /** 默认圆角半径比例值 */
    private static final float DEFAULT_CORNER_RADIUS = 1F / 18F;
    /** 默认阴影半径比例值 */
    private static final float DEFAULT_SHADOW_RADIUS = 7F / 90F;

    private Paint mSpacePaint;
    private Path mPath;

    /** 箭头宽 */
    private float mArrowWidth = -1F;
    /** 箭头高 */
    private float mArrowHeight = -1F;
    /** 箭头方向 */
    private @Direction int mArrowDirection = TOP;
    /** 从背景空间坐标轴原点 (除去阴影距离和边框距离的坐标原点) 到 箭头中心点 的距离 (可能是水平距离，也可能是垂直距离，取决于箭头位置) */
    private float mArrowDistanceFromOrigin = -1F;
    /** 圆角半径 */
    private float mCornerRadius = -1F;
    /** 背景颜色 */
    private int mBgColor = Color.WHITE;

    /** 是否显示阴影  true: 显示  false: 不显示 */
    private boolean mShowShadow = false;
    /** 阴影半径 */
    private float mShadowRadius = -1F;
    /** 阴影水平偏移量 (正值向右偏移，负值向左偏移) */
    private float mShadowDx = 0F;
    /** 阴影垂直偏移量 (正值向下偏移，负值向上偏移) */
    private float mShadowDy = 0F;
    /** 阴影颜色 */
    private @ColorInt int mShadowColor = Color.parseColor("#EEEEEE");

    public BubbleBackgroundView(@NonNull Context context) {
        this(context, null);
    }

    public BubbleBackgroundView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BubbleBackgroundView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public BubbleBackgroundView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        mSpacePaint = new Paint();
        mSpacePaint.setStyle(Paint.Style.FILL);
        mSpacePaint.setAntiAlias(true);
        // 禁用硬件加速 (考虑到阴影的情况，但文本绘制阴影不用关闭硬件加速)
        setLayerType(LAYER_TYPE_SOFTWARE, mSpacePaint);

        mPath = new Path();

        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BubbleBackgroundView);

        mArrowWidth = typedArray.getDimension(R.styleable.BubbleBackgroundView_arrowWidth, -1F);
        mArrowHeight = typedArray.getDimension(R.styleable.BubbleBackgroundView_arrowHeight, -1F);
        mArrowDirection = typedArray.getInt(R.styleable.BubbleBackgroundView_arrowDirection, TOP);
        mArrowDistanceFromOrigin = typedArray.getDimension(R.styleable.BubbleBackgroundView_arrowDistanceFromOrigin, -1F);
        mCornerRadius = typedArray.getDimension(R.styleable.BubbleBackgroundView_cornerRadius, -1F);
        mBgColor = typedArray.getColor(R.styleable.BubbleBackgroundView_bgColor, Color.WHITE);

        mShowShadow = typedArray.getBoolean(R.styleable.BubbleBackgroundView_showShadow, false);
        mShadowRadius = typedArray.getDimension(R.styleable.BubbleBackgroundView_shadowRadius, -1F);
        mShadowDx = typedArray.getDimension(R.styleable.BubbleBackgroundView_shadowDx, 0F);
        mShadowDy = typedArray.getDimension(R.styleable.BubbleBackgroundView_shadowDy, 0F);
        mShadowColor = typedArray.getColor(R.styleable.BubbleBackgroundView_shadowColor, Color.parseColor("#EEEEEE"));
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int measureWidth = MeasureSpec.getSize(widthMeasureSpec);
        final int measureHeight = MeasureSpec.getSize(heightMeasureSpec);
        final int measureWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int measureHeightMode = MeasureSpec.getMode(heightMeasureSpec);

        final int paddingStart = getPaddingStart();
        final int paddingEnd = getPaddingEnd();
        final int paddingTop = getPaddingTop();
        final int paddingBottom = getPaddingBottom();

        int width = 0;
        int height = 0;

        final int childCount = getChildCount();

        for (int index = 0; index < childCount; ++index) {
            final View child = getChildAt(index);
            final LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();

            measureChild(child, widthMeasureSpec, heightMeasureSpec);

            int childWidthWithMargin = child.getMeasuredWidth();
            int childHeightWithMargin = child.getMeasuredHeight();

            if (layoutParams != null) {
                childWidthWithMargin += layoutParams.getMarginStart() + layoutParams.getMarginEnd();
                childHeightWithMargin += layoutParams.topMargin + layoutParams.bottomMargin;
            }

            width = Math.max(width, childWidthWithMargin);
            height = Math.max(height, childHeightWithMargin);
        }

        width += paddingStart + paddingEnd;
        height += paddingTop + paddingBottom;

        if (mCornerRadius < 0F) {
            mCornerRadius = DEFAULT_CORNER_RADIUS * Math.min(width, height);
        }
        if (mShadowRadius < 0F) {
            mShadowRadius = DEFAULT_SHADOW_RADIUS * Math.max(width, height);
        }

        // 阴影半径
        final int shadowRadius = (int) mShadowRadius;
        // 箭头方向
        final @Direction int arrowDirection = mArrowDirection;

        if (mShowShadow) {
            width += shadowRadius;
            height += shadowRadius;
        }
        switch (arrowDirection) {
            // 顶部
            case TOP:
            // 底部
            case BOTTOM:
                if (mArrowWidth < 0F) {
                    mArrowWidth = DEFAULT_ARROW_WIDTH * width;
                }
                if (mArrowHeight < 0F) {
                    mArrowHeight = DEFAULT_ARROW_HEIGHT * width;
                }
                height += mArrowHeight;
                break;
            // 左部
            case LEFT:
            // 右部
            case RIGHT:
                if (mArrowWidth < 0F) {
                    mArrowWidth = DEFAULT_ARROW_WIDTH * height;
                }
                if (mArrowHeight < 0F) {
                    mArrowHeight = DEFAULT_ARROW_HEIGHT * height;
                }
                width += mArrowHeight;
                break;
            default:
                break;
        }

        setMeasuredDimension(
            measureWidthMode == MeasureSpec.EXACTLY ? measureWidth : width,
            measureHeightMode == MeasureSpec.EXACTLY ? measureHeight : height
        );
    }

    @SuppressLint({"SwitchIntDef", "RtlHardcoded"})
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        // 箭头方向
        final @Direction int arrowDirection = mArrowDirection;
        // 箭头高度
        final int arrowHeight = (int) mArrowHeight;
        final int shadowRadius;

        if (mShowShadow) {
            shadowRadius = (int) mShadowRadius;
        } else {
            shadowRadius = 0;
        }

        final int paddingStart = getPaddingStart();
        final int paddingEnd = getPaddingEnd();
        final int paddingTop = getPaddingTop();
        final int paddingBottom = getPaddingBottom();

        int top = shadowRadius + paddingTop;
        final int bottom = b - t - paddingBottom;
        int left = shadowRadius + paddingStart;
        final int right = r - l - paddingEnd;

        final int childCount = getChildCount();

        if (arrowDirection == TOP) {
            // 顶部
            top += arrowHeight;
        } else if (arrowDirection == LEFT) {
            // 左部
            left += arrowHeight;
        }
        for (int index = 0; index < childCount; ++index) {
            final View child = getChildAt(index);
            final LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
            final int childWidth = child.getMeasuredWidth();
            final int childHeight = child.getMeasuredHeight();
            final int marginStart;
            final int marginEnd;
            final int marginTop;
            final int marginBottom;
            final int gravity;

            if (layoutParams != null) {
                marginStart = layoutParams.getMarginStart();
                marginEnd = layoutParams.getMarginEnd();
                marginTop = layoutParams.topMargin;
                marginBottom = layoutParams.bottomMargin;
                gravity = layoutParams.gravity;
            } else {
                marginStart = 0;
                marginEnd = 0;
                marginTop = 0;
                marginBottom = 0;
                gravity = Gravity.START | Gravity.TOP;
            }

            final int layoutDirection = getLayoutDirection();
            final int absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection);
            final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;
            int childLeft;
            int childTop;

            switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                case Gravity.CENTER_HORIZONTAL:
                    childLeft = left + (right - left - childWidth) / 2 + marginStart - marginEnd;
                    break;
                case Gravity.RIGHT:
                    childLeft = right - childWidth - marginEnd;
                    break;
                case Gravity.LEFT:
                default:
                    childLeft = left + marginStart;
            }

            switch (verticalGravity) {
                case Gravity.CENTER_VERTICAL:
                    childTop = top + (bottom - top - childHeight) / 2 + marginTop - marginBottom;
                    break;
                case Gravity.BOTTOM:
                    childTop = bottom - childHeight - marginBottom;
                    break;
                case Gravity.TOP:
                default:
                    childTop = top + marginTop;
            }

            child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
        }
    }

    /**
     * ViewGroup 会执行 dispatchDraw(Canvas) 而不执行 onDraw(Canvas)
     * @param canvas
     */
    @Override
    protected void dispatchDraw(Canvas canvas) {
        // 测试
//        mArrowWidth = 0;
//        mArrowHeight = 0;
//        mArrowDirection = TOP;
//        mArrowDistanceFromOrigin = dp2px(40F);
//        mCornerRadius = dp2px(10F);
//        mBgColor = Color.RED;
//        mShowShadow = true;
        final float width = getWidth();
        final float height = getHeight();

        float top = 0F;
        float bottom = top + height;
        float left = 0F;
        float right = left + width;

        final float arrowHeight = mArrowHeight;
        final float arrowWidth = mArrowWidth;
        final @Direction int arrowDirection = mArrowDirection;
        final @ColorInt int bgColor = mBgColor;
        final float cornerRadius = mCornerRadius;

        float arrowDistanceFromOrigin = mArrowDistanceFromOrigin;

        if (mArrowDistanceFromOrigin < 0) {
            if (arrowDirection == TOP || arrowDirection == BOTTOM) {
                arrowDistanceFromOrigin = width / 2F;
            } else {
                arrowDistanceFromOrigin = height / 2F;
            }
        }

        mSpacePaint.setColor(bgColor);
        if (mShowShadow) {
            // 设置阴影
            mSpacePaint.setShadowLayer(mShadowRadius, mShadowDx, mShadowDy, mShadowColor);
            // 计算去除阴影所占的面积后的剩余空间矩形
            top = top + mShadowRadius;
            bottom = bottom - mShadowRadius;
            left = left + mShadowRadius;
            right = right - mShadowRadius;
            if (mArrowDistanceFromOrigin < 0) {
                arrowDistanceFromOrigin = arrowDistanceFromOrigin - mShadowRadius;
            }
        } else {
            // 清除阴影
            mSpacePaint.clearShadowLayer();
        }

        mPath.reset();
        switch (arrowDirection) {
            // 顶部
            case TOP:
                // 从箭头顶部开始，顺时针绘制
                mPath.moveTo(left + arrowDistanceFromOrigin, top);
                mPath.lineTo(left + arrowDistanceFromOrigin + arrowWidth / 2F, arrowHeight + top);
                mPath.lineTo(right - cornerRadius, top + arrowHeight);
                mPath.arcTo(right - 2F * cornerRadius, top + arrowHeight, right, top + arrowHeight + 2F * cornerRadius, 270F, 90F, false);
                mPath.lineTo(right, bottom - cornerRadius);
                mPath.arcTo(right - 2F * cornerRadius, bottom - 2F * cornerRadius, right, bottom, 0F, 90F, false);
                mPath.lineTo(left + cornerRadius, bottom);
                mPath.arcTo(left, bottom - 2F * cornerRadius, left + 2F * cornerRadius, bottom, 90F, 90F, false);
                mPath.lineTo(left, top + arrowHeight + cornerRadius);
                mPath.arcTo(left, top + arrowHeight, top + 2F * cornerRadius, top + arrowHeight + 2F * cornerRadius, 180F, 90F, false);
                mPath.lineTo(left + arrowDistanceFromOrigin - arrowWidth / 2F, top + arrowHeight);
                mPath.lineTo(left + arrowDistanceFromOrigin, top);
                break;
            // 底部
            case BOTTOM:
                // 从箭头顶部开始，顺时针绘制
                mPath.moveTo(left + arrowDistanceFromOrigin, bottom);
                mPath.lineTo(left + arrowDistanceFromOrigin - arrowWidth / 2F, bottom - arrowHeight);
                mPath.lineTo(left + cornerRadius, bottom - arrowHeight);
                mPath.arcTo(left, bottom - arrowHeight - 2F * cornerRadius, left + 2F * cornerRadius, bottom - arrowHeight, 90F, 90F, false);
                mPath.lineTo(left, top + cornerRadius);
                mPath.arcTo(left, top, left + 2F * cornerRadius, top + 2F * cornerRadius, 180F, 90F, false);
                mPath.lineTo(right - cornerRadius, top);
                mPath.arcTo(right - 2F * cornerRadius, top, right, top + 2F * cornerRadius, 270F, 90F, false);
                mPath.lineTo(right, bottom - arrowHeight - cornerRadius);
                mPath.arcTo(right - 2F * cornerRadius, bottom - arrowHeight - 2F * cornerRadius, right, bottom - arrowHeight, 0F, 90F, false);
                mPath.lineTo(left + arrowDistanceFromOrigin + arrowWidth / 2F, bottom - arrowHeight);
                mPath.lineTo(left + arrowDistanceFromOrigin, bottom);
                break;
            // 左部
            case LEFT:
                // 从箭头顶部开始，顺时针绘制
                mPath.moveTo(left, top + arrowDistanceFromOrigin);
                mPath.lineTo(left + arrowHeight, top + arrowDistanceFromOrigin - arrowWidth / 2F);
                mPath.lineTo(left + arrowHeight, top + cornerRadius);
                mPath.arcTo(left + arrowHeight, top, left + arrowHeight + 2F * cornerRadius, top + 2F * cornerRadius, 180F, 90F, false);
                mPath.lineTo(right - cornerRadius, top);
                mPath.arcTo(right - 2F * cornerRadius, top, right, top + 2F * cornerRadius, 270F, 90F, false);
                mPath.lineTo(right, bottom - cornerRadius);
                mPath.arcTo(right - 2F * cornerRadius, bottom - 2F * cornerRadius, right, bottom, 0F, 90F, false);
                mPath.lineTo(left + arrowHeight + cornerRadius, bottom);
                mPath.arcTo(left + arrowHeight, bottom - 2F * cornerRadius, left + arrowHeight + 2F * cornerRadius, bottom, 90F, 90F, false);
                mPath.lineTo(left + arrowHeight, top + arrowDistanceFromOrigin + arrowWidth / 2F);
                mPath.lineTo(left, top + arrowDistanceFromOrigin);
                break;
            // 右部
            case RIGHT:
                // 从箭头顶部开始，顺时针绘制
                mPath.moveTo(right, top + arrowDistanceFromOrigin);
                mPath.lineTo(right - arrowHeight, top + arrowDistanceFromOrigin + arrowWidth / 2F);
                mPath.lineTo(right - arrowHeight, bottom - cornerRadius);
                mPath.arcTo(right - arrowHeight - 2F * cornerRadius, bottom - 2F * cornerRadius, right - arrowHeight, bottom, 0F, 90F, false);
                mPath.lineTo(left + cornerRadius, bottom);
                mPath.arcTo(left, bottom - 2F * cornerRadius, left + 2F * cornerRadius, bottom, 90F, 90F, false);
                mPath.lineTo(left, top + cornerRadius);
                mPath.arcTo(left, top, left + 2F * cornerRadius, top + 2F * cornerRadius, 180F, 90F, false);
                mPath.lineTo(right - arrowHeight - cornerRadius, top);
                mPath.arcTo(right - arrowHeight - 2F * cornerRadius, top, right - arrowHeight, top + 2F * cornerRadius, 270F, 90F, false);
                mPath.lineTo(right - arrowHeight, top + arrowDistanceFromOrigin - arrowWidth / 2F);
                mPath.lineTo(right, top + arrowDistanceFromOrigin);
                break;
            default:
                break;
        }
        canvas.drawPath(mPath, mSpacePaint);

        super.dispatchDraw(canvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    /**
     * 设置箭头宽
     * @param arrowWidth 箭头宽
     * @return BubbleBackgroundView
     */
    public BubbleBackgroundView setArrowWidth(float arrowWidth) {
        this.mArrowWidth = arrowWidth;
        return this;
    }

    /**
     * 设置箭头高
     * @param arrowHeight 箭头高
     * @return BubbleBackgroundView
     */
    public BubbleBackgroundView setArrowHeight(float arrowHeight) {
        this.mArrowHeight = arrowHeight;
        return this;
    }

    /**
     * 设置箭头方向
     * @param direction 方向
     * @return BubbleBackgroundView
     */
    public BubbleBackgroundView setArrowDirection(@Direction int direction) {
        this.mArrowDirection = direction;
        return this;
    }

    /**
     * 设置 从控件坐标轴原点 (包括阴影距离) 到 箭头中心点 的距离,
     * 可能是水平距离，也可能是垂直距离，取决于箭头位置
     * @param distance 距离
     * @return BubbleBackgroundView
     */
    public BubbleBackgroundView setArrowDistanceFromOrigin(float distance) {
        this.mArrowDistanceFromOrigin = distance;
        return this;
    }

    /**
     * 设置圆角半径
     * @param cornerRadius 圆角半径
     * @return BubbleBackgroundView
     */
    public BubbleBackgroundView setCornerRadius(float cornerRadius) {
        this.mCornerRadius = cornerRadius;
        return this;
    }

    /**
     * 设置背景颜色
     * @param bgColor 背景颜色
     * @return BubbleBackgroundView
     */
    public BubbleBackgroundView setBgColor(@ColorInt int bgColor) {
        this.mBgColor = bgColor;
        return this;
    }

    /**
     * 是否显示阴影
     * @param show  true: 显示  false: 不显示
     * @return BubbleBackgroundView
     */
    public BubbleBackgroundView showShadow(boolean show) {
        this.mShowShadow = show;
        return this;
    }

    /**
     * 设置 阴影半径
     * @param radius 阴影半径
     * @return BubbleBackgroundView
     */
    public BubbleBackgroundView setShadowRadius(@FloatRange(from = 0F, to = Float.MAX_VALUE) float radius) {
        this.mShadowRadius = radius;
        return this;
    }

    /**
     * 设置 阴影水平偏移量 (正值向右偏移，负值向左偏移)
     * @param dx 阴影水平偏移量 (正值向右偏移，负值向左偏移)
     * @return BubbleBackgroundView
     */
    public BubbleBackgroundView setShadowX(float dx) {
        this.mShadowDx = dx;
        return this;
    }

    /**
     * 设置 阴影垂直偏移量 (正值向下偏移，负值向上偏移)
     * @param dy 阴影垂直偏移量 (正值向下偏移，负值向上偏移)
     * @return BubbleBackgroundView
     */
    public BubbleBackgroundView setShadowY(float dy) {
        this.mShadowDy = dy;
        return this;
    }

    /**
     * 设置 阴影颜色
     * @param color 阴影颜色
     * @return BubbleBackgroundView
     */
    public BubbleBackgroundView setShadowColor(@ColorInt int color) {
        this.mShadowColor = color;
        return this;
    }

    /**
     * 方向类型 限制注解
     */
    @IntDef({TOP, BOTTOM, LEFT, RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.LOCAL_VARIABLE})
    @interface Direction {

        /** 顶部 */
        int TOP = 0;
        /** 底部 */
        int BOTTOM = 1;
        /** 左部 */
        int LEFT = 2;
        /** 右部 */
        int RIGHT = 3;

    }


}
