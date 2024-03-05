package at.ac.uibk.dps.cirrina.core.objects;

import dev.cel.common.CelAbstractSyntaxTree;
import dev.cel.common.CelValidationException;
import dev.cel.compiler.CelCompiler;
import dev.cel.compiler.CelCompilerFactory;
import dev.cel.runtime.CelEvaluationException;
import dev.cel.runtime.CelRuntimeFactory;

public class Expression {
    private final String source;

    private final CelCompiler celCompiler;

    private final CelAbstractSyntaxTree ast;

    public Expression(String source) throws IllegalArgumentException {
        this.source = source;

        // Attempt to parse the expression into an abstract syntax tree
        celCompiler = CelCompilerFactory.standardCelCompilerBuilder().build();

        // Parse the expression
        var parseResult = celCompiler.parse(this.source);

        // Check if parsing succeeded
        if (parseResult.hasError()) {
            throw new IllegalArgumentException(String.format("Failed to compile expression: %s", parseResult.getErrorString()));
        }

        // Attempt to acquire the abstract syntax tree
        try {
            ast = parseResult.getAst();
        } catch (CelValidationException e) {
            throw new IllegalArgumentException(String.format("Failed to compile expression: %s", e.getMessage()));
        }
    }

    public Object execute() throws IllegalArgumentException { // TODO: This should throw a Cirrina runtime error and return a variable
        // Perform checking
        var checkResult = celCompiler.check(ast);

        // Check if parsing succeeded
        if (checkResult.hasError()) {
            throw new IllegalArgumentException(String.format("Failed to execute expression: %s", checkResult.getErrorString()));
        }

        try {
            var celRuntime = CelRuntimeFactory.standardCelRuntimeBuilder().build();
            var program = celRuntime.createProgram(checkResult.getAst());
            return program.eval();
        } catch (CelValidationException | CelEvaluationException e) {
            throw new IllegalArgumentException(String.format("Failed to execute expression: %s", e.getMessage()));
        }
    }
}
