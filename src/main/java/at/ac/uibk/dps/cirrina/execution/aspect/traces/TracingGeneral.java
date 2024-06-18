package at.ac.uibk.dps.cirrina.execution.aspect.traces;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

@Aspect
public class TracingGeneral {
  private final Tracer tracer = GlobalOpenTelemetry.getTracer("General");

  @Around("@annotation(TracesGeneral)")
  public <T> T startTracing(ProceedingJoinPoint joinPoint){
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    String methodName = methodSignature.getName();
    String className = joinPoint.getTarget().getClass().getName();

    Span span = tracer.spanBuilder(className + ": " + methodName).startSpan();
    T result = null;
    try(Scope scope = span.makeCurrent()){
      result = (T) joinPoint.proceed();
    } catch (Throwable ex){
      span.setStatus(StatusCode.ERROR, ex.getMessage());
      span.recordException(ex);
    } finally {
      span.end();
    }
    return result;
  }
}
