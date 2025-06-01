# Compiler Project - Group 5e

## Optimizations

### CP2: jmm Compiler

Our compiler implements several optimization strategies to improve code efficiency:

#### Register Allocation

We implemented a graph coloring algorithm for register allocation that reduces register usage by assigning variables to a limited set of registers. The implementation:

1. **Liveness Analysis**: Tracks variable lifetimes by computing Use, Def, In and Out sets for each instruction
2. **Interference Graph Construction**: Creates connections between variables that are live simultaneously 
3. **Graph Coloring**: Assigns registers by coloring the interference graph, prioritizing variables with more connections
4. **Register Reuse**: Efficiently reuses registers for variables with non-overlapping lifetimes

The allocator respects function parameters and handles edge cases, supporting configurable register limits.

#### Constant Propagation

Our constant propagation implementation tracks variable values throughout the program flow:

- **Value Tracking**: Maintains a context map of variables with constant values
- **Propagation**: Replaces variable references with their constant values when safe
- **Flow-Sensitive Analysis**: Handles control flow constructs (if/else, loops) by respecting scoping rules
- **Conservative Propagation**: Avoids propagation when variables might change in conditional branches
- **Branch-Aware Analysis**: Handles differently assigned values in different branches

The implementation handles special cases such as reassignments, conditional assignments, and nested control structures.

#### Constant Folding

We implemented arithmetic and logical expression evaluation at compile time:

- **Binary Operations**: Folds `+`, `-`, `*`, `/`, `<`, and `&&` operations on constant operands
- **Unary Operations**: Folds boolean negation (`!`) operations
- **Integration**: Works together with constant propagation to maximize optimization opportunities
- **Error Prevention**: Guards against issues like division by zero

### CP3: Jasmin Code

- **Low-cost Instructions**: Generated Jasmin bytecode uses optimized instruction selection to reduce execution overhead.

---

### Extra
#### Dead Code Elimination

Our dead code elimination implementation identifies and removes code that has no observable effect on program execution. The implementation includes:

1. **Liveness Analysis**: Identifies variables that are "live" at each program point
2. **Dead Assignment Detection**: Removes assignments to variables that are never used after the assignment
3. **Side Effect Analysis**: Preserves statements with observable side effects (method calls, array assignments, etc.)
4. **Iterative Processing**: Applies elimination repeatedly to find additional dead code revealed by earlier removals

Implementation correctly handles:
- **Simple unused assignments**: Removes basic variable assignments that have no subsequent uses
- **Method call preservation**: Maintains method calls that might have side effects
- **Conditional assignments**: Analyzes control flow to determine variable usage across branches
- **Array operations**: Preserves array element assignments since they might affect visible state
- **Return value tracking**: Ensures variables used in return statements are preserved
- **Chained operations**: Analyzes dependencies in sequences of operations

---

### Developed by:
| Student            | Student ID | Contribution |
|--------------------|------------|------------|
| Diana Nunes        | up202208247 | Constant propagation, Dead code elimination |
| Sofia Gonçalves    | up202205020 | Constant folding, Register allocation |
| Teresa Mascarenhas | up202208247 | Dead code elimination, OLLIR optimization |