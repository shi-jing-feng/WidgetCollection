package com.shijingfeng.widget_collection.annotation.define;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Function:
 * Date: 2020/9/16 22:33
 * Description:
 *
 * @author ShiJingFeng
 */
@IntDef({
    IsoscelesTriangleViewStyle.STYLE_TOP_TO_BOTTOM,
    IsoscelesTriangleViewStyle.STYLE_BOTTOM_TO_TOP,
    IsoscelesTriangleViewStyle.STYLE_LEFT_TO_RIGHT,
    IsoscelesTriangleViewStyle.STYLE_RIGHT_TO_LEFT
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
public @interface IsoscelesTriangleViewStyle {

    /** 样式: 以两腰之间的尖端开始，从顶部到底部 (垂直方向正三角形) */
    int STYLE_TOP_TO_BOTTOM = 0;
    /** 样式: 以两腰之间的尖端开始，从底部到顶部 (垂直方向倒三角形) */
    int STYLE_BOTTOM_TO_TOP = 1;
    /** 样式: 以两腰之间的尖端开始，从左部到右部 (水平方向正三角形) */
    int STYLE_LEFT_TO_RIGHT = 2;
    /** 样式: 以两腰之间的尖端开始，从右部到左部 (水平方向倒三角形) */
    int STYLE_RIGHT_TO_LEFT = 3;

}
