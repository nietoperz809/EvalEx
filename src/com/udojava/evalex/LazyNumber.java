package com.udojava.evalex;

import java.math.BigDecimal;

/**
 * LazyNumber interface created for lazily evaluated functions
 */
interface LazyNumber
{
    BigDecimal eval ();
}
