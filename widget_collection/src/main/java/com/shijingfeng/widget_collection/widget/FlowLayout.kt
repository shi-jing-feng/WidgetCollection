package com.shijingfeng.widget_collection.widget

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.SparseIntArray
import android.view.ViewGroup
import androidx.core.util.forEach
import com.shijingfeng.widget_collection.R
import com.shijingfeng.widget_collection.annotation.define.FlowGravity
import com.shijingfeng.widget_collection.annotation.define.FlowGravity.*
import com.shijingfeng.widget_collection.annotation.define.FlowOrientation
import com.shijingfeng.widget_collection.annotation.define.FlowOrientation.FLOW_ORIENTATION_HORIZONTAL
import com.shijingfeng.widget_collection.annotation.define.FlowOrientation.FLOW_ORIENTATION_VERTICAL
import kotlin.math.max

/**
 * Function: 逐行排列 逐列排列 布局
 * Date: 2020/9/24 10:48
 * Description:
 * @author ShiJingFeng
 */
class FlowLayout @JvmOverloads constructor(
    /** Context环境  */
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    /** 行列方向 */
    @FlowOrientation private var mOrientation = FLOW_ORIENTATION_VERTICAL
    /** Child所处的位置 */
    @FlowGravity private var mGravity = FLOW_GRAVITY_CENTER
    /** 行间隔 (注: 会和 topMargin, bottomMargin 叠加) */
    private var mRawSpace = 0
    /** 列间隔 (注: 会和 leftMargin(getMarginStart()), rightMargin(getMarginEnd()) 叠加) */
    private var mColumnSpace = 0

    /**
     * Key: 行或列序号 ([FLOW_ORIENTATION_VERTICAL]: 行(从零开始)  [FLOW_ORIENTATION_HORIZONTAL]: 列(从零开始))
     * Value: 行列中的Child数量
     */
    private val mRankSparseArray = SparseIntArray()

    /**
     * Key: 行或列序号 ([FLOW_ORIENTATION_VERTICAL]: 行(从零开始)  [FLOW_ORIENTATION_HORIZONTAL]: 列(从零开始))
     * Value: 行列中的最大尺寸 ([FLOW_ORIENTATION_VERTICAL]: 行中最大高  [FLOW_ORIENTATION_HORIZONTAL]: 列中最大宽)
     */
    private val mRankMaxSizeSparseArray = SparseIntArray()

    init {
        context.obtainStyledAttributes(attrs, R.styleable.FlowLayout).run {
            mOrientation = getInt(R.styleable.FlowLayout_flowOrientation, FLOW_ORIENTATION_VERTICAL)
            mGravity = getInt(R.styleable.FlowLayout_flowGravity, FLOW_GRAVITY_CENTER)
            mRawSpace = getDimensionPixelSize(R.styleable.FlowLayout_flowRawSpace, 0)
            mColumnSpace = getDimensionPixelSize(R.styleable.FlowLayout_flowColumnSpace, 0)
            //一定要回收，否则会内存泄漏
            recycle()
        }
    }

    /**
     * 测量自身大小
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val orientation = mOrientation
        val rawSpace = mRawSpace
        val columnSpace = mColumnSpace

        val measureWidth = MeasureSpec.getSize(widthMeasureSpec)
        val measureHeight = MeasureSpec.getSize(heightMeasureSpec)
        val measureWidthMode = MeasureSpec.getMode(widthMeasureSpec)
        val measureHeightMode = MeasureSpec.getMode(heightMeasureSpec)

        val realWidth = measureWidth - paddingStart - paddingEnd
        val realHeight = measureHeight - paddingTop - paddingBottom

        var lineWidth = 0
        var lineHeight = 0
        var width = 0
        var height = 0
        val childCount = childCount
        // 当前行 或 当前列
        var curRank = 0
        // 当前行是不是第一行 或 当前列是不是第一列
        var firstRawOrColumn = true

        mRankSparseArray.clear()
        mRankMaxSizeSparseArray.clear()
        for (index in 0 until childCount) {
            val child = getChildAt(index).apply {
                measureChild(this, widthMeasureSpec, heightMeasureSpec)
            }
            val childLayoutParams = child.layoutParams
            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight
            var childWidthWithMargin = childWidth
            var childHeightWithMargin = childHeight

            if (childLayoutParams is MarginLayoutParams) {
                childWidthWithMargin += childLayoutParams.marginStart + childLayoutParams.marginEnd
                childHeightWithMargin += childLayoutParams.topMargin + childLayoutParams.bottomMargin
            }

            when (orientation) {
                // 行列方向: 逐行排列, 可上下滚动
                FLOW_ORIENTATION_VERTICAL -> {
                    if (index > 0 && lineWidth + childWidthWithMargin + columnSpace > realWidth) {
                        // 需要换行
                        // 处理上一行
                        width = max(lineWidth, width)
                        height += if (firstRawOrColumn) {
                            firstRawOrColumn = false
                            lineHeight
                        } else {
                            lineHeight + rawSpace
                        }
                        mRankMaxSizeSparseArray.put(curRank, lineHeight)
                        // 处理换行后的当前行
                        mRankSparseArray.put(++curRank, 1)
                        // 如果当前Child是最后一个则不加列间隔
                        lineWidth = if (index == childCount - 1) {
                            childWidthWithMargin
                        } else {
                            childWidthWithMargin + columnSpace
                        }
                        lineHeight = childHeightWithMargin
                    } else {
                        // 不需要换行
                        mRankSparseArray.put(curRank, mRankSparseArray[curRank] + 1)
                        // 如果当前Child是最后一个则不加列间隔
                        lineWidth += if (index == childCount - 1) {
                            childWidthWithMargin
                        } else {
                            childWidthWithMargin + columnSpace
                        }
                        lineHeight = max(childHeightWithMargin, lineHeight)
                    }
                    // 最后一个单独处理 (用于处理当前行)
                    if (index == childCount - 1) {
                        mRankMaxSizeSparseArray.put(curRank, lineHeight)
                        width = max(lineWidth, width)
                        height += if (firstRawOrColumn) {
                            lineHeight
                        } else {
                            lineHeight + rawSpace
                        }
                    }
                }
                // 行列方向: 逐列行列, 可左右滚动
                FLOW_ORIENTATION_HORIZONTAL -> {
                    if (index > 0 && lineHeight + childHeightWithMargin + rawSpace > realHeight) {
                        // 需要换列
                        // 处理上一列
                        width += if (firstRawOrColumn) {
                            firstRawOrColumn = false
                            lineWidth
                        } else {
                            lineWidth + rawSpace
                        }
                        height = max(lineHeight, height)
                        mRankMaxSizeSparseArray.put(curRank, lineWidth)
                        // 处理换列后的当前列
                        mRankSparseArray.put(++curRank, 1)
                        lineWidth = childWidthWithMargin
                        // 如果当前Child是最后一个则不加行间隔
                        lineHeight = if (index == childCount - 1) {
                            childHeightWithMargin
                        } else {
                            childHeightWithMargin + rawSpace
                        }
                    } else {
                        // 不需要换列
                        mRankSparseArray.put(curRank, mRankSparseArray[curRank] + 1)
                        lineWidth = max(childWidthWithMargin, lineWidth)
                        // 如果当前Child是最后一个则不加行间隔
                        lineHeight += if (index == childCount - 1) {
                            childHeightWithMargin
                        } else {
                            childHeightWithMargin + rawSpace
                        }
                    }
                    // 最后一列单独处理
                    if (index == childCount - 1) {
                        mRankMaxSizeSparseArray.put(curRank, lineWidth)
                        width += if (firstRawOrColumn) {
                            lineWidth
                        } else {
                            lineWidth + rawSpace
                        }
                        height = max(lineHeight, height)
                    }
                }
                else -> {}
            }
        }

        when (orientation) {
            // 行列方向: 逐行排列, 可上下滚动
            FLOW_ORIENTATION_VERTICAL -> {
                if (measureWidthMode == MeasureSpec.AT_MOST) {
                    width += (paddingStart + paddingEnd)
                }
                height += (paddingTop + paddingBottom)
            }
            // 行列方向: 逐列行列, 可左右滚动
            FLOW_ORIENTATION_HORIZONTAL -> {
                width += (paddingStart + paddingEnd)
                if (measureHeightMode == MeasureSpec.AT_MOST) {
                    height += (paddingTop + paddingBottom)
                }
            }
        }
        setMeasuredDimension(
            if (measureWidthMode == MeasureSpec.EXACTLY) measureWidth else width,
            if (measureHeightMode == MeasureSpec.EXACTLY) measureHeight else height
        )
    }

    /**
     * 对 Child View 进行布局
     */
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val orientation = mOrientation
        val gravity = mGravity
        val rawSpace = mRawSpace
        val columnSpace = mColumnSpace

        var lineWidth = 0
        var lineHeight = 0
        var top = paddingTop
        var left = paddingStart
        val width = measuredWidth - paddingStart - paddingEnd
        val height = measuredHeight - paddingTop - paddingBottom
        val childCount = childCount

        for (index in 0 until childCount) {
            val child = getChildAt(index)
            val layoutParams = child.layoutParams
            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight
            var childWidthWithMargin = childWidth
            var childHeightWithMargin = childHeight
            var marginStart = 0
            var marginEnd = 0
            var marginTop = 0
            var marginBottom = 0

            if (layoutParams is MarginLayoutParams) {
                marginStart = layoutParams.marginStart
                marginEnd = layoutParams.marginEnd
                marginTop = layoutParams.topMargin
                marginBottom = layoutParams.bottomMargin
                childWidthWithMargin += marginStart + marginEnd
                childHeightWithMargin += marginTop + marginBottom
            }

            var realLeft: Int
            var realRight: Int
            var realTop: Int
            var realBottom: Int
            val rankIndex = getRankByIndex(index)
            val maxSize = if (rankIndex != -1) {
                mRankMaxSizeSparseArray.get(rankIndex, 0)
            } else {
                0
            }

            when (orientation) {
                // 行列方向: 逐行排列, 可上下滚动
                FLOW_ORIENTATION_VERTICAL -> {
                    if (lineWidth + childWidthWithMargin + columnSpace > width) {
                        // 换行了
                        top += (lineHeight + rawSpace)
                        left = paddingStart
                        // 如果当前Child是最后一个则不加列间隔
                        lineWidth = if (index == childCount - 1) {
                            childWidthWithMargin
                        } else {
                            childWidthWithMargin + columnSpace
                        }
                        lineHeight = childHeightWithMargin
                    } else {
                        // 没有换行
                        // 如果当前Child是最后一个则不加列间隔
                        lineWidth += if (index == childCount - 1) {
                            childWidthWithMargin
                        } else {
                            childWidthWithMargin + columnSpace
                        }
                        lineHeight = max(lineHeight, childHeightWithMargin)
                    }

                    realLeft = left + marginStart
                    realRight = realLeft + childWidth
                    realTop = when (gravity) {
                        // 垂直居中
                        FLOW_GRAVITY_CENTER -> top + (maxSize - childHeight) / 2 + marginTop - marginBottom
                        // 垂直靠上
                        FLOW_GRAVITY_TOP -> top + marginTop
                        // 垂直靠下
                        FLOW_GRAVITY_BOTTOM -> top + (maxSize - childHeight) - marginBottom
                        // 默认垂直居中
                        else -> top + (maxSize - childHeight) / 2 + marginTop - marginBottom
                    }
                    realBottom = realTop + childHeight

                    // 如果当前Child是最后一个则不加列间隔
                    left += if (index == childCount - 1) {
                        childWidthWithMargin
                    } else {
                        childWidthWithMargin + columnSpace
                    }
                }
                // 行列方向: 逐列行列, 可左右滚动
                FLOW_ORIENTATION_HORIZONTAL -> {
                    if (lineHeight + childHeightWithMargin + rawSpace > height) {
                        // 换列了
                        top = paddingTop
                        left += (lineWidth + columnSpace)
                        lineWidth = childWidthWithMargin
                        // 如果当前Child是最后一个则不加行间隔
                        lineHeight = if (index == childCount - 1) {
                            childHeightWithMargin
                        } else {
                            childHeightWithMargin + rawSpace
                        }
                    } else {
                        // 没有换列
                        lineWidth = max(lineWidth, childWidthWithMargin)
                        // 如果当前Child是最后一个则不加行间隔
                        lineHeight += if (index == childCount - 1) {
                            childHeightWithMargin
                        } else {
                            childHeightWithMargin + rawSpace
                        }
                    }

                    realLeft = when (gravity) {
                        // 水平居中
                        FLOW_GRAVITY_CENTER -> left + (maxSize - childWidth) / 2 + marginStart - marginEnd
                        // 水平靠左
                        FLOW_GRAVITY_LEFT -> left + marginStart
                        // 水平靠右
                        FLOW_GRAVITY_RIGHT -> left + (maxSize - childWidth) - marginEnd
                        // 默认水平居中
                        else -> left + (maxSize - childWidth) / 2 + marginStart - marginEnd
                    }
                    realRight = realLeft + childWidth
                    realTop = top + marginTop
                    realBottom = realTop + childHeight

                    top += if (index == childCount - 1) {
                        childHeightWithMargin
                    } else {
                        childHeightWithMargin + rawSpace
                    }
                }
                else -> {
                    realLeft = 0
                    realRight = 0
                    realTop = 0
                    realBottom = 0
                }
            }
            child.layout(realLeft, realTop, realRight, realBottom)
        }
    }

    /**
     * 通过Child索引 获取 行或列 序号 (从零开始)
     *
     * @param index Child索引
     * @return 行或列 序号
     */
    private fun getRankByIndex(index: Int): Int {
        var sum = 0

        mRankSparseArray.forEach { key, value ->
            sum += value
            if (index < sum) {
                return key
            }
        }
        return -1
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

    /**
     * 获取 和 设置 行列方向
     */
    var orientation: Int
        @FlowOrientation get() = mOrientation
        set(@FlowOrientation orientation) {
            this.mOrientation = orientation
        }

    /**
     * 获取 和 设置 Child所处的位置
     */
    var gravity: Int
        @FlowGravity get() = mGravity
        set(@FlowGravity gravity) {
            this.mGravity = gravity
        }

    /**
     * 获取 和 设置 行间隔 (注: 会和 topMargin, bottomMargin 叠加)
     */
    var rawSpace: Int
        get() = mRawSpace
        set(rawSpace) {
            this.mRawSpace = rawSpace
        }

    /**
     * 列间隔 (注: 会和 leftMargin(getMarginStart()), rightMargin(getMarginEnd()) 叠加)
     */
    var columnSpace: Int
        get() = mColumnSpace
        set(columnSpace) {
            this.mColumnSpace = columnSpace
        }

}