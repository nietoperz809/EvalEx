package com.udojava.evalex;

/**
 * Created by Administrator on 2/19/2017.
 */
class PitDecimal extends MyComplex
{
    private String varToken;

    PitDecimal (double val, double img)
    {
        super(val, img);
    }

    void setVarToken (String varToken)
    {
        this.varToken = varToken;
    }

    String getVarToken ()
    {
        return varToken;
    }
}
