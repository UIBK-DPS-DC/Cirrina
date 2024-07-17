package at.ac.uibk.dps.cirrina.execution.aspect.logging;

import at.ac.uibk.dps.cirrina.execution.object.event.Event;
import at.ac.uibk.dps.cirrina.execution.object.state.State;
import at.ac.uibk.dps.cirrina.execution.object.statemachine.StateMachine;
import at.ac.uibk.dps.cirrina.execution.object.transition.Transition;
import com.beust.jcommander.internal.Nullable;
import java.lang.reflect.Field;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class LoggingStateMachine {
  public static final Logger logger = LogManager.getLogger(LoggingStateMachine.class);
  StateMachine stateMachine;

  @Pointcut("execution(* at.ac.uibk.dps.cirrina.execution.object.statemachine.StateMachine.*onReceiveEvent(..))")
  public void onReceiveEvent() {}

  @Pointcut("execution(* at.ac.uibk.dps.cirrina.execution.object.statemachine.StateMachine.*switchActiveState(..))")
  public void switchActiveState() {}

  @Pointcut("execution(* at.ac.uibk.dps.cirrina.execution.object.statemachine.StateMachine.*doEnter(..))")
  public void doEnter() {}

  @Pointcut("execution(* at.ac.uibk.dps.cirrina.execution.object.statemachine.StateMachine.*doTransition(..))")
  public void doTransition() {}

  @Pointcut("execution(* at.ac.uibk.dps.cirrina.execution.object.statemachine.StateMachine.*doExit(..))")
  public void doExit() {}

  @Pointcut("execution(* at.ac.uibk.dps.cirrina.execution.object.statemachine.StateMachine.*handleEvent(..))")
  public void handleEvent() {}

  private String getActiveState(JoinPoint joinPoint) {
    StateMachine stateMachine = getStateMachine(joinPoint);
    String activeStateName = null;
    try {
      Field activeStateField = stateMachine.getClass().getDeclaredField("activeState");
      activeStateField.setAccessible(true);
      activeStateName = (String) activeStateField.get(stateMachine);
      activeStateField.setAccessible(false);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      logger.error(e.getMessage());
    }
    return activeStateName;
  }

  private StateMachine getStateMachine(JoinPoint joinPoint) {
    stateMachine = (StateMachine) joinPoint.getThis();
    return stateMachine;
  }


  @Before("onReceiveEvent() && args(event)")
  public void receiveEvent(JoinPoint joinPoint, Event event) {
    StateMachine stateMachine = getStateMachine(joinPoint);
    if(stateMachine.isTerminated()){
      return;
    }
    String activeStateName = getActiveState(joinPoint);

    logger.info("State Machine: {}received Event: {} ({})  in State: {}",
        stateMachine.getStateMachineInstanceId(), event.getName(), event.getId(), activeStateName);
  }

  @Before("switchActiveState() && args(state)")
  public void switchActiveState(JoinPoint joinPoint, State state) {
    StateMachine stateMachine = getStateMachine(joinPoint);
    String activeStateName = getActiveState(joinPoint);

    logger.info("State Machine {} switching from state {} to {}",
        stateMachine.getStateMachineInstanceId(), activeStateName, state.getStateObject().getName());

  }

  @Before(value = "doExit() && args(exitingState, raisingEvent)", argNames = "joinPoint,exitingState,raisingEvent")
  public void doExit(JoinPoint joinPoint, State exitingState, @Nullable Event raisingEvent) {
    StateMachine stateMachine = getStateMachine(joinPoint);
    if (raisingEvent != null) {
      logger.info("State Machine {} exiting from state {} due to Event: {} ({}) ",
          stateMachine.getStateMachineInstanceId(), exitingState, raisingEvent.getName(), raisingEvent.getId());
    } else {
      logger.info("State Machine {} exiting from state {}", stateMachine.getStateMachineInstanceId(), exitingState);
    }

  }

  @Before(value = "doTransition() && args(transition, raisingEvent)", argNames = "joinPoint,transition,raisingEvent")
  public void doTransition(JoinPoint joinPoint, Transition transition,  @Nullable Event raisingEvent) {
    if(transition.isElse()){
      return;
    }
    StateMachine stateMachine = getStateMachine(joinPoint);

    String activeStateName = getActiveState(joinPoint);
    if (raisingEvent != null) {
      logger.info("State Machine {} transitioning from state {} to {} due to Event: {} ({})",
          stateMachine.getStateMachineInstanceId(), activeStateName, transition.getTargetStateName(),
          raisingEvent.getName(), raisingEvent.getId());
    } else {
      logger.info("State Machine {} transitioning from state {} to {}",
          stateMachine.getStateMachineInstanceId(), activeStateName, transition.getTargetStateName());
    }
  }

  @Before(value = "doEnter() && args(enteringState, raisingEvent)", argNames = "joinPoint,enteringState,raisingEvent")
  public void doEnter(JoinPoint joinPoint, State enteringState, @Nullable Event raisingEvent) {
    StateMachine stateMachine = getStateMachine(joinPoint);

    if(raisingEvent != null){
      logger.info("State Machine {} entering state {} due to Event: {} ({})",
          stateMachine.getStateMachineInstanceId(), enteringState, raisingEvent.getName(), raisingEvent.getId());
    } else {
      logger.info("State Machine {} entering state {}",
          stateMachine.getStateMachineInstanceId(), enteringState);
    }
  }

  @Before("handleEvent() && args(event)")
  public void handleEvent(JoinPoint joinPoint, Event event) {
    StateMachine stateMachine = getStateMachine(joinPoint);
    logger.info("State Machine {} handling Event: {} ({})",
        stateMachine.getStateMachineInstanceId(), event.getName(), event.getId());
  }



}
