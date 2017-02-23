package com.udojava.evalex;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Administrator on 2/21/2017.
 */
public class Variables
{
    private static TreeMap<String, BigComplex> _variables = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public void put (String k, BigComplex v)
    {
        char c = k.charAt(0);
        if (c=='x' || c=='o' || c== 'b' || c=='h')
            throw new ExpressionException("not allowed as first char: "+c);
        _variables.put(k, v);
    }

    public Map getMap()
    {
        return _variables;
    }

    public boolean containsKey(String k)
    {
        return _variables.containsKey(k);
    }

    public BigComplex get (String k)
    {
        return _variables.get(k);
    }

    public void setMap (TreeMap<String, BigComplex> m)
    {
        _variables = m;
    }
}
