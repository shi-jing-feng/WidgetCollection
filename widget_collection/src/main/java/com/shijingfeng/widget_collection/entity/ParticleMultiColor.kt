package com.shijingfeng.widget_collection.entity

import androidx.annotation.ColorInt

/**
 * Function: 粒子多颜色配置 实体类
 * Date: 2020/11/11 13:16
 * Description:
 * @author ShiJingFeng
 */
data class ParticleMultiColor(

    /** 颜色值 */
    @ColorInt
    var color: Int,

    /** 比重 默认: 1 */
    var weight: Int = 1

)