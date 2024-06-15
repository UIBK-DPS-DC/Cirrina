package at.ac.uibk.dps.cirrina.execution.aspect.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class LoggingTimeoutManager {
  public static final Logger logger = LogManager.getLogger(LoggingTimeoutManager.class);


  @Pointcut("execution(* at.ac.uibk.dps.cirrina.execution.object.statemachine.TimeoutActionManager.start(..))")
  public void start() {}

  @Pointcut("execution(* at.ac.uibk.dps.cirrina.execution.object.statemachine.TimeoutActionManager.stop(..))")
  public void stop() {}

  @Pointcut("execution(* at.ac.uibk.dps.cirrina.execution.object.statemachine.TimeoutActionManager.stopAll(..))")
  public void stopAll() {}



  @Before("start() && args(actionName)")
  public void start(String actionName) {
    logger.info("TIMEOUT: Starting " + actionName);
  }

  @Before("stop() && args(actionName)")
  public void stop(String actionName) {
    logger.info("TIMEOUT: Stopping " + actionName);
  }

  @Before("stopAll()")
  public void stopAllTimeouts() {
    logger.info("TIMEOUT: Stopping all");
  }
}
