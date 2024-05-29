package at.ac.uibk.dps.cirrina.execution.object.exchange;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Value exchange, responsible for converting a value object to a consistent exchange format, using Protocol Buffers.
 * <p>
 * See the exchange protos for a protocol description.
 * <p>
 * The supported value types for exchange are:
 * <ul>
 *   <li>Integer</li>
 *   <li>Floating-point</li>
 *   <li>Long integer</li>
 *   <li>Double-precision floating-point</li>
 *   <li>String</li>
 *   <li>Boolean</li>
 *   <li>Byte array</li>
 * </ul>
 */
public class ValueExchange {

  /**
   * The value object.
   */
  private final Object value;

  /**
   * Initializes this value exchange instance.
   *
   * @param value Value object.
   */
  public ValueExchange(Object value) {
    this.value = value;
  }

  /**
   * Construct a value exchange from byte data.
   *
   * @param data Byte data.
   * @return Value exchange.
   * @throws UnsupportedOperationException If the value could not be read.
   * @throws UnsupportedOperationException If the value type is unknown.
   */
  public static ValueExchange fromBytes(byte[] data) throws UnsupportedOperationException {
    try {
      return new ValueExchange(fromProto(ContextVariableProtos.Value.parseFrom(data)));
    } catch (InvalidProtocolBufferException e) {
      throw new UnsupportedOperationException("Could not read value from bytes");
    }
  }

  /**
   * Construct a value object from a proto.
   *
   * @param proto Value proto.
   * @return Value object.
   * @throws UnsupportedOperationException If the value type is unknown.
   */
  public static Object fromProto(ContextVariableProtos.Value proto) throws UnsupportedOperationException {
    switch (proto.getValueCase()) {
      case INTEGER -> {
        return proto.getInteger();
      }
      case FLOAT -> {
        return proto.getFloat();
      }
      case LONG -> {
        return proto.getLong();
      }
      case DOUBLE -> {
        return proto.getDouble();
      }
      case STRING -> {
        return proto.getString();
      }
      case BOOL -> {
        return proto.getBool();
      }
      case BYTES -> {
        return proto.getBytes().toByteArray();
      }
      default -> throw new UnsupportedOperationException("Context variable value type could not be read");
    }
  }

  /**
   * Converts this exchange instance to bytes.
   *
   * @return Bytes.
   */
  public byte[] toBytes() throws UnsupportedOperationException {
    return toProto().toByteArray();
  }

  /**
   * Returns a proto from this exchange.
   *
   * @return Proto.
   * @throws UnsupportedOperationException If the value type is unknown.
   */
  public ContextVariableProtos.Value toProto() throws UnsupportedOperationException {
    final var builder = ContextVariableProtos.Value.newBuilder();

    switch (value) {
      case Integer i -> builder.setInteger(i);
      case Float f -> builder.setFloat(f);
      case Long l -> builder.setLong(l);
      case Double d -> builder.setDouble(d);
      case String s -> builder.setString(s);
      case Boolean b -> builder.setBool(b);
      case byte[] bytes -> builder.setBytes(ByteString.copyFrom(bytes));
      default -> throw new UnsupportedOperationException("Value type could not be converted to proto");
    }

    return builder.build();
  }

  /**
   * Returns the value object.
   *
   * @return Value object.
   */
  public Object getValue() {
    return value;
  }
}
