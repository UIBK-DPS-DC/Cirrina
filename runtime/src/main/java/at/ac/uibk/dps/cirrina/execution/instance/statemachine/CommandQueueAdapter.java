package at.ac.uibk.dps.cirrina.execution.instance.statemachine;

import at.ac.uibk.dps.cirrina.execution.command.Command;
import java.util.List;

public interface CommandQueueAdapter {
  
  /**
   * Add a collection of commands to the back of the command queue.
   *
   * @param commandList Commands to add to the queue.
   */
  void addCommandsToBack(List<Command> commandList);

  /**
   * Add a collection of commands to the front of the command queue.
   *
   * @param commandList Commands to add to the queue.
   */
  void addCommandsToFront(List<Command> commandList);

  interface CommandConsumer {

    void consume(List<Command> commands);
  }
}
