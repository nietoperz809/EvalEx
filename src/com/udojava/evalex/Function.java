package com.udojava.evalex;

import java.util.ArrayList;
import java.util.List;

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
        List<MyComplex> params = new ArrayList<>();
        for (LazyNumber lazyParam : lazyParams)
        {
            params.add(lazyParam.eval());
        }
        if (params.get(0).type == ValueType.ARRAY
                && params.size() == 1
                && getNumParams() == -1
                && !params.get(0).isPoly())
        {
            params = params.get(0).list;
        }
        List<MyComplex> finalParams = params;
        return () -> Function.this.eval(finalParams);
    }

    /**
     * Implementation for this function.
     *
     * @param parameters Parameters will be passed by the expression evaluator as a
     *                   {@link List} of {@link MyComplex} values.
     * @return The function must return a new {@link MyComplex} value as a
     * computing result.
     */
    public abstract MyComplex eval (List<MyComplex> parameters);
}
