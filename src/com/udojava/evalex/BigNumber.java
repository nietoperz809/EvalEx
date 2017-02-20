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
    public BigNumber (String val, String img)
    {
        super(val);
        imaginary = new BigDecimal(img);
    }

    public BigNumber (double val, double img)
    {
        super(val);
        imaginary = new BigDecimal(img);
    }

    public BigNumber (BigInteger val, BigInteger img)
    {
        super(val);
        imaginary = new BigDecimal(img);
    }
//
//    public BigNumber (int val)
//    {
//        super(val);
//    }
//
//    public BigNumber (long val)
//    {
//        super(val);
//    }

    public BigNumber add (BigNumber n)
    {
        BigDecimal b = super.add(n);
        BigDecimal img = imaginary.add (n.imaginary);
        return new BigNumber(b.toBigInteger(), img.toBigInteger());
    }

    public BigNumber subtract (BigNumber n)
    {
        BigDecimal b = super.subtract(n);
        BigDecimal img = imaginary.subtract (n.imaginary);
        return new BigNumber(b.toBigInteger(), img.toBigInteger());
    }

    // (a+bi)(c+di) = (ac−bd) + (ad+bc)i
    public BigNumber multiply (BigNumber n)
    {
        BigDecimal ac = super.multiply(n);
        BigDecimal bd = imaginary.multiply(n.imaginary);
        BigDecimal ad = super.multiply(n.imaginary);
        BigDecimal bc = imaginary.multiply(n);
        return new BigNumber(ac.subtract(bd).toBigInteger(),
                ad.add(bc).toBigInteger());
    }

    public BigNumber divide (BigNumber n, MathContext m)
    {
        BigDecimal b = super.divide(n, m);
        return new BigNumber(b.toBigInteger(), n.imaginary.toBigInteger());
    }

    public BigNumber remainder (BigNumber n, MathContext m)
    {
        BigDecimal b = super.remainder(n, m);
        return new BigNumber(b.toBigInteger(), n.imaginary.toBigInteger());
    }

    public BigNumber pow (int n)
    {
        BigDecimal b = super.pow(n);
        return new BigNumber(b.toBigInteger(), null);
    }

    public BigNumber pow (int n, MathContext m)
    {
        BigDecimal b = super.pow(n, m);
        return new BigNumber(b.toBigInteger(), null);
    }

    public BigNumber abs ()
    {
        BigDecimal b = super.abs();
        BigDecimal c = imaginary.abs();
        return new BigNumber(b.toBigInteger(), c.toBigInteger());
    }

    public BigNumber setScale (int s, RoundingMode r)
    {
        BigDecimal b = super.setScale(s, r);
        BigDecimal c = imaginary.setScale(s, r);
        return new BigNumber(b.toBigInteger(), c.toBigInteger());
    }

    public BigNumber stripTrailingZeros ()
    {
        BigDecimal b = super.stripTrailingZeros();
        BigDecimal c = imaginary.stripTrailingZeros();
        return new BigNumber(b.toBigInteger(), c.toBigInteger());
    }

    public String toString()
    {
        if (imaginary.compareTo(BigNumber.ZERO)==0)
            return super.toString();
        else
            return super.toString()+"+"+imaginary.toString()+"i";
    }

    public String toPlainString()
    {
        if (imaginary.compareTo(BigNumber.ZERO)==0)
            return super.toPlainString();
        else
            return super.toPlainString()+"+"+imaginary.toPlainString()+"i";
    }


//    public BigInteger toBigIntegerExact ()
//    {
//        return super.toBigIntegerExact();
//    }

    static public BigNumber valueOf (double d, double i)
    {
        BigDecimal b = BigDecimal.valueOf(d);
        BigDecimal c = BigDecimal.valueOf(i);
        return new BigNumber(b.toBigInteger(), c.toBigInteger());
    }

    public static final BigNumber ONE = new BigNumber(BigDecimal.ONE.toBigInteger(),
            BigDecimal.ZERO.toBigInteger());
    public static final BigNumber ZERO = new BigNumber(BigDecimal.ZERO.toBigInteger(),
            BigDecimal.ZERO.toBigInteger());

    public BigDecimal imaginary = BigDecimal.ZERO;
}
