/*
 * Copyright 2012 Udo Klimaschewski
 * 
 * http://UdoJava.com/
 * http://about.me/udo.klimaschewski
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 */
package com.udojava.evalex;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.util.CombinatoricsUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

import static org.apache.commons.math3.primes.Primes.nextPrime;
import static org.apache.commons.math3.stat.StatUtils.variance;

/**
 * <h1>EvalEx - Java Expression Evaluator</h1>
 *
 * @author Udo Klimaschewski (http://about.me/udo.klimaschewski)
 */
public class Expression
{
    /**
     * What character to use for decimal separators.
     */
    private static final char decimalSeparator = '.';
    /**
     * What character to use for minus sign (negative values).
     */
    private static final char minusSign = '-';
    /**
     * The BigComplex representation of the left parenthesis,
     * used for parsing varying numbers of function parameters.
     */
    private static final LazyNumber PARAMS_START = () -> null;
    private final LinkedList<String> history;
    /**
     * All defined operators with name and implementation.
     */
    private final Map<String, Operator> operators = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    /**
     * All defined functions with name and implementation.
     */
    private final Map<String, LazyFunction> functions = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    /**
     * All defined variables with name and value.
     */
    //private final Map<String, BigComplex> variables = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private Variables  mainVars;

    /**
     * The characters (other than letters and digits) allowed as the first character in a variable.
     */
    private String firstVarChars = "_";
    /**
     * The characters (other than letters and digits) allowed as the second or subsequent characters in a variable.
     */
    private String varChars = "_";
    /**
     * The current infix expression, with optional variable substitutions.
     */
    private String expression = null;
    /**
     * The cached RPN (Reverse Polish Notation) of the expression.
     */
    private List<String> rpn = null;

    /**
     * Creates a new expression instance from an expression string with a given
     * default match context.
     *
     * @param expression The expression. E.g. <code>"2.4*sin(3)/(2-4)"</code> or
     *                   <code>"sin(y)>0 & max(z, 3)>3"</code>
     */
    public Expression (String expression, LinkedList<String> hist, Variables vars)
    {
        this.history = hist;
        this.expression = expression;

        mainVars = vars;

        addOperator(new Operator("+", 20, true,
                "Addition")
        {
            @Override
            public BigComplex eval (BigComplex v1, BigComplex v2)
            {
                return v1.add(v2);
            }
        });
        addOperator(new Operator("-", 20, true,
                "Subtraction")
        {
            @Override
            public BigComplex eval (BigComplex v1, BigComplex v2)
            {
                return v1.subtract(v2);
            }
        });
        addOperator(new Operator("*", 30, true,
                "Real number multiplication")
        {
            @Override
            public BigComplex eval (BigComplex v1, BigComplex v2)
            {
                return v1.multiply(v2);
            }
        });
        addOperator(new Operator("/", 30, true,
                "Real number division")
        {
            @Override
            public BigComplex eval (BigComplex v1, BigComplex v2)
            {
                return v1.divide(v2, MathContext.DECIMAL128);
            }
        });
        addOperator(new Operator("%", 30, true,
                "Remainder of integer division")
        {
            @Override
            public BigComplex eval (BigComplex v1, BigComplex v2)
            {
                BigDecimal r = v1.remainder(v2);
                return new BigComplex (r, BigDecimal.ZERO);
            }
        });
        addOperator(new Operator("^", 40, false,
                "Exponentation. See: https://en.wikipedia.org/wiki/Exponentiation")
        {
            @Override
            public BigComplex eval (BigComplex v1, BigComplex v2)
            {
                return v1.pow(v2);
            }
        });
        addOperator(new Operator("&&", 4, false,
                "Logical AND. Evaluates to 1 if both operands are not 0")
        {
            @Override
            public BigComplex eval (BigComplex v1, BigComplex v2)
            {
                boolean b1 = !v1.equals(BigComplex.ZERO);
                boolean b2 = !v2.equals(BigComplex.ZERO);
                return b1 && b2 ? BigComplex.ONE : BigComplex.ZERO;
            }
        });

        addOperator(new Operator("||", 2, false,
                "Logical OR. Evaluates to 0 if both operands are 0")
        {
            @Override
            public BigComplex eval (BigComplex v1, BigComplex v2)
            {
                boolean b1 = !v1.equals(BigComplex.ZERO);
                boolean b2 = !v2.equals(BigComplex.ZERO);
                return b1 || b2 ? BigComplex.ONE : BigComplex.ZERO;
            }
        });

        addOperator(new Operator(">", 10, false,
                "Greater than. See: See: https://en.wikipedia.org/wiki/Inequality_(mathematics)")
        {
            @Override
            public BigComplex eval (BigComplex v1, BigComplex v2)
            {
                return v1.compareTo(v2) == 1 ? BigComplex.ONE : BigComplex.ZERO;
            }
        });

        addOperator(new Operator(">=", 10, false,
                "Greater or equal")
        {
            @Override
            public BigComplex eval (BigComplex v1, BigComplex v2)
            {
                return v1.compareTo(v2) >= 0 ? BigComplex.ONE : BigComplex.ZERO;
            }
        });

        addOperator(new Operator("<", 10, false,
                "Less than. See: https://en.wikipedia.org/wiki/Inequality_(mathematics)")
        {
            @Override
            public BigComplex eval (BigComplex v1, BigComplex v2)
            {
                return v1.compareTo(v2) == -1 ? BigComplex.ONE
                        : BigComplex.ZERO;
            }
        });

        addOperator(new Operator("<=", 10, false,
                "less or equal")
        {
            @Override
            public BigComplex eval (BigComplex v1, BigComplex v2)
            {
                return v1.compareTo(v2) <= 0 ? BigComplex.ONE : BigComplex.ZERO;
            }
        });

        addOperator(new Operator("->", 7, false,
                "Set variable v to new value ")
        {
            @Override
            public BigComplex eval (BigComplex v1, BigComplex v2)
            {
                if (v1 instanceof PitDecimal)
                {
                    PitDecimal target = (PitDecimal) v1;
                    String s = target.getVarToken();
                    setVariable(s, v2);
                    return v2;
                }
                throw new ExpressionException("LHS not variable");
            }
        });

        addOperator(new Operator("=", 7, false,
                "Equality")
        {
            @Override
            public BigComplex eval (BigComplex v1, BigComplex v2)
            {
                return v1.compareTo(v2) == 0 ? BigComplex.ONE : BigComplex.ZERO;
            }
        });

        addOperator(new Operator("!=", 7, false,
                "Inequality. See: https://en.wikipedia.org/wiki/Inequality_(mathematics)")
        {
            @Override
            public BigComplex eval (BigComplex v1, BigComplex v2)
            {
                return v1.compareTo(v2) != 0 ? BigComplex.ONE : BigComplex.ZERO;
            }
        });
        addOperator(new Operator("or", 7, false,
                "Bitwise OR. See: https://en.wikipedia.org/wiki/Logical_disjunction")
        {
            @Override
            public BigComplex eval (BigComplex v1, BigComplex v2)
            {
                return new BigComplex(v1.longValue() | v2.longValue(), 0);
            }
        });
        addOperator(new Operator("and", 7, false,
                "Bitwise AND. See: https://en.wikipedia.org/wiki/Logical_conjunction")
        {
            @Override
            public BigComplex eval (BigComplex v1, BigComplex v2)
            {
                return new BigComplex(v1.longValue() & v2.longValue(), 0);
            }
        });
        addOperator(new Operator("xor", 7, false,
                "Bitwise XOR, See: https://en.wikipedia.org/wiki/Exclusive_or")
        {
            @Override
            public BigComplex eval (BigComplex v1, BigComplex v2)
            {
                return new BigComplex(v1.longValue() ^ v2.longValue(), 0);
            }
        });

        addOperator(new Operator("!", 50, true,
                "Factorial. See https://en.wikipedia.org/wiki/Factorial")
        {
            public BigInteger factorial(int n)
            {
                BigInteger factorial = BigInteger.ONE;
                for (int i = 1; i <= n; i++)
                {
                    factorial = factorial.multiply(BigInteger.valueOf(i));
                }
                return factorial;
            }
            @Override
            public BigComplex eval (BigComplex v1, BigComplex v2)
            {
                BigInteger fact = factorial(v1.intValue());
                return new BigComplex(fact, BigInteger.ZERO);
            }
        });

        addOperator(new Operator("~", 8, false,
                "Bitwise negation")
        {
            @Override
            public BigComplex eval (BigComplex v1, BigComplex v2)
            {
                BigInteger bi = v2.toBigInteger();
                int c = bi.bitLength();
                if (c == 0)
                {
                    return BigComplex.ONE;
                }
                for (int s = 0; s < c; s++)
                {
                    bi = bi.flipBit(s);
                }
                return new BigComplex(bi, BigInteger.ZERO);
            }
        });

        addOperator(new Operator("shl", 8, false,
                "Left Bit shift")
        {
            @Override
            public BigComplex eval (BigComplex v1, BigComplex v2)
            {
                return new BigComplex(v1.longValue() << v2.longValue(), 0);
            }
        });

        addOperator(new Operator("shr", 8, false,
                "Right bit shift")
        {
            @Override
            public BigComplex eval (BigComplex v1, BigComplex v2)
            {
                return new BigComplex(v1.longValue() >>> v2.longValue(), 0);
            }
        });

        addFunction(new Function("NOT", 1,
                "evaluates to 0 if argument != 0")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                boolean zero = parameters.get(0).compareTo(BigComplex.ZERO) == 0;
                return zero ? BigComplex.ONE : BigComplex.ZERO;
            }
        });

        addFunction(new Function("RND", 2,
                "Give random number in the range between first and second argument")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                double low = parameters.get(0).doubleValue();
                double high = parameters.get(1).doubleValue();
                return new BigComplex(low + Math.random() * (high - low), 0);
            }
        });

        MersenneTwister mers = new MersenneTwister(System.nanoTime());

        addFunction(new Function("MRS", 0,
                "Mersenne twister random generator")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                return new BigComplex(mers.nextDouble(), 0);
            }
        });

        addFunction(new Function("BIN", 2,
                "Binomial Coefficient 'n choose k'")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                int n = parameters.get(0).intValue();
                int k = parameters.get(1).intValue();
                double d = CombinatoricsUtils.binomialCoefficientDouble(n, k);
                return new BigComplex(d, 0);
            }
        });
        addFunction(new Function("STIR", 2,
                "Stirling number of 2nd kind: http://mathworld.wolfram.com/StirlingNumberoftheSecondKind.html")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                int n = parameters.get(0).intValue();
                int k = parameters.get(1).intValue();
                double d = CombinatoricsUtils.stirlingS2(n, k);
                return new BigComplex(d, 0);
            }
        });

        addFunction(new Function("SIN", 1,
                "Sine function")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                double d = Math.sin(parameters.get(0)
                        .doubleValue());
                return new BigComplex(d, 0);
            }
        });
        addFunction(new Function("COS", 1,
                "Cosine function")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                double d = Math.cos(parameters.get(0)
                        .doubleValue());
                return new BigComplex(d, 0);
            }
        });
        addFunction(new Function("TAN", 1,
                "Tangent")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                double d = Math.tan(parameters.get(0)
                        .doubleValue());
                return new BigComplex(d, 0);
            }
        });
        addFunction(new Function("ASIN", 1,
                "Reverse Sine")
        { // added by av
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                double d = Math.asin(parameters.get(0)
                        .doubleValue());
                return new BigComplex(d, 0);
            }
        });
        addFunction(new Function("ACOS", 1,
                "Reverse Cosine")
        { // added by av
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                double d = Math.acos(parameters.get(0)
                        .doubleValue());
                return new BigComplex(d, 0);
            }
        });
        addFunction(new Function("ATAN", 1,
                "Reverse Tangent")
        { // added by av
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                double d = Math.atan(parameters.get(0)
                        .doubleValue());
                return new BigComplex(d, 0);
            }
        });
        addFunction(new Function("SINH", 1,
                "Hyperbolic Sine")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                double d = Math.sinh(parameters.get(0).doubleValue());
                return new BigComplex(d, 0);
            }
        });
        addFunction(new Function("COSH", 1,
                "Hyperbolic Cosine")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                double d = Math.cosh(parameters.get(0).doubleValue());
                return new BigComplex(d, 0);
            }
        });
        addFunction(new Function("TANH", 1,
                "Hyperbolic Tangent")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                double d = Math.tanh(parameters.get(0).doubleValue());
                return new BigComplex(d, 0);
            }
        });
        addFunction(new Function("RAD", 1,
                "Transform degree to radian")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                double d = Math.toRadians(parameters.get(0).doubleValue());
                return new BigComplex(d, 0);
            }
        });
        addFunction(new Function("DEG", 1,
                "Transform radian to degree")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                double d = Math.toDegrees(parameters.get(0).doubleValue());
                return new BigComplex(d, 0);
            }
        });
        addFunction(new Function("MAX", -1,
                "Find the biggest value in a list")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                if (parameters.size() == 0)
                {
                    throw new ExpressionException("MAX requires at least one parameter");
                }
                BigComplex max = null;
                for (BigComplex parameter : parameters)
                {
                    if (max == null || parameter.compareTo(max) > 0)
                    {
                        max = parameter;
                    }
                }
                return max;
            }
        });
///////////////////////////////////////////////////////
        addFunction(new Function("IF", 3,
                "Conditional: give param3 if param1 is 0, otherwise param2")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                if (parameters.get(0).compareTo(BigComplex.ZERO)==0)
                {
                    return parameters.get(2);
                }
                return parameters.get(1);
            }
        });

        addFunction(new Function("PERC", 2,
                "Get param1 percent of param2")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                return parameters.get(0).
                        divide(new BigComplex(100, 0), MathContext.DECIMAL128).
                        multiply(parameters.get(1));
            }
        });

        addFunction(new Function("PER", 2,
                "How many percent is param1 of param2")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                return parameters.get(0).
                        multiply(new BigComplex(100, 0)).
                        divide(parameters.get(1), MathContext.DECIMAL128);
            }
        });

        addFunction(new Function("H", 1,
                "Evaluate _history element")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                int i = parameters.get(0).intValue();
                Expression ex = new Expression(history.get(i), history, mainVars);
                return ex.eval();
            }
        });

        addFunction(new Function("MERS", 1,
                "Calculate Mersenne Number")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                BigComplex p = parameters.get(0);
                return new BigComplex(2, 0).pow(p).subtract(BigComplex.ONE);
            }
        });

        addFunction(new Function("GCD", 2,
                "Find greatest common divisor of 2 values")
        {
            private BigDecimal GCD (BigDecimal a, BigDecimal b)
            {
                if (b.compareTo(BigComplex.ZERO) == 0)
                {
                    return a;
                }
                return GCD(b, a.remainder(b, MathContext.DECIMAL128));
            }

            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                BigDecimal a = parameters.get(0);
                BigDecimal b = parameters.get(1);
                return new BigComplex(GCD(a,b), BigDecimal.ZERO);
            }
        });
        addFunction(new Function("LCM", 2,
                "Find least common multiple of 2 values")
        {
            private final Function gcd = (Function) functions.get("GCD");

            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                return parameters.get(0).multiply(parameters.get(1))
                        .divide(gcd.eval(parameters), MathContext.DECIMAL128);
            }
        });
        addFunction(new Function("AMEAN", -1,
                "Arithmetic mean of a set of values")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                if (parameters.size() == 0)
                {
                    throw new ExpressionException("MEAN requires at least one parameter");
                }
                BigComplex res = BigComplex.ZERO;
                int num = 0;
                for (BigComplex parameter : parameters)
                {
                    res = res.add(parameter);
                    num++;
                }
                return res.divide(new BigComplex(num, 0), MathContext.DECIMAL128);
            }
        });
        addFunction(new Function("BYT", -1,
                "Value from sequence of bytes")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                if (parameters.size() == 0)
                {
                    return BigComplex.ZERO;
                }
                BigInteger res = BigInteger.ZERO;
                for (BigComplex parameter : parameters)
                {
                    if (parameter.intValue() < 0 || parameter.intValue() > 255)
                    {
                        throw new ExpressionException("not a byte value");
                    }
                    res = res.shiftLeft(8);
                    res = res.or(parameter.toBigInteger());
                }
                return new BigComplex(res, BigInteger.ZERO);
            }
        });
        addFunction(new Function("ANG", 1,
                "Angle phi of complex number in radians")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                BigDecimal b = parameters.get(0).angle();
                return new BigComplex (b, BigDecimal.ZERO);
            }
        });

        addFunction(new Function("IM", 1,
                "Get imaginary part")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                return new BigComplex(parameters.get(0).imaginary, BigDecimal.ZERO);
            }
        });

        addFunction(new Function("RE", 1,
                "Get real part")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                return new BigComplex(parameters.get(0), BigDecimal.ZERO);
            }
        });

        addFunction(new Function("POL", 2,
                "Make complex number from polar coords. angle is first arg")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                double angle = parameters.get(0).doubleValue();
                double len = parameters.get(1).doubleValue();
                return new BigComplex (len*Math.cos(angle), len*Math.sin(angle));
            }
        });

        addFunction(new Function("GMEAN", -1,
                "Geometric mean of a set of values")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                if (parameters.size() == 0)
                {
                    throw new ExpressionException("MEAN requires at least one parameter");
                }
                BigComplex res = BigComplex.ONE;
                int num = 0;
                for (BigComplex parameter : parameters)
                {
                    res = res.multiply(parameter);
                    num++;
                }
                res = new BigComplex (res.abs(), BigDecimal.ZERO);
                BigDecimal bd = MathTools.nthRoot(num, res, new BigComplex(0.000000001, 0));
                return new BigComplex (bd, BigDecimal.ZERO);
            }
        });
        addFunction(new Function("HMEAN", -1,
                "Harmonic mean of a set of values")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                if (parameters.size() == 0)
                {
                    throw new ExpressionException("MEAN requires at least one parameter");
                }
                BigComplex res = BigComplex.ZERO;
                int num = 0;
                for (BigComplex parameter : parameters)
                {
                    res = res.add(BigComplex.ONE.divide(parameter, MathContext.DECIMAL128));
                    num++;
                }
                res = new BigComplex(res.abs(), BigDecimal.ZERO);
                return new BigComplex(num, 0).divide(res, MathContext.DECIMAL128);
            }
        });

        addFunction(new Function("VAR", -1,
                "Variance of a set of values")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                if (parameters.size() == 0)
                {
                    throw new ExpressionException("MEAN requires at least one parameter");
                }
                double[] arr = new double[parameters.size()];
                for (int s = 0; s < parameters.size(); s++)
                {
                    arr[s] = parameters.get(s).doubleValue();
                }
                return new BigComplex(variance(arr), 0);
            }
        });

        addFunction(new Function("NPR", 1,
                "Next prime number greater or equal the argument")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                return new BigComplex(nextPrime(parameters.get(0).intValue()), 0);
            }
        });

        addFunction(new Function("NSWP", 1,
                "Swap nibbles")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                BigInteger bi = parameters.get(0).toBigInteger();
                String s = bi.toString(16);
                s = new StringBuilder(s).reverse().toString();
                return new BigComplex(new BigInteger(s, 16), BigInteger.ZERO);
            }
        });

        addFunction(new Function("BSWP", 1,
                "Swap bytes")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                BigInteger bi = parameters.get(0).toBigInteger();
                String s = bi.toString(16);
                while (s.length() % 4 != 0)
                {
                    s = s + "0";
                }
                if (bi.intValue() < 256)
                {
                    s = "00" + s;
                }
                s = MathTools.reverseHex(s);
                return new BigComplex(new BigInteger(s, 16), BigInteger.ZERO);
            }
        });

        addFunction(new Function("PYT", 2,
                "Pythagoras's result = sqrt(param1^2+param2^2) https://en.wikipedia.org/wiki/Pythagorean_theorem")
        {
            @Override
            public BigComplex eval (List<BigComplex> par)
            {
                double a = par.get(0).doubleValue();
                double b = par.get(1).doubleValue();
                return new BigComplex(Math.sqrt(a * a + b * b), 0);
            }
        });

        addFunction(new Function("FIB", 1,
                "Fibonacci number")
        {
            // --Commented out by Inspection (2/19/2017 7:46 PM):private final Operator exp = operators.get("^");

            @Override
            public BigComplex eval (List<BigComplex> par)
            {
                return MathTools.iterativeFibonacci(par.get(0).intValue());
            }
        });

        ///////////////////////////////////////////////

        addFunction(new Function("MIN", -1,
                "Find the smallest in a list of values")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                if (parameters.size() == 0)
                {
                    throw new ExpressionException("MIN requires at least one parameter");
                }
                BigComplex min = null;
                for (BigComplex parameter : parameters)
                {
                    if (min == null || parameter.compareTo(min) < 0)
                    {
                        min = parameter;
                    }
                }
                return min;
            }
        });
        addFunction(new Function("ABS", 1,
                "Get absolute value of a number")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                return new BigComplex (parameters.get(0).abs(), BigDecimal.ZERO);
            }
        });
        addFunction(new Function("LN", 1,
                "Logarithm base e of the argument")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                double d = Math.log(parameters.get(0).doubleValue());
                return new BigComplex(d, 0);
            }
        });
        addFunction(new Function("LOG", 1,
                "Logarithm base 10 of the argument")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                double d = Math.log10(parameters.get(0).doubleValue());
                return new BigComplex(d, 0);
            }
        });
        addFunction(new Function("FLOOR", 1,
                "Rounds DOWN to nearest Integer")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                BigComplex toRound = parameters.get(0);
                return toRound.setScale(0, RoundingMode.FLOOR);
            }
        });
        addFunction(new Function("CEIL", 1,
                "Rounds UP to nearest Integer")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                BigComplex toRound = parameters.get(0);
                return toRound.setScale(0, RoundingMode.CEILING);
            }
        });
        addFunction(new Function("ROU", 1,
                "Rounds to nearest Integer")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                BigComplex toRound = parameters.get(0);
                return toRound.setScale(0, RoundingMode.HALF_UP);
            }
        });
        addFunction(new Function("SQRT", 1,
                "Square root")
        {
            @Override
            public BigComplex eval (List<BigComplex> parameters)
            {
                BigComplex p = parameters.get(0);
                if (p.imaginary.compareTo(BigDecimal.ZERO) == 0)
                {
                    double d = parameters.get(0).doubleValue();
                    return new BigComplex(MathTools.sqrt(p), BigDecimal.ZERO);
                }
                return p.sqrt();
            }
        });
    }

    /**
     * Adds an operator to the list of supported operators.
     *
     * @param operator The operator to add.
     * @return The previous operator with that name, or <code>null</code> if
     * there was none.
     */
    private void addOperator (Operator operator)
    {
        operators.put(operator.getName(), operator);
    }

    /**
     * Sets a variable value.
     *
     * @param variable The variable name.
     * @param value    The variable value.
     * @return The expression, allows to chain methods.
     */
    private void setVariable (String variable, BigComplex value)
    {
        mainVars.put(variable, value);
    }

    /**
     * Adds a function to the list of supported functions
     *
     * @param function The function to add.
     * @return The previous operator with that name, or <code>null</code> if
     * there was none.
     */
    private void addFunction (Function function)
    {
        functions.put(function.getName(), function);
    }

    /**
     * Evaluates the expression.
     *
     * @return The result of the expression.
     */
    public BigComplex eval ()
    {
        Stack<LazyNumber> stack = new Stack<>();

        for (final String token : getRPN())
        {
            if (operators.containsKey(token))
            {
                final LazyNumber v1 = stack.pop();
                final LazyNumber v2 = stack.pop();
                LazyNumber number = () -> operators.get(token).eval(v2.eval(), v1.eval());
                stack.push(number);
            }
            else if (mainVars.getMap().containsKey(token))
            {
                PitDecimal bd = new PitDecimal(mainVars.get(token),
                        mainVars.get(token).imaginary);
                bd.setVarToken(token);
                stack.push(() -> bd);
            }
            else if (functions.containsKey(token.toUpperCase(Locale.ROOT)))
            {
                LazyFunction f = functions.get(token.toUpperCase(Locale.ROOT));
                ArrayList<LazyNumber> p = new ArrayList<>(
                        !f.numParamsVaries() ? f.getNumParams() : 0);
                // pop parameters off the stack until we hit the start of
                // this function's parameter list
                while (!stack.isEmpty() && stack.peek() != PARAMS_START)
                {
                    p.add(0, stack.pop());
                }
                if (stack.peek() == PARAMS_START)
                {
                    stack.pop();
                }
                LazyNumber fResult = f.lazyEval(p);
                stack.push(fResult);
            }
            else if ("(".equals(token))
            {
                stack.push(PARAMS_START);
            }
            else
            {
                BigComplex bd;
                if (token.endsWith("i"))
                {
                    String str = token.substring(0, token.length()-1);
                    if (str.isEmpty())
                        str = "1";
                    bd = new BigComplex("0", str);
                }
                else
                {
                    bd = new BigComplex(token, "0");
                }
                BigComplex finalBd = bd;
                stack.push(() -> finalBd);   // blank constant
            }
        }
        return stack.pop().eval().stripTrailingZeros();
    }

    /*
    * Cached access to the RPN notation of this expression, ensures only one
     * calculation of the RPN per expression instance. If no cached instance
     * exists, a new one will be created and put to the cache.
     *
     * @return The cached RPN instance.
     */
    private List<String> getRPN ()
    {
        if (rpn == null)
        {
            rpn = shuntingYard(this.expression);
            validate(rpn);
        }
        return rpn;
    }

    /**
     * Implementation of the <i>Shunting Yard</i> algorithm to transform an
     * infix expression to a RPN expression.
     *
     * @param expression The input expression in infx.
     * @return A RPN representation of the expression, with each token as a list
     * member.
     */
    private List<String> shuntingYard (String expression)
    {
        List<String> outputQueue = new ArrayList<>();
        Stack<String> stack = new Stack<>();

        Tokenizer tokenizer = new Tokenizer(expression);

        String lastFunction = null;
        String previousToken = null;
        while (tokenizer.hasNext())
        {
            String token = tokenizer.next();
            if (isNumber(token))
            {
                if (token.startsWith("x"))
                {

                    BigInteger bd = new BigInteger(token.substring(1), 16);
                    outputQueue.add(bd.toString(10));
                }
                else if (token.startsWith("b"))
                {
                    BigInteger bd = new BigInteger(token.substring(1), 2);
                    outputQueue.add(bd.toString(10));
                }
                else if (token.startsWith("o"))
                {
                    BigInteger bd = new BigInteger(token.substring(1), 8);
                    outputQueue.add(bd.toString(10));
                }
                else
                {
                    outputQueue.add(token);
                }
            }
            else if (mainVars.containsKey(token))
            {
                outputQueue.add(token);
            }
            else if (functions.containsKey(token.toUpperCase(Locale.ROOT)))
            {
                stack.push(token);
                lastFunction = token;
            }
            else if ((Character.isLetter(token.charAt(0)) || token.charAt(0) == '_')
                    && !operators.containsKey(token))
            {
                mainVars.put(token, BigComplex.ZERO);   // create variable
                outputQueue.add(token);
                //stack.push(token);
            }
            else if (",".equals(token))
            {
                if (operators.containsKey(previousToken))
                {
                    throw new ExpressionException("Missing parameter(s) for operator " + previousToken +
                            " at character position " + (tokenizer.getPos() - 1 - previousToken.length()));
                }
                while (!stack.isEmpty() && !"(".equals(stack.peek()))
                {
                    outputQueue.add(stack.pop());
                }
                if (stack.isEmpty())
                {
                    throw new ExpressionException("Parse error for function '"
                            + lastFunction + "'");
                }
            }
            else if (operators.containsKey(token))
            {
                if (",".equals(previousToken) || "(".equals(previousToken))
                {
                    throw new ExpressionException("Missing parameter(s) for operator " + token +
                            " at character position " + (tokenizer.getPos() - token.length()));
                }
                Operator o1 = operators.get(token);
                String token2 = stack.isEmpty() ? null : stack.peek();
                while (token2 != null &&
                        operators.containsKey(token2)
                        && ((o1.isLeftAssoc() && o1.getPrecedence() <= operators
                        .get(token2).getPrecedence()) || (o1
                        .getPrecedence() < operators.get(token2)
                        .getPrecedence())))
                {
                    outputQueue.add(stack.pop());
                    token2 = stack.isEmpty() ? null : stack.peek();
                }
                stack.push(token);
            }
            else if ("(".equals(token))
            {
                if (previousToken != null)
                {
                    if (isNumber(previousToken))
                    {
                        throw new ExpressionException(
                                "Missing operator at character position "
                                        + tokenizer.getPos());
                    }
                    // if the ( is preceded by a valid function, then it
                    // denotes the start of a parameter list
                    if (functions.containsKey(previousToken.toUpperCase(Locale.ROOT)))
                    {
                        outputQueue.add(token);
                    }
                }
                stack.push(token);
            }
            else if (")".equals(token))
            {
                if (operators.containsKey(previousToken))
                {
                    throw new ExpressionException("Missing parameter(s) for operator " + previousToken +
                            " at character position " + (tokenizer.getPos() - 1 - previousToken.length()));
                }
                while (!stack.isEmpty() && !"(".equals(stack.peek()))
                {
                    outputQueue.add(stack.pop());
                }
                if (stack.isEmpty())
                {
                    throw new ExpressionException("Mismatched parentheses");
                }
                stack.pop();
                if (!stack.isEmpty()
                        && functions.containsKey(stack.peek().toUpperCase(
                        Locale.ROOT)))
                {
                    outputQueue.add(stack.pop());
                }
            }
            previousToken = token;
        }
        while (!stack.isEmpty())
        {
            String element = stack.pop();
            if ("(".equals(element) || ")".equals(element))
            {
                throw new ExpressionException("Mismatched parentheses");
            }

            if (!operators.containsKey(element))
            {
                throw new ExpressionException("Unknown operator or function: "
                        + element);
            }
            outputQueue.add(element);
        }
        return outputQueue;
    }

    /**
     * Check that the expression has enough numbers and variables to fit the
     * requirements of the operators and functions, also check
     * for only 1 result stored at the end of the evaluation.
     */
    private void validate (List<String> rpn)
    {
		/*-
		* Thanks to Norman Ramsey:
		* http://http://stackoverflow.com/questions/789847/postfix-notation-validation
		*/
        // each push on to this stack is a new function scope, with the value of each
        // layer on the stack being the count of the number of parameters in that scope
        Stack<Integer> stack = new Stack<>();

        // push the 'global' scope
        stack.push(0);

        for (final String token : rpn)
        {
            if (operators.containsKey(token))
            {
                if (stack.peek() < 2)
                {
                    throw new ExpressionException("Missing parameter(s) for operator " + token);
                }
                // pop the operator's 2 parameters and add the result
                stack.set(stack.size() - 1, stack.peek() - 2 + 1);
            }
            else if (mainVars.containsKey(token))
            {
                stack.set(stack.size() - 1, stack.peek() + 1);
            }
            else if (functions.containsKey(token.toUpperCase(Locale.ROOT)))
            {
                LazyFunction f = functions.get(token.toUpperCase(Locale.ROOT));
                int numParams = stack.pop();
                if (!f.numParamsVaries() && numParams != f.getNumParams())
                {
                    throw new ExpressionException("Function " + token + " expected " + f.getNumParams() + " parameters, got " + numParams);
                }
                if (stack.size() <= 0)
                {
                    throw new ExpressionException("Too many function calls, maximum scope exceeded");
                }
                // push the result of the function
                stack.set(stack.size() - 1, stack.peek() + 1);
            }
            else if ("(".equals(token))
            {
                stack.push(0);
            }
            else
            {
                stack.set(stack.size() - 1, stack.peek() + 1);
            }
        }

        if (stack.size() > 1)
        {
            throw new ExpressionException("Too many unhandled function parameter lists");
        }
        else if (stack.peek() > 1)
        {
            throw new ExpressionException("Too many numbers or variables");
        }
        else if (stack.peek() < 1)
        {
            throw new ExpressionException("Empty expression");
        }
    }

    /**
     * Is the string a number?
     *
     * @param st The string.
     * @return <code>true</code>, if the input string is a number.
     */
    private boolean isNumber (String st)
    {
        if (st.startsWith("x") && !st.equals("xor") ||
                (st.startsWith("b") && (st.charAt(1) == '0' || st.charAt(1) == '1')) ||
                st.startsWith("o") && !st.equals("or"))
        {
            return true;
        }
        if (st.charAt(0) == minusSign && st.length() == 1)
        {
            return false;
        }
        if (st.charAt(0) == '+' && st.length() == 1)
        {
            return false;
        }
        if (st.charAt(0) == 'e' || st.charAt(0) == 'E')
        {
            return false;
        }
        for (char ch : st.toCharArray())
        {
            if (!Character.isDigit(ch) && ch != minusSign
                    && ch != decimalSeparator
                    && ch != 'e'
                    && ch != 'i'
                    && ch != 'E'
                    && ch != '+')
            {
                return false;
            }
        }
        return true;
    }

    public Map<String, Operator> getOps ()
    {
        return operators;
    }

    public Map<String, LazyFunction> getFuncs ()
    {
        return functions;
    }


    /**
     * Sets a variable value.
     *
     * @param variable The variable to set.
     * @param value    The variable value.
     * @return The expression, allows to chain methods.
     */
    private Expression setVariable (String variable, String value)
    {
        if (isNumber(value))
        {
            mainVars.put(variable, new BigComplex(value, "0"));
        }
        else
        {
            expression = expression.replaceAll("(?i)\\b" + variable + "\\b", "("
                    + value + ")");
            rpn = null;
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode ()
    {
        return this.expression == null ? 0 : this.expression.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals (Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        Expression that = (Expression) o;
        if (this.expression == null)
        {
            return that.expression == null;
        }
        else
        {
            return this.expression.equals(that.expression);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString ()
    {
        return this.expression;
    }

    /**
     * Expression tokenizer that allows to iterate over a {@link String}
     * expression token by token. Blank characters will be skipped.
     */
    private class Tokenizer implements Iterator<String>
    {
        /**
         * The original input expression.
         */
        private final String input;
        /**
         * Actual position in expression string.
         */
        private int pos = 0;
        /**
         * The previous token or <code>null</code> if none.
         */
        private String previousToken;

        /**
         * Creates a new tokenizer for an expression.
         *
         * @param input The expression string.
         */
        public Tokenizer (String input)
        {
            this.input = input.trim();
        }

        //@Override
        public boolean hasNext ()
        {
            return (pos < input.length());
        }

        //@Override
        public String next ()
        {
            StringBuilder token = new StringBuilder();
            if (pos >= input.length())
            {
                return previousToken = null;
            }
            char ch = input.charAt(pos);
            while (Character.isWhitespace(ch) && pos < input.length())
            {
                ch = input.charAt(++pos);
            }
            if (Character.isDigit(ch))
            {
                while ((Character.isDigit(ch) 
                        || ch == decimalSeparator
                        || ch == 'e'
                        || ch == 'i'
                        || ch == 'E'
                        || (ch == minusSign && token.length() > 0
                        && ('e' == token.charAt(token.length() - 1)
                        || 'E' == token.charAt(token.length() - 1)))
                        || (ch == '+' && token.length() > 0
                        && ('e' == token.charAt(token.length() - 1)
                        || 'E' == token.charAt(token.length() - 1)))
                ) && (pos < input.length()))
                {
                    token.append(input.charAt(pos++));
                    ch = pos == input.length() ? 0 : input.charAt(pos);
                }
            }
            else if (ch == minusSign
                    && Character.isDigit(peekNextChar())
                    && ("(".equals(previousToken) || ",".equals(previousToken)
                    || previousToken == null || operators
                    .containsKey(previousToken)))
            {
                token.append(minusSign);
                pos++;
                token.append(next());
            }
            else if (Character.isLetter(ch) || firstVarChars.indexOf(ch) >= 0)
            {
                while ((Character.isLetter(ch) || Character.isDigit(ch)
                        || varChars.indexOf(ch) >= 0 || token.length() == 0 && firstVarChars.indexOf(ch) >= 0)
                        && (pos < input.length()))
                {
                    token.append(input.charAt(pos++));
                    ch = pos == input.length() ? 0 : input.charAt(pos);
                }
            }
            else if (ch == '(' || ch == ')' || ch == ',')
            {
                token.append(ch);
                pos++;
            }
            else
            {
                while (!Character.isLetter(ch) && !Character.isDigit(ch)
                        && firstVarChars.indexOf(ch) < 0 && !Character.isWhitespace(ch)
                        && ch != '(' && ch != ')' && ch != ','
                        && (pos < input.length()))
                {
                    token.append(input.charAt(pos));
                    pos++;
                    ch = pos == input.length() ? 0 : input.charAt(pos);
                    if (ch == minusSign)
                    {
                        break;
                    }
                }
                if (!operators.containsKey(token.toString()))
                {
                    throw new ExpressionException("Unknown operator '" + token
                            + "' at position " + (pos - token.length() + 1));
                }
            }
            return previousToken = token.toString();
        }

        //@Override
        public void remove ()
        {
            throw new ExpressionException("remove() not supported");
        }

        /**
         * Peek at the next character, without advancing the iterator.
         *
         * @return The next character or character 0, if at end of string.
         */
        private char peekNextChar ()
        {
            if (pos < (input.length() - 1))
            {
                return input.charAt(pos + 1);
            }
            else
            {
                return 0;
            }
        }

        /**
         * Get the actual character position in the string.
         *
         * @return The actual character position.
         */
        public int getPos ()
        {
            return pos;
        }

    }

}
