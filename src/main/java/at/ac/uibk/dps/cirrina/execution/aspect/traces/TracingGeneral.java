package at.ac.uibk.dps.cirrina.execution.aspect.traces;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
public class TracingGeneral {
  private final Tracer tracer = GlobalOpenTelemetry.getTracer("Action");

  @AfterThrowing(pointcut = "@annotation(TracesGeneral))", throwing = "ex")
  public void exceptionProcessor(Exception ex){
    Span span = Span.current();
    span.recordException(ex);
    span.setStatus(StatusCode.ERROR, ex.getMessage());
  }

  @After("@annotation(TracesGeneral)")
  public void stopTracing(){
    Span span = Span.current();
    span.end();
  }

  @Before("@annotation(TracesGeneral)")
  public void startTracing(JoinPoint joinPoint) throws Throwable {
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    String methodName = methodSignature.getName();
    String className = joinPoint.getTarget().getClass().getName();

    Span span = tracer.spanBuilder(className + ": " + methodName).startSpan();
  }
}
