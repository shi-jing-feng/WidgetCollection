package com.shijingfeng.library.annotation.define;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Function: MarkerView 样式限定注解
 * Date: 2020/8/27 15:55
 * Description:
 *
 * @author ShiJingFeng
 */
@IntDef({
    MarkerViewStyle.STYLE_TRIANGLE,
    MarkerViewStyle.STYLE_CORNER_TRIANGLE,
    MarkerViewStyle.STYLE_MISSING_TRIANGLE,
})
@Target({
    // 类属性
    FIELD,
    // 函数
    METHOD,
    // 函数参数
    PARAMETER,
    // 局部变量
    LOCAL_VARIABLE
})
@Retention(SOURCE)
public @interface MarkerViewStyle {

    /** 样式: 等腰三角形 */
    int STYLE_TRIANGLE = 0;
    /** 样式: 圆角等腰三角形 */
    int STYLE_CORNER_TRIANGLE = 1;
    /** 样式: 缺失的三角形 (看起来像旋转一定角度的梯形, 缺失的三角形部分是整体的缩小版) */
    int STYLE_MISSING_TRIANGLE = 2;

}
