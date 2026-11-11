package com.ceres.common.utils;

import com.ceres.common.exception.ServiceException;

import java.util.Collection;

public class AssertUtils {


    public static void notEmpty(String string, String message) {
        if (StringUtils.isEmpty(string)) {
            throw new ServiceException(message);
        }
    }

    public static void notEmpty(Collection<?> collection, String message) {
        if (collection == null || collection.isEmpty()) {
            throw new ServiceException(message);
        }
    }

    public static void notBlank(String string, String message) {
        if (StringUtils.isBlank(string)) {
            throw new ServiceException(message);
        }
    }

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new ServiceException(message);
        }
    }

    public static void isTrue(boolean value, String message) {
        if (!value) {
            throw new ServiceException(message);
        }
    }

}
