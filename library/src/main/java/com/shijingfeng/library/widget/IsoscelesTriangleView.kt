package com.shijingfeng.library.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import com.shijingfeng.library.R
import com.shijingfeng.library.annotation.define.IsoscelesTriangleViewStyle
import com.shijingfeng.library.annotation.define.IsoscelesTriangleViewStyle.*

/**
 * Function: 三角形 View (用于下拉选择箭头)
 * Date: 2020/9/16 22:24
 * Description:
 * @author ShiJingFeng
 */
class IsoscelesTriangleView @JvmOverloads constructor(
    /** Context环境  */
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    /** 路径 */
    private val mPath = Path()
    /** 画笔 */
    private val mPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    /** 颜色 */
    @ColorInt
    private var mColor = Color.BLACK

    /** 样式 */
    @IsoscelesTriangleViewStyle
    private var mStyle = STYLE_TOP_TO_BOTTOM

    init {
        context.obtainStyledAttributes(attrs, R.styleable.IsoscelesTriangleView).run {
            mColor = getColor(R.styleable.IsoscelesTriangleView_color, Color.BLACK)
            mStyle = getInt(R.styleable.IsoscelesTriangleView_isosceles_triangle_style, STYLE_TOP_TO_BOTTOM)
            //一定要回收，否则会内存泄漏
            recycle()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val paddingStart = paddingStart
        val paddingEnd = paddingEnd
        val paddingTop = paddingTop
        val paddingBottom = paddingBottom
        val width = width
        val height = height

        var realWidth = (width - paddingStart - paddingEnd).toFloat()
        var realHeight = (height - paddingTop - paddingBottom).toFloat()

        if (realWidth < 0F) {
            realWidth = 0F
        }
        if (realHeight < 0F) {
            realHeight = 0F
        }

        val left = paddingStart.toFloat()
        val top = paddingTop.toFloat()
        val right = left + realWidth
        val bottom = top + realHeight

        when (mStyle) {
            // 样式: 以两腰之间的尖端开始，从顶部到底部 (垂直方向正三角形)
            STYLE_TOP_TO_BOTTOM -> {
                mPath.run {
                    reset()
                    moveTo((right - left) / 2F, top)
                    lineTo(right, bottom)
                    lineTo(left, bottom)
                    lineTo((right - left) / 2F, top)
                }
            }
            // 样式: 以两腰之间的尖端开始，从底部到顶部 (垂直方向倒三角形)
            STYLE_BOTTOM_TO_TOP -> {
                mPath.run {
                    reset()
                    moveTo(left, top)
                    lineTo(right, top)
                    lineTo((right - left) / 2F, bottom)
                    lineTo(left, top)
                }
            }
            // 样式: 以两腰之间的尖端开始，从左部到右部 (水平方向正三角形)
            STYLE_LEFT_TO_RIGHT -> {
                mPath.run {
                    reset()
                    moveTo(left, (bottom - top) / 2F)
                    lineTo(right, top)
                    lineTo(right, bottom)
                    lineTo(left, (bottom - top) / 2F)
                }
            }
            // 样式: 以两腰之间的尖端开始，从右部到左部 (水平方向倒三角形)
            STYLE_RIGHT_TO_LEFT -> {
                mPath.run {
                    reset()
                    moveTo(right, (bottom - top) / 2F)
                    lineTo(left, bottom)
                    lineTo(left, top)
                    lineTo(right, (bottom - top) / 2F)
                }
            }
        }
        canvas?.drawPath(mPath, mPaint.apply {
            color = mColor
        })
    }

    /**
     * 设置颜色 (ColoInt)
     */
    var color: Int
        @ColorInt get() = this.mColor
        set(@ColorInt color) {
            this.mColor = color
        }

    /**
     * 设置样式
     */
    var style: Int
        @IsoscelesTriangleViewStyle get() = this.mStyle
        set(@IsoscelesTriangleViewStyle style) {
            this.mStyle = style
        }

}