package com.ceres.common.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Arith
{

    private static final int DEF_DIV_SCALE = 10;

    private Arith()
    {
    }

    public static double add(double v1, double v2)
    {
        return decimal(v1).add(decimal(v2)).doubleValue();
    }

    public static double sub(double v1, double v2)
    {
        return decimal(v1).subtract(decimal(v2)).doubleValue();
    }

    public static double mul(double v1, double v2)
    {
        return decimal(v1).multiply(decimal(v2)).doubleValue();
    }

    /** Divide with 10 fractional digits using half-up rounding. */
    public static double div(double v1, double v2)
    {
        return div(v1, v2, DEF_DIV_SCALE);
    }

    /** Divide with the requested fractional scale using half-up rounding. */
    public static double div(double v1, double v2, int scale)
    {
        requireValidScale(scale);
        BigDecimal dividend = decimal(v1);
        if (dividend.signum() == 0)
        {
            return 0D;
        }
        return dividend.divide(decimal(v2), scale, RoundingMode.HALF_UP).doubleValue();
    }

    public static double round(double v, int scale)
    {
        requireValidScale(scale);
        return decimal(v).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    private static BigDecimal decimal(double value)
    {
        return new BigDecimal(Double.toString(value));
    }

    private static void requireValidScale(int scale)
    {
        if (scale < 0)
        {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }
    }
}
