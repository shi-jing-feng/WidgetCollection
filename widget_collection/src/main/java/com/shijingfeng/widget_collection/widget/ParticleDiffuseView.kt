package com.shijingfeng.widget_collection.widget

import android.animation.ValueAnimator
import android.animation.ValueAnimator.INFINITE
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.annotation.AnyThread
import androidx.annotation.ColorInt
import com.shijingfeng.widget_collection.R
import com.shijingfeng.widget_collection.util.coordinateToRadian
import com.shijingfeng.widget_collection.util.runOnUiThread
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

/** 默认粒子颜色 白色 */
private const val DEFAULT_PARTICLE_COLOR = Color.WHITE

/** 比例基准值 */
private const val SCALE_BASE_VALUE = 1F / 1000F
/** 默认粒子半径大小比例值 */
private const val DEFAULT_PARTICLE_RADIUS_SCALE_VALUE = 1.7F * SCALE_BASE_VALUE
/** 粒子最慢速度大小比例值 */
private const val DEFAULT_PARTICLE_SLOWEST_SPEED_SCALE_VALUE = 1.5F * SCALE_BASE_VALUE

/** 粒子数量 */
private const val PARTICLE_NUMBER = 2000

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

    /** Random */
    private val mRandom = Random()

    /** 粒子颜色 */
    @ColorInt
    private var mParticleColor = DEFAULT_PARTICLE_COLOR
    /** 粒子半径 */
    private var mParticleRadius = 0F
    /** 粒子最慢速度 */
    private var mParticleSlowestSpeed = 0F
    /** 粒子多颜色列表 */
//    private var mParticleMultiColorList: List<ParticleMultiColor>? = null
    /** 粒子列表 */
    private val mParticleList = mutableListOf<Particle>()
    /** 粒子更新的次数 */
    private var mParticleUpdatedCount = 0
    /** 粒子要绘制的话需要更新的最小次数 (延迟刷新, 用于解决动画开始时的粒子向外扩散不美观的问题) */
    private var mParticleStartUpdateCount = 0
    /** 粒子动画 */
    private val mAnimator = ValueAnimator.ofFloat(0F, 1F)

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
                // 注意: 粒子速度和动画duration没有关系, 和 Particle.speed 大小有关系
                updateParticle()
                invalidate()
                ++mParticleUpdatedCount
            }
        }
    }

    /**
     * 初始化粒子
     */
    private fun initParticle() {
        mPaint.color = mParticleColor
        // CW: 顺时针  CCW: 逆时针
        mPath.reset()
        mPath.addCircle(mSize / 2F, mSize / 2F, mInnerCircleRadius, Path.Direction.CCW)
        mPathMeasure.setPath(mPath, false)
        mParticleList.clear()
        for (i in 0..PARTICLE_NUMBER) {
            // 计算路径
            mPathMeasure.getPosTan(i / PARTICLE_NUMBER.toFloat() * mPathMeasure.length, mInnerCirclePos, mInnerCircleTan)

            val center = mSize / 2F
            val x = mInnerCirclePos[0]
            val y = mInnerCirclePos[1]
            // 速度 (以动画每次刷新作为单位时间)
            val speed = getRandomSpeed()
            // 弧度值  弧度方向: 以X轴(正轴)为起点, 顺时针为角度增加方向
            val angle = coordinateToRadian(
                x = x - center,
                y = y - center
            )
            // 当前移动距离
            val offset = 0F
            // 最大移动距离
            val maxOffset = mRandom.nextInt(mRingThickness.toInt())

            mParticleList.add(Particle(
                // 在扩展圆(内圆) 边界线 X轴方向 左右浮动
                x = x + getCoordinateRandomOffset(),
                // 在扩展圆(内圆) 边界线 Y轴方向 上下浮动
                y = y + getCoordinateRandomOffset(),
                speed = speed,
                angle = angle,
                offset = offset,
                maxOffset = maxOffset.toFloat()
            ))
        }
        mParticleUpdatedCount = 0
        mParticleStartUpdateCount = (mRingThickness / mParticleSlowestSpeed).toInt()
        mAnimator.cancel()
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
                particle.speed = getRandomSpeed()
                // 最大移动距离
                particle.maxOffset = mRandom.nextInt(mRingThickness.toInt()).toFloat()
                // 计算路径
                mPathMeasure.getPosTan(mRandom.nextInt(mPathMeasure.length.toInt() + 1).toFloat(), mInnerCirclePos, mInnerCircleTan)
                // X轴初始位置
                particle.x = mInnerCirclePos[0] + getCoordinateRandomOffset()
                // Y轴初始位置
                particle.y = mInnerCirclePos[1] + getCoordinateRandomOffset()
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
        // 延迟刷新, 用于解决动画开始时的粒子向外扩散不美观的问题
        if (mParticleUpdatedCount >= mParticleStartUpdateCount) {
            mParticleList.forEach { particle ->
                val alphaFloat = 1F - particle.offset / particle.maxOffset
                val alpha = (alphaFloat * 255F).toInt()

                // 加界限判断, 防止闪烁
                mPaint.alpha = when {
                    alpha < 0 -> 0
                    alpha > 255 -> 255
                    else -> alpha
                }
                canvas?.drawCircle(particle.x, particle.y, mParticleRadius, mPaint)
            }
        }
    }

    /**
     * 获取坐标随机偏移量
     */
    private fun getCoordinateRandomOffset() = mRandom.nextInt((19.2F * SCALE_BASE_VALUE * mSize).toInt()) - 9.6F * SCALE_BASE_VALUE * mSize

    /**
     * 获取随机速度量
     */
    private fun getRandomSpeed(): Float {
        return mRandom.nextInt(mParticleSlowestSpeed.toInt()) + mParticleSlowestSpeed
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val width = right - left
        val height = bottom - top

        mSize = if (width > height) height.toFloat() else width.toFloat()
        // 默认扩展圆(内圆)半径为宽的一半
        if (mInnerCircleRadius.toInt() == 0) {
            mInnerCircleRadius = mSize / 4F
        }
        mRingThickness = mSize / 2 - mInnerCircleRadius
        mParticleRadius = DEFAULT_PARTICLE_RADIUS_SCALE_VALUE * mSize
        mParticleSlowestSpeed = DEFAULT_PARTICLE_SLOWEST_SPEED_SCALE_VALUE * mSize
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

    /**
     * 扩展圆(内圆) 半径
     */
    var radius: Float
        get() = this.mInnerCircleRadius
        set(radius) {
            this.mInnerCircleRadius = radius
        }

    /**
     * 粒子颜色
     */
    var color: Int
        @ColorInt get() = this.mParticleColor
        set(@ColorInt color) {
            this.mParticleColor = color
        }

    /**
     * 颜色列表
     */
//    var colorList: List<ParticleMultiColor>?
//        get() = this.mParticleMultiColorList
//        set(colorList) {
//            this.mParticleMultiColorList = colorList
//        }

    /**
     * 刷新
     */
    @AnyThread
    fun refresh() = runOnUiThread {
        requestLayout()
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

    /** 粒子发散的角度(弧度值) 弧度方向: 以X轴(正轴)为起点, 顺时针为角度增加方向 */
    var angle: Double,

    /** 当前移动距离 */
    var offset: Float,

    /** 最大移动距离 */
    var maxOffset: Float

)