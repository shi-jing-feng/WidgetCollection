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
 * Function: MarkerView 位置限定注解
 * Date: 2020/8/14 17:10
 * Description:
 *
 * @author ShiJingFeng
 */
@IntDef({
    MarkerViewPosition.POSITION_LEFT_TOP,
    MarkerViewPosition.POSITION_LEFT_BOTTOM,
    MarkerViewPosition.POSITION_RIGHT_TOP,
    MarkerViewPosition.POSITION_RIGHT_BOTTOM
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
public @interface MarkerViewPosition {
    /** 位置: 左上角 */
    int POSITION_LEFT_TOP = 0;
    /** 位置: 左下角 */
    int POSITION_LEFT_BOTTOM = 1;
    /** 位置: 右上角 */
    int POSITION_RIGHT_TOP = 2;
    /** 位置: 右下角 */
    int POSITION_RIGHT_BOTTOM = 3;
}