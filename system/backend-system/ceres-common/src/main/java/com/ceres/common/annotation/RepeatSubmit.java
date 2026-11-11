package com.ceres.common.annotation;

import java.lang.annotation.*;

@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RepeatSubmit
{
    /** Submission interval in milliseconds; a shorter interval counts as a duplicate. */
    public int interval() default 5000;

    public String message() default "不允许重复提交，请稍候再试";
}
