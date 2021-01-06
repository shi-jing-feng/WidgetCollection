package com.shijingfeng.library.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import com.shijingfeng.library.R
import com.shijingfeng.library.annotation.define.FlowGravity
import com.shijingfeng.library.annotation.define.FlowOrientation
import java.lang.IllegalArgumentException

/** 默认阴影颜色 */
private const val DEFAULT_SHADOW_COLOR = Color.GRAY
/** 默认阴影半径(px值) */
private const val DEFAULT_SHADOW_RADIUS = 10F
/** 默认阴影X轴偏移距离(px值), 大于0向右偏移, 小于0向左偏移, 等于0不偏移 */
private const val DEFAULT_SHADOW_OFFSET_X = 0F
/** 默认阴影Y轴偏移距离(px值), 大于0向下偏移, 小于0向上偏移, 等于0不偏移 */
private const val DEFAULT_SHADOW_OFFSET_Y = 0F

/** 默认圆角半径(px值) */
private const val DEFAULT_CORNER_RADIUS = 0F

/**
 * Function: 阴影卡片View
 * Date: 2021/1/6 20:42
 * Description:
 * @author ShiJingFeng
 */
class ShadowCardView @JvmOverloads constructor(
    /** Context环境  */
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    /** 画笔 */
    private val mPaint = Paint()

    /** 阴影颜色 */
    private var mShadowColor = DEFAULT_SHADOW_COLOR
    /** 阴影半径(px值) */
    private var mShadowRadius = DEFAULT_SHADOW_RADIUS
    /** 阴影X轴偏移距离(px值), 大于0向右偏移, 小于0向左偏移, 等于0不偏移 */
    private var mShadowOffsetX = DEFAULT_SHADOW_OFFSET_X
    /** 阴影Y轴偏移距离(px值), 大于0向下偏移, 小于0向上偏移, 等于0不偏移 */
    private var mShadowOffsetY = DEFAULT_SHADOW_OFFSET_Y

    /** 圆角半径(px值) */
    private var mCornerRadius = DEFAULT_CORNER_RADIUS

    /** 是否开启阴影  true:开启  false:关闭 */
    private var mEnableShadow = true

    /** 背景 */
    private var mBackground: Drawable? = null

    init {
        // setShadowLayer()函数只有文字绘制阴影支持硬件加速, 其他都不支持硬件加速, 故禁用硬件加速
        setLayerType(LAYER_TYPE_SOFTWARE, mPaint)
        context.obtainStyledAttributes(attrs, R.styleable.ShadowCardView).run {
            mShadowColor = getColor(R.styleable.ShadowCardView_shadowColor, DEFAULT_SHADOW_COLOR)
            mShadowRadius = getDimension(R.styleable.ShadowCardView_shadowRadius, DEFAULT_CORNER_RADIUS)
            mShadowOffsetX = getDimension(R.styleable.ShadowCardView_shadowOffsetX, DEFAULT_SHADOW_OFFSET_X)
            mShadowOffsetY = getDimension(R.styleable.ShadowCardView_shadowOffsetY, DEFAULT_SHADOW_OFFSET_Y)
            mCornerRadius = getDimension(R.styleable.ShadowCardView_cornerRadius, DEFAULT_CORNER_RADIUS)
            mEnableShadow = getBoolean(R.styleable.ShadowCardView_enableShadow, true)
            //一定要回收，否则会内存泄漏
            recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val measureWidth = MeasureSpec.getSize(widthMeasureSpec)
        val measureHeight = MeasureSpec.getSize(heightMeasureSpec)
        val measureWidthMode = MeasureSpec.getMode(widthMeasureSpec)
        val measureHeightMode = MeasureSpec.getMode(heightMeasureSpec)
        val childCount = childCount
        var width = 0
        var height = 0

        if (childCount > 0) {
            if (childCount > 1) {
                throw IllegalArgumentException("ShadowCardView只能有一个子View")
            }
            val child = getChildAt(0).apply {
                measureChild(this, widthMeasureSpec, heightMeasureSpec)
            }
            val childLayoutParams = child.layoutParams

            width = child.measuredWidth
            height = child.measuredHeight
            if (childLayoutParams is MarginLayoutParams) {
                width += (childLayoutParams.marginStart +  + childLayoutParams.marginEnd)
                height += (childLayoutParams.topMargin +  + childLayoutParams.bottomMargin)
            }
            if (mEnableShadow) {
                width += (mShadowRadius * 2).toInt()
                height += (mShadowRadius * 2).toInt()
            }
            if (measureWidthMode == MeasureSpec.AT_MOST) {
                width += (paddingStart + paddingEnd)
            }
            if (measureHeightMode == MeasureSpec.AT_MOST) {
                height += (paddingTop + paddingBottom)
            }
        }
        setMeasuredDimension(
            if (measureWidthMode == MeasureSpec.EXACTLY) measureWidth else width,
            if (measureHeightMode == MeasureSpec.EXACTLY) measureHeight else height
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (childCount > 0) {
            if (childCount > 1) {
                throw IllegalArgumentException("ShadowCardView只能有一个子View")
            }
            val child = getChildAt(0)
            val childLayoutParams = child.layoutParams

            var childLeft = paddingStart
            var childTop = paddingTop

            if (childLayoutParams is MarginLayoutParams) {
                childLeft += childLayoutParams.marginStart
                childTop += childLayoutParams.topMargin
            }
            if (mEnableShadow) {
                childLeft += mShadowRadius.toInt()
                childTop += mShadowRadius.toInt()
            }
            child.layout(childLeft, childTop, childLeft + child.measuredWidth, childTop + child.measuredHeight)
        } else {
            super.onLayout(changed, left, top, right, bottom)
        }
    }

    /**
     * 当它没有背景时直接调用的是dispatchDraw()方法, 而不会调用draw()方法。
     * 当它有背景的时候就调用draw()方法，而draw()方法里包含了dispatchDraw()方法的调用。
     * 因此要在ViewGroup上绘制东西的时候往往重写的是dispatchDraw()方法而不是onDraw()方法。
     */
    override fun dispatchDraw(canvas: Canvas?) {
        val width = width
        val height = height

        if (width == 0 || height == 0) {
            // 宽高只要有一个是0, 则不绘制阴影
            return
        }
        if (mEnableShadow) {
            mPaint.setShadowLayer(mShadowRadius, mShadowOffsetX, mShadowOffsetY, mShadowColor)
            canvas?.drawRoundRect(mShadowRadius, mShadowRadius, width - mShadowRadius, height - mShadowRadius, mCornerRadius, mCornerRadius, mPaint)
        } else {
            mPaint.clearShadowLayer()
            canvas?.drawRoundRect(0F, 0F, width.toFloat(), height.toFloat(), mCornerRadius, mCornerRadius, mPaint)
        }
        super.dispatchDraw(canvas)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

//    override fun setBackgroundDrawable(background: Drawable?) {
////        super.setBackgroundDrawable(background)
//        if (this.mBackground != background) {
//            this.mBackground = background
//            postInvalidate()
//        }
//    }
}