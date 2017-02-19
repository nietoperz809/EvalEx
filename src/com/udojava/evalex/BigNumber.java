package com.udojava.evalex;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Created by Administrator on 2/19/2017.
 */
public class BigNumber extends BigDecimal
{

    public BigNumber (char[] in)
    {
        super(in);
    }

    public BigNumber (String val)
    {
        super(val);
    }

    public BigNumber (double val)
    {
        super(val);
    }

    public BigNumber (BigInteger val)
    {
        super(val);
    }

    public BigNumber (int val)
    {
        super(val);
    }

    public BigNumber (long val)
    {
        super(val);
    }

    public BigNumber add (BigNumber n)
    {
        BigDecimal b = super.add(n);
        return new BigNumber(b.toBigInteger());
    }

    public BigNumber subtract (BigNumber n)
    {
        BigDecimal b = super.subtract(n);
        return new BigNumber(b.toBigInteger());
    }

    public BigNumber multiply (BigNumber n)
    {
        BigDecimal b = super.multiply(n);
        return new BigNumber(b.toBigInteger());
    }

    public BigNumber divide (BigNumber n, MathContext m)
    {
        BigDecimal b = super.divide(n, m);
        return new BigNumber(b.toBigInteger());
    }

    public BigNumber remainder (BigNumber n, MathContext m)
    {
        BigDecimal b = super.remainder(n, m);
        return new BigNumber(b.toBigInteger());
    }

    public BigNumber pow (int n)
    {
        BigDecimal b = super.pow(n);
        return new BigNumber(b.toBigInteger());
    }

    public BigNumber pow (int n, MathContext m)
    {
        BigDecimal b = super.pow(n, m);
        return new BigNumber(b.toBigInteger());
    }

    public BigNumber abs ()
    {
        BigDecimal b = super.abs();
        return new BigNumber(b.toBigInteger());
    }

    public BigNumber setScale (int s, RoundingMode r)
    {
        BigDecimal b = super.setScale(s, r);
        return new BigNumber(b.toBigInteger());
    }

    public BigNumber stripTrailingZeros ()
    {
        BigDecimal b = super.stripTrailingZeros();
        return new BigNumber(b.toBigInteger());
    }

//    public BigInteger toBigIntegerExact ()
//    {
//        return super.toBigIntegerExact();
//    }

    static public BigNumber valueOf (double d)
    {
        BigDecimal b = BigDecimal.valueOf(d);
        return new BigNumber(b.toBigInteger());
    }

    public static final BigNumber ONE = new BigNumber(BigDecimal.ONE.toBigInteger());
    public static final BigNumber ZERO = new BigNumber(BigDecimal.ZERO.toBigInteger());

}
