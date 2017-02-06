package com.udojava.evalex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by Administrator on 1/26/2017.
 */
@SuppressWarnings("unchecked")
public class Main
{
    // Generated by ANT
    private static final String BUILD_NUMBER = "203";

    static LinkedList<String> _history = new LinkedList<>();
    static int _radix = 0;

    public static void help()
    {
        System.out.println("Use x, b or o prefix to denote hex, bin or octal numbers.");
        System.out.println("Type 'ops' to see list of operators, or 'funcs' for functions.");
        System.out.println("Type 'hist' to see the _history or 'rep' to re-evaluate the last term.");
        System.out.println("if 'rep' is followed by a number 'n' then _history[n] will be re-evaluated.");
        System.out.println("if 'rep' is followed by 'all' then the whole history is replayed.");
        System.out.println("To change the output, type 'radix n' where n can be from 2 to 36 inclusively");
        System.out.println("If radix is 0 then output is presented as real number, otherwise integer.");
        System.out.println("... or type any term - or 'bye' to exit ...");
    }

    public static void main (String[] args) throws IOException, InterruptedException, URISyntaxException
    {
        System.out.println("\n*** Programmer's Console Calculator (build " + BUILD_NUMBER + ") ***");
        System.out.println("Type 'help' for help");
        start();
    }

    private static int exec (String s)
    {
        if (s.startsWith("radix"))
        {
            s = s.substring(5).trim();
            if (s.isEmpty())
            {
                System.out.println("Current radix: " + _radix);
                return 0;
            }
            _radix = Integer.parseInt(s);
            return 0;
        }
        else if (s.startsWith("rep"))
        {
            if (!_history.isEmpty())
            {
                s = s.substring(3).trim();
                String old;
                if (s.isEmpty())
                {
                    old = _history.get(_history.size()-1);
                }
                else
                {
                    if (s.equals("all"))
                    {
                        _history.forEach (Main::runParser);
                        return 0;
                    }
                    int num = Integer.parseInt(s);
                    old = _history.get (num);
                }
                runParser(old);
            }
            return 0;
        }
        else if (s.startsWith("funcs"))
        {
            s = s.substring(5).trim();
            Expression e = new Expression("", null);
            printMathObjects(e.getFuncs(),s);
            return 0;
        }
        else if (s.startsWith("ops"))
        {
            s = s.substring(3).trim();
            Expression e = new Expression("", null);
            printMathObjects(e.getOps(),s);
            return 0;
        }
        switch (s)
        {
            case "help":
                help();
                break;
            case "hist":
                for (int n = 0; n < _history.size(); n++)
                {
                    System.out.println("" + n + ": " + _history.get(n));
                }
                break;
            case "bye":
                System.exit(0);
            default:
                runParser(s);
                break;
        }
        return 0;
    }

    private static void start ()
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        do
        {
            try
            {
                System.out.print("> ");
                String s = br.readLine().toLowerCase().trim();
                for (String cmd : s.split(":"))
                {
                    exec(cmd.trim());
                }
            }
            catch (Exception e)
            {
                System.out.println("Error: "+e);
            }
        } while (true);
    }

    private static void runParser (String s)
    {
        Expression e = runUdoParser(s);
        BigDecimal ret = e.eval();
        if (!_history.contains(s))
            _history.addLast(s);
        BigInteger big = ret.toBigInteger();
        System.out.print(s + " = ");
        if (_radix == 0)
        {
            System.out.println(ret.toString());
        }
        else
        {
            System.out.println(big.toString(_radix)+" (radix:"+ _radix +")");
        }
    }

    private static void printMathObjects (Map m, String filter)
    {
        for (Object obj : m.entrySet())
        {
            Map.Entry<String, Expression.Mathobject> mo =
                    (Map.Entry<String, Expression.Mathobject>) obj;
            String name = mo.getKey();
            String desc = mo.getValue().getDescription();
            if (filter == null || name.toLowerCase().startsWith(filter.toLowerCase()))
            {
                System.out.print(name);
                if (desc != null)
                {
                    System.out.println(" : " + desc);
                }
                else
                {
                    System.out.println();
                }
            }
        }
    }

    private static Expression runUdoParser (String s)
    {
        s = s.replaceAll("\\s+", ""); // remove whitespace
        s = s.replace("!", "!0");  // fake op for factorial
        s = s.replace("~", "0~"); // fake op for negation
        s = realReplaceAll(s, "(+(", "(0+(");  // fix unary + bug
        s = realReplaceAll(s, "(-(", "(0-(");  // fix unary - bug
        if (s.startsWith("-") || s.startsWith("+"))
        {
            s = "0" + s;
        }
        s = s.replaceAll("floor", "§§§1"); // save some words
        s = s.replaceAll ("xor", "§§§2");
        s = s.replaceAll("or", " or ");
        s = s.replaceAll("and", " and ");
        s = s.replaceAll("shl", " shl ");
        s = s.replaceAll("shr", " shr ");
        s = s.replaceAll("§§§1", "floor"); // restore some words
        s = s.replaceAll ("§§§2", " xor ");
        return new Expression(s, _history);
    }

    private static String realReplaceAll (String s, String a, String b)
    {
        for (; ; )
        {
            String n = s.replace(a, b);
            if (n.equals(s))
            {
                return n;
            }
            s = n;
        }
    }
}
