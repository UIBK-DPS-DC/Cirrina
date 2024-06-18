package at.ac.uibk.dps.cirrina.execution.aspect.logging;

import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    logger.info("Service invoked by Sender with ID: {}", id);
  }

  @Before("handleResponse() && args(response)")
  public void handleResponse(SimpleHttpResponse response) {
    logger.info("Handling response: {}", response.getBodyText());
  }
}
