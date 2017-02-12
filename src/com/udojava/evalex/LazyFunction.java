package com.udojava.evalex;

import java.util.List;
import java.util.Locale;

/**
 * Created by Administrator on 2/12/2017.
 */
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
