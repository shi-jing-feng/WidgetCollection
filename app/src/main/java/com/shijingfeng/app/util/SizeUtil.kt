/** 生成的 Java 类名 */
@file:JvmName("SizeUtil")
package com.shijingfeng.app.util

import android.content.res.Resources

/**
 * Function: 尺寸相关 工具类
 * Date: 2020/9/23 14:08
 * Description:
 * @author ShiJingFeng
 */

/**
 * Value of dp to value of px.
 *
 * @param dpValue The value of dp.
 * @return value of px
 */
internal fun dp2px(dpValue: Float): Int {
    val scale = Resources.getSystem().displayMetrics.density

    return (dpValue * scale + 0.5f).toInt()
}

/**
 * Value of px to value of dp.
 *
 * @param pxValue The value of px.
 * @return value of dp
 */
internal fun px2dp(pxValue: Float): Int {
    val scale = Resources.getSystem().displayMetrics.density

    return (pxValue / scale + 0.5f).toInt()
}

/**
 * Value of sp to value of px.
 *
 * @param spValue The value of sp.
 * @return value of px
 */
internal fun sp2px(spValue: Float): Int {
    val fontScale = Resources.getSystem().displayMetrics.scaledDensity

    return (spValue * fontScale + 0.5f).toInt()
}

/**
 * Value of px to value of sp.
 *
 * @param pxValue The value of px.
 * @return value of sp
 */
internal fun px2sp(pxValue: Float): Int {
    val fontScale = Resources.getSystem().displayMetrics.scaledDensity

    return (pxValue / fontScale + 0.5f).toInt()
}