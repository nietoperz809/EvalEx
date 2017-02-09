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
    private static final BigDecimal SQRT5 = new BigDecimal("2.236067977499789805051477742381393909454345703125");

    public static BigInteger getFactorialUsingGammaApproximation (
            int n, int precision)
    {
        if (n == 0 || n == 1)
        {
            return BigInteger.ONE;
        }
        return pow(
                Math.E,
                factorialLog(n),
                precision)
                .toBigInteger().add(BigInteger.ONE);
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

    public static BigDecimal nthRoot (final int n, final BigDecimal a, final BigDecimal p)
    {
        if (a.compareTo(BigDecimal.ZERO) < 0)
        {
            throw new IllegalArgumentException("nth root can only be calculated for positive numbers");
        }
        if (a.equals(BigDecimal.ZERO))
        {
            return BigDecimal.ZERO;
        }
        BigDecimal xPrev = a;
        BigDecimal x = a.divide(new BigDecimal(n), MathContext.DECIMAL128);  // starting "guessed" value...
        while (x.subtract(xPrev).abs().compareTo(p) > 0)
        {
            xPrev = x;
            x = BigDecimal.valueOf(n - 1.0)
                    .multiply(x)
                    .add(a.divide(x.pow(n - 1), MathContext.DECIMAL128))
                    .divide(new BigDecimal(n), MathContext.DECIMAL128);
        }
        return x;
    }

    public static String reverseHex (String originalHex)
    {
        // TODO: Validation that the length is even
        int lengthInBytes = originalHex.length() / 2;
        char[] chars = new char[lengthInBytes * 2];
        for (int index = 0; index < lengthInBytes; index++)
        {
            int reversedIndex = lengthInBytes - 1 - index;
            chars[reversedIndex * 2] = originalHex.charAt(index * 2);
            chars[reversedIndex * 2 + 1] = originalHex.charAt(index * 2 + 1);
        }
        return new String(chars);
    }

    public static BigDecimal iterativeFibonacci (int number)
    {
        // error condition
        if (number < 0)
        {
            throw new ExpressionException("input is negative");
        }
        // two special cases
        if (number == 0 || number == 1)
        {
            return new BigDecimal(number);
        }
        // values for n = 0, 1, 2
        BigDecimal first = BigDecimal.ZERO;
        BigDecimal second = BigDecimal.ONE;
        BigDecimal third = first.add(second);
        // calculate next value
        for (int i = 3; i <= number; i++)
        {
            first = second;
            second = third;
            third = first.add(second);
        }
        return third;
    }

//    public static BigDecimal approxFibonacci (int n)
//    {
////                BigDecimal Phi = sqrt5.add(BigDecimal.ONE).divide(new BigDecimal(2), MathContext.DECIMAL128);
////                BigDecimal phi = Phi.subtract(BigDecimal.ONE);
////                BigDecimal b1 = exp.eval(Phi, par.get(0));
////                BigDecimal b2 = exp.eval(phi, par.get(0));
////                BigDecimal r = b1.subtract(b2).divide(sqrt5, MathContext.DECIMAL128);
////                return r;
//    }
}
