package com.udojava.evalex;

import java.math.BigDecimal;

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
