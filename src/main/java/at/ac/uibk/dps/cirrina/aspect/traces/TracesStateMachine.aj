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
public class TracesStateMachine {
  private final Tracer tracer = GlobalOpenTelemetry.getTracer("State Machine");

  String package_name = "at.ac.uibk.dps.cirrina.classes.statemachine.StateMachineClass";

  @Pointcut("execution(private * at.ac.uibk.dps.cirrina.execution.object.statemachine.StateMachine.*)")
  public void stateMachine(){
  }

  @Pointcut("execution(* at.ac.uibk.dps.cirrina.execution.object.statemachine.StateMachine.state*(..))")
  public void factory(){
  }

  @Pointcut("execution(* at.ac.uibk.dps.cirrina.execution.object.statemachine.StateMachine.find*(..))")
  public void find(){
  }

  @Pointcut("execution(at.ac.uibk.dps.cirrina.classes.statemachine.StateMachineClass.*(..))")
  public void stateMachineClass(){
  }

  @Pointcut("stateMachineClass() && (execution(* find*(..)) || execution(* get*Names(..)) || execution(* get*Events(..)) || execution(* getInitialState(..)))")
  public void stateMachineClassMethods(){
  }

  @AfterThrowing(pointcut = "(stateMachine() && !factory() && !find()) || stateMachineClassMethods()", throwing = "ex")
  public void exceptionProcessor(Exception ex){
    Span span = Span.current();
    span.recordException(ex);
    span.setStatus(StatusCode.ERROR, ex.getMessage());
  }

  @After("(stateMachine() && !factory()) || stateMachineClassMethods()")
  public void stopTracing(){
    Span span = Span.current();
    span.end();
  }

  @Before("(stateMachine() && !factory()) || stateMachineClassMethods()")
  public Object startTracing(ProceedingJoinPoint joinPoint) throws Throwable {
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    String methodName = methodSignature.getName();

    Span span = tracer.spanBuilder(methodName).startSpan();
    return joinPoint.proceed();
  }
}
