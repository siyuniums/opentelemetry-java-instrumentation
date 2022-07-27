/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.api.config;

import static java.util.Objects.requireNonNull;
import static java.util.logging.Level.FINE;

import com.google.auto.value.AutoValue;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * Represents the global instrumentation configuration consisting of system properties and
 * environment variables; and, if using the OpenTelemetry javaagent, contents of the agent
 * configuration file and properties defined by the {@code ContextCustomizer} SPI implementations.
 *
 * <p>In case any {@code get*()} method variant gets called for the same property more than once
 * (e.g. each time an advice class executes) it is suggested to cache the result instead of
 * repeatedly calling {@link Config}. The instrumentation configuration does not change during the
 * runtime so retrieving the property once and storing its result in a static final field allows JIT
 * to do its magic and remove some code branches.
 *
 * @deprecated This class is deprecated and will be removed from instrumentation-api in the next
 *     release. Please use programmatic configuration (e.g. builder methods) instead.
 */
@Deprecated
@AutoValue
@AutoValue.CopyAnnotations
public abstract class Config {
  private static final Logger logger = Logger.getLogger(Config.class.getName());

  // lazy initialized, so that javaagent can set it, and library instrumentation can fall back and
  // read system properties
  @Nullable private static volatile Config instance = null;

  /** Start building a new {@link Config} instance. */
  public static ConfigBuilder builder() {
    return new ConfigBuilder();
  }

  static Config create(Map<String, String> allProperties) {
    return new AutoValue_Config(allProperties);
  }

  // package protected constructor to make extending this class impossible
  Config() {}

  /**
   * Sets the instrumentation configuration singleton. This method is only supposed to be called
   * once, during the javaagent initialization, just before {@link Config#get()} is used for the
   * first time.
   *
   * <p>This method is internal and is hence not for public use. Its API is unstable and can change
   * at any time.
   */
  public static void internalInitializeConfig(Config config) {
    if (instance != null) {
      logger.warning("Config#INSTANCE was already set earlier");
      return;
    }
    instance = requireNonNull(config);
  }

  /** Returns the global instrumentation configuration. */
  public static Config get() {
    if (instance == null) {
      // this should only happen in library instrumentation
      //
      // no need to synchronize because worst case is creating instance more than once
      instance = builder().addEnvironmentVariables().addSystemProperties().build();
    }
    return instance;
  }

  /**
   * Returns all properties stored in this {@link Config} instance. The returned map is
   * unmodifiable.
   */
  public abstract Map<String, String> getAllProperties();

  /**
   * Returns a string-valued configuration property or {@code null} if a property with name {@code
   * name} has not been configured.
   */
  @Nullable
  public String getString(String name) {
    return getRawProperty(name, null);
  }

  /**
   * Returns a string-valued configuration property or {@code defaultValue} if a property with name
   * {@code name} has not been configured.
   */
  public String getString(String name, String defaultValue) {
    return getRawProperty(name, defaultValue);
  }

  /**
   * Returns a boolean-valued configuration property or {@code defaultValue} if a property with name
   * {@code name} has not been configured.
   */
  public boolean getBoolean(String name, boolean defaultValue) {
    return safeGetTypedProperty(name, ConfigValueParsers::parseBoolean, defaultValue);
  }

  /**
   * Returns an integer-valued configuration property or {@code defaultValue} if a property with
   * name {@code name} has not been configured or when parsing has failed.
   */
  public int getInt(String name, int defaultValue) {
    return safeGetTypedProperty(name, ConfigValueParsers::parseInt, defaultValue);
  }

  /**
   * Returns a long-valued configuration property or {@code defaultValue} if a property with name
   * {@code name} has not been configured or when parsing has failed.
   */
  public long getLong(String name, long defaultValue) {
    return safeGetTypedProperty(name, ConfigValueParsers::parseLong, defaultValue);
  }

  /**
   * Returns a double-valued configuration property or {@code defaultValue} if a property with name
   * {@code name} has not been configured or when parsing has failed.
   */
  public double getDouble(String name, double defaultValue) {
    return safeGetTypedProperty(name, ConfigValueParsers::parseDouble, defaultValue);
  }

  /**
   * Returns a duration-valued configuration property or {@code defaultValue} if a property with
   * name {@code name} has not been configured or when parsing has failed.
   *
   * <p>Durations can be of the form "{number}{unit}", where unit is one of:
   *
   * <ul>
   *   <li>ms
   *   <li>s
   *   <li>m
   *   <li>h
   *   <li>d
   * </ul>
   *
   * <p>If no unit is specified, milliseconds is the assumed duration unit.
   *
   * <p>Examples: 10s, 20ms, 5000
   */
  public Duration getDuration(String name, Duration defaultValue) {
    return safeGetTypedProperty(name, ConfigValueParsers::parseDuration, defaultValue);
  }

  /**
   * Returns a list-valued configuration property or {@code defaultValue} if a property with name
   * {@code name} has not been configured. The format of the original value must be comma-separated,
   * e.g. {@code one,two,three}. The returned list is unmodifiable.
   */
  public List<String> getList(String name, List<String> defaultValue) {
    return safeGetTypedProperty(name, ConfigValueParsers::parseList, defaultValue);
  }

  /**
   * Returns a map-valued configuration property or {@code defaultValue} if a property with name
   * {@code name} has not been configured or when parsing has failed. The format of the original
   * value must be comma-separated for each key, with an '=' separating the key and value, e.g.
   * {@code key=value,anotherKey=anotherValue}. The returned map is unmodifiable.
   */
  public Map<String, String> getMap(String name, Map<String, String> defaultValue) {
    return safeGetTypedProperty(name, ConfigValueParsers::parseMap, defaultValue);
  }

  private <T> T safeGetTypedProperty(String name, ConfigValueParser<T> parser, T defaultValue) {
    try {
      T value = getTypedProperty(name, parser);
      return value == null ? defaultValue : value;
    } catch (RuntimeException t) {
      if (logger.isLoggable(FINE)) {
        logger.log(FINE, "Error occurred during parsing: " + t.getMessage(), t);
      }
      return defaultValue;
    }
  }

  @Nullable
  private <T> T getTypedProperty(String name, ConfigValueParser<T> parser) {
    String value = getRawProperty(name, null);
    if (value == null || value.trim().isEmpty()) {
      return null;
    }
    return parser.parse(name, value);
  }

  private String getRawProperty(String name, String defaultValue) {
    return getAllProperties().getOrDefault(NamingConvention.DOT.normalize(name), defaultValue);
  }

  /**
   * Returns a new {@link ConfigBuilder} instance populated with the properties of this {@link
   * Config}.
   */
  public ConfigBuilder toBuilder() {
    return new ConfigBuilder(getAllProperties());
  }
}
