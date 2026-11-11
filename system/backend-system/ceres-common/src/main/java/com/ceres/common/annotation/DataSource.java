package com.ceres.common.annotation;

import com.ceres.common.enums.DataSourceType;

import java.lang.annotation.*;

/** Method-level datasource selection overrides class-level selection. */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface DataSource
{
    public DataSourceType value() default DataSourceType.MASTER;
}
