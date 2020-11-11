/** 生成的 Java 类名 */
@file:JvmName("MathUtil")
package com.shijingfeng.library.util

import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Function: 数学相关工具类
 * Date: 2020/11/11 10:16
 * Description:
 * @author ShiJingFeng
 */

/**
 * 坐标转弧度  弧度方向: 以X轴(正轴)为起点, 顺时针为角度增加方向
 *
 * @param x: X坐标(使用的是笛卡尔坐标系, 而不是屏幕坐标系)
 * @param y: Y坐标(使用的是笛卡尔坐标系, 而不是屏幕坐标系)
 */
internal fun coordinateToRadian(
    x: Float,
    y: Float,
): Double {
    val radius = sqrt(x.pow(2) + y.pow(2))

    return if (x >= 0F && y >= 0F) {
        // 第一象限
        Math.toRadians(360.0) - acos(x / radius).toDouble()
    } else if (x <= 0F && y >= 0F) {
        // 第二象限
        Math.toRadians(180.0) + acos(-x / radius).toDouble()
    } else if (x <= 0F && y <= 0F) {
        // 第三象限
        Math.toRadians(180.0) - acos(-x / radius).toDouble()
    } else {
        // 第四象限
        acos(x / radius).toDouble()
    }
}