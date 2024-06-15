package at.ac.uibk.dps.cirrina.execution.aspect.logging;

import at.ac.uibk.dps.cirrina.execution.object.state.State;
import at.ac.uibk.dps.cirrina.execution.object.statemachine.StateMachine;
import at.ac.uibk.dps.cirrina.execution.object.transition.Transition;
import java.lang.reflect.Field;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class LoggingStateMachine {
  public static final Logger logger = LogManager.getLogger(LoggingStateMachine.class);
  StateMachine stateMachine;

  @Pointcut("execution(* at.ac.uibk.dps.cirrina.execution.object.statemachine.StateMachine.onReceiveEvent(..))")
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

  @Before("onReceiveEvent()")
  public void receiveEvent(JoinPoint joinPoint) {
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

    logger.info("State Machine: " + stateMachine.getStateMachineInstanceId() + "received Event: " + Arrays.toString(joinPoint.getArgs()) + "in State: " +
        activeStateName);
  }

  @Before("switchActiveState() && args(state)")
  public void switchActiveState(JoinPoint joinPoint, State state) {
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

    logger.info("State Machine " + stateMachine.getStateMachineInstanceId() + "switching from state "
        + activeStateName + " to " + state.getStateObject().getName());

  }

  @Before("doExit() && args(exitingState)")
  public void doExit(JoinPoint joinPoint, State exitingState) {
    stateMachine = (StateMachine) joinPoint.getThis();
    logger.info("State Machine " + stateMachine.getStateMachineInstanceId() + " exiting from state " + exitingState);
  }

  @Before("doTransition() && args(transition)")
  public void doTransition(JoinPoint joinPoint, Transition transition) {
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

    logger.info("State Machine " + stateMachine.getStateMachineInstanceId() +
        " transitioning from state " + activeStateName + " to " + transition.getTargetStateName());
  }

  @Before("doEnter() && args(enteringState)")
  public void doEnter(JoinPoint joinPoint, State enteringState) {
    stateMachine = (StateMachine) joinPoint.getThis();
    logger.info("State Machine " + stateMachine.getStateMachineInstanceId() + " entering state " + enteringState);
  }

  @After("handleEvent()")
  public void handleEvent(JoinPoint joinPoint) {
    stateMachine = (StateMachine) joinPoint.getThis();
    logger.info("State Machine " + stateMachine.getStateMachineInstanceId() + "handling Event ");
  }



}
