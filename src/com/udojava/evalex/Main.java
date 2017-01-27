package com.udojava.evalex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URISyntaxException;

/**
 * Created by Administrator on 1/26/2017.
 */
public class Main
{
    public static void main (String [] args) throws IOException, InterruptedException, URISyntaxException
    {
        System.out.println("* Programmer's Console Calculator *\n");
        start();
    }

    private static String realReplaceAll (String s, String a, String b)
    {
        for (;;)
        {
            String n = s.replace(a,b);
            if (n.equals(s))
                return n;
            s = n;
        }
    }


    private static Expression runUdoParser (String in)
    {
        String[] splits = in.split(":");
        for (String s : splits)
        {
            String sraw = s;
            s = s.replaceAll("\\s+", ""); // remove whitespace
            s = s.replace("!", "!0");  // fake op for factorial
            s = s.replace("~", "0~"); // fake op for negation
            s = realReplaceAll(s, "(+(", "(0+(");  // fix unary + bug
            s = realReplaceAll(s, "(-(", "(0-(");  // fix unary - bug
            if (s.startsWith("-") || s.startsWith("+"))
            {
                s = "0" + s;
            }
            s = s.replaceAll("or", " or "); // remove whitespace
            s = s.replaceAll("and", " and "); // remove whitespace
            s = s.replaceAll("xor", " xor "); // remove whitespace
            s = s.replaceAll("shl", " shl "); // remove whitespace
            s = s.replaceAll("shr", " shr "); // remove whitespace
            return new Expression(s);
        }
        return null;
    }

    public static void start ()
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String s;
        do
        {
            try
            {
                s = br.readLine();
                if (s.equals("bye"))
                    break;
                Expression e = runUdoParser(s);
                System.out.println("==>");
                BigDecimal ret = e.eval();
                System.out.println("Decimal: "+ret);
                System.out.println("Integer: "+ret.intValue());
                System.out.println("Octal  : "+Integer.toOctalString(ret.intValue()));
                System.out.println("Hex    : "+String.format("%x", ret.intValue()));
                System.out.println("Bin    : "+Integer.toBinaryString(ret.intValue()));
                System.out.println("Char   : "+(char)ret.intValue());
                System.out.println();
            }
            catch (Exception e)
            {
                s = "";
                System.out.println(e);
            }
        } while (true);

    }
}
