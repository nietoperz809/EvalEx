package com.udojava.evalex;

import java.math.BigInteger;
import java.math.MathContext;

import static org.apache.commons.math3.util.CombinatoricsUtils.factorialLog;

/**
 * Created by Administrator on 1/28/2017.
 */
public class MathTools
{
    private static final BigNumber SQRT5 = new BigNumber("2.236067977499789805051477742381393909454345703125");

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

    public static BigNumber pow (double base, double exp, int precision)
    {
        //get integer part of base
        BigNumber a = new BigNumber(
                new BigNumber(exp).toBigInteger());
        // get fractional part of base
        double b = exp - a.doubleValue();
        BigNumber aToExp = new BigNumber(base).pow(
                a.toBigIntegerExact().intValue(),
                new MathContext(precision));
        //putting all together
        BigNumber bToExp = new BigNumber(Math.pow(base, b));
        return aToExp.multiply(bToExp);
    }

    public static BigNumber nthRoot (final int n, final BigNumber a, final BigNumber p)
    {
        if (a.compareTo(BigNumber.ZERO) < 0)
        {
            throw new IllegalArgumentException("nth root can only be calculated for positive numbers");
        }
        if (a.equals(BigNumber.ZERO))
        {
            return BigNumber.ZERO;
        }
        BigNumber xPrev = a;
        BigNumber x = a.divide(new BigNumber(n), MathContext.DECIMAL128);  // starting "guessed" value...
        while (x.subtract(xPrev).abs().compareTo(p) > 0)
        {
            xPrev = x;
            x = BigNumber.valueOf(n - 1.0)
                    .multiply(x)
                    .add(a.divide(x.pow(n - 1), MathContext.DECIMAL128))
                    .divide(new BigNumber(n), MathContext.DECIMAL128);
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

    public static BigNumber iterativeFibonacci (int number)
    {
        // error condition
        if (number < 0)
        {
            throw new ExpressionException("input is negative");
        }
        // two special cases
        if (number == 0 || number == 1)
        {
            return new BigNumber(number);
        }
        // values for n = 0, 1, 2
        BigNumber first = BigNumber.ZERO;
        BigNumber second = BigNumber.ONE;
        BigNumber third = first.add(second);
        // calculate next value
        for (int i = 3; i <= number; i++)
        {
            first = second;
            second = third;
            third = first.add(second);
        }
        return third;
    }

//    public static BigNumber approxFibonacci (int n)
//    {
////                BigNumber Phi = sqrt5.add(BigNumber.ONE).divide(new BigNumber(2), MathContext.DECIMAL128);
////                BigNumber phi = Phi.subtract(BigNumber.ONE);
////                BigNumber b1 = exp.eval(Phi, par.get(0));
////                BigNumber b2 = exp.eval(phi, par.get(0));
////                BigNumber r = b1.subtract(b2).divide(sqrt5, MathContext.DECIMAL128);
////                return r;
//    }
}
