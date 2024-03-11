package at.ac.uibk.dps.cirrina.core.objects;

import at.ac.uibk.dps.cirrina.core.CoreException;
import at.ac.uibk.dps.cirrina.core.objects.context.Context;
import dev.cel.common.CelAbstractSyntaxTree;
import dev.cel.common.CelValidationException;
import dev.cel.common.types.CelTypes;
import dev.cel.compiler.CelCompiler;
import dev.cel.compiler.CelCompilerFactory;
import dev.cel.parser.CelStandardMacro;
import dev.cel.runtime.CelEvaluationException;
import dev.cel.runtime.CelRuntime;
import dev.cel.runtime.CelRuntimeFactory;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Expression {

  private final CelAbstractSyntaxTree ast;

  public Expression(String source) throws IllegalArgumentException {
    // Attempt to parse the expression into an abstract syntax tree
    var celCompiler = getCelCompiler(Optional.empty());

    // Parse the expression
    var parseResult = celCompiler.parse(source);

    // Check if parsing succeeded
    if (parseResult.hasError()) {
      throw new IllegalArgumentException(
          String.format("Failed to compile expression: %s", parseResult.getErrorString()));
    }

    // Attempt to acquire the abstract syntax tree
    try {
      ast = parseResult.getAst();
    } catch (CelValidationException e) {
      throw new IllegalArgumentException(
          String.format("Failed to compile expression: %s", e.getMessage()));
    }
  }

  private CelCompiler getCelCompiler(Optional<Context> context) throws IllegalArgumentException {
    var builder = CelCompilerFactory.standardCelCompilerBuilder();

    builder.setStandardMacros(CelStandardMacro.ALL);
    builder.setStandardEnvironmentEnabled(true);

    context.ifPresent(c -> getVariables(c).keySet()
        .forEach(name -> builder.addVar(name, CelTypes.DYN)));

    return builder.build();
  }

  private CelRuntime getCelRuntime() {
    var builder = CelRuntimeFactory.standardCelRuntimeBuilder();

    builder.setStandardEnvironmentEnabled(true);

    return builder.build();
  }

  private Map<String, Object> getVariables(Context context) {
    return context.getAll().stream()
        .collect(Collectors.toMap(variable -> variable.name, Context.ContextVariable::getValue));
  }

  public Object execute(Context context) throws CoreException {
    var celCompiler = getCelCompiler(Optional.of(context));

    // Perform checking
    var checkResult = celCompiler.check(ast);

    // Check if parsing succeeded
    if (checkResult.hasError()) {
      throw new IllegalArgumentException(
          String.format("Failed to execute expression: %s", checkResult.getErrorString()));
    }

    try {
      var celRuntime = CelRuntimeFactory.standardCelRuntimeBuilder().build();
      var program = celRuntime.createProgram(checkResult.getAst());

      return program.eval(getVariables(context));
    } catch (CelValidationException | CelEvaluationException e) {
      throw new CoreException(String.format("Failed to execute expression: %s", e.getMessage()));
    }
  }
}
