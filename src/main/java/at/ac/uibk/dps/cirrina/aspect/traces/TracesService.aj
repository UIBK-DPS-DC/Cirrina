package at.ac.uibk.dps.cirrina.aspect.traces;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
public class TracesService {
  private final Tracer tracer = GlobalOpenTelemetry.getTracer("Service");

  @Pointcut("execution(at.ac.uibk.dps.cirrina.execution.service..*.handleResponse(..))")
  public void handleResponse(){
  }

  @Pointcut("execution(at.ac.uibk.dps.cirrina.execution.service..*.invoke(..))")
  public void invokeMethod(){
  }

  @AfterThrowing(pointcut = "handleResponse() || invokeMethod()", throwing = "ex")
  public void exceptionProcessor(Exception ex){
    Span span = Span.current();
    span.recordException(ex);
    span.setStatus(StatusCode.ERROR, ex.getMessage());
  }

  @After("handleResponse() || invokeMethod()")
  public void stopTracing(){
    Span span = Span.current();
    span.end();
  }

  @Before("handleResponse()) || invokeMethod()")
  public Object startTracing(ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    String methodName = methodSignature.getName();

    Span span = tracer.spanBuilder(methodName).startSpan();
    return joinPoint.proceed();
  }


}
