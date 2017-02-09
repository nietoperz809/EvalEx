package com.udojava.evalex;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.io.*;
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
    private static final String BUILD_NUMBER = "254";

    public static final String ANSI_CLS = "\u001b[2J";
    public static final String ANSI_BLINK = "\u001b[5m";
    public static final String ANSI_UNDERLINE = "\u001b[4m";
    public static final String ANSI_HOME = "\u001b[H";
    public static final String ANSI_BOLD = "\u001b[1m";
    public static final String ANSI_AT55 = "\u001b[10;10H";
    public static final String ANSI_REVERSEON = "\u001b[7m";
    public static final String ANSI_NORMAL = "\u001b[0m";
    public static final String ANSI_WHITEONBLUE = "\u001b[37;44m";

    static LinkedList<String> _history = new LinkedList<>();
    static int _radix = 0;

    public static String wg (String s)
    {
        return Ansi.ansi().fg(Ansi.Color.WHITE)+s+Ansi.ansi().fg(Ansi.Color.YELLOW);
    }

    public static void help()
    {
        AnsiConsole.out.print(Ansi.ansi().fg(Ansi.Color.YELLOW));
        AnsiConsole.out.println("Type "+ wg(".?")+" to see this help text.");
        AnsiConsole.out.println("Use x, b or o prefix to denote hex, bin or octal numbers.");
        AnsiConsole.out.println("Type "+ wg(".o")+" to see list of operators, or "+ wg(".f")+" for functions.");
        AnsiConsole.out.println("Both .o and .f can be narrowed giving an argument that is the first char of what is searched.?.");
        AnsiConsole.out.println("Type "+ wg(".h")+" to see the history or "+ wg(".p")+" to re-evaluate the last term.");
        AnsiConsole.out.println("if '.p' is followed by a number 'n' then history[n] will be re-evaluated.");
        AnsiConsole.out.println("if '.p' is followed by 'all' then the whole history is replayed.");
        AnsiConsole.out.println("You can save and load the history using "+ wg(".s")+" and "+ wg(".l")+" followed by file name.");
        AnsiConsole.out.println("To change the output, type '"+ wg(".r")+" n' where n can be any value from 2 to 36 inclusively.");
        AnsiConsole.out.println("If .r (radix) is 0, then output is presented as real number, otherwise it is integer.");
        AnsiConsole.out.println("... or type any term (that is evaluated immediately) - or "+ wg(".x")+" to exit ...");
        AnsiConsole.out.print(Ansi.ansi().reset());
    }

    private static void dir ()
    {
        File[] filesInFolder = new File(".").listFiles();
        AnsiConsole.out.print(Ansi.ansi().fg(Ansi.Color.MAGENTA));
        for (final File fileEntry : filesInFolder)
        {
            if (fileEntry.isFile())
            {
                String formatted = String.format("\n%-15s = %d",
                        fileEntry.getName(), fileEntry.length() );
                AnsiConsole.out.print(formatted);
            }
        }
        AnsiConsole.out.println(Ansi.ansi().reset());
    }

    public static void main (String[] args) throws IOException, InterruptedException, URISyntaxException
    {
        AnsiConsole.systemInstall();
        AnsiConsole.out.println("\n*** Programmer's Console Calculator (build " + BUILD_NUMBER + ") ***");
        AnsiConsole.out.println("Type '.?' for help");
        start();
    }

    private static int exec (String s) throws Exception
    {
        if (s.startsWith(".r"))  // radix
        {
            s = s.substring(2).trim();
            if (s.isEmpty())
            {
                AnsiConsole.out.println("Current radix: " + _radix);
                return 0;
            }
            _radix = Integer.parseInt(s);
            return 0;
        }
        else if (s.startsWith(".d"))
        {
            dir();
            return 0;
        }
        else if (s.startsWith(".s")) // save
        {
            s = s.substring(2).trim()+".list";
            FileOutputStream fout= new FileOutputStream(s);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(_history);
            fout.close();
            return 0;
        }
        else if (s.startsWith(".l")) // load
        {
            s = s.substring(2).trim()+".list";
            FileInputStream fin= new FileInputStream (s);
            ObjectInputStream ois = new ObjectInputStream(fin);
            _history = (LinkedList<String>)ois.readObject();
            fin.close();            return 0;
        }
        else if (s.startsWith(".p"))  // repeat
        {
            if (!_history.isEmpty())
            {
                s = s.substring(2).trim();
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
        else if (s.startsWith(".f")) // funcs
        {
            s = s.substring(2).trim();
            Expression e = new Expression("", null);
            printMathObjects(e.getFuncs(),s);
            return 0;
        }
        else if (s.startsWith(".o")) // operands
        {
            s = s.substring(2).trim();
            Expression e = new Expression("", null);
            printMathObjects(e.getOps(),s);
            return 0;
        }
        switch (s)
        {
            case ".?":    // help
                help();
                break;
            case ".h":   // show history
                for (int n = 0; n < _history.size(); n++)
                {
                    AnsiConsole.out.println("" + n + ": " + _history.get(n));
                }
                break;
            case ".x":  // exit
                AnsiConsole.out.println(ANSI_CLS);
                AnsiConsole.systemUninstall();
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
                AnsiConsole.out.print("> ");
                AnsiConsole.out.flush();
                String s = br.readLine().toLowerCase().trim();
                for (String cmd : s.split(":"))
                {
                    exec(cmd.trim());
                }
            }
            catch (Exception e)
            {
                AnsiConsole.out.println("Error: "+e);
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
        AnsiConsole.out.print(s + " = ");
        AnsiConsole.out.print(ANSI_BOLD+ANSI_WHITEONBLUE);
        if (_radix == 0)
        {
            AnsiConsole.out.println(ret.toPlainString()+ANSI_NORMAL);
        }
        else
        {
            AnsiConsole.out.println(big.toString(_radix)+" (r:"+ _radix +")");
        }
        AnsiConsole.out.print(ANSI_NORMAL);
        AnsiConsole.out.flush();
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
                AnsiConsole.out.print(Ansi.ansi().fg(Ansi.Color.CYAN));
                AnsiConsole.out.print(name);
                AnsiConsole.out.print(Ansi.ansi().reset());
                if (desc != null)
                {
                    AnsiConsole.out.println(" : " + desc);
                }
                else
                {
                    AnsiConsole.out.println();
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
