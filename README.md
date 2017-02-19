Simple command line calculator for programmers
==============================================

Use x, b or o prefix to denote hex, bin or octal numbers.

Type .o to see list of operators, or .f for functions.

Both .o and .f can be narrowed giving an argument that is the first char of what is searched.?.

Type .h to see the history or .p to re-evaluate the last term.

if '.p' is followed by a number 'n' then history[n] will be re-evaluated.

if '.p' is followed by 'all' then the whole history is replayed.

You can save and load the state of the calculator using .s and .l followed by file name.

To change the output, type '.r n' where n can be any value from 2 to 36 inclusively.

If .r (radix) is 0, then output is presented as real number, otherwise it is integer.

Type .v to see the list of variables.

If .v is followed by x->y then variable x is set to value y.

Use .i to do iteration.

.i accepts 3 or 4 arguments separated by comma. First 2 args are upper and lower level of iteration.

The 3rd argument is either the term or the step width. If no step width is given it defaults to 1.
_ is used as iteration variable. So the term must contain _ whereever the variable is needed.

Example: .i1,2,0.1,sin(_) calculates 11 sine values from 1 to 2 step 0.1.

... or type any term (that is evaluated immediately) - or .x to exit ...
