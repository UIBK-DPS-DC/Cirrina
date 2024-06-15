package at.ac.uibk.dps.cirrina.execution.aspect.traces;

import at.ac.uibk.dps.cirrina.execution.object.state.State;
import at.ac.uibk.dps.cirrina.execution.object.statemachine.StateMachine;
import at.ac.uibk.dps.cirrina.execution.object.transition.Transition;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import java.lang.reflect.Field;

@Aspect
public class TracingStateMachine {
  private final Tracer tracer = GlobalOpenTelemetry.getTracer("StateMachine");
  StateMachine stateMachine;

  @Pointcut("execution(* at.ac.uibk.dps.cirrina.execution.object.statemachine.StateMachine.*doTransition(..))")
  public void doTransition(){
  }

  @Pointcut("execution(* at.ac.uibk.dps.cirrina.execution.object.statemachine.StateMachine.*doEnter(..))")
  public void doEnter(){
  }

  @Pointcut("execution(* at.ac.uibk.dps.cirrina.execution.object.statemachine.StateMachine.*doExit(..))")
  public void doExit(){
  }

  @Pointcut("execution(* at.ac.uibk.dps.cirrina.execution.object.statemachine.StateMachine.*handleEvent(..))")
  public void handleEvent(){
  }

  @Around("doTransition() && args(transition)")
  public void doTransition(ProceedingJoinPoint joinPoint, Transition transition){
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    String methodName = methodSignature.getName();
    String className = joinPoint.getTarget().getClass().getName();
    stateMachine = (StateMachine) joinPoint.getThis();

    String activeStateName = null;
    try {
      Field activeStateField = stateMachine.getClass().getDeclaredField("activeState");
      activeStateField.setAccessible(true);
      activeStateName = (String) activeStateField.get(stateMachine);
      activeStateField.setAccessible(false);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
    }



    Span span = tracer.spanBuilder(className + ": " + methodName).startSpan();
    span.setAttribute("Source State", activeStateName);
    span.setAttribute("Target State", transition.getTargetStateName().isPresent() ? transition.getTargetStateName().get() : "None");
    span.setAttribute("State Machine", stateMachine.getId());
    span.setAttribute("Internal", transition.isInternalTransition());

    try{
      joinPoint.proceed();
    } catch (Throwable e){
      span.recordException(e);
      span.setStatus(StatusCode.ERROR, e.getMessage());
    } finally {
      span.end();
    }

  }

  @Around("doEnter() && args(enteringState)")
  public void StateEnter(ProceedingJoinPoint joinPoint, State enteringState){
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    String methodName = methodSignature.getName();
    stateMachine = (StateMachine) joinPoint.getThis();
    String className = joinPoint.getTarget().getClass().getName();

    Span span = tracer.spanBuilder(className + ": " + methodName).startSpan();

    span.setAttribute("Entered State", enteringState.getStateObject().getName());
    span.setAttribute("State Machine", stateMachine.getId());

    try{
      joinPoint.proceed();
    } catch (Throwable e){
      span.recordException(e);
      span.setStatus(StatusCode.ERROR, e.getMessage());
    } finally {
      span.end();
    }

  }

  @Around("doExit() && args(exitedState)")
  public void StateExit(ProceedingJoinPoint joinPoint, State exitedState){
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    String methodName = methodSignature.getName();
    stateMachine = (StateMachine) joinPoint.getThis();

    String className = joinPoint.getTarget().getClass().getName();

    Span span = tracer.spanBuilder(className + ": " + methodName).startSpan();
    span.setAttribute("Exited State", exitedState.getStateObject().getName());
    span.setAttribute("State Machine", stateMachine.getId());

    try{
      joinPoint.proceed();
    } catch (Throwable e){
      span.recordException(e);
      span.setStatus(StatusCode.ERROR, e.getMessage());
    } finally {
      span.end();
    }

  }

  @Around("handleEvent()")
  public void handleEvent(ProceedingJoinPoint joinPoint) {
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    String methodName = methodSignature.getName();
    String className = joinPoint.getTarget().getClass().getName();
    stateMachine = (StateMachine) joinPoint.getThis();

    String activeStateName = null;
    try {
      Field activeStateField = stateMachine.getClass().getDeclaredField("activeState");
      activeStateField.setAccessible(true);
      activeStateName = (String) activeStateField.get(stateMachine);
      activeStateField.setAccessible(false);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
    }

    Span span = tracer.spanBuilder(className + ": " + methodName).startSpan();
    span.setAttribute("State Machine", stateMachine.getId());
    span.setAttribute("Active State", activeStateName);

    try {
      joinPoint.proceed();
    } catch (Throwable e) {
      span.recordException(e);
      span.setStatus(StatusCode.ERROR, e.getMessage());
    } finally {
      span.end();
    }

  }


}
