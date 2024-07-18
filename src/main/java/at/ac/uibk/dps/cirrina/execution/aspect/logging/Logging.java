package at.ac.uibk.dps.cirrina.execution.aspect.logging;

import at.ac.uibk.dps.cirrina.execution.object.event.Event;
import java.net.http.HttpResponse;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Logging {

  public final Logger logger = LogManager.getLogger(Logging.class);

  public void logStateMachineStart(String stateMachineName) {
    logger.info("State Machine {} starting", stateMachineName);
  }

  public void logExeption(String stateMachineId, Throwable ex) {
    logger.error("State Machine {} {}",stateMachineId, ex.getMessage(), ex);
  }

  public void logAction(String actionName, String stateMachineId) {
    logger.info("State Machine {} executing action {}", stateMachineId, actionName);
  }

  public void logTimeout(String type, String actionName, String stateMachineId) {
    switch (type) {
      case "Start":
        logger.info("State Machine {} starting timeout {}", stateMachineId, actionName);

      case "Stop":
        logger.info("State Machine {} stopping {}", stateMachineId, actionName);

      case "Stop alL":
        logger.info("State Machine {} stopping all timeout actions", stateMachineId);

    }
  }

  public void logServiceInvocation(String serviceName, String id) {
    logger.info("State Machine {}: Service invocation: {}", id, serviceName);
  }

  public void logServiceResponseHandling(String serviceName, HttpResponse response, String stateMachineId){
    logger.info("State Machine {}: Handling service response: {} with status code {} and body {}",
        stateMachineId, serviceName, response.statusCode(), response.body().toString());
  }

  public void logEventReception(String stateMachne, Event event, String state){
    logger.info("State Machine {} received event {} ({}) in State {}", stateMachne, event.getName(), event.getId(), state);
  }

  public void logActiveStateSwitch(String stateMachine, String currentState, String newState){
    logger.info("State Machine {} switched from {} to {}", stateMachine, currentState, newState);

  }

  public void logStateExit(String stateMachine, String exitedState, Event event){
    if (event != null) {
      logger.info("State Machine {} exiting from state {} due to Event: {} ({}) ",
          stateMachine, exitedState, event.getName(), event.getId());
    } else {
      logger.info("State Machine {} exiting from state {}", stateMachine, exitedState);
    }
  }

  public void logStateEntry(String stateMachine, String enteringState, Event event){
    if (event != null) {
      logger.info("State Machine {} entered state {} due to Event: {} ({}) ",
          stateMachine, enteringState, event.getName(), event.getId());
    } else {
      logger.info("State Machine {} entering state {}", stateMachine, enteringState);
    }
  }

  public void logTransition(String stateMachine, String fromState, String toState, Event event){
    if (event != null) {
      logger.info("State Machine {} transitioning from state {} to {} due to Event: {} ({})",
          stateMachine, fromState, toState, event.getName(), event.getId());
    } else {
      logger.info("State Machine {} transitioning from state {} to {}",
          stateMachine, fromState, toState);
    }
  }

  public void logEventHandling(String stateMachine, Event event){
    logger.info("State Machine {} handling Event: {} ({})",
        stateMachine, event.getName(), event.getId());
  }

  public void logGuardEvaluation(String expression, String stateMachineId){
    logger.info("State Machine {} evaluating guard with expression: {}", stateMachineId, expression);
  }

  public void logEventSending(Event event, String stateMachine){
    logger.info("State Machine {} sending event {} ({})", stateMachine, event.getName(), event.getId());
  }


}
