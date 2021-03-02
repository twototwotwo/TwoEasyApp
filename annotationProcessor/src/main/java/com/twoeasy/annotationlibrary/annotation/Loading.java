package com.twoeasy.annotationlibrary.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author wsjiu
 * @date 2020/12/03
 * 用于耗时的方法，弹出加载动画优化用户体验
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
@Inherited
public @interface Loading {
    String context() default "";
}

