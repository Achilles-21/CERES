package com.ceres.system.common;

import com.ceres.common.utils.Arith;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArithTest
{
    @Test
    void decimalOperationsAvoidBinaryFloatingPointArtifacts()
    {
        assertEquals(0.3D, Arith.add(0.1D, 0.2D));
        assertEquals(0.1D, Arith.sub(0.3D, 0.2D));
        assertEquals(0.02D, Arith.mul(0.1D, 0.2D));
    }

    @Test
    void divisionUsesHalfUpRoundingAtTheRequestedScale()
    {
        assertEquals(0.3333333333D, Arith.div(1D, 3D));
        assertEquals(0.67D, Arith.div(2D, 3D, 2));
        assertEquals(-0.67D, Arith.div(-2D, 3D, 2));
    }

    @Test
    void divisionPreservesExistingZeroDividendBehavior()
    {
        assertEquals(0D, Arith.div(0D, 7D, 4));
        assertEquals(0D, Arith.div(0D, 0D, 4));
        assertThrows(ArithmeticException.class, () -> Arith.div(1D, 0D, 4));
    }

    @Test
    void roundUsesHalfUpForPositiveAndNegativeValues()
    {
        assertEquals(1.24D, Arith.round(1.235D, 2));
        assertEquals(-1.24D, Arith.round(-1.235D, 2));
        assertEquals(2D, Arith.round(1.5D, 0));
    }

    @Test
    void invalidInputsKeepTheirExceptionContracts()
    {
        assertThrows(IllegalArgumentException.class, () -> Arith.div(1D, 2D, -1));
        assertThrows(IllegalArgumentException.class, () -> Arith.round(1D, -1));
        assertThrows(NumberFormatException.class, () -> Arith.add(Double.NaN, 1D));
    }
}
