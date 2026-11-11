package com.ceres.common.utils;

import com.ceres.common.constant.Constants;
import com.ceres.common.utils.spring.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;

import java.util.Locale;

@Slf4j
public class MessageUtils
{
    public static String message(String code, Object... args)
    {
        MessageSource messageSource = SpringUtils.getBean(MessageSource.class);
        return messageSource.getMessage(code, args, new Locale(Constants.LANG));
    }
}
