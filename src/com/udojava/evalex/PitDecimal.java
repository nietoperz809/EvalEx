package com.udojava.evalex;

/**
 * Created by Administrator on 2/19/2017.
 */
class PitDecimal extends BigNumber
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
