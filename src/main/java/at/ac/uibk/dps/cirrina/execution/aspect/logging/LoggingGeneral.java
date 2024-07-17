package at.ac.uibk.dps.cirrina.execution.aspect.logging;

import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Aspect
public class LoggingGeneral {
  public static final Logger logger = LogManager.getLogger(LoggingGeneral.class);

  @AfterThrowing(pointcut = "@annotation(LogGeneral)", throwing = "ex")
  public void exceptionLogger(Exception ex){
    logger.error(ex.getMessage(), ex);
  }


}
