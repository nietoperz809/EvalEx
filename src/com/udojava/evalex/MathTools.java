package com.udojava.evalex;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

import static org.apache.commons.math3.util.CombinatoricsUtils.factorialLog;

/**
 * Created by Administrator on 1/28/2017.
 */
public class MathTools
{
    public static BigInteger getFactorialUsingGammaApproximation (
            int n, int precision)
    {
        return pow(
                Math.E,
                factorialLog(n),
                precision)
                .toBigInteger();
    }

    public static BigDecimal pow (double base, double exp, int precision)
    {
        //get integer part of base
        BigDecimal a = new BigDecimal(
                new BigDecimal(exp).toBigInteger());
        // get fractional part of base
        double b = exp - a.doubleValue();
        BigDecimal aToExp = new BigDecimal(base).pow(
                a.toBigIntegerExact().intValue(),
                new MathContext(precision));
        //putting all together
        BigDecimal bToExp = new BigDecimal(Math.pow(base, b));
        return aToExp.multiply(bToExp);
    }
}
