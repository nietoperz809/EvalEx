package com.udojava.evalex;

import java.math.BigDecimal;

/**
 * Created by Administrator on 1/28/2017.
 */
public class MathTools
{

    public static BigDecimal sqrt(BigDecimal value)
    {
        BigDecimal x = new BigDecimal(Math.sqrt(value.doubleValue()));
        return x.add(new BigDecimal(value.subtract(x.multiply(x)).doubleValue() / (x.doubleValue() * 2.0)));
    }

//    public static BigInteger getFactorialUsingGammaApproximation (
//            int n, int precision)
//    {
//        if (n == 0 || n == 1)
//        {
//            return BigInteger.ONE;
//        }
//        return pow(
//                Math.E,
//                factorialLog(n),
//                precision)
//                .toBigInteger().add(BigInteger.ONE);
//    }

//    public static MyComplex pow (double base, double exp, int precision)
//    {
//        //get integer part of base
//        MyComplex a = new MyComplex(
//                new MyComplex(exp,0).toBigInteger(),BigInteger.ZERO);
//        // get fractional part of base
//        double b = exp - a.doubleValue();
//        MyComplex aToExp = new MyComplex(base,0).pow(
//                a.toBigIntegerExact().intValue(),
//                new MathContext(precision));
//        //putting all together
//        MyComplex bToExp = new MyComplex(Math.pow(base, b),0);
//        return aToExp.multiply(bToExp);
//    }

//    public static BigDecimal nthRoot (final int n, final BigDecimal a, final BigDecimal p)
//    {
//        if (a.compareTo(Big.ZERO) < 0)
//        {
//            throw new IllegalArgumentException("nth root can only be calculated for positive numbers");
//        }
//        if (a.equals(MyComplex.ZERO))
//        {
//            return MyComplex.ZERO;
//        }
//        BigDecimal xPrev = a;
//        BigDecimal x = a.divide(new MyComplex(n,0), MathContext.DECIMAL128);  // starting "guessed" value...
//        while (x.subtract(xPrev).abs().compareTo(p) > 0)
//        {
//            xPrev = x;
//            x = MyComplex.valueOf(n - 1.0, 0)
//                    .multiply(x)
//                    .add(a.divide(x.pow(n - 1), MathContext.DECIMAL128))
//                    .divide(new MyComplex(n,0), MathContext.DECIMAL128);
//        }
//        return x;
//    }

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

    public static MyComplex iterativeFibonacci (int number)
    {
        // error condition
        if (number < 0)
        {
            throw new ExpressionException("input is negative");
        }
        // two special cases
        if (number == 0 || number == 1)
        {
            return new MyComplex(number, 0);
        }
        // values for n = 0, 1, 2
        MyComplex first = new MyComplex(0,0);
        MyComplex second = new MyComplex(1,0);
        MyComplex third = first.add(second);
        // calculate next value
        for (int i = 3; i <= number; i++)
        {
            first = second;
            second = third;
            third = first.add(second);
        }
        return third;
    }

//    public static MyComplex approxFibonacci (int n)
//    {
////                MyComplex Phi = sqrt5.add(MyComplex.ONE).divide(new MyComplex(2), MathContext.DECIMAL128);
////                MyComplex phi = Phi.subtract(MyComplex.ONE);
////                MyComplex b1 = exp.eval(Phi, par.get(0));
////                MyComplex b2 = exp.eval(phi, par.get(0));
////                MyComplex r = b1.subtract(b2).divide(sqrt5, MathContext.DECIMAL128);
////                return r;
//    }
}
