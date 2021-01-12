/** 生成的 Java 类名 */
@file:JvmName("ThreadUtil")

package com.shijingfeng.widget_collection.util

import android.os.Handler
import android.os.Looper
import androidx.annotation.AnyThread

/**
 * Function: 线程相关 工具类
 * Date: 2020/9/23 11:10
 * Description:
 * @author ShiJingFeng
 */

/** 主线程 Handler */
private val MAIN_HANDLER = Handler(Looper.getMainLooper())

/**
 * 获取 主线程 Handler
 */
internal val mainHandler: Handler
    get() = MAIN_HANDLER

/**
 * 判断是否是主线程
 *
 * @return true: 主线程  false: 非主线程
 */
internal val isMainThread: Boolean @AnyThread get() = Looper.myLooper() == Looper.getMainLooper()

/**
 * 运行在主线程
 *
 * @param delay 延迟时间 (毫秒值)
 * @param action 回调函数
 */
@JvmOverloads
internal fun runOnUiThread(
    delay: Long = 0L,
    action: () -> Unit
) {
    if (isMainThread && delay <= 0L) {
        action.invoke()
    } else {
        MAIN_HANDLER.postDelayed(action, delay)
    }
}