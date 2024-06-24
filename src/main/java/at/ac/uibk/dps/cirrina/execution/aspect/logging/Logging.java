package at.ac.uibk.dps.cirrina.execution.aspect.logging;

import at.ac.uibk.dps.cirrina.execution.object.event.Event;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Logging {

  public final Logger logger = LogManager.getLogger(LoggingActions.class);

  public void logExeption(Throwable ex) {
    logger.error(ex.getMessage(), ex);
  }

  public void logAction(String actionName) {
    logger.info("Executing action {}", actionName);
  }

  public void logTimeout(String type, String actionName) {
    switch (type) {
      case "Start":
        logger.info("TIMEOUT: Starting {}", actionName);

      case "Stop":
        logger.info("TIMEOUT: Stopping {}", actionName);

      case "Stop alL":
        logger.info("TIMEOUT: Stopping all");

    }
  }

  public void logServiceInvocation(String serviceName, String id) {
    logger.info("Service invocation: {} with ID {}", serviceName, id);
  }

  public void logServiceResponseHandling(String serviceName, SimpleHttpResponse response){
    logger.info("Handling service response: {} with status code {} and body {}",
        serviceName, response.getCode(), response.getBodyText());
  }

  public void logEventReception(String stateMachne, Event event, String state){
    logger.info("State Machine: {}, received event {} ({}) in State {}", stateMachne, event.getName(), event.getId(), state);
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

  public void logGuardEvaluation(){
    logger.info("Guard evaluation");
  }

  public void logEventSending(Event event){
    logger.info("Sending event {} ({})", event.getName(), event.getId());
  }


}
