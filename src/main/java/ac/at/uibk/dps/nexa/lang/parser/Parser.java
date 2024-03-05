package ac.at.uibk.dps.nexa.lang.parser;

import ac.at.uibk.dps.nexa.lang.parser.classes.CollaborativeStateMachineClass;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Bean deserializer with validation. Provides additional bean validation after deserialization.
 */
class BeanDeserializerWithValidation extends BeanDeserializer {

  private final Validator validator;

  /**
   * Initializes the bean deserializer with validation.
   *
   * @param source Source deserializer.
   */
  protected BeanDeserializerWithValidation(BeanDeserializerBase source) {
    super(source);

    // Create the validator
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  /**
   * Perform deserialization. Will perform default deserialization, with additional bean validation after
   * deserialization. Any validation error will result in an IllegalArgumentException to be thrown.
   *
   * @param parser  JSON parser.
   * @param context Deserialization context.
   * @return Deserialized and validated object.
   * @throws IOException              Default exceptions.
   * @throws IllegalArgumentException In case of validation errors.
   */
  @Override
  public Object deserialize(JsonParser parser, DeserializationContext context)
      throws IOException {
    // Call the base deserialization
    Object instance = super.deserialize(parser, context);

    // Check for violations, in case we find violations we throw an IllegalArgumentException that
    // is handled in the parse function below
    var violations = validator.validate(instance);
    if (!violations.isEmpty()) {
      throw new IllegalArgumentException(violations.stream()
          .map(
              v -> v.getMessage())
          .collect(Collectors.joining(",")));
    }

    return instance;
  }
}

/**
 * Modifier for bean deserializer. Provides a bean deserializer with validation in the place of a bean deserializer.
 */
class BeanDeserializerModifierWithValidation extends BeanDeserializerModifier {

  /**
   * Provides a bean deserializer with validation in the place of a bean deserializer.
   *
   * @param configuration Deserialization configuration.
   * @param description   Bean description.
   * @param deserializer  Deserializer to modify.
   * @return The deserializer or bean deserializer with validation instead of bean deserializer.
   */
  @Override
  public JsonDeserializer<?> modifyDeserializer(DeserializationConfig configuration,
      BeanDescription description, JsonDeserializer<?> deserializer) {
    // Provide the deserializer with validation
    if (deserializer instanceof BeanDeserializer) {
      return new BeanDeserializerWithValidation((BeanDeserializer) deserializer);
    }

    return deserializer;
  }
}

/**
 * CSML parser. Provides parsing functionality for descriptions written in the CSML language. A description is parsed
 * into a structure consisting of CSML models.
 */
public class Parser {

  private final Options options;
  private final ObjectMapper mapper;

  /**
   * Initializes the parser, provided the parser options.
   *
   * @param options Parser options.
   */
  public Parser(Options options) {
    this.options = options;

    // Add a deserialization module that adds bean validation
    SimpleModule validationModule = new SimpleModule();
    validationModule.setDeserializerModifier(new BeanDeserializerModifierWithValidation());

    // Construct the mapper
    mapper = JsonMapper.builder()
        .addModule(new ParameterNamesModule())
        .addModule(new Jdk8Module())
        .addModule(validationModule)
        .enable(JsonParser.Feature.ALLOW_COMMENTS)
        .enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES)
        .enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
        .build();
  }

  /**
   * Parse a description in JSON. Returns a collaborative state machine (top-level) model. Any errors will result in a
   * LanguageException being thrown. Errors could be the result of errors in the description such as syntax errors as
   * well as validation errors such as missing fields.
   *
   * @param json JSON description.
   * @return Collaborative state machine model.
   * @throws ParserException In case an error occurs during parsing or validation.
   */
  public CollaborativeStateMachineClass parse(String json) throws ParserException {
    try {
      return mapper.readValue(json, CollaborativeStateMachineClass.class);
    } catch (JsonProcessingException | IllegalArgumentException e) {
      throw new ParserException(
          String.format("Parsing error: %s", e.getMessage()));
    }
  }

  /**
   * Parser options.
   */
  public record Options() {

  }
}
