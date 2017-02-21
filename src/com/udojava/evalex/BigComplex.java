package com.udojava.evalex;

import org.apache.commons.math3.complex.Complex;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Created by Administrator on 2/19/2017.
 */
public class BigComplex extends BigDecimal
{
    public BigComplex (String val, String img)
    {
        super(val);
        imaginary = new BigDecimal(img);
    }

    public BigComplex (double val, double img)
    {
        super(val);
        imaginary = new BigDecimal(img);
    }

    public BigComplex (BigDecimal re, BigDecimal img)
    {
        this (Double.toString(re.doubleValue()), Double.toString(img.doubleValue()));
    }

    public BigComplex (BigInteger val, BigInteger img)
    {
        super(val);
        imaginary = new BigDecimal(img);
    }
//
//    public BigComplex (int val)
//    {
//        super(val);
//    }
//
//    public BigComplex (long val)
//    {
//        super(val);
//    }
//////////////////////////////////////////////////////////////

    public BigComplex conjugate()
    {
        Complex c = new Complex (this.doubleValue(), imaginary.doubleValue());
        c = c.conjugate();
        return new BigComplex (c.getReal(), c.getImaginary());
    }

    public BigComplex invert()
    {
        Complex c = new Complex (this.doubleValue(), imaginary.doubleValue());
        c = c.reciprocal();
        return new BigComplex (c.getReal(), c.getImaginary());
    }

    public BigComplex negate()
    {
        Complex c = new Complex (this.doubleValue(), imaginary.doubleValue());
        c = c.negate();
        return new BigComplex (c.getReal(), c.getImaginary());
    }


    public BigComplex cos()
    {
        Complex c = new Complex (this.doubleValue(), imaginary.doubleValue());
        c = c.cos();
        return new BigComplex (c.getReal(), c.getImaginary());
    }

    public BigComplex sqrt()
    {
        Complex c = new Complex (this.doubleValue(), imaginary.doubleValue());
        c = c.sqrt();
        return new BigComplex (c.getReal(), c.getImaginary());
    }

    public BigComplex add (BigComplex n)
    {
        Complex c = new Complex (this.doubleValue(), imaginary.doubleValue());
        Complex d = new Complex (n.doubleValue(), n.imaginary.doubleValue());
        c = c.add(d);
        return new BigComplex (c.getReal(), c.getImaginary());
    }

    public BigComplex subtract (BigComplex n)
    {
        Complex c = new Complex (this.doubleValue(), imaginary.doubleValue());
        Complex d = new Complex (n.doubleValue(), n.imaginary.doubleValue());
        c = c.subtract(d);
        return new BigComplex (c.getReal(), c.getImaginary());
    }

    // (a+bi)(c+di) = (ac−bd) + (ad+bc)i
    public BigComplex multiply (BigComplex n)
    {
        Complex c = new Complex (this.doubleValue(), imaginary.doubleValue());
        Complex d = new Complex (n.doubleValue(), n.imaginary.doubleValue());
        c = c.multiply(d);
        return new BigComplex (c.getReal(), c.getImaginary());
    }

    public BigComplex divide (BigComplex n, MathContext m)
    {
        Complex c = new Complex (this.doubleValue(), imaginary.doubleValue());
        Complex d = new Complex (n.doubleValue(), n.imaginary.doubleValue());
        c = c.divide(d);
        return new BigComplex (c.getReal(), c.getImaginary());
    }

//    public BigComplex nthRoot(int n)
//    {
//        Complex c = new Complex (this.doubleValue(), imaginary.doubleValue());
//        c = c.c.nthRoot(n);
//        return new BigComplex (c.getReal(), c.getImaginary());
//    }

//    public BigComplex remainder (BigComplex n, MathContext m)
//    {
//
//        Complex c = new Complex (this.doubleValue(), imaginary.doubleValue());
//        Complex d = new Complex (n.doubleValue(), n.imaginary.doubleValue());
//        c = c.subtract(d);
//        return new BigComplex (c.getReal(), c.getImaginary());
//    }

    public BigComplex pow (BigComplex n)
    {
        Complex c = new Complex (this.doubleValue(), imaginary.doubleValue());
        Complex d = new Complex (n.doubleValue(), n.imaginary.doubleValue());
        c = c.pow(d);
        return new BigComplex (c.getReal(), c.getImaginary());
    }

    public BigDecimal angle ()
    {
        Complex c = new Complex (this.doubleValue(), imaginary.doubleValue());
        double d = c.getArgument();
        return new BigDecimal (d);
    }

//    public BigComplex pow (int n, MathContext m)
//    {
//        BigDecimal b = super.pow(n, m);
//        return new BigComplex(b, BigDecimal.ZERO);
//    }

    public BigDecimal abs ()
    {
        Complex c = new Complex (this.doubleValue(), imaginary.doubleValue());
        return new BigDecimal (c.abs());
    }

    public BigComplex setScale (int s, RoundingMode r)
    {
        BigDecimal b = super.setScale(s, r);
        BigDecimal c = imaginary.setScale(s, r);
        return new BigComplex(b, c);
    }

    public BigComplex stripTrailingZeros ()
    {
        BigDecimal b = super.stripTrailingZeros();
        BigDecimal c = imaginary.stripTrailingZeros();
        return new BigComplex(b, c);
    }

    public String toStringComplex()
    {
        if (imaginary.compareTo(BigComplex.ZERO)==0)
            return toString();
        else
            return toString()+"+"+imaginary.toString()+"i";
    }

    public String toPlainStringComplex()
    {
        if (imaginary.compareTo(BigComplex.ZERO)==0)
            return toPlainString();
        else
            return toPlainString()+"+"+imaginary.toPlainString()+"i";
    }

    public BigDecimal norm()
    {
        return super.multiply(this)
                .add(imaginary.multiply(imaginary));
    }

    static public BigComplex valueOf (double d, double i)
    {
        BigDecimal b = BigDecimal.valueOf(d);
        BigDecimal c = BigDecimal.valueOf(i);
        return new BigComplex(b, c);
    }

    public static final BigComplex ONE = new BigComplex(BigDecimal.ONE,
            BigDecimal.ZERO);
    public static final BigComplex ZERO = new BigComplex(BigDecimal.ZERO,
            BigDecimal.ZERO);

    public BigDecimal imaginary = BigDecimal.ZERO;
}
