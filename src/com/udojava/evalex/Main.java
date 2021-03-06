package com.udojava.evalex;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
import jline.TerminalFactory;
import jline.console.ConsoleReader;
import jline.console.completer.StringsCompleter;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 1/26/2017.
 */
@SuppressWarnings("unchecked")
public class Main implements Serializable
{
    // Generated by ANT
    private static final String BUILD_NUMBER = "506";
    private static final String ANSI_CLS = "\u001b[2J";
    // --Commented out by Inspection (2/19/2017 10:27 AM):public static final String ANSI_BLINK = "\u001b[5m";
    // --Commented out by Inspection (2/19/2017 10:27 AM):public static final String ANSI_UNDERLINE = "\u001b[4m";
    // --Commented out by Inspection (2/19/2017 10:27 AM):public static final String ANSI_HOME = "\u001b[H";
    private static final String ANSI_BOLD = "\u001b[1m";
    // --Commented out by Inspection (2/19/2017 10:27 AM):public static final String ANSI_AT55 = "\u001b[10;10H";
    // --Commented out by Inspection (2/19/2017 10:27 AM):public static final String ANSI_REVERSEON = "\u001b[7m";
    private static final String ANSI_NORMAL = "\u001b[0m";
    private static final String ANSI_WHITEONBLUE = "\u001b[37;44m";
    private static final String ANSI_ERROR = "\u001b[93;41m";
    /**
     * Definition of PI as a constant, can be used in expressions as variable.
     */
    private static final MyComplex PI = new MyComplex(Math.PI);
    /**
     * Definition of e: "Euler's number" as a constant, can be used in expressions as variable.
     */
    private static final MyComplex e = new MyComplex (Math.E);
    private static ConsoleReader _console;
    private static final String[] cmdList = {".i", ".?", ".r", ".d", ".s", ".l", ".p", ".f", ".o", ".h", ".x", ".v"};

    private Variables  _variables = new Variables();
    private LinkedList<String> _history = new LinkedList<>();
    private Integer _radix = 0;

    public static void main (String[] args) throws Exception
    {
        Main m = new Main();
        m._variables.put("e", e);
        m._variables.put("PI", PI);
        m._variables.put("TRUE", new MyComplex (1));
        m._variables.put("FALSE", new MyComplex (0));
        m._variables.put("BUILD", new MyComplex(Main.BUILD_NUMBER));

//        execCommandOrTerm(".i1;2;0.1,sin(_)");
        //        _variables.put("jaja",
//                new MyComplex(2.10130339252156783658165295491926372051239013671875,
//                1.1897377641407580473043026358936913311481475830078125));
//        Expression e = getExpression(".i1;2;0.1,sin(_)");
//        MyComplex ret = e.eval();
//        printBigNumber(ret);
//        System.exit(0);

        AnsiConsole.systemInstall();
        AnsiConsole.out.println("\n*** Programmer's Console Calculator (build " + BUILD_NUMBER + ") ***");
        AnsiConsole.out.println("Type '.?' for help");
        m.runInputLoop();
    }

    private void runInputLoop ()
    {
        try
        {
            _console = new ConsoleReader();
            _console.setBellEnabled(true);
            _console.setPrompt("calc> ");
            _console.addCompleter(new StringsCompleter(cmdList));
            String line;
            while ((line = _console.readLine()) != null)
            {
                if (line.isEmpty())
                    continue;
                try
                {
                    execCommandOrTerm(line.trim());
                }
                catch (Exception e)
                {
                    AnsiConsole.out.println(ANSI_ERROR+"   Error: " + e+ANSI_NORMAL);
                    _console.beep();
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

    private void execCommandOrTerm (String s) throws Exception
    {
        if (s.charAt(0) != '.') // no command
        {
            runParser(s);
            return;
        }
        char c = s.charAt(1);  // Command
        s = s.substring(2).trim(); // param
        switch (c)
        {
            case 'i':
                iterate(s);
                break;

            case 'v': // variables
                if (s.isEmpty())
                {
                    printVars(_variables.getMap());
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
                    return;
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
                            _history.forEach(this::runParser);
                            return;
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

            default:
                throw new ExpressionException("Unknown command");
        }
    }

    // .i1;2;0.1;sin(_)
    private void iterate (String s)
    {
        String[] toks = s.split(";");
        if (toks.length < 3 || toks.length > 5)
            throw new ExpressionException("need 3,4 or 5 args");
        MyComplex v1 = new MyComplex(toks[0]);  // start value
        MyComplex v2 = new MyComplex(toks[1]);  // end value
        double step = 1.0;
        String term = toks[2];
        if (toks.length >= 4)
        {
            step = new MyComplex(toks[2]).abs();   // step width
            term = toks[3]; // expression
        }
        if (v2.real < v1.real)  // swap if end vale is smaller
        {
            MyComplex t = v1;
            v1 = v2;
            v2 = t;
        }
        if (v2.real == v1.real)
        {
            v2 = v2.add(new MyComplex (1));
        }
        ArrayList<MyComplex> arr = new ArrayList<>();
        while (v1.compareToReal(v2)<=0)  // Run loop
        {
            _variables.put("_", v1);
            AnsiConsole.out.print(v1.toPlainStringComplex()+ " -> ");
            arr.addAll(runParser(term));
            v1 = v1.add(new MyComplex(step)); // inc loop counter
        }
        if (toks.length == 5)
        {
            MyComplex mc = new MyComplex(arr);
            _variables.put (toks[4], mc);
        }
    }

    private void save (String name) throws IOException
    {
        if (name.isEmpty())
            name = "default";
        String j1 = JsonWriter.objectToJson(this);
        PrintWriter p = new PrintWriter(name+".json");
        p.println (JsonWriter.formatJson(j1));
        p.close();
    }

    private void load (String name) throws IOException, ClassNotFoundException
    {
        if (name.isEmpty())
            name = "default";
        byte[] b = Files.readAllBytes(Paths.get(name+".json"));
        String s = new String(b);
        Main m = (Main) JsonReader.jsonToJava(s);
        this._history = m._history;
        this._radix = m._radix;
        this._variables = m._variables;
    }

    private List<MyComplex> runParser (String terms)
    {
        ArrayList<MyComplex> arr = new ArrayList<>();
        for (String s : terms.split(":"))
        {
            Expression e = runUdoParser(s);
            MyComplex ret = e.eval();
            arr.add(ret);
            if (!_history.contains(s))
            {
                _history.addLast(s);
            }
            AnsiConsole.out.print(s + " = ");
            printBigNumber(ret);
        }
        return arr;
    }

    private void printVars (Map m)
    {
        for (Object obj : m.entrySet())
        {
            Map.Entry<String, MyComplex> mo =
                    (Map.Entry<String, MyComplex>) obj;
            String name = mo.getKey();
            MyComplex val = mo.getValue();
            AnsiConsole.out.print(Ansi.ansi().fg(Ansi.Color.CYAN));
            AnsiConsole.out.print(name + "\t-> ");
            AnsiConsole.out.print(Ansi.ansi().reset());
            printBigNumber(val);
        }
    }

    private Expression getExpression (String s)
    {
        return new Expression(s, _history, _variables);
    }

    private void dir ()
    {
        File[] filesInFolder = new File(".").listFiles();
        AnsiConsole.out.print(Ansi.ansi().fg(Ansi.Color.MAGENTA));
        for (final File fileEntry : filesInFolder != null ? filesInFolder : new File[0])
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

    private void printMathObjects (Map m, String filter)
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

    private void help ()
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
        AnsiConsole.out.println("Use "+wg(".i")+ " to do iteration.");
        AnsiConsole.out.println(".i accepts 3, 4 or 5 arguments separated by semicolon. First 2 args are upper and lower level of iteration.");
        AnsiConsole.out.println("The 3rd argument is either the term or the step width. If no step width is given it defaults to 1.");
        AnsiConsole.out.println("The last argument, if given, is the name of the variable where the output is stored (as array).");
        AnsiConsole.out.println("_ is used as iteration variable. So the term must contain _ whereever the variable is needed.");
        AnsiConsole.out.println("Example: .i1;2;0.1;sin(_) calculates 11 sine values from 1 to 2 step 0.1.");
        AnsiConsole.out.println("... or type any term (that is evaluated immediately) - or " + wg(".x") + " to exit ...");
        AnsiConsole.out.print(Ansi.ansi().reset());
    }

    private Expression runUdoParser (String s)
    {
        s = s.replaceAll("\\s+", ""); // remove whitespace
        s = s.replace("!", "!0");  // fake op for factorial
        s = s.replace("~", "0~"); // fake op for negation
        s = Misc.realReplaceAll(s, "(+(", "(0+(");  // fix unary + bug
        s = Misc.realReplaceAll(s, "(-(", "(0-(");  // fix unary - bug
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

    private void printBigNumber (MyComplex ret)
    {
        AnsiConsole.out.print(ANSI_BOLD + ANSI_WHITEONBLUE);
        if (_radix != 0)
        {
            String s = Long.toString((long)(ret.real), _radix);
            AnsiConsole.out.println(s+ANSI_NORMAL+" (r:" + _radix + ")");
        }
        else
        {
            AnsiConsole.out.println(ret.toPlainStringComplex() + ANSI_NORMAL);
        }
        AnsiConsole.out.flush();
    }

    private String wg (String s)
    {
        return Ansi.ansi().fg(Ansi.Color.WHITE) + s + Ansi.ansi().fg(Ansi.Color.YELLOW);
    }
}
