package at.ac.uibk.dps.cirrina.aspect.traces;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
public class TracesGuards {
  private final Tracer tracer = GlobalOpenTelemetry.getTracer("Guards");

  @Pointcut("execution(at.ac.uibk.dps.cirrina.execution.object.guard..*.evaluate(..))")
  public void evaluateGuard(){
  }

  @Around("evaluateGuard()")
  public Object startTracing(ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    String methodName = methodSignature.getName();

    Span span = tracer.spanBuilder(methodName).startSpan();
    try {
      return joinPoint.proceed();

    } catch (IllegalArgumentException | UnsupportedOperationException e){
      span.recordException(e);
      span.setStatus(StatusCode.ERROR, e.getMessage());
      throw e;
    } finally {
      span.end();
    }
  }

}
