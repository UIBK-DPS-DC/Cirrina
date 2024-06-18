package at.ac.uibk.dps.cirrina.execution.aspect.traces;

import at.ac.uibk.dps.cirrina.execution.object.event.Event;
import at.ac.uibk.dps.cirrina.execution.object.state.State;
import at.ac.uibk.dps.cirrina.execution.object.statemachine.StateMachine;
import at.ac.uibk.dps.cirrina.execution.object.transition.Transition;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import java.util.Optional;
import javax.annotation.Nullable;
import org.aspectj.lang.JoinPoint;
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
  @Pointcut("execution(* at.ac.uibk.dps.cirrina.execution.object.statemachine.StateMachine.*onReceiveEvent(..))")
  public void onReceiveEvent() {}

  @Pointcut("execution(* at.ac.uibk.dps.cirrina.execution.object.statemachine.StateMachine.*switchActiveState(..))")
  public void switchActiveState() {}

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

  private StateMachine getStateMachine(JoinPoint joinPoint) {
    stateMachine = (StateMachine) joinPoint.getThis();
    return stateMachine;
  }

  @Around("onReceiveEvent() && args(event)")
  public boolean onReceiveEvent(ProceedingJoinPoint joinPoint, Event event){
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    String methodName = methodSignature.getName();
    String className = joinPoint.getTarget().getClass().getName();
    stateMachine = getStateMachine(joinPoint);
    Span span = tracer.spanBuilder(className + ":" + methodName + "()").startSpan();

    try(Scope scope = span.makeCurrent()) {
      String activeStateName = null;
      try {
        Field activeStateField = stateMachine.getClass().getDeclaredField("activeState");
        activeStateField.setAccessible(true);
        activeStateName = (String) activeStateField.get(stateMachine);
        activeStateField.setAccessible(false);
      } catch (NoSuchFieldException | IllegalAccessException e) {
        span.recordException(e);
        span.setStatus(StatusCode.ERROR);
      }

      if (activeStateName != null) {
        span.setAttribute("activeState", activeStateName);
      }
      span.setAttribute("Event", event.getName() + " (" + event.getId() + ")");
      span.setAttribute("State Machine", stateMachine.getId());
      boolean result = false;
      try {
        result = (boolean) joinPoint.proceed();
      } catch (Throwable t) {
        span.recordException(t);
        span.setStatus(StatusCode.ERROR);
      } finally {
        span.end();
      }
      return result;
    }
  }

  @Around("switchActiveState() && args(state)")
  public void switchActiveState(ProceedingJoinPoint joinPoint, State state){
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    String methodName = methodSignature.getName();
    String className = joinPoint.getTarget().getClass().getName();
    stateMachine = getStateMachine(joinPoint);
    Span span = tracer.spanBuilder(className + ":" + methodName + "()").startSpan();

    try(Scope scope = span.makeCurrent()) {
      String activeStateName = null;
      try {
        Field activeStateField = stateMachine.getClass().getDeclaredField("activeState");
        activeStateField.setAccessible(true);
        activeStateName = (String) activeStateField.get(stateMachine);
        activeStateField.setAccessible(false);
      } catch (NoSuchFieldException | IllegalAccessException e) {
        span.recordException(e);
        span.setStatus(StatusCode.ERROR);
      }

      if (activeStateName != null) {
        span.setAttribute("ActiveState", activeStateName);
      }
      span.setAttribute("State", state.getStateObject().getName());
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

  @Around(value = "doTransition() && args(transition, raisingEvent)", argNames = "joinPoint,transition,raisingEvent")
  public void doTransition(ProceedingJoinPoint joinPoint, Transition transition, @Nullable Event raisingEvent) {
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    String methodName = methodSignature.getName();
    String className = joinPoint.getTarget().getClass().getName();
    stateMachine = getStateMachine(joinPoint);
    Span span = tracer.spanBuilder(className + ": " + methodName).startSpan();

    try (Scope scope = span.makeCurrent()) {
      String activeStateName = null;
      try {
        Field activeStateField = stateMachine.getClass().getDeclaredField("activeState");
        activeStateField.setAccessible(true);
        activeStateName = (String) activeStateField.get(stateMachine);
        activeStateField.setAccessible(false);
      } catch (NoSuchFieldException | IllegalAccessException e) {
        span.recordException(e);
        span.setStatus(StatusCode.ERROR);
      }

      if (activeStateName != null) {
        span.setAttribute("Source State", activeStateName);
      }
      span.setAttribute("Target State", transition.getTargetStateName().isPresent() ? transition.getTargetStateName().get() : "None");
      span.setAttribute("State Machine", stateMachine.getId());
      span.setAttribute("Internal", transition.isInternalTransition());
      if (raisingEvent != null) {
        span.setAttribute("Event", raisingEvent.getName() + " (" + raisingEvent.getId() + ")");
      }

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

  @Around(value = "doEnter() && args(enteringState, raisingEvent)", argNames = "joinPoint,enteringState,raisingEvent")
  public Optional<Transition> StateEnter(ProceedingJoinPoint joinPoint, State enteringState, @Nullable Event raisingEvent){
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    String methodName = methodSignature.getName();
    stateMachine = getStateMachine(joinPoint);
    String className = joinPoint.getTarget().getClass().getName();
    Span span = tracer.spanBuilder(className + ": " + methodName).startSpan();

    try (Scope scope = span.makeCurrent()) {
      span.setAttribute("Entered State", enteringState.getStateObject().getName());
      span.setAttribute("State Machine", stateMachine.getId());
      if (raisingEvent != null) {
        span.setAttribute("Event", raisingEvent.getName() + " (" + raisingEvent.getId() + ")");
      }
      Optional<Transition> result = Optional.empty();
      try {
        result = (Optional<Transition>) joinPoint.proceed();

      } catch (Throwable e) {
        span.recordException(e);
        span.setStatus(StatusCode.ERROR, e.getMessage());
      } finally {
        span.end();
      }
      return result;
    }
  }

  @Around(value = "doExit() && args(exitedState, raisingEvent)", argNames = "joinPoint,exitedState,raisingEvent")
  public void StateExit(ProceedingJoinPoint joinPoint, State exitedState, @Nullable Event raisingEvent){
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    String methodName = methodSignature.getName();
    stateMachine = getStateMachine(joinPoint);

    String className = joinPoint.getTarget().getClass().getName();

    Span span = tracer.spanBuilder(className + ": " + methodName).startSpan();
    span.setAttribute("Exited State", exitedState.getStateObject().getName());
    span.setAttribute("State Machine", stateMachine.getId());
    if(raisingEvent != null) {
      span.setAttribute("Event", raisingEvent.getName() + " (" + raisingEvent.getId() + ")");
    }

    try{
      joinPoint.proceed();
    } catch (Throwable e){
      span.recordException(e);
      span.setStatus(StatusCode.ERROR, e.getMessage());
    } finally {
      span.end();
    }

  }

  @Around("handleEvent() && args(event)")
  public Optional<Transition> handleEvent(ProceedingJoinPoint joinPoint, Event event) {
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    String methodName = methodSignature.getName();
    String className = joinPoint.getTarget().getClass().getName();
    stateMachine = getStateMachine(joinPoint);
    Span span = tracer.spanBuilder(className + ": " + methodName).startSpan();
    try (Scope scope = span.makeCurrent()) {
      String activeStateName = null;
      try {
        Field activeStateField = stateMachine.getClass().getDeclaredField("activeState");
        activeStateField.setAccessible(true);
        activeStateName = (String) activeStateField.get(stateMachine);
        activeStateField.setAccessible(false);
      } catch (NoSuchFieldException | IllegalAccessException e) {
        span.recordException(e);
        span.setStatus(StatusCode.ERROR);
      }

      span.setAttribute("State Machine", stateMachine.getId());
      span.setAttribute("Active State", activeStateName);
      span.setAttribute("Event: ", event.getName() + " (" + event.getId() + ")");

      Optional<Transition> result = Optional.empty();
      try {
        result = (Optional<Transition>) joinPoint.proceed();

      } catch (Throwable e) {
        span.recordException(e);
        span.setStatus(StatusCode.ERROR, e.getMessage());
      } finally {
        span.end();
      }
      return result;
    }
  }
}
