package com.udojava.evalex;

import org.apache.commons.math3.complex.Complex;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2/19/2017.
 */
public class MyComplex //extends BigDecimal
{
    public double imaginary = 0.0;
    public double real = 0.0;
    public ArrayList<MyComplex> list = new ArrayList<>();
    public ValueType type;

    public MyComplex (List<MyComplex> l)
    {
        list.addAll(l);
        type = ValueType.ARRAY;
    }


    public MyComplex (double val, double img)
    {
        real = val;
        imaginary = img;
        type = ValueType.COMPLEX;
    }

    public MyComplex (double val)
    {
        real = val;
        type = ValueType.REAL;
    }

    public MyComplex (BigDecimal re, BigDecimal img)
    {
        this(Double.toString(re.doubleValue()), Double.toString(img.doubleValue()));
    }

    public MyComplex (BigDecimal re)
    {
        this(Double.toString(re.doubleValue()));
    }

    public MyComplex (String val, String img)
    {
        real = Double.parseDouble(val);
        imaginary = Double.parseDouble(img);
        type = ValueType.COMPLEX;
    }

    public MyComplex (String val)
    {
        real = Double.parseDouble(val);
        type = ValueType.REAL;
    }

    public MyComplex (BigInteger val, BigInteger img)
    {
        real = val.doubleValue();
        imaginary = img.doubleValue();
        type = ValueType.COMPLEX;
    }

    public MyComplex (BigInteger val)
    {
        real = val.doubleValue();
        type = ValueType.REAL;
    }

    public int compareToReal (MyComplex val)
    {
        if (real == val.real)
            return 0;
        if (real > val.real)
            return 1;
        return -1;
    }

    public BigInteger toBigIntegerReal()
    {
        return BigInteger.valueOf((long)real);
    }

    public BigInteger toBigIntegerImaginary()
    {
        return BigInteger.valueOf((long)real);
    }


    public MyComplex conjugate ()
    {
        Complex c = new Complex(real, imaginary);
        c = c.conjugate();
        return new MyComplex(c.getReal(), c.getImaginary());
    }

    public MyComplex invert ()
    {
        Complex c = new Complex(real, imaginary);
        c = c.reciprocal();
        return new MyComplex(c.getReal(), c.getImaginary());
    }

    public MyComplex cos ()
    {
        Complex c = new Complex(real, imaginary);
        c = c.cos();
        return new MyComplex(c.getReal(), c.getImaginary());
    }

    public MyComplex sin ()
    {
        Complex c = new Complex(real, imaginary);
        c = c.sin();
        return new MyComplex(c.getReal(), c.getImaginary());
    }

    public MyComplex tan ()
    {
        Complex c = new Complex(real, imaginary);
        c = c.tan();
        return new MyComplex(c.getReal(), c.getImaginary());
    }

    public MyComplex acos ()
    {
        Complex c = new Complex(real, imaginary);
        c = c.acos();
        return new MyComplex(c.getReal(), c.getImaginary());
    }

    public MyComplex asin ()
    {
        Complex c = new Complex(real, imaginary);
        c = c.sin();
        return new MyComplex(c.getReal(), c.getImaginary());
    }

    public MyComplex atan ()
    {
        Complex c = new Complex(real, imaginary);
        c = c.acos();
        return new MyComplex(c.getReal(), c.getImaginary());
    }

    public MyComplex sinh ()
    {
        Complex c = new Complex(real, imaginary);
        c = c.sinh();
        return new MyComplex(c.getReal(), c.getImaginary());
    }

    public MyComplex cosh ()
    {
        Complex c = new Complex(real, imaginary);
        c = c.cosh();
        return new MyComplex(c.getReal(), c.getImaginary());
    }

    public MyComplex tanh ()
    {
        Complex c = new Complex(real, imaginary);
        c = c.tanh();
        return new MyComplex(c.getReal(), c.getImaginary());
    }

    public MyComplex sqrt ()
    {
        Complex c = new Complex(real, imaginary);
        c = c.sqrt();
        return new MyComplex(c.getReal(), c.getImaginary());
    }

    public MyComplex add (MyComplex n)
    {
        Complex c = new Complex(real, imaginary);
        Complex d = new Complex(n.real, n.imaginary);
        c = c.add(d);
        return new MyComplex(c.getReal(), c.getImaginary());
    }

//    public MyComplex nthRoot(int n)
//    {
//        Complex c = new Complex (real, imaginary);
//        c = c.c.nthRoot(n);
//        return new MyComplex (c.getReal(), c.getImaginary());
//    }

//    public MyComplex remainder (MyComplex n, MathContext m)
//    {
//
//        Complex c = new Complex (real, imaginary);
//        Complex d = new Complex (n.real, n.imaginary);
//        c = c.subtract(d);
//        return new MyComplex (c.getReal(), c.getImaginary());
//    }

    public MyComplex subtract (MyComplex n)
    {
        Complex c = new Complex(real, imaginary);
        Complex d = new Complex(n.real, n.imaginary);
        c = c.subtract(d);
        return new MyComplex(c.getReal(), c.getImaginary());
    }

    // (a+bi)(c+di) = (ac−bd) + (ad+bc)i
    public MyComplex multiply (MyComplex n)
    {
        Complex c = new Complex(real, imaginary);
        Complex d = new Complex(n.real, n.imaginary);
        c = c.multiply(d);
        return new MyComplex(c.getReal(), c.getImaginary());
    }

//    public MyComplex pow (int n, MathContext m)
//    {
//        BigDecimal b = super.pow(n, m);
//        return new MyComplex(b, BigDecimal.ZERO);
//    }

    public MyComplex divide (MyComplex n, MathContext m)
    {
        Complex c = new Complex(real, imaginary);
        Complex d = new Complex(n.real, n.imaginary);
        c = c.divide(d);
        return new MyComplex(c.getReal(), c.getImaginary());
    }

    public MyComplex pow (MyComplex n)
    {
        Complex c = new Complex(real, imaginary);
        Complex d = new Complex(n.real, n.imaginary);
        c = c.pow(d);
        return new MyComplex(c.getReal(), c.getImaginary());
    }

    public double angle ()
    {
        Complex c = new Complex(real, imaginary);
        double d = c.getArgument();
        return d;
    }

    public double abs ()
    {
        Complex c = new Complex(real, imaginary);
        return c.abs();
    }

    public MyComplex negate ()
    {
        Complex c = new Complex(real, imaginary);
        c = c.negate();
        return new MyComplex(c.getReal(), c.getImaginary());
    }

    public String toStringComplex ()
    {
        StringBuilder sb = new StringBuilder();
        if (type == ValueType.ARRAY)
        {
            sb.append('[');
            for (int s=0; s<list.size(); s++)
            {
                sb.append(list.get(s).toStringComplex()).append(',');
            }
            sb.setLength(sb.length() - 1);
            sb.append(']');
        }
        else
        {
            if (real != 0.0 || imaginary == 0.0)
                sb.append(Double.toString(real));
            if (imaginary > 0.0)
            {
                sb.append("+").append(Double.toString(imaginary)).append("i");
            }
            else if (imaginary < 0.0)
            {
                sb.append(Double.toString(imaginary)).append("i");
            }
        }
        return sb.toString();
    }

    public String toPlainStringComplex ()
    {
        return toStringComplex();
    }

    public double norm ()
    {
        return real*real + imaginary*imaginary;
    }
}
