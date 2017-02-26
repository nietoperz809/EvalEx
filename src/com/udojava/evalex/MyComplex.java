package com.udojava.evalex;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.complex.Complex;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2/19/2017.
 */
public class MyComplex
{
    private PolynomialFunction m_poly;
    public double imaginary = 0.0;
    public double real = 0.0;
    public final ArrayList<MyComplex> list = new ArrayList<>();
    public ValueType type;

    public MyComplex (List<MyComplex> l)
    {
        list.addAll(l);
        type = ValueType.ARRAY;
    }

    public MyComplex (PolynomialFunction p)
    {
        m_poly = p;
        double[] d = p.getCoefficients();
        list.addAll(MyComplex.listFromRealArray(d));
        type = ValueType.ARRAY;
    }

    public MyComplex (Complex c)
    {
        real = c.getReal();
        imaginary = c.getImaginary();
        type = ValueType.COMPLEX;
    }

    public boolean equals (Object o)
    {
        MyComplex oo = (MyComplex)o;
        if (type == ValueType.REAL)
        {
            return real == oo.real;
        }
        if (type == ValueType.COMPLEX)
        {
            return real == oo.real && imaginary == oo.imaginary;
        }
        return CollectionUtils.isEqualCollection(this.list, oo.list);
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
        return new MyComplex(c);
    }

    public MyComplex invert ()
    {
        Complex c = new Complex(real, imaginary);
        c = c.reciprocal();
        return new MyComplex(c);
    }

    public MyComplex cos ()
    {
        Complex c = new Complex(real, imaginary);
        c = c.cos();
        MyComplex m = new MyComplex(c);
        m.type = type;
        return m;
    }

    public MyComplex sin ()
    {
        Complex c = new Complex(real, imaginary);
        c = c.sin();
        MyComplex m = new MyComplex(c);
        m.type = type;
        return m;
    }

    public MyComplex tan ()
    {
        Complex c = new Complex(real, imaginary);
        c = c.tan();
        MyComplex m = new MyComplex(c);
        m.type = type;
        return m;
    }

    public MyComplex acos ()
    {
        Complex c = new Complex(real, imaginary);
        c = c.acos();
        MyComplex m = new MyComplex(c);
        m.type = type;
        return m;
    }

    public MyComplex asin ()
    {
        Complex c = new Complex(real, imaginary);
        c = c.sin();
        MyComplex m = new MyComplex(c);
        m.type = type;
        return m;
    }

    public MyComplex atan ()
    {
        Complex c = new Complex(real, imaginary);
        c = c.acos();
        MyComplex m = new MyComplex(c);
        m.type = type;
        return m;
    }

    public MyComplex sinh ()
    {
        Complex c = new Complex(real, imaginary);
        c = c.sinh();
        MyComplex m = new MyComplex(c);
        m.type = type;
        return m;
    }

    public MyComplex cosh ()
    {
        Complex c = new Complex(real, imaginary);
        c = c.cosh();
        MyComplex m = new MyComplex(c);
        m.type = type;
        return m;
    }

    public MyComplex tanh ()
    {
        Complex c = new Complex(real, imaginary);
        c = c.tanh();
        MyComplex m = new MyComplex(c);
        m.type = type;
        return m;
    }

    public MyComplex sqrt ()
    {
        Complex c = new Complex(real, imaginary);
        c = c.sqrt();
        MyComplex m = new MyComplex(c);
        m.type = type;
        return m;
    }

    public MyComplex add (MyComplex n)
    {
        Complex c = new Complex(real, imaginary);
        Complex d = new Complex(n.real, n.imaginary);
        c = c.add(d);
        MyComplex m = new MyComplex(c);
        if (type == ValueType.REAL && n.type == ValueType.REAL)
            m.type = ValueType.REAL;
        return m;
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
        MyComplex m = new MyComplex(c);
        if (type == ValueType.REAL && n.type == ValueType.REAL)
            m.type = ValueType.REAL;
        return m;
    }

    // (a+bi)(c+di) = (ac−bd) + (ad+bc)i
    public MyComplex multiply (MyComplex n)
    {
        Complex c = new Complex(real, imaginary);
        Complex d = new Complex(n.real, n.imaginary);
        c = c.multiply(d);
        MyComplex m = new MyComplex(c);
        if (type == ValueType.REAL && n.type == ValueType.REAL)
            m.type = ValueType.REAL;
        return m;
    }

//    public MyComplex pow (int n, MathContext m)
//    {
//        BigDecimal b = super.pow(n, m);
//        return new MyComplex(b, BigDecimal.ZERO);
//    }

    public MyComplex divide (MyComplex n)
    {
        Complex c = new Complex(real, imaginary);
        Complex d = new Complex(n.real, n.imaginary);
        c = c.divide(d);
        MyComplex m = new MyComplex(c);
        if (type == ValueType.REAL && n.type == ValueType.REAL)
            m.type = ValueType.REAL;
        return m;
    }

    public MyComplex pow (MyComplex n)
    {
        Complex c = new Complex(real, imaginary);
        Complex d = new Complex(n.real, n.imaginary);
        c = c.pow(d);
        MyComplex m = new MyComplex(c);
        if (type == ValueType.REAL && n.type == ValueType.REAL)
            m.type = ValueType.REAL;
        return m;
    }

    public double angle ()
    {
        Complex c = new Complex(real, imaginary);
        return c.getArgument();
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
        return new MyComplex(c);
    }

    private String fmt (Double d)
    {
        DecimalFormat df = new DecimalFormat("#.############");
        return df.format(d); //d.longValue() == d ? "" + d.longValue() : "" + d;
    }

    public boolean isPoly()
    {
        return m_poly != null;
    }

    public PolynomialFunction getPoly()
    {
        return m_poly;
    }

    public String toStringComplex ()
    {
        if (m_poly != null)
        {
            return m_poly.toString();
        }
        StringBuilder sb = new StringBuilder();
        if (type == ValueType.ARRAY)
        {
            sb.append('[');
            for (MyComplex aList : list)
            {
                sb.append(aList.toStringComplex()).append(',');
            }
            sb.setLength(sb.length() - 1);
            sb.append(']');
        }
        else
        {
            if (real != 0.0 || imaginary == 0.0)
                sb.append(fmt(real));
            if (imaginary > 0.0)
            {
                sb.append("+").append(fmt(imaginary)).append("i");
            }
            else if (imaginary < 0.0)
            {
                sb.append(fmt(imaginary)).append("i");
            }
        }
        return sb.toString();
    }

    public String toPlainStringComplex ()
    {
        return toStringComplex();
    }

    public static double[] getRealArray (List<MyComplex>l)
    {
        double[] d = new double[l.size()];
        for (int s=0; s<l.size(); s++)
        {
            d[s] = l.get(s).real;
        }
        return d;
    }

    public static List<MyComplex> listFromRealArray (double[] d)
    {
        ArrayList<MyComplex> l = new ArrayList<>();
        for (double aD : d)
        {
            l.add(new MyComplex(aD));
        }
        return l;
    }

    public double[] getRealArray ()
    {
        if (type != ValueType.ARRAY)
            throw new ExpressionException("must be array");
        return getRealArray(list);
    }

    public double norm ()
    {
        return real*real + imaginary*imaginary;
    }
}
