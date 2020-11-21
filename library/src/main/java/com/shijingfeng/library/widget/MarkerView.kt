package com.shijingfeng.library.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log.e
import android.view.View
import androidx.annotation.AnyThread
import androidx.annotation.ColorInt
import androidx.core.content.res.getDimensionOrThrow
import com.shijingfeng.library.R
import com.shijingfeng.library.annotation.define.MarkerViewPosition
import com.shijingfeng.library.annotation.define.MarkerViewPosition.*
import com.shijingfeng.library.annotation.define.MarkerViewStyle
import com.shijingfeng.library.annotation.define.MarkerViewStyle.*
import com.shijingfeng.library.util.dp2px
import com.shijingfeng.library.util.runOnUiThread
import com.shijingfeng.library.util.sp2px
import kotlin.math.pow
import kotlin.math.sqrt

/** 默认文本大小比例值 */
private const val DEFAULT_TEXT_SIZE_SCALE_VALUE = 42F / 179F
/** 默认圆角大小比例值 */
private const val DEFAULT_CORNER_RADIUS_SCALE_VALUE = 27F / 179F
/** 默认缺失的三角形(整体的三角形缩小版)的腰长(等腰三角形)大小比例值 */
private const val DEFAULT_MISSING_TRIANGLE_WAIST_LENGTH_SCALE_VALUE = 27F / 179F

/**
 * Function: 三角标记 View
 * Date: 2020/8/13 17:54
 * Description:
 * @author ShiJingFeng
 */
class MarkerView @JvmOverloads constructor(
    /** Context环境  */
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val mBgPath = Path()
    private val mBgPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    private val mTextPaint = Paint().apply {
        isAntiAlias = true
    }

    /** 辅助线 (测试用) */
//    private val mLinePath = Path()
//    private val mLinePaint = Paint().apply {
//        isAntiAlias = true
//        color = Color.WHITE
//        style = Paint.Style.STROKE
//        // 默认 1dp
//        strokeWidth = dp2px(1F).toFloat()
//    }

    /** 文本 (默认空字符串) */
    private var mText = ""

    /** 文本大小 px值 */
    private var mTextSize = -1F
    /** 是否自定义文本大小  true:自定义  false:默认 */
    private var mCustomTextSize = false

    /** 文本颜色 (默认纯白色) */
    @ColorInt
    private var mTextColor = Color.WHITE

    /** 三角背景颜色 (默认纯红色) */
    @ColorInt
    private var mBgColor = Color.RED

    /** 圆角半径 px值 (在圆角三角形样式下有效) */
    private var mCornerRadius = -1F
    /** 是否自定义圆角半径  true:自定义  false:默认 */
    private var mCustomCornerRadius = false

    /** 缺失的三角形(整体的三角形缩小版)的腰长 px值(等腰三角形) (在缺失三角形样式下有效) */
    private var mMissingTriangleWaistLength = -1F
    /** 是否自定义缺失的三角形的腰长  true:自定义  false:默认 */
    private var mCustomMissingTriangleWaistLength = false

    /** 纵轴偏移距离 (小于0: 向上偏移  大于0: 向下偏移) */
    private var mOffset = 0F

    /** 控件位置 (默认左上角样式) */
    @MarkerViewPosition
    private var mPosition = POSITION_LEFT_TOP

    /** 控件样式 (默认三角形) */
    @MarkerViewStyle
    private var mStyle = STYLE_TRIANGLE

    init {
        context.obtainStyledAttributes(attrs, R.styleable.MarkerView).run {
            mText = getString(R.styleable.MarkerView_text) ?: ""

            try {
                mTextSize = getDimensionOrThrow(R.styleable.MarkerView_textSize)
                mCustomTextSize = true
            } catch (e: IllegalArgumentException) {
                mCustomTextSize = false
            }

            mTextColor = getColor(R.styleable.MarkerView_textColor, Color.WHITE)
            mBgColor = getColor(R.styleable.MarkerView_bgColor, Color.RED)

            try {
                mCornerRadius = getDimensionOrThrow(R.styleable.MarkerView_cornerRadius)
                mCustomCornerRadius = true
            } catch (e: IllegalArgumentException) {
                mCustomCornerRadius = false
            }

            try {
                mMissingTriangleWaistLength = getDimensionOrThrow(R.styleable.MarkerView_missing_triangle_waist_length)
                mCustomMissingTriangleWaistLength = true
            } catch (e: IllegalArgumentException) {
                mCustomMissingTriangleWaistLength = false
            }

            mOffset = getDimension(R.styleable.MarkerView_offset, 0F)
            mPosition = getInt(R.styleable.MarkerView_position, POSITION_LEFT_TOP)
            mStyle = getInt(R.styleable.MarkerView_marker_view_style, STYLE_TRIANGLE)
            //一定要回收，否则会内存泄漏
            recycle()
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val width = width.toFloat()
        val height = height.toFloat()
        val size = if (width > height) height else width

        if (!mCustomTextSize) {
            mTextSize = size * DEFAULT_TEXT_SIZE_SCALE_VALUE
        }
        if (!mCustomCornerRadius) {
            mCornerRadius = size * DEFAULT_CORNER_RADIUS_SCALE_VALUE
        }
        if (!mCustomMissingTriangleWaistLength) {
            mMissingTriangleWaistLength = size * DEFAULT_MISSING_TRIANGLE_WAIST_LENGTH_SCALE_VALUE
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        // 测试使用
//        mPosition = POSITION_RIGHT_BOTTOM
//        mTextSize = sp2px(100F).toFloat()
//        mTextColor = Color.BLACK
//        mText = "重要"
//        mStyle = STYLE_TRIANGLE
//        mCornerRadius = dp2px(100F).toFloat()
//        mMissingTriangleWaistLength = dp2px(100F).toFloat()
////        mOffset = dp2px(20F).toFloat()
//        val paddingTop = dp2px(50F).toFloat()
//        val paddingBottom = dp2px(50F).toFloat()
//        val paddingStart = dp2px(50F).toFloat()
//        val paddingEnd = dp2px(50F).toFloat()

        val paddingTop = paddingTop.toFloat()
        val paddingBottom = paddingBottom.toFloat()
        val paddingStart = paddingStart.toFloat()
        val paddingEnd = paddingEnd.toFloat()

        val totalWidth = if (width != 0) width else dp2px(30F)
        val totalHeight = if (height != 0) height else dp2px(30F)
        val totalSize = if (totalWidth > totalHeight) totalHeight.toFloat() else totalWidth.toFloat()
        val realWidth = if (totalWidth - paddingStart - paddingEnd < 0) 0F else totalWidth - paddingStart - paddingEnd
        val realHeight = if (totalHeight - paddingTop - paddingBottom < 0) 0F else totalHeight - paddingTop - paddingBottom
        val realSize = if (realWidth > realHeight) realHeight else realWidth

        val top = paddingTop
        val bottom = top + realSize
        val left = paddingStart
        val right = left + realSize

        val cornerRadius = mCornerRadius
        val missingTriangleWaistLength = mMissingTriangleWaistLength
        val offset = mOffset

        // 绘制三角形背景
        mBgPaint.run {
            color = mBgColor
        }
        mBgPath.run {
            reset()
            when (mPosition) {
                // 左上角
                POSITION_LEFT_TOP -> {
                    when (mStyle) {
                        // 等腰三角形
                        STYLE_TRIANGLE -> {
                            moveTo(left, top)
                            lineTo(right, top)
                            lineTo(left, bottom)
                            lineTo(left, top)
                        }
                        // 圆角等腰三角形
                        STYLE_CORNER_TRIANGLE -> {
                            moveTo(left + cornerRadius, top)
                            lineTo(right, top)
                            lineTo(left, bottom)
                            lineTo(left, top + cornerRadius)
                            arcTo(
                                left,
                                top,
                                left + 2F * cornerRadius,
                                top + 2F * cornerRadius,
                                180F,
                                90F,
                                false
                            )
                        }
                        // 缺失的三角形 (看起来像旋转一定角度的梯形, 缺失的三角形部分是整体的缩小版)
                        STYLE_MISSING_TRIANGLE -> {
                            moveTo(left + missingTriangleWaistLength, top)
                            lineTo(right, top)
                            lineTo(left, bottom)
                            lineTo(left, top + missingTriangleWaistLength)
                            lineTo(left + missingTriangleWaistLength, top)
                        }
                        else -> {
                        }
                    }
                }
                // 左下角
                POSITION_LEFT_BOTTOM -> {
                    when (mStyle) {
                        // 等腰三角形
                        STYLE_TRIANGLE -> {
                            moveTo(left, top)
                            lineTo(right, bottom)
                            lineTo(left, bottom)
                            lineTo(left, top)
                        }
                        // 圆角等腰三角形
                        STYLE_CORNER_TRIANGLE -> {
                            moveTo(left, top)
                            lineTo(right, bottom)
                            lineTo(left + cornerRadius, bottom)
                            arcTo(
                                left,
                                bottom - 2F * cornerRadius,
                                left + 2F * cornerRadius,
                                bottom,
                                90F,
                                90F,
                                false
                            )
                            lineTo(left, top)
                        }
                        // 缺失的三角形 (看起来像旋转一定角度的梯形, 缺失的三角形部分是整体的缩小版)
                        STYLE_MISSING_TRIANGLE -> {
                            moveTo(left, top)
                            lineTo(right, bottom)
                            lineTo(left + missingTriangleWaistLength, bottom)
                            lineTo(left, bottom - missingTriangleWaistLength)
                            lineTo(left, top)
                        }
                        else -> {
                        }
                    }
                }
                // 右上角
                POSITION_RIGHT_TOP -> {
                    when (mStyle) {
                        // 等腰三角形
                        STYLE_TRIANGLE -> {
                            moveTo(left, top)
                            lineTo(right, top)
                            lineTo(right, bottom)
                            lineTo(left, top)
                        }
                        // 圆角等腰三角形
                        STYLE_CORNER_TRIANGLE -> {
                            moveTo(left, top)
                            lineTo(right - cornerRadius, top)
                            arcTo(
                                right - 2F * cornerRadius,
                                top,
                                right,
                                top + 2F * cornerRadius,
                                270F,
                                90F,
                                false
                            )
                            lineTo(right, bottom)
                            lineTo(left, top)
                        }
                        // 缺失的三角形 (看起来像旋转一定角度的梯形, 缺失的三角形部分是整体的缩小版)
                        STYLE_MISSING_TRIANGLE -> {
                            moveTo(left, top)
                            lineTo(right - missingTriangleWaistLength, top)
                            lineTo(right, top + missingTriangleWaistLength)
                            lineTo(right, bottom)
                            lineTo(left, top)
                        }
                        else -> {
                        }
                    }
                }
                // 右下角
                POSITION_RIGHT_BOTTOM -> {
                    when (mStyle) {
                        // 等腰三角形
                        STYLE_TRIANGLE -> {
                            moveTo(right, top)
                            lineTo(right, bottom)
                            lineTo(left, bottom)
                            lineTo(right, top)
                        }
                        // 圆角等腰三角形
                        STYLE_CORNER_TRIANGLE -> {
                            moveTo(right, top)
                            lineTo(right, bottom - cornerRadius)
                            arcTo(
                                right - 2F * cornerRadius,
                                bottom - 2F * cornerRadius,
                                right,
                                bottom,
                                0F,
                                90F,
                                false
                            )
                            lineTo(left, bottom)
                            lineTo(right, top)
                        }
                        // 缺失的三角形 (看起来像旋转一定角度的梯形, 缺失的三角形部分是整体的缩小版)
                        STYLE_MISSING_TRIANGLE -> {
                            moveTo(right, top)
                            lineTo(right, bottom - missingTriangleWaistLength)
                            lineTo(right - missingTriangleWaistLength, bottom)
                            lineTo(left, bottom)
                            lineTo(right, top)
                        }
                        else -> {
                        }
                    }
                }
                else -> {
                }
            }
        }
        canvas?.drawPath(mBgPath, mBgPaint)

        mTextPaint.run {
            color = mTextColor
            textSize = mTextSize
        }

        val text = mText
        val textWidth = mTextPaint.measureText(text)
        val textHeight = measureTextHeight(mTextPaint)
        val midlineLength = sqrt(1F / 2F) * realSize
        // 去掉Padding后 等腰三角形底边 (等腰三角形最长斜边)
        val realHemlineLength = sqrt(2F) * realSize
        // 去掉Padding后 等腰三角形腰长 (两个相等的边长称为腰长)
        val realWaistLength = realSize
        // 去掉Padding后 等腰三角形中线长度 (连接一个顶点与它对边中点的线段,叫做三角形的中线)
        val realMidlineLength = sqrt(1F / 2F) * realSize
        // 去掉Padding后 等腰三角形中位线 (连接三角形两边中点的线段叫做三角形的中位线)
        val realMiddleLineLength = realHemlineLength / 2F

        // 绘制文字
        // 旋转画布后, 坐标系也随之旋转, 旋转角度规则: 以第一象限X轴为0度, 顺时针旋转方向为角度增加方向
        when (mPosition) {
            // 左上角
            POSITION_LEFT_TOP -> {
                val textX = -(textWidth / 2F)
                // 以三角形的中线1/4处为水平基准 (中线起点: 最大边中间位置)
                val textY =
                    sqrt(paddingStart.pow(2) + paddingTop.pow(2)) + realMidlineLength * 3F / 4F + offset

                // 辅助线 (测试用)
//                mLinePath.reset()
//                mLinePath.moveTo(left, top)
//                mLinePath.lineTo(left + realSize / 2F, top + realSize / 2F)
//                mLinePath.moveTo(left, top + realSize / 4F)
//                mLinePath.lineTo(left + realSize / 4F, top)
//                mLinePath.moveTo(left, top + realSize * 2F / 4F)
//                mLinePath.lineTo(left + realSize * 2F / 4F, top)
//                mLinePath.moveTo(left, top + realSize * 3F / 4F)
//                mLinePath.lineTo(left + realSize * 3F / 4F, top)
//                mLinePaint.color = Color.WHITE
//                canvas?.drawPath(mLinePath, mLinePaint)

                canvas?.rotate(-45F)
                canvas?.drawText(text, textX, textY, mTextPaint)
            }
            // 左下角
            POSITION_LEFT_BOTTOM -> {
                val textX = sqrt(1F / 2F) * totalSize - textWidth / 2F
                // 以三角形的中线1/2处为水平基准 (中线起点: 最大边中间位置)
                val textY = realMidlineLength / 2F + offset

                // 辅助线 (测试用)
//                mLinePath.reset()
//                mLinePath.moveTo(left, bottom)
//                mLinePath.lineTo(left + realSize / 2F, top + realSize / 2F)
//                mLinePath.moveTo(left, top + realSize / 4F)
//                mLinePath.lineTo(right - realSize / 4F, bottom)
//                mLinePath.moveTo(left, top + realSize * 2F / 4F)
//                mLinePath.lineTo(right - realSize * 2F / 4F, bottom)
//                mLinePath.moveTo(left, top + realSize * 3F / 4F)
//                mLinePath.lineTo(right - realSize * 3F / 4F, bottom)
//                mLinePaint.color = Color.WHITE
//                canvas?.drawPath(mLinePath, mLinePaint)

                canvas?.rotate(45F)
                canvas?.drawText(text, textX, textY, mTextPaint)
            }
            // 右上角
            POSITION_RIGHT_TOP -> {
                val textX = sqrt(1F / 2F) * totalSize - textWidth / 2F
                // 以三角形的中线1/4处为水平基准 (中线起点: 最大边中间位置)
                val textY = -(realMidlineLength / 4F) + offset

                // 辅助线 (测试用)
//                mLinePath.reset()
//                mLinePath.moveTo(right, top)
//                mLinePath.lineTo(right - realSize / 2F, bottom - realSize / 2F)
//                mLinePath.moveTo(right - realSize / 4F, top)
//                mLinePath.lineTo(right, top + realSize / 4F)
//                mLinePath.moveTo(right - realSize * 2F / 4F, top)
//                mLinePath.lineTo(right, top + realSize * 2F / 4F)
//                mLinePath.moveTo(right - realSize * 3F / 4F, top)
//                mLinePath.lineTo(right, top + realSize * 3F / 4F)
//                mLinePaint.color = Color.WHITE
//                canvas?.drawPath(mLinePath, mLinePaint)

                canvas?.rotate(45F)
                canvas?.drawText(text, textX, textY, mTextPaint)
            }
            // 右下角
            POSITION_RIGHT_BOTTOM -> {
                val textX = -(textWidth / 2F)
                // 以三角形的中线1/2处为水平基准 (中线起点: 最大边中间位置)
                val textY =
                    sqrt(paddingStart.pow(2) + paddingTop.pow(2)) + realMidlineLength + realMidlineLength / 2F + offset

                // 辅助线 (测试用)
//                mLinePath.reset()
//                mLinePath.moveTo(right, bottom)
//                mLinePath.lineTo(right - realSize / 2F, bottom - realSize / 2F)
//                mLinePath.moveTo(right, bottom - realSize / 4F)
//                mLinePath.lineTo(right - realSize / 4F, bottom)
//                mLinePath.moveTo(right, bottom - realSize * 2F / 4F)
//                mLinePath.lineTo(right - realSize * 2F / 4F, bottom)
//                mLinePath.moveTo(right, bottom - realSize * 3F / 4F)
//                mLinePath.lineTo(right - realSize * 3F / 4F, bottom)
//                mLinePaint.color = Color.WHITE
//                canvas?.drawPath(mLinePath, mLinePaint)

                canvas?.rotate(-45F)
                canvas?.drawText(text, textX, textY, mTextPaint)
            }
            else -> {
            }
        }
    }

    /**
     * 测量文字的高度
     * --经测试后发现，采用另一种带Rect的方式，获得的数据并不准确。
     * 特别是在一些对文字有一些倾斜处理的时候
     * @param paint
     * @return
     */
    private fun measureTextHeight(paint: Paint?) = if (paint == null) 0F else paint.fontMetrics.descent - paint.fontMetrics.ascent

    /**
     * 设置文本
     */
    var text: String
        get() = this.mText
        set(text) {
            this.mText = text
        }

    /**
     * 设置文本颜色 (ColoInt)
     */
    var textColor: Int
        @ColorInt get() = this.mTextColor
        set(@ColorInt textColor) {
            this.mTextColor = textColor
        }

    /**
     * 设置文本大小 (px值)
     */
    var textSize: Float
        get() = this.mTextSize
        set(textSize) {
            this.mTextSize = textSize
            this.mCustomTextSize = true
        }

    /**
     * 设置三角背景颜色 (ColoInt)
     */
    var bgColor: Int
        @ColorInt get() = this.mBgColor
        set(@ColorInt bgColor) {
            this.mBgColor = bgColor
        }

    /**
     * 圆角半径 (在圆角三角形样式下有效, 默认5dp)
     */
    var cornerRadius: Float
        get() = this.mCornerRadius
        set(cornerRadius) {
            this.mCornerRadius = cornerRadius
            this.mCustomCornerRadius = true
        }

    /**
     * 缺失的三角形(整体的三角形缩小版)的腰长(等腰三角形) (在缺失三角形样式下有效, 默认5dp)
     */
    var missingTriangleWaistLength: Float
        get() = this.mMissingTriangleWaistLength
        set(missingTriangleWaistLength) {
            this.mMissingTriangleWaistLength = missingTriangleWaistLength
            this.mCustomMissingTriangleWaistLength = true
        }

    /**
     * 纵轴偏移距离 (小于0: 向上偏移  大于0: 向下偏移)
     */
    var offset: Float
        get() = this.mOffset
        set(offset) {
            this.mOffset = offset
        }

    /**
     * 设置位置
     */
    var position: Int
        @MarkerViewPosition get() = this.mPosition
        set(@MarkerViewPosition position) {
            this.mPosition = position
        }

    /**
     * 设置样式
     */
    var style: Int
        @MarkerViewStyle get() = this.mStyle
        set(@MarkerViewStyle style) {
            this.mStyle = style
        }

    /**
     * 刷新
     */
    @AnyThread
    fun refresh() = runOnUiThread {
        invalidate()
    }

}