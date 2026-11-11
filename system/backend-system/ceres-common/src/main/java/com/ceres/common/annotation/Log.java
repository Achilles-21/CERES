package com.ceres.common.annotation;

import com.ceres.common.enums.BusinessType;
import com.ceres.common.enums.OperatorType;

import java.lang.annotation.*;

@Target({ ElementType.PARAMETER, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log
{
    public String title() default "";

    public BusinessType businessType() default BusinessType.OTHER;

    public OperatorType operatorType() default OperatorType.OTHER;

    public boolean isSaveRequestData() default true;

    public boolean isSaveResponseData() default false;

    public String[] excludeParamNames() default {};
}
