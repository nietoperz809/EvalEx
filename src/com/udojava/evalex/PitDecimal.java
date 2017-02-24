package com.udojava.evalex;

/**
 * Created by Administrator on 2/19/2017.
 */
class PitDecimal extends MyComplex
{
    String varToken;

    public PitDecimal (double val, double img)
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
