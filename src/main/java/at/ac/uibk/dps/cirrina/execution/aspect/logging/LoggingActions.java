package at.ac.uibk.dps.cirrina.execution.aspect.logging;

import at.ac.uibk.dps.cirrina.execution.command.ActionCommand;
import at.ac.uibk.dps.cirrina.execution.object.statemachine.StateMachine;
import java.lang.reflect.Field;
import java.util.List;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class LoggingActions {

  public static final Logger logger = LogManager.getLogger(LoggingActions.class);
  StateMachine stateMachine;

  @Before("@annotation(LogAction)")
  public void logExecution(JoinPoint joinPoint) {
    stateMachine = (StateMachine) joinPoint.getThis();

    String activeStateName = null;
    try {
      Field activeStateField = stateMachine.getClass().getDeclaredField("activeState");
      activeStateField.setAccessible(true);
      activeStateName = (String) activeStateField.get(stateMachine);
      activeStateField.setAccessible(false);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      logger.error(e.getMessage());
    }

    logger.info("State Machine {}executing action: {} in state: {}",
        stateMachine, joinPoint.getThis().getClass().getSimpleName(), activeStateName);
  }

  @AfterReturning(value = "@annotation(LogAction)", returning = "result")
  public void logResult(List<ActionCommand> result) {
    logger.info("Result: {}", result.isEmpty() ? "None" : result.toString());
  }
}
