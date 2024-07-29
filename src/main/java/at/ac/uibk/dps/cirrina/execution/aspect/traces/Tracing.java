package at.ac.uibk.dps.cirrina.execution.aspect.traces;

import io.opentelemetry.context.Context;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import java.util.Map;

public class Tracing {

  public Tracer initializeTracer(String name){
    return GlobalOpenTelemetry.getTracer(name);
  }

  public Span initializeSpan(String name, Tracer tracer, Span parentSpan){
    if(parentSpan == null) {
      return tracer.spanBuilder(name).startSpan();
    } else {
      return tracer.spanBuilder(name).setParent(Context.current().with(parentSpan)).startSpan();
    }
  }

  public void addAttributes(Map<String, String> attributes, Span span){
    for (Map.Entry<String, String> entry : attributes.entrySet()){
      span.setAttribute(entry.getKey(), entry.getValue());
    }
  }

  public void recordException(Throwable throwable, Span span){
    span.recordException(throwable);
    span.setStatus(StatusCode.ERROR, throwable.getMessage());
  }

  public void addEvent(String event, Span span){
    span.addEvent(event);
  }


}
