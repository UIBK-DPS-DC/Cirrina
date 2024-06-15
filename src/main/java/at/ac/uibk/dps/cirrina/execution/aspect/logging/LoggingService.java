package at.ac.uibk.dps.cirrina.execution.aspect.logging;

import java.net.http.HttpResponse;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class LoggingService {
  public static final Logger logger = LogManager.getLogger(LoggingService.class);


  @Pointcut("execution(* at.ac.uibk.dps.cirrina.execution.service.HttpServiceImplementation.invoke(..))")
  public void invoke() {}

  @Pointcut("execution(* at.ac.uibk.dps.cirrina.execution.service.HttpServiceImplementation.*handleResponse(..))")
  public void handleResponse() {}

  @Before("invoke() && args(id)")
  public void invoke(String id) {
    logger.info("Service invoked by " + id);
  }

  @Before("handleResponse() && args(response)")
  public void handleResponse(JoinPoint joinPoint, HttpResponse<byte[]> response) {
    logger.info("Handling response: " + Arrays.toString(response.body()));
  }
}
