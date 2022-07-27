/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.instrumentation.api.instrumenter;

import static java.util.Collections.emptyMap;

import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.instrumenter.net.NetClientAttributesGetter;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Extractor of the {@code peer.service} span attribute, described in <a
 * href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/trace/semantic_conventions/span-general.md#general-remote-service-attributes">the
 * specification</a>.
 */
public final class PeerServiceAttributesExtractor<REQUEST, RESPONSE>
    implements AttributesExtractor<REQUEST, RESPONSE> {

  private final NetClientAttributesGetter<REQUEST, RESPONSE> attributesGetter;
  private final Map<String, String> peerServiceMapping;

  // visible for tests
  PeerServiceAttributesExtractor(
      NetClientAttributesGetter<REQUEST, RESPONSE> attributesGetter,
      Map<String, String> peerServiceMapping) {
    this.attributesGetter = attributesGetter;
    this.peerServiceMapping = peerServiceMapping;
  }

  /**
   * Returns a new {@link PeerServiceAttributesExtractor} that will use the passed {@code
   * netAttributesExtractor} instance to determine the value of the {@code peer.service} attribute.
   *
   * @deprecated Use {@link #create(NetClientAttributesGetter, Map)} instead.
   */
  @Deprecated
  public static <REQUEST, RESPONSE> PeerServiceAttributesExtractor<REQUEST, RESPONSE> create(
      NetClientAttributesGetter<REQUEST, RESPONSE> attributesGetter) {
    return create(
        attributesGetter,
        io.opentelemetry.instrumentation.api.config.Config.get()
            .getMap("otel.instrumentation.common.peer-service-mapping", emptyMap()));
  }

  /**
   * Returns a new {@link PeerServiceAttributesExtractor} that will use the passed {@code
   * netAttributesExtractor} instance to determine the value of the {@code peer.service} attribute.
   */
  public static <REQUEST, RESPONSE> PeerServiceAttributesExtractor<REQUEST, RESPONSE> create(
      NetClientAttributesGetter<REQUEST, RESPONSE> attributesGetter,
      Map<String, String> peerServiceMapping) {
    return new PeerServiceAttributesExtractor<>(attributesGetter, peerServiceMapping);
  }

  @Override
  public void onStart(AttributesBuilder attributes, Context parentContext, REQUEST request) {}

  @Override
  public void onEnd(
      AttributesBuilder attributes,
      Context context,
      REQUEST request,
      @Nullable RESPONSE response,
      @Nullable Throwable error) {

    if (peerServiceMapping.isEmpty()) {
      // optimization for common case
      return;
    }

    String peerName = attributesGetter.peerName(request, response);
    String peerService = mapToPeerService(peerName);
    if (peerService == null) {
      String peerIp = attributesGetter.peerIp(request, response);
      peerService = mapToPeerService(peerIp);
    }
    if (peerService != null) {
      attributes.put(SemanticAttributes.PEER_SERVICE, peerService);
    }
  }

  @Nullable
  private String mapToPeerService(String endpoint) {
    if (endpoint == null) {
      return null;
    }
    return peerServiceMapping.get(endpoint);
  }
}
