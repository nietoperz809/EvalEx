package com.udojava.evalex;

import java.math.BigDecimal;

/**
 * Created by Administrator on 2/19/2017.
 */
class PitDecimal extends BigDecimal
{
    String varToken;

    public PitDecimal (String val)
    {
        super(val);
    }

    public void setVarToken (String varToken)
    {
        this.varToken = varToken;
    }

    public String getVarToken ()
    {
        return varToken;
    }
}
