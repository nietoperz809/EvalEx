package com.udojava.evalex;

import jline.TerminalFactory;
import jline.console.ConsoleReader;
import jline.console.completer.StringsCompleter;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Administrator on 1/26/2017.
 */
@SuppressWarnings("unchecked")
public class Main
{
    // Generated by ANT
    public static final String BUILD_NUMBER = "301";
    public static final String ANSI_CLS = "\u001b[2J";
    public static final String ANSI_BLINK = "\u001b[5m";
    public static final String ANSI_UNDERLINE = "\u001b[4m";
    public static final String ANSI_HOME = "\u001b[H";
    public static final String ANSI_BOLD = "\u001b[1m";
    public static final String ANSI_AT55 = "\u001b[10;10H";
    public static final String ANSI_REVERSEON = "\u001b[7m";
    public static final String ANSI_NORMAL = "\u001b[0m";
    public static final String ANSI_WHITEONBLUE = "\u001b[37;44m";
    /**
     * Definition of PI as a constant, can be used in expressions as variable.
     */
    private static final BigDecimal PI = new BigDecimal(
            "3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679");

    /**
     * Definition of e: "Euler's number" as a constant, can be used in expressions as variable.
     */
    private static final BigDecimal e = new BigDecimal(
            "2.71828182845904523536028747135266249775724709369995957496696762772407663");
    static TreeMap<String, BigDecimal> _variables = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    static LinkedList<String> _history = new LinkedList<>();
    static Integer _radix = 0;
    static ConsoleReader _console;
    private static String[] cmdList = {".?", ".r", ".d", ".s", ".l", ".p", ".f", ".o", ".h", ".x", ".v"};

    public static void main (String[] args) throws IOException, InterruptedException, URISyntaxException
    {
        AnsiConsole.systemInstall();
        AnsiConsole.out.println("\n*** Programmer's Console Calculator (build " + BUILD_NUMBER + ") ***");
        AnsiConsole.out.println("Type '.?' for help");
        start();
    }

    private static void start ()
    {
        _variables.put("e", e);
        _variables.put("PI", PI);
        _variables.put("TRUE", BigDecimal.ONE);
        _variables.put("FALSE", BigDecimal.ZERO);
        _variables.put("BUILD", new BigDecimal(Main.BUILD_NUMBER));
        try
        {
            _console = new ConsoleReader();
            _console.setBellEnabled(true);
            _console.setPrompt("calc> ");
            _console.addCompleter(new StringsCompleter(cmdList));
            String line = null;
            while ((line = _console.readLine()) != null)
            {
                for (String cmd : line.split(":"))
                {
                    try
                    {
                        exec(cmd.trim());
                    }
                    catch (Exception e)
                    {
                        AnsiConsole.out.println("Error: " + e);
                        _console.beep();
                    }
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                TerminalFactory.get().restore();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private static int exec (String s) throws Exception
    {
        if (s.charAt(0) != '.') // no command
        {
            runParser(s);
            return 0;
        }
        char c = s.charAt(1);  // Command
        s = s.substring(2).trim(); // param
        switch (c)
        {
            case 'v': // variables
                if (s.isEmpty())
                {
                    printVars(_variables);
                }
                else
                {
                    String[] vs = s.split("->");
                    _variables.put(vs[0].trim(), getExpression(vs[1].trim()).eval());
                }
                break;

            case 'r': // radix
                if (s.isEmpty())
                {
                    AnsiConsole.out.println("Current radix: " + _radix);
                    return 0;
                }
                _radix = Integer.parseInt(s);
                break;

            case 'd':
                dir();
                break;

            case 's': // save
                save(s);
                break;

            case 'l':  // load
                load(s);
                break;

            case 'p': // repeat
                if (!_history.isEmpty())
                {
                    String old;
                    if (s.isEmpty())
                    {
                        old = _history.get(_history.size() - 1);
                    }
                    else
                    {
                        if (s.equals("all"))
                        {
                            _history.forEach(Main::runParser);
                            return 0;
                        }
                        int num = Integer.parseInt(s);
                        old = _history.get(num);
                    }
                    runParser(old);
                }
                break;

            case 'f': // funcs
                printMathObjects(getExpression("").getFuncs(), s);
                break;

            case 'o': // operands
                printMathObjects(getExpression("").getOps(), s);
                break;

            case '?':
                help();
                break;

            case 'h':
                for (int n = 0; n < _history.size(); n++)
                {
                    AnsiConsole.out.println("" + n + ": " + _history.get(n));
                }
                break;

            case 'x':
                String pr = _console.getPrompt();
                _console.setPrompt("Exit? (y/n)");
                String yn = _console.readLine();
                if (yn.equals("y"))
                {
                    AnsiConsole.out.println(ANSI_CLS);
                    AnsiConsole.systemUninstall();
                    System.exit(0);
                }
                _console.setPrompt(pr);
                break;
        }
        return 0;
    }

    private static void save (String name) throws IOException
    {
        FileOutputStream fout = new FileOutputStream(name + ".list");
        try (ObjectOutputStream oos = new ObjectOutputStream(fout))
        {
            oos.writeObject(_history);
            oos.writeObject(_variables);
            oos.writeObject(_radix);
        }
        fout.close();
    }

    private static void load (String name) throws IOException, ClassNotFoundException
    {
        try (FileInputStream fin = new FileInputStream(name + ".list"))
        {
            try (ObjectInputStream ois = new ObjectInputStream(fin))
            {
                _history = (LinkedList<String>) ois.readObject();
                _variables = (TreeMap<String, BigDecimal>) ois.readObject();
                _radix = (Integer) ois.readObject();
            }
            fin.close();
        }
    }

    private static void runParser (String s)
    {
        Expression e = runUdoParser(s);
        BigDecimal ret = e.eval();
        if (!_history.contains(s))
        {
            _history.addLast(s);
        }
        AnsiConsole.out.print(s + " = ");
        printBigDecimal(ret);
    }

    private static void printVars (Map m)
    {
        for (Object obj : m.entrySet())
        {
            Map.Entry<String, BigDecimal> mo =
                    (Map.Entry<String, BigDecimal>) obj;
            String name = mo.getKey();
            BigDecimal val = mo.getValue();
            AnsiConsole.out.print(Ansi.ansi().fg(Ansi.Color.CYAN));
            AnsiConsole.out.print(name + "\t-> ");
            AnsiConsole.out.print(Ansi.ansi().reset());
            printBigDecimal(val);
        }
    }

    private static Expression getExpression (String s)
    {
        Expression e = new Expression(s, _history, _variables);
        return e;
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
                        fileEntry.getName(), fileEntry.length());
                AnsiConsole.out.print(formatted);
            }
        }
        AnsiConsole.out.println(Ansi.ansi().reset());
    }

    private static void printMathObjects (Map m, String filter)
    {
        for (Object obj : m.entrySet())
        {
            Map.Entry<String, Mathobject> mo =
                    (Map.Entry<String, Mathobject>) obj;
            String name = mo.getKey();
            String desc = mo.getValue().getDescription();
            if (filter == null || name.toLowerCase().startsWith(filter.toLowerCase()))
            {
                AnsiConsole.out.print(Ansi.ansi().fg(Ansi.Color.CYAN));
                AnsiConsole.out.print(name);
                AnsiConsole.out.print(Ansi.ansi().reset());
                if (desc != null)
                {
                    AnsiConsole.out.println("\t:  " + desc);
                }
                else
                {
                    AnsiConsole.out.println();
                }
            }
        }
    }

    public static void help ()
    {
        AnsiConsole.out.print(Ansi.ansi().fg(Ansi.Color.YELLOW));
        AnsiConsole.out.println("Use x, b or o prefix to denote hex, bin or octal numbers.");
        AnsiConsole.out.println("Type " + wg(".o") + " to see list of operators, or " + wg(".f") + " for functions.");
        AnsiConsole.out.println("Both .o and .f can be narrowed giving an argument that is the first char of what is searched.?.");
        AnsiConsole.out.println("Type " + wg(".h") + " to see the history or " + wg(".p") + " to re-evaluate the last term.");
        AnsiConsole.out.println("if '.p' is followed by a number 'n' then history[n] will be re-evaluated.");
        AnsiConsole.out.println("if '.p' is followed by 'all' then the whole history is replayed.");
        AnsiConsole.out.println("You can save and load the state of the calculator using " + wg(".s") + " and " + wg(".l") + " followed by file name.");
        AnsiConsole.out.println("To change the output, type '" + wg(".r") + " n' where n can be any value from 2 to 36 inclusively.");
        AnsiConsole.out.println("If .r (radix) is 0, then output is presented as real number, otherwise it is integer.");
        AnsiConsole.out.println("Type " + wg(".v") + " to see the list of variables.");
        AnsiConsole.out.println("If .v is followed by x->y then variable x is set to value y.");
        AnsiConsole.out.println("... or type any term (that is evaluated immediately) - or " + wg(".x") + " to exit ...");
        AnsiConsole.out.print(Ansi.ansi().reset());
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
        s = s.replaceAll("xor", "§§§2");
        s = s.replaceAll("or", " or ");
        s = s.replaceAll("and", " and ");
        s = s.replaceAll("shl", " shl ");
        s = s.replaceAll("shr", " shr ");
        s = s.replaceAll("§§§1", "floor"); // restore some words
        s = s.replaceAll("§§§2", " xor ");
        return getExpression(s);
    }

    private static void printBigDecimal (BigDecimal ret)
    {
        BigInteger big = ret.toBigInteger();
        AnsiConsole.out.print(ANSI_BOLD + ANSI_WHITEONBLUE);
        if (_radix == 0)
        {
            AnsiConsole.out.println(ret.toPlainString() + ANSI_NORMAL);
        }
        else
        {
            AnsiConsole.out.println(big.toString(_radix) + " (r:" + _radix + ")");
        }
        AnsiConsole.out.print(ANSI_NORMAL);
        AnsiConsole.out.flush();
    }

    public static String wg (String s)
    {
        return Ansi.ansi().fg(Ansi.Color.WHITE) + s + Ansi.ansi().fg(Ansi.Color.YELLOW);
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
