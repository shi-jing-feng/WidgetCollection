package com.shijingfeng.library.widget

import android.animation.ValueAnimator
import android.animation.ValueAnimator.INFINITE
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.Log.e
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.annotation.ColorInt
import com.shijingfeng.library.R
import com.shijingfeng.library.util.dp2px
import java.util.*
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

/** 默认粒子颜色 白色 */
private const val DEFAULT_PARTICLE_COLOR = Color.WHITE

/** 粒子数量 */
private const val PARTICLE_NUMBER = 2000
/** 粒子半径 */
private val PARTICLE_RADIUS = dp2px(1F).toFloat()

/** 动画持续时间(毫秒值) 默认: 2000毫秒 */
private const val ANIMATOR_DURATION_MS = 2000L

/**
 * Function: 粒子扩散效果 View (仿网易云空灵轻音特效)
 * Date: 2020/11/6 17:08
 * Description:
 * @author ShiJingFeng
 */
class ParticleDiffuseView @JvmOverloads constructor(
    /** Context环境  */
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    /** 画笔 */
    private val mPaint = Paint().apply {
        // 画笔颜色(白色)
        color = Color.WHITE
        // 抗锯齿(true)
        isAntiAlias = true
    }
    /** 路径(用来画中心圆的路径) */
    private val mPath = Path()
    /** 用于测量粒子所在路径的位置 */
    private val mPathMeasure = PathMeasure()

    /** 粒子动画 */
    private val mAnimator = ValueAnimator.ofFloat(0F, 1F)

    /** Random */
    private val mRandom = Random()

    /** 粒子颜色 */
    @ColorInt
    private var mParticleColor = DEFAULT_PARTICLE_COLOR
    /** 粒子列表 */
    private val mParticleList = mutableListOf<Particle>()

    /** 扩散圆(内圆) 半径 */
    private var mInnerCircleRadius = 0F
    /** 扩散圆(内圆) 上某一点的坐标 (0下标: X坐标, 1下标: Y坐标) */
    private val mInnerCirclePos = FloatArray(2)
    /** 扩散圆(内圆) 上某一点切线坐标 (0下标: 和X轴相交的X轴坐标, 1坐标: 和Y轴相交的Y轴坐标) */
    private val mInnerCircleTan = FloatArray(2)

    /** 尺寸大小 (宽和高取最小作为尺寸大小) */
    private var mSize = 0F
    /** 外圆半径 减去 扩展圆(内圆)半径 */
    private var mRingThickness = 0F

    init {
        context.obtainStyledAttributes(attrs, R.styleable.ParticleDiffuseView).apply {
            mInnerCircleRadius = getDimension(R.styleable.ParticleDiffuseView_radius, 0F)
            mParticleColor = getColor(R.styleable.ParticleDiffuseView_color, DEFAULT_PARTICLE_COLOR)
            //一定要回收，否则会内存泄漏
            recycle()
        }
        initAnimator()
    }

    /**
     * 初始化动画
     */
    private fun initAnimator() {
        mAnimator.run {
            duration = ANIMATOR_DURATION_MS
            repeatCount = INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                updateParticle()
                invalidate()
            }
        }
    }

    /**
     * 初始化粒子
     */
    private fun initParticle() {
        // CW: 顺时针  CCW: 逆时针
        mPath.addCircle(mSize / 2F, mSize / 2F, mInnerCircleRadius, Path.Direction.CCW)
        mPathMeasure.setPath(mPath, false)
        mParticleList.clear()
        for (i in 0..PARTICLE_NUMBER) {
            // 计算路径
            mPathMeasure.getPosTan(i / PARTICLE_NUMBER.toFloat() * mPathMeasure.length, mInnerCirclePos, mInnerCircleTan)

            val center = mSize / 2F
            val x = mInnerCirclePos[0] - center
            val y = mInnerCirclePos[1] - center
            // 在扩展圆(内圆) 边界线 X轴方向 左右浮动
            val randomX = mRandom.nextInt(dp2px(6F)) - dp2px(3F)
            // 在扩展圆(内圆) 边界线 Y轴方向 上下浮动
            val randomY = mRandom.nextInt(dp2px(6F)) - dp2px(3F)
            // 速度 (以动画每次刷新作为单位时间)
            val speed = mRandom.nextInt(dp2px(2F)) + dp2px(0.5F)
            // 弧度值
            val angle = if (x >= 0F && y >= 0F) {
                // 第一象限
                Math.toRadians(270.0) + acos(if (y > mInnerCircleRadius) 1.0 else (y / mInnerCircleRadius).toDouble())
            } else if (x <= 0F && y >= 0F) {
                // 第二象限
                Math.toRadians(180.0) + acos(if (-x > mInnerCircleRadius) 1.0 else (-x / mInnerCircleRadius).toDouble())
            } else if (x <= 0F && y <= 0F) {
                // 第三象限
                Math.toRadians(90.0) + acos(if (-y > mInnerCircleRadius) 1.0 else (-y / mInnerCircleRadius).toDouble())
            } else {
                // 第四象限
                acos(if (x > mInnerCircleRadius) 1.0 else (x / mInnerCircleRadius).toDouble())
            }
            e("测试", "angle: ${Math.toDegrees(angle)}")
            // 当前移动距离
            val offset = 0F
            // 最大移动距离
            val maxOffset = mRandom.nextInt(mRingThickness.toInt())

            mParticleList.add(Particle(
                x = x + randomX,
                y = y + randomY,
                speed = speed.toFloat(),
                angle = angle,
                offset = offset,
                maxOffset = maxOffset.toFloat()
            ))
        }
        mAnimator.start()
    }

    /**
     * 更新粒子 (以动画刷新一次作为单位时间)
     */
    private fun updateParticle() {
        mParticleList.forEach { particle ->
            if (particle.offset >= particle.maxOffset) {
                // 当前粒子重置
                // 当前移动距离
                particle.offset = 0F
                // 速度 (以动画每次刷新作为单位时间)
                particle.speed = mRandom.nextInt(dp2px(2F)) + dp2px(0.5F).toFloat()
                // 最大移动距离
                particle.maxOffset = mRandom.nextInt(mRingThickness.toInt()).toFloat()
                // 计算路径
                mPathMeasure.getPosTan(mRandom.nextInt(mPathMeasure.length.toInt() + 1).toFloat(), mInnerCirclePos, mInnerCircleTan)
                // X轴初始位置
                particle.x = mInnerCirclePos[0] + mRandom.nextInt(dp2px(6F)) - dp2px(3F)
                // Y轴初始位置
                particle.y = mInnerCirclePos[1] + mRandom.nextInt(dp2px(6F)) - dp2px(3F)
            } else {
                // 当前移动距离
                particle.offset += particle.speed
                // X轴初始位置
                particle.x = mSize / 2 + (mInnerCircleRadius + particle.offset) * cos(particle.angle).toFloat()
                // Y轴初始位置
                particle.y = mSize / 2 + (mInnerCircleRadius + particle.offset) * sin(particle.angle).toFloat()
            }
        }
    }

    /**
     * 绘制粒子
     */
    private fun drawParticle(canvas: Canvas?) {
        mParticleList.forEach { particle ->
            mPaint.alpha = ((1F - particle.offset / particle.maxOffset) * 255F).toInt()

            canvas?.drawCircle(particle.x, particle.y, PARTICLE_RADIUS, mPaint)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mSize = if (w > h) h.toFloat() else w.toFloat()
        // 默认扩展圆(内圆)半径为宽的一半
        if (mInnerCircleRadius.toInt() == 0) {
            mInnerCircleRadius = mSize / 4F
        }
        mRingThickness = mSize / 2 - mInnerCircleRadius
        initParticle()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawParticle(canvas)
    }

    /**
     * 当View销毁时会调用
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // 销毁动画
        mAnimator.run {
            removeAllUpdateListeners()
            removeAllListeners()
            cancel()
        }
    }

}

/**
 * 粒子实体类
 */
private data class Particle(

    /** X轴坐标 */
    var x: Float,

    /** Y轴坐标 */
    var y: Float,

    /** 粒子速度 */
    var speed: Float,

    /** 粒子发散的角度 */
    var angle: Double,

    /** 当前移动距离 */
    var offset: Float,

    /** 最大移动距离 */
    var maxOffset: Float

)