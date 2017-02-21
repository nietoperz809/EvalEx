package com.udojava.evalex;

import java.math.BigDecimal;

/**
 * Created by Administrator on 2/19/2017.
 */
class PitDecimal extends BigComplex
{
    String varToken;

    public PitDecimal (String val, String img)
    {
        super(val, img);
    }

    public PitDecimal (BigDecimal val, BigDecimal img)
    {
        super(val, img);
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
