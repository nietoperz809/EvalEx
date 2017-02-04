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
import org.apache.commons.math3.util.ArithmeticUtils;

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
@SuppressWarnings({"Since15", "BigDecimalMethodWithoutRoundingCalled"})
public class Expression
{

    /**
     * Definition of PI as a constant, can be used in expressions as variable.
     */
    private static final BigDecimal PI = new BigDecimal(
            "3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679");

    /**
     * Definition of e: "Euler's number" as a constant, can be used in expressions as variable.
     */
    private static final BigDecimal e = new BigDecimal(
            "2.71828182845904523536028747135266249775724709369995957496696762772407663");
    private final LinkedList<String> history;


    /**
     * The characters (other than letters and digits) allowed as the first character in a variable.
     */
    private String firstVarChars = "_";

    /**
     * The characters (other than letters and digits) allowed as the second or subsequent characters in a variable.
     */
    private String varChars = "_";

    /**
     * The original infix expression.
     */
    private final String originalExpression;

    /**
     * The current infix expression, with optional variable substitutions.
     */
    private String expression = null;

    /**
     * The cached RPN (Reverse Polish Notation) of the expression.
     */
    private List<String> rpn = null;

    /**
     * All defined operators with name and implementation.
     */
    private final Map<String, Operator> operators = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public Map<String, Operator> getOps()
    {
        return operators;
    }

    /**
     * All defined functions with name and implementation.
     */
    private final Map<String, LazyFunction> functions = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public Map<String, LazyFunction> getFuncs()
    {
        return functions;
    }

    /**
     * All defined variables with name and value.
     */
    private final Map<String, BigDecimal> variables = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    /**
     * What character to use for decimal separators.
     */
    private static final char decimalSeparator = '.';

    /**
     * What character to use for minus sign (negative values).
     */
    private static final char minusSign = '-';

    /**
     * The BigDecimal representation of the left parenthesis,
     * used for parsing varying numbers of function parameters.
     */
    private static final LazyNumber PARAMS_START = () -> null;

    public Expression (String s)
    {
        this (s, null);
    }

    /**
     * Creates a new expression instance from an expression string with a given
     * default match context.
     *
     * @param expression         The expression. E.g. <code>"2.4*sin(3)/(2-4)"</code> or
     *                           <code>"sin(y)>0 & max(z, 3)>3"</code>
     */
    public Expression (String expression, LinkedList<String> hist)
    {
        this.history = hist;
        this.expression = expression;
        this.originalExpression = expression;
        addOperator(new Operator("+", 20, true,
                "Addition")
        {
            @Override
            public BigDecimal eval (BigDecimal v1, BigDecimal v2)
            {
                return v1.add(v2);
            }
        });
        addOperator(new Operator("-", 20, true,
                "Subtraction")
        {
            @Override
            public BigDecimal eval (BigDecimal v1, BigDecimal v2)
            {
                return v1.subtract(v2);
            }
        });
        addOperator(new Operator("*", 30, true,
                "Real number multiplication")
        {
            @Override
            public BigDecimal eval (BigDecimal v1, BigDecimal v2)
            {
                return v1.multiply(v2);
            }
        });
        addOperator(new Operator("/", 30, true,
                "Real number division")
        {
            @Override
            public BigDecimal eval (BigDecimal v1, BigDecimal v2)
            {
                return v1.divide(v2, MathContext.DECIMAL128);
            }
        });
        addOperator(new Operator("%", 30, true,
                "Remainder of integer division")
        {
            @Override
            public BigDecimal eval (BigDecimal v1, BigDecimal v2)
            {
                return v1.remainder(v2);
            }
        });
        addOperator(new Operator("^", 40, false,
                "Exponentation. See: https://en.wikipedia.org/wiki/Exponentiation")
        {
            @Override
            public BigDecimal eval (BigDecimal v1, BigDecimal v2)
            {
                /*-
				 * Thanks to Gene Marin:
				 * http://stackoverflow.com/questions/3579779/how-to-do-a-fractional-power-on-bigdecimal-in-java
				 */
                int signOf2 = v2.signum();
                double dn1 = v1.doubleValue();
                v2 = v2.multiply(new BigDecimal(signOf2)); // n2 is now positive
                BigDecimal remainderOf2 = v2.remainder(BigDecimal.ONE);
                BigDecimal n2IntPart = v2.subtract(remainderOf2);
                BigDecimal intPow = v1.pow(n2IntPart.intValueExact());
                BigDecimal doublePow = new BigDecimal(Math.pow(dn1,
                        remainderOf2.doubleValue()));

                BigDecimal result = intPow.multiply(doublePow);
                if (signOf2 == -1)
                {
                    result = BigDecimal.ONE.divide(result, MathContext.DECIMAL128);
                }
                return result;
            }
        });
        addOperator(new Operator("&&", 4, false,
                "Logical AND. Evaluates to 1 if both operands are not 0")
        {
            @Override
            public BigDecimal eval (BigDecimal v1, BigDecimal v2)
            {
                boolean b1 = !v1.equals(BigDecimal.ZERO);
                boolean b2 = !v2.equals(BigDecimal.ZERO);
                return b1 && b2 ? BigDecimal.ONE : BigDecimal.ZERO;
            }
        });

        addOperator(new Operator("||", 2, false,
                "Logical OR. Evaluates to 0 if both operands are 0")
        {
            @Override
            public BigDecimal eval (BigDecimal v1, BigDecimal v2)
            {
                boolean b1 = !v1.equals(BigDecimal.ZERO);
                boolean b2 = !v2.equals(BigDecimal.ZERO);
                return b1 || b2 ? BigDecimal.ONE : BigDecimal.ZERO;
            }
        });

        addOperator(new Operator(">", 10, false,
                "Greater than. See: See: https://en.wikipedia.org/wiki/Inequality_(mathematics)")
        {
            @Override
            public BigDecimal eval (BigDecimal v1, BigDecimal v2)
            {
                return v1.compareTo(v2) == 1 ? BigDecimal.ONE : BigDecimal.ZERO;
            }
        });

        addOperator(new Operator(">=", 10, false,
                "Greater or equal")
        {
            @Override
            public BigDecimal eval (BigDecimal v1, BigDecimal v2)
            {
                return v1.compareTo(v2) >= 0 ? BigDecimal.ONE : BigDecimal.ZERO;
            }
        });

        addOperator(new Operator("<", 10, false,
                "Less than. See: https://en.wikipedia.org/wiki/Inequality_(mathematics)")
        {
            @Override
            public BigDecimal eval (BigDecimal v1, BigDecimal v2)
            {
                return v1.compareTo(v2) == -1 ? BigDecimal.ONE
                        : BigDecimal.ZERO;
            }
        });

        addOperator(new Operator("<=", 10, false,
                "less or equal")
        {
            @Override
            public BigDecimal eval (BigDecimal v1, BigDecimal v2)
            {
                return v1.compareTo(v2) <= 0 ? BigDecimal.ONE : BigDecimal.ZERO;
            }
        });

        addOperator(new Operator("=", 7, false,
                "Equality. See https://en.wikipedia.org/wiki/Equality_(mathematics)")
        {
            @Override
            public BigDecimal eval (BigDecimal v1, BigDecimal v2)
            {
                return v1.compareTo(v2) == 0 ? BigDecimal.ONE : BigDecimal.ZERO;
            }
        });

        addOperator(new Operator("!=", 7, false,
                "Inequality. See: https://en.wikipedia.org/wiki/Inequality_(mathematics)")
        {
            @Override
            public BigDecimal eval (BigDecimal v1, BigDecimal v2)
            {
                return v1.compareTo(v2) != 0 ? BigDecimal.ONE : BigDecimal.ZERO;
            }
        });
        addOperator(new Operator("or", 7, false,
                "Bitwise OR. See: https://en.wikipedia.org/wiki/Logical_disjunction")
        {
            @Override
            public BigDecimal eval (BigDecimal v1, BigDecimal v2)
            {
                return new BigDecimal(v1.longValue() | v2.longValue());
            }
        });
        addOperator(new Operator("and", 7, false,
                "Bitwise AND. See: https://en.wikipedia.org/wiki/Logical_conjunction")
        {
            @Override
            public BigDecimal eval (BigDecimal v1, BigDecimal v2)
            {
                return new BigDecimal(v1.longValue() & v2.longValue());
            }
        });
        addOperator(new Operator("xor", 7, false,
                "Bitwise XOR, See: https://en.wikipedia.org/wiki/Exclusive_or")
        {
            @Override
            public BigDecimal eval (BigDecimal v1, BigDecimal v2)
            {
                return new BigDecimal(v1.longValue() ^ v2.longValue());
            }
        });

        addOperator(new Operator("!", 50, true,
                "Factorial. See https://en.wikipedia.org/wiki/Factorial")
        {
            @Override
            public BigDecimal eval (BigDecimal v1, BigDecimal v2)
            {
                BigInteger fact = MathTools.getFactorialUsingGammaApproximation(v1.intValue(), 100);
                //ArithmeticUtils.
                //double n = Gamma.gamma(v1.doubleValue());
                return new BigDecimal(fact);
            }
        });

        addOperator(new Operator("~", 8, false,
                "Bitwise negation")
        {
            @Override
            public BigDecimal eval (BigDecimal v1, BigDecimal v2)
            {
                return new BigDecimal(~v2.longValue());
            }
        });

        addOperator(new Operator("shl", 8, false,
                "Left Bit shift")
        {
            @Override
            public BigDecimal eval (BigDecimal v1, BigDecimal v2)
            {
                return new BigDecimal(v1.longValue() << v2.longValue());
            }
        });

        addOperator(new Operator("shr", 8, false,
                "Right bit shift")
        {
            @Override
            public BigDecimal eval (BigDecimal v1, BigDecimal v2)
            {
                return new BigDecimal(v1.longValue() >>> v2.longValue());
            }
        });

        addFunction(new Function("NOT", 1,
                "evaluates to 0 if argument != 0")
        {
            @Override
            public BigDecimal eval (List<BigDecimal> parameters)
            {
                boolean zero = parameters.get(0).compareTo(BigDecimal.ZERO) == 0;
                return zero ? BigDecimal.ONE : BigDecimal.ZERO;
            }
        });

        addFunction(new Function("RND", 2,
                "Give random number in the range between first and second argument")
        {
            @Override
            public BigDecimal eval (List<BigDecimal> parameters)
            {
                double low = parameters.get(0).doubleValue();
                double high = parameters.get(1).doubleValue();
                return new BigDecimal(low+Math.random()*(high-low));
            }
        });

        MersenneTwister mers = new MersenneTwister(System.nanoTime());

        addFunction(new Function("MRS", 0,
                "Mersenne twister random generator")
        {
            @Override
            public BigDecimal eval (List<BigDecimal> parameters)
            {
                return new BigDecimal(mers.nextDouble());
            }
        });

        addFunction(new Function("BIN", 2,
                "Binomial Coefficient 'n choose k'")
        {
            @Override
            public BigDecimal eval (List<BigDecimal> parameters)
            {
                int n = parameters.get(0).intValue();
                int k = parameters.get(1).intValue();
                double d = ArithmeticUtils.binomialCoefficientDouble(n,k);
                return new BigDecimal(d);
            }
        });

        addFunction(new Function("SIN", 1,
                "Sine function")
        {
            @Override
            public BigDecimal eval (List<BigDecimal> parameters)
            {
                double d = Math.sin(parameters.get(0)
                        .doubleValue());
                return new BigDecimal(d);
            }
        });
        addFunction(new Function("COS", 1,
                "Cosine function")
        {
            @Override
            public BigDecimal eval (List<BigDecimal> parameters)
            {
                double d = Math.cos(parameters.get(0)
                        .doubleValue());
                return new BigDecimal(d);
            }
        });
        addFunction(new Function("TAN", 1,
                "Tangent")
        {
            @Override
            public BigDecimal eval (List<BigDecimal> parameters)
            {
                double d = Math.tan(parameters.get(0)
                        .doubleValue());
                return new BigDecimal(d);
            }
        });
        addFunction(new Function("ASIN", 1,
                "Reverse Sine")
        { // added by av
            @Override
            public BigDecimal eval (List<BigDecimal> parameters)
            {
                double d = Math.asin(parameters.get(0)
                        .doubleValue());
                return new BigDecimal(d);
            }
        });
        addFunction(new Function("ACOS", 1,
                "Reverse Cosine")
        { // added by av
            @Override
            public BigDecimal eval (List<BigDecimal> parameters)
            {
                double d = Math.acos(parameters.get(0)
                        .doubleValue());
                return new BigDecimal(d);
            }
        });
        addFunction(new Function("ATAN", 1,
                "Reverse Tangent")
        { // added by av
            @Override
            public BigDecimal eval (List<BigDecimal> parameters)
            {
                double d = Math.atan(parameters.get(0)
                        .doubleValue());
                return new BigDecimal(d);
            }
        });
        addFunction(new Function("SINH", 1,
                "Hyperbolic Sine")
        {
            @Override
            public BigDecimal eval (List<BigDecimal> parameters)
            {
                double d = Math.sinh(parameters.get(0).doubleValue());
                return new BigDecimal(d);
            }
        });
        addFunction(new Function("COSH", 1,
                "Hyperbolic Cosine")
        {
            @Override
            public BigDecimal eval (List<BigDecimal> parameters)
            {
                double d = Math.cosh(parameters.get(0).doubleValue());
                return new BigDecimal(d);
            }
        });
        addFunction(new Function("TANH", 1,
                "Hyperbolic Tangent")
        {
            @Override
            public BigDecimal eval (List<BigDecimal> parameters)
            {
                double d = Math.tanh(parameters.get(0).doubleValue());
                return new BigDecimal(d);
            }
        });
        addFunction(new Function("RAD", 1,
                "Transform degree to radian")
        {
            @Override
            public BigDecimal eval (List<BigDecimal> parameters)
            {
                double d = Math.toRadians(parameters.get(0).doubleValue());
                return new BigDecimal(d);
            }
        });
        addFunction(new Function("DEG", 1,
                "Transform radian to degree")
        {
            @Override
            public BigDecimal eval (List<BigDecimal> parameters)
            {
                double d = Math.toDegrees(parameters.get(0).doubleValue());
                return new BigDecimal(d);
            }
        });
        addFunction(new Function("MAX", -1,
                "Find the biggest value in a list")
        {
            @Override
            public BigDecimal eval (List<BigDecimal> parameters)
            {
                if (parameters.size() == 0)
                {
                    throw new ExpressionException("MAX requires at least one parameter");
                }
                BigDecimal max = null;
                for (BigDecimal parameter : parameters)
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
        addFunction(new Function("PERC", 2,
                "Calculates how many percent is param1 of the whole (param2)")
        {
            @Override
            public BigDecimal eval (List<BigDecimal> parameters)
            {
                return parameters.get(0).divide(new BigDecimal(100)).multiply(parameters.get(1));
            }
        });
        addFunction(new Function("H", 1,
                "Evaluate _history element")
        {
            @Override
            public BigDecimal eval (List<BigDecimal> parameters)
            {
                int i = parameters.get(0).intValue();
                Expression ex = new Expression(history.get(i), history);
                return ex.eval();
            }
        });
        addFunction(new Function("MERS", 1,
                "Calculate Mersenne Number")
        {
            @Override
            public BigDecimal eval (List<BigDecimal> parameters)
            {
                BigDecimal p = parameters.get(0); 
                return new BigDecimal(2).pow(p.intValue()).subtract(BigDecimal.ONE);
            }
        });

        addFunction(new Function("GCD", 2,
                "Find greatest common divisor of 2 values")
        {
            private BigDecimal GCD(BigDecimal a, BigDecimal b)
            {
                if (b.compareTo(BigDecimal.ZERO)==0)
                    return a;
                return GCD(b,a.remainder(b));
            }
            @Override
            public BigDecimal eval (List<BigDecimal> parameters)
            {
                return GCD(parameters.get(0), parameters.get(1));
            }
        });
        addFunction(new Function("LCM", 2,
                "Find least common multiple of 2 values")
        {
            private final Function gcd = (Function)functions.get("GCD");

            @Override
            public BigDecimal eval (List<BigDecimal> parameters)
            {
                return parameters.get(0).multiply(parameters.get(1))
                        .divide(gcd.eval(parameters), MathContext.DECIMAL128);
            }
        });
        addFunction(new Function("AMEAN", -1,
                "Arithmetic mean of a set of values")
        {
            @Override
            public BigDecimal eval (List<BigDecimal> parameters)
            {
                if (parameters.size() == 0)
                {
                    throw new ExpressionException("MEAN requires at least one parameter");
                }
                BigDecimal res = BigDecimal.ZERO;
                int num = 0;
                for (BigDecimal parameter : parameters)
                {
                    res = res.add(parameter);
                    num++;
                }
                return res.divide(new BigDecimal(num), MathContext.DECIMAL128);
            }
        });
        addFunction(new Function("GMEAN", -1,
                "Geometric mean of a set of values")
        {
            @Override
            public BigDecimal eval (List<BigDecimal> parameters)
            {
                if (parameters.size() == 0)
                {
                    throw new ExpressionException("MEAN requires at least one parameter");
                }
                BigDecimal res = BigDecimal.ONE;
                int num = 0;
                for (BigDecimal parameter : parameters)
                {
                    res = res.multiply(parameter);
                    num++;
                }
                res = res.abs();
                return MathTools.nthRoot(num, res, new BigDecimal (0.000000001));
            }
        });
        addFunction(new Function("HMEAN", -1,
                "Harmonic mean of a set of values")
        {
            @Override
            public BigDecimal eval (List<BigDecimal> parameters)
            {
                if (parameters.size() == 0)
                {
                    throw new ExpressionException("MEAN requires at least one parameter");
                }
                BigDecimal res = BigDecimal.ZERO;
                int num = 0;
                for (BigDecimal parameter : parameters)
                {
                    res = res.add (BigDecimal.ONE.divide(parameter,MathContext.DECIMAL128));
                    num++;
                }
                res = res.abs();
                return new BigDecimal(num).divide(res, MathContext.DECIMAL128);
            }
        });

        addFunction(new Function("VAR", -1,
                "Variance of a set of values")
        {
            @Override
            public BigDecimal eval (List<BigDecimal> parameters)
            {
                if (parameters.size() == 0)
                {
                    throw new ExpressionException("MEAN requires at least one parameter");
                }
                double[] arr = new double[parameters.size()];
                for (int s=0; s<parameters.size(); s++)
                {
                    arr[s] = parameters.get(s).doubleValue();
                }
                return new BigDecimal(variance(arr));
            }
        });

        addFunction(new Function("NPR", 1,
                "Next prime number greater or euqal the argument")
        {
            @Override
            public BigDecimal eval (List<BigDecimal> parameters)
            {
                return new BigDecimal(nextPrime(parameters.get(0).intValue()));
            }
        });

        ///////////////////////////////////////////////

        addFunction(new Function("MIN", -1,
                "Find the smallest in a list of values")
        {
            @Override
            public BigDecimal eval (List<BigDecimal> parameters)
            {
                if (parameters.size() == 0)
                {
                    throw new ExpressionException("MIN requires at least one parameter");
                }
                BigDecimal min = null;
                for (BigDecimal parameter : parameters)
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
            public BigDecimal eval (List<BigDecimal> parameters)
            {
                return parameters.get(0).abs();
            }
        });
        addFunction(new Function("LN", 1,
                "Logarithm base e of the argument")
        {
            @Override
            public BigDecimal eval (List<BigDecimal> parameters)
            {
                double d = Math.log(parameters.get(0).doubleValue());
                return new BigDecimal(d);
            }
        });
        addFunction(new Function("LOG", 1,
                "Logarithm base 10 of the argument")
        {
            @Override
            public BigDecimal eval (List<BigDecimal> parameters)
            {
                double d = Math.log10(parameters.get(0).doubleValue());
                return new BigDecimal(d);
            }
        });
        addFunction(new Function("FLOOR", 1,
                "Rounds down to nearest Integer")
        {
            @Override
            public BigDecimal eval (List<BigDecimal> parameters)
            {
                BigDecimal toRound = parameters.get(0);
                return toRound.setScale(100, RoundingMode.FLOOR);
            }
        });
        addFunction(new Function("CEIL", 1,
                "Rounds up to nearest Integer")
        {
            @Override
            public BigDecimal eval (List<BigDecimal> parameters)
            {
                BigDecimal toRound = parameters.get(0);
                return toRound.setScale(100, RoundingMode.CEILING);
            }
        });
        addFunction(new Function("SQRT", 1,
                "Square root")
        {
            @Override
            public BigDecimal eval (List<BigDecimal> parameters)
            {
				double d = parameters.get(0).doubleValue();
                return new BigDecimal(Math.sqrt(d));
            }
        });

        variables.put("e", e);
        variables.put("PI", PI);
        variables.put("TRUE", BigDecimal.ONE);
        variables.put("FALSE", BigDecimal.ZERO);
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
                (st.startsWith("b") && (st.charAt(1) == '0' || st.charAt(1)=='1')) ||
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
                    && ch != 'e' && ch != 'E' && ch != '+')
            {
                return false;
            }
        }
        return true;
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
                    
                    BigInteger bd = new BigInteger (token.substring(1), 16);
                    outputQueue.add(bd.toString(10));
                }
                else if (token.startsWith("b"))
                {
                    BigInteger bd = new BigInteger (token.substring(1), 2);
                    outputQueue.add(bd.toString(10));
                }
                else if (token.startsWith("o"))
                {
                    BigInteger bd = new BigInteger (token.substring(1), 8);
                    outputQueue.add(bd.toString(10));
                }
                else
                {
                    outputQueue.add(token);
                }
            }
            else if (variables.containsKey(token))
            {
                outputQueue.add(token);
            }
            else if (functions.containsKey(token.toUpperCase(Locale.ROOT)))
            {
                stack.push(token);
                lastFunction = token;
            }
            else if (Character.isLetter(token.charAt(0)))
            {
                stack.push(token);
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
     * Sets the precision for expression evaluation.
     *
     * @param precision The new precision.
     * @return The expression, allows to chain methods.
     */
    public Expression setPrecision (int precision)
    {
        return this;
    }    /**
     * Evaluates the expression.
     *
     * @return The result of the expression.
     */
    public BigDecimal eval ()
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
            else if (variables.containsKey(token))
            {
                stack.push(() -> variables.get(token));
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
                stack.push(() -> new BigDecimal(token));
            }
        }
        return stack.pop().eval().stripTrailingZeros();
    }

    /**
     * Sets the rounding _radix for expression evaluation.
     *
     * @param roundingMode The new rounding _radix.
     * @return The expression, allows to chain methods.
     */
    public Expression setRoundingMode (RoundingMode roundingMode)
    {
        return this;
    }

    /**
     * Sets the characters other than letters and digits that are valid as the
     * first character of a variable.
     *
     * @param chars The new set of variable characters.
     * @return The expression, allows to chain methods.
     */
    public Expression setFirstVariableCharacters (String chars)
    {
        this.firstVarChars = chars;
        return this;
    }

    /**
     * Sets the characters other than letters and digits that are valid as the
     * second and subsequent characters of a variable.
     *
     * @param chars The new set of variable characters.
     * @return The expression, allows to chain methods.
     */
    public Expression setVariableCharacters (String chars)
    {
        this.varChars = chars;
        return this;
    }

    /**
     * Sets a variable value.
     *
     * @param variable The variable to set.
     * @param value    The variable value.
     * @return The expression, allows to chain methods.
     */
    public Expression with (String variable, BigDecimal value)
    {
        return setVariable(variable, value);
    }

    /**
     * Sets a variable value.
     *
     * @param variable The variable name.
     * @param value    The variable value.
     * @return The expression, allows to chain methods.
     */
    public Expression setVariable (String variable, BigDecimal value)
    {
        variables.put(variable, value);
        return this;
    }    /**
     * Adds an operator to the list of supported operators.
     *
     * @param operator The operator to add.
     * @return The previous operator with that name, or <code>null</code> if
     * there was none.
     */
    public Operator addOperator (Operator operator)
    {
        return operators.put(operator.getName(), operator);
    }

    /**
     * Sets a variable value.
     *
     * @param variable The variable to set.
     * @param value    The variable value.
     * @return The expression, allows to chain methods.
     */
    public Expression and (String variable, String value)
    {
        return setVariable(variable, value);
    }    /**
     * Adds a function to the list of supported functions
     *
     * @param function The function to add.
     * @return The previous operator with that name, or <code>null</code> if
     * there was none.
     */
    public Function addFunction (Function function)
    {
        return (Function) functions.put(function.getName(), function);
    }

    /**
     * Sets a variable value.
     *
     * @param variable The variable to set.
     * @param value    The variable value.
     * @return The expression, allows to chain methods.
     */
    public Expression setVariable (String variable, String value)
    {
        if (isNumber(value))
        {
            variables.put(variable, new BigDecimal(value));
        }
        else
        {
            expression = expression.replaceAll("(?i)\\b" + variable + "\\b", "("
                    + value + ")");
            rpn = null;
        }
        return this;
    }    /**
     * Adds a lazy function function to the list of supported functions
     *
     * @param function The function to add.
     * @return The previous operator with that name, or <code>null</code> if
     * there was none.
     */
private LazyFunction addLazyFunction (LazyFunction function)
    {
        return functions.put(function.getName(), function);
    }


    /**
     * Sets a variable value.
     *
     * @param variable The variable to set.
     * @param value    The variable value.
     * @return The expression, allows to chain methods.
     */
    public Expression with (String variable, String value)
    {
        return setVariable(variable, value);
    }

    /**
     * Get an iterator for this expression, allows iterating over an expression
     * token by token.
     *
     * @return A new iterator instance for this expression.
     */
    public Iterator<String> getExpressionTokenizer ()
    {
        return new Tokenizer(this.expression);
    }

    /**
     * Get a string representation of the RPN (Reverse Polish Notation) for this
     * expression.
     *
     * @return A string with the RPN representation for this expression.
     */
    public String toRPN ()
    {
        StringBuilder result = new StringBuilder();
        for (String st : getRPN())
        {
            if (result.length() != 0)
            {
                result.append(" ");
            }
            result.append(st);
        }
        return result.toString();
    }

    /**
     * Exposing declared variables in the expression.
     *
     * @return All declared variables.
     */
    public Set<String> getDeclaredVariables ()
    {
        return Collections.unmodifiableSet(variables.keySet());
    }

    /**
     * Exposing declared operators in the expression.
     *
     * @return All declared operators.
     */
    public Set<String> getDeclaredOperators ()
    {
        return Collections.unmodifiableSet(operators.keySet());
    }

    /**
     * Exposing declared functions.
     *
     * @return All declared functions.
     */
    public Set<String> getDeclaredFunctions ()
    {
        return Collections.unmodifiableSet(functions.keySet());
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
     * Returns a list of the variables in the expression.
     *
     * @return A list of the variable names in this expression.
     */
    public List<String> getUsedVariables ()
    {
        List<String> result = new ArrayList<>();
        Tokenizer tokenizer = new Tokenizer(expression);
        while (tokenizer.hasNext())
        {
            String token = tokenizer.next();
            if (functions.containsKey(token) || operators.containsKey(token)
                    || token.equals("(") || token.equals(")")
                    || token.equals(",") || isNumber(token)
                    || token.equals("PI") || token.equals("e")
                    || token.equals("TRUE") || token.equals("FALSE"))
            {
                continue;
            }
            result.add(token);
        }
        return result;
    }    /**
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
            else if (variables.containsKey(token))
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

// --Commented out by Inspection START (1/28/2017 1:58 PM):
//    /**
//     * The original expression used to construct this expression, without
//     * variables substituted.
//     */
//    public String getOriginalExpression ()
//    {
//        return this.originalExpression;
//    }
// --Commented out by Inspection STOP (1/28/2017 1:58 PM)

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
     * LazyNumber interface created for lazily evaluated functions
     */
    interface LazyNumber
    {
        BigDecimal eval ();
    }

    /**
     * The expression evaluators exception class.
     */
    public static class ExpressionException extends RuntimeException
    {
        private static final long serialVersionUID = 1118142866870779047L;

        public ExpressionException (String message)
        {
            super(message);
        }
    }

    class Mathobject
    {
        String name = null;
        String desc = null;

        public String getDescription()
        {
            return desc;
        }

        public String getName ()
        {
            return name;
        }

    }

    @SuppressWarnings("Since15")
    public abstract class LazyFunction extends Mathobject
    {
        /**
         * Number of parameters expected for this function.
         * <code>-1</code> denotes a variable number of parameters.
         */
        private final int numParams;

        /**
         * Creates a new function with given name and parameter count.
         *
         * @param name      The name of the function.
         * @param numParams The number of parameters for this function.
         *                  <code>-1</code> denotes a variable number of parameters.
         */
        public LazyFunction (String name, int numParams)
        {
            this.name = name.toUpperCase(Locale.ROOT);
            this.numParams = numParams;
        }


        public int getNumParams ()
        {
            return numParams;
        }

        public boolean numParamsVaries ()
        {
            return numParams < 0;
        }

        public abstract LazyNumber lazyEval (List<LazyNumber> lazyParams);
    }

    /**
     * Abstract definition of a supported expression function. A function is
     * defined by a name, the number of parameters and the actual processing
     * implementation.
     */
    public abstract class Function extends LazyFunction
    {
        public Function (String name, int numParams, String desc)
        {
            super(name, numParams);
            this.desc = desc;
        }

        public Function (String name, int numParams)
        {
            super(name, numParams);
        }

        public LazyNumber lazyEval (List<LazyNumber> lazyParams)
        {
            final List<BigDecimal> params = new ArrayList<>();
            for (LazyNumber lazyParam : lazyParams)
            {
                params.add(lazyParam.eval());
            }
            return () -> Function.this.eval(params);
        }

        /**
         * Implementation for this function.
         *
         * @param parameters Parameters will be passed by the expression evaluator as a
         *                   {@link List} of {@link BigDecimal} values.
         * @return The function must return a new {@link BigDecimal} value as a
         * computing result.
         */
        public abstract BigDecimal eval (List<BigDecimal> parameters);
    }

    /**
     * Abstract definition of a supported operator. An operator is defined by
     * its name (pattern), precedence and if it is left- or right associative.
     */
    public abstract class Operator extends Mathobject
    {
        /**
         * Operators precedence.
         */
        private final int precedence;
        /**
         * Operator is left associative.
         */
        private final boolean leftAssoc;

        /**
         * Creates a new operator.
         *
         * @param name       The operator name (pattern).
         * @param precedence The operators precedence.
         * @param leftAssoc  <code>true</code> if the operator is left associative,
         *                   else <code>false</code>.
         */
        public Operator (String name, int precedence, boolean leftAssoc)
        {
            this (name, precedence, leftAssoc, null);
        }

        public Operator (String name, int precedence, boolean leftAssoc, String desc)
        {
            this.name = name;
            this.precedence = precedence;
            this.leftAssoc = leftAssoc;
            this.desc = desc;
        }

        public int getPrecedence ()
        {
            return precedence;
        }

        public boolean isLeftAssoc ()
        {
            return leftAssoc;
        }

        /**
         * Implementation for this operator.
         *
         * @param v1 Operand 1.
         * @param v2 Operand 2.
         * @return The result of the operation.
         */
        public abstract BigDecimal eval (BigDecimal v1, BigDecimal v2);
    }

    /**
     * Expression tokenizer that allows to iterate over a {@link String}
     * expression token by token. Blank characters will be skipped.
     */
    private class Tokenizer implements Iterator<String>
    {

        /**
         * Actual position in expression string.
         */
        private int pos = 0;

        /**
         * The original input expression.
         */
        private final String input;
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
                while ((Character.isDigit(ch) || ch == decimalSeparator
                        || ch == 'e' || ch == 'E'
                        || (ch == minusSign && token.length() > 0
                        && ('e' == token.charAt(token.length() - 1) || 'E' == token.charAt(token.length() - 1)))
                        || (ch == '+' && token.length() > 0
                        && ('e' == token.charAt(token.length() - 1) || 'E' == token.charAt(token.length() - 1)))
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
