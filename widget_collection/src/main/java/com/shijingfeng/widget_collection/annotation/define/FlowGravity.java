package com.shijingfeng.widget_collection.annotation.define;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.SOURCE;
import com.shijingfeng.widget_collection.widget.FlowLayout;

/**
 * Function: {@link FlowLayout} Child所处的位置 限制注解
 * Date: 2020/9/24 15:47
 * Description:
 *
 * @author ShiJingFeng
 */
@IntDef({
    FlowGravity.FLOW_GRAVITY_CENTER,
    FlowGravity.FLOW_GRAVITY_TOP,
    FlowGravity.FLOW_GRAVITY_BOTTOM,
    FlowGravity.FLOW_GRAVITY_LEFT,
    FlowGravity.FLOW_GRAVITY_RIGHT
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
public @interface FlowGravity {
    
    /** Child所处的位置: {@link FlowOrientation#FLOW_ORIENTATION_HORIZONTAL}: 水平居中  {@link FlowOrientation#FLOW_ORIENTATION_VERTICAL}: 垂直居中 */
    int FLOW_GRAVITY_CENTER = 0;

    /** Child所处的位置: 顶部  用于{@link FlowOrientation#FLOW_ORIENTATION_VERTICAL} */
    int FLOW_GRAVITY_TOP = 1;

    /** Child所处的位置: 底部  用于{@link FlowOrientation#FLOW_ORIENTATION_VERTICAL} */
    int FLOW_GRAVITY_BOTTOM = 2;

    /** Child所处的位置: 左部  用于{@link FlowOrientation#FLOW_ORIENTATION_HORIZONTAL} */
    int FLOW_GRAVITY_LEFT = 3;

    /** Child所处的位置: 右部  用于{@link FlowOrientation#FLOW_ORIENTATION_HORIZONTAL} */
    int FLOW_GRAVITY_RIGHT = 4;
    
}
