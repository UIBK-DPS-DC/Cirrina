package at.ac.uibk.dps.cirrina.execution.object.statemachine;

import at.ac.uibk.dps.cirrina.execution.aspect.logging.Logging;
import at.ac.uibk.dps.cirrina.execution.aspect.traces.Tracing;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Timeout action manager, a manager for timeout tasks that encapsulate the execution of timeout actions.
 * <p>
 * This class is thread-safe.
 */
public final class TimeoutActionManager {

  /**
   * The number of threads in the timeout scheduled thread pool.
   */
  private final int NUM_THREADS = 1;

  /**
   * Timeout task scheduler, configured with a pre-defined number of threads.
   */
  private final ScheduledExecutorService timeoutTaskScheduler = Executors.newScheduledThreadPool(NUM_THREADS);

  /**
   * A collection of running timeout tasks encapsulating the execution of timeout actions. The keys are timeout action names.
   */
  private final ConcurrentMap<String, ScheduledFuture<?>> timeoutTasks = new ConcurrentHashMap<>();

  private final Logging logging = new Logging();
  private final Tracing tracing = new Tracing();
  private final Tracer tracer = tracing.initializeTracer("Action");

  /**
   * Starts the provided timeout action.
   * <p>
   * The action name must be unique, no timeout action with the same must have been started before without being stopped.
   *
   * @param actionName Name of the timeout action.
   * @param delayInMs  Delay in milliseconds.
   * @param task       The task to execute.
   * @throws IllegalArgumentException If two timeout actions with the same name have been started without being stopped.
   */
  public void start(String actionName, Number delayInMs, Runnable task) throws IllegalArgumentException {
    logging.logTimeout("start", actionName);
    Span span = tracing.initianlizeSpan("Start Timeout Actions", tracer, null);
      try(Scope scope = span.makeCurrent()) {

        try {
          // Ensure unique timeout action names
          if (timeoutTasks.containsKey(actionName)) {
            throw new IllegalArgumentException("Duplicate timeout action name '%s'".formatted(actionName));
          }
        } catch (IllegalArgumentException e){
          tracing.recordException(e, span);
          logging.logExeption(e);
          throw e;
        }

        // Schedule at an interval
        final var future = timeoutTaskScheduler.scheduleWithFixedDelay(task, delayInMs.intValue(), delayInMs.intValue(),
            TimeUnit.MILLISECONDS);

        // Keep the task
        timeoutTasks.put(actionName, future);
      } finally {
        span.end();
      }
  }

  /**
   * Stops a timeout action with the provided name. The encapsulating task will be cancelled immediately.
   * <p>
   * A timeout action with the provided name must have been started.
   *
   * @param actionName Name of action to stop.
   * @throws IllegalArgumentException If not exactly one timeout action was found with the provided name.
   */
  public void stop(String actionName) throws IllegalArgumentException {
    logging.logTimeout("stop", actionName);
    Span span = tracing.initianlizeSpan("Stopping Timeout Action", tracer, null);
    try(Scope scope = span.makeCurrent()) {
      tracing.addAttributes(Map.of("Action", actionName), span);

      // Retrieve the timeout task
      final var timeoutTasksWithName = timeoutTasks.entrySet().stream()
          .filter(entry -> entry.getKey().equals(actionName))
          .map(Map.Entry::getValue)
          .toList();
      try {
        if (timeoutTasksWithName.size() != 1) {
          throw new IllegalArgumentException("Expected exactly one timeout action with the name '%s'".formatted(actionName));
        }
      }catch (IllegalArgumentException e){
        tracing.recordException(e, span);
        logging.logExeption(e);
        throw e;
      }

      final var timeoutTask = timeoutTasksWithName.getFirst();

      // Cancel the task
      timeoutTask.cancel(true);

      // Remove the task
      timeoutTasks.remove(actionName);
    } finally {
      span.end();
    }
  }

  /**
   * Stops all timeout actions.
   */
  public void stopAll() {
    logging.logTimeout("stopAll", null);
    Span span = tracing.initianlizeSpan("Stopping All Timeout Actions", tracer, null);
    try (Scope scope = span.makeCurrent()) {
      // Cancel all tasks
      timeoutTasks.values()
          .forEach(future -> future.cancel(true));

      // And clear
      timeoutTasks.clear();
    } finally {
      span.end();
    }
  }
}
