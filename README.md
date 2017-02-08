Simple command line calculator for programmers
==============================================

Type .? to see this help text.
Use x, b or o prefix to denote hex, bin or octal numbers.
Type .o to see list of operators, or .f for functions.
Both .o and .f can be narrowed giving an argument that is the first char of listed functions.
Type .h to see the history or .p to re-evaluate the last term.
if '.p' is followed by a number 'n' then history[n] will be re-evaluated.
if '.p' is followed by 'all' then the whole history is replayed.
You can save and load the history unsing .s and .l followed by file name.
To change the output, type '.r n' where n can be from 2 to 36 inclusively
If .r (radix) is 0 then output is presented as real number, otherwise integer.
To clear the screen type .cls.
... or type any term - or .x to exit ...

Based on Udo's EvalEx --> https://github.com/uklimaschewski/EvalEx

