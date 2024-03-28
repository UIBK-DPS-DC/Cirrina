package at.ac.uibk.dps.cirrina.core.object.expression;

import static at.ac.uibk.dps.cirrina.core.exception.VerificationException.Message.EXPRESSION_COULD_NOT_BE_PARSED;

import at.ac.uibk.dps.cirrina.core.exception.RuntimeException;
import at.ac.uibk.dps.cirrina.core.exception.VerificationException;
import at.ac.uibk.dps.cirrina.core.object.context.Context;
import at.ac.uibk.dps.cirrina.core.object.context.ContextVariable;
import at.ac.uibk.dps.cirrina.core.object.context.Extent;
import dev.cel.common.CelAbstractSyntaxTree;
import dev.cel.common.CelValidationException;
import dev.cel.compiler.CelCompiler;
import dev.cel.compiler.CelCompilerFactory;
import dev.cel.parser.CelStandardMacro;
import dev.cel.runtime.CelEvaluationException;
import dev.cel.runtime.CelRuntime;
import dev.cel.runtime.CelRuntimeFactory;
import dev.cel.runtime.CelVariableResolver;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * CEL expression, an expression using Google's CEL language.
 *
 * @see <a href="https://github.com/google/cel-spec">Google CEL specification</a>
 */
public final class CelExpression extends Expression {

  private final CelAbstractSyntaxTree ast;

  /**
   * Initializes a CEL expression. Parses the expression and reports an error in case parsing fails.
   *
   * @param source Source string.
   * @throws IllegalArgumentException In case the expression could not be parsed.
   * @throws IllegalStateException    In case the expression could not be parsed.
   */
  CelExpression(String source) throws IllegalArgumentException, IllegalStateException {
    super(source);

    // Attempt to parse the expression into an abstract syntax tree
    CelCompiler celCompiler = null;

    try {
      celCompiler = getCelCompiler();
    } catch (RuntimeException e) {
      throw new IllegalStateException(String.format("Could not acquire a CEL compiler: %s", e.getMessage()));
    }

    // Parse the expression
    var parseResult = celCompiler.parse(source);

    // Check if parsing succeeded
    if (parseResult.hasError()) {
      throw new IllegalArgumentException(VerificationException.from(EXPRESSION_COULD_NOT_BE_PARSED, parseResult.getErrorString()));
    }

    // Attempt to acquire the abstract syntax tree
    try {
      ast = parseResult.getAst();
    } catch (CelValidationException e) {
      throw new IllegalArgumentException(VerificationException.from(EXPRESSION_COULD_NOT_BE_PARSED, e.getMessage()));
    }
  }

  /**
   * Returns the standard compiler.
   *
   * @return CEL compiler.
   * @throws RuntimeException In case the compiler could not be acquired.
   */
  private CelCompiler getCelCompiler() throws IllegalArgumentException, RuntimeException {
    var builder = CelCompilerFactory.standardCelCompilerBuilder();

    builder.setStandardMacros(CelStandardMacro.ALL);
    builder.setStandardEnvironmentEnabled(true);

    return builder.build();
  }

  /**
   * Returns the standard runtime.
   *
   * @return CEL runtime.
   */
  private CelRuntime getCelRuntime() {
    var builder = CelRuntimeFactory.standardCelRuntimeBuilder();

    builder.setStandardEnvironmentEnabled(true);

    return builder.build();
  }

  /**
   * Converts context variables to a map of name and value.
   *
   * @param context Context for retrieving variables.
   * @return Map of names and values.
   * @throws RuntimeException If the variables could not be retrieved.
   */
  private Map<String, Object> getVariables(Context context) throws RuntimeException {
    try {
      return context.getAll().stream()
          .collect(Collectors.toMap(ContextVariable::name, ContextVariable::value));
    } catch (RuntimeException e) {
      throw RuntimeException.from("Failed to retrieve context variables: %s", e.getMessage());
    }
  }

  /**
   * Executes this expression, producing a value.
   *
   * @param extent Extent for resolving variables.
   * @return Result of the expression.
   * @throws RuntimeException In case of an error while executing the expression.
   */
  public Object execute(Extent extent) throws RuntimeException {
    var celCompiler = getCelCompiler();

    // Perform checking
    var checkResult = celCompiler.check(ast);

    // Check if parsing succeeded
    if (checkResult.hasError()) {
      throw RuntimeException.from("Failed to execute expression: %s", checkResult.getErrorString());
    }

    try {
      var celRuntime = CelRuntimeFactory.standardCelRuntimeBuilder().build();
      var program = celRuntime.createProgram(checkResult.getAst());

      return program.eval(new ExtentVariableResolver());
    } catch (CelValidationException | CelEvaluationException e) {
      throw RuntimeException.from("Failed to execute expression: %s", e.getMessage());
    }
  }

  class ExtentVariableResolver implements CelVariableResolver {

    @Override
    public Optional<Object> find(String name) {
      return Optional.empty();
    }
  }
}
