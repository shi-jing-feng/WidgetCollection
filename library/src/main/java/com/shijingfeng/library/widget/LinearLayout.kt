package com.shijingfeng.library.widget

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View.MeasureSpec.EXACTLY
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import kotlin.math.max

/**
 * Function: 线性布局
 * Date: 2020/9/23 16:57
 * Description:
 * @author ShiJingFeng
 */
class LinearLayout @JvmOverloads constructor(
    /** Context环境  */
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val measureWidth = MeasureSpec.getSize(widthMeasureSpec)
        val measureHeight = MeasureSpec.getSize(heightMeasureSpec)
        val measureWidthMode = MeasureSpec.getMode(widthMeasureSpec)
        val measureHeightMode = MeasureSpec.getMode(heightMeasureSpec)

        var height = 0
        var width = 0
        val count= childCount

        for (i in 0 until count) {
            // 测量子控件
            val child = getChildAt(i).apply {
                measureChild(this , widthMeasureSpec , heightMeasureSpec)
            }
            val layoutParams = child.layoutParams
            // 获得子控件的宽度和高度
            var childWidth = child.measuredWidth
            var childHeight = child.measuredHeight

            if (layoutParams is MarginLayoutParams) {
                childWidth += layoutParams.marginStart + layoutParams.marginEnd
                childHeight += layoutParams.topMargin + layoutParams.bottomMargin
            }

            // 得到最大宽度，并且累加高度
            width = max(childWidth, width)
            height += childHeight
        }
        setMeasuredDimension(
            if (measureWidthMode == EXACTLY) measuredWidth else width,
            if (measureHeightMode == EXACTLY) measureHeight else height
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var top = 0
        val count = childCount
        val parentWidth = r - l

        for (i in 0 until count) {
            val child = getChildAt(i)
            val layoutParams = child.layoutParams
            val childWidth = child.measuredWidthAndState
            val childHeight = child.measuredHeight
            var marginStart = 0
            var marginEnd = 0
            var marginTop = 0
            var marginBottom = 0

            if (layoutParams is MarginLayoutParams) {
                marginStart = layoutParams.marginStart
                marginEnd = layoutParams.marginEnd
                marginTop = layoutParams.topMargin
                marginBottom = layoutParams.bottomMargin
            }

            val right = if (childWidth == MATCH_PARENT || childWidth > parentWidth - marginStart - marginEnd) {
                parentWidth - marginEnd
            } else {
                marginStart + childWidth
            }

            child.layout(marginStart, top + marginTop, right, top + marginTop + childHeight)
            top += childHeight + marginTop + marginBottom
        }
    }

    /**
     * 当它没有背景时直接调用的是dispatchDraw()方法, 而不会调用draw()方法。
     * 当它有背景的时候就调用draw()方法，而draw()方法里包含了dispatchDraw()方法的调用。
     * 因此要在ViewGroup上绘制东西的时候往往重写的是dispatchDraw()方法而不是onDraw()方法。
     */
    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

    override fun generateLayoutParams(attrs: AttributeSet?) = MarginLayoutParams(context, attrs)

    override fun generateLayoutParams(p: LayoutParams?) = MarginLayoutParams(p)

    override fun generateDefaultLayoutParams() = MarginLayoutParams(
        LayoutParams.WRAP_CONTENT,
        LayoutParams.WRAP_CONTENT
    )
}