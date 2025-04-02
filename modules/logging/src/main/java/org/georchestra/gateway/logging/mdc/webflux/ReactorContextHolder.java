/*
 * Copyright (C) 2025 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.georchestra.gateway.logging.mdc.webflux;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.MDC;

import lombok.experimental.UtilityClass;
import reactor.core.publisher.Mono;

/**
 * Utility class to access Reactor Context from non-reactive code, enabling MDC
 * propagation across thread boundaries in reactive applications.
 * <p>
 * In reactive applications using WebFlux, the standard SLF4J MDC (Mapped
 * Diagnostic Context) cannot be used directly because the reactive pipeline may
 * execute across multiple threads. When execution switches between threads, the
 * thread-local MDC values would normally be lost, breaking the logging context.
 * <p>
 * This class solves this problem by:
 * <ol>
 * <li>Storing MDC data in the Reactor Context, which flows with the reactive
 * chain regardless of thread boundaries</li>
 * <li>Providing utilities to temporarily restore MDC values from the Reactor
 * Context to the current thread's MDC when logging needs to occur</li>
 * <li>Ensuring thread safety by properly managing the thread-local MDC
 * state</li>
 * </ol>
 * <p>
 * The thread management "magic" happens because:
 * <ul>
 * <li>Reactor Context is designed to propagate through the reactive chain,
 * across thread boundaries and operator boundaries</li>
 * <li>When execution switches threads, the original thread-local MDC is lost,
 * but the Reactor Context (containing our MDC data) flows to the new
 * thread</li>
 * <li>This class provides hooks that can be used at specific points in the
 * reactive flow to restore MDC from the Context when needed</li>
 * </ul>
 * <p>
 * The MDC data is stored in the Reactor Context under the key
 * {@link #MDC_CONTEXT_KEY}.
 * 
 * @see org.slf4j.MDC
 * @see reactor.util.context.Context
 */
@UtilityClass
public class ReactorContextHolder {

    /**
     * Key used to store MDC data in Reactor context.
     * <p>
     * This constant defines the key under which the MDC map is stored in the
     * Reactor Context. It should be used consistently across all code that needs to
     * access or modify the MDC data in a reactive context.
     */
    public static final String MDC_CONTEXT_KEY = "MDC_CONTEXT";

    /**
     * Extracts the MDC map from a ContextView without blocking.
     * <p>
     * This method safely extracts the MDC map from the provided ContextView,
     * handling potential type casting and null checks.
     *
     * @param context the reactor context view
     * @return the extracted MDC map, or an empty map if none exists
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> extractMdcMapFromContext(reactor.util.context.ContextView context) {
        Map<String, String> mdcMap = new HashMap<>();
        if (context != null && context.hasKey(MDC_CONTEXT_KEY)) {
            Object mdcObj = context.get(MDC_CONTEXT_KEY);
            if (mdcObj instanceof Map) {
                mdcMap.putAll((Map<String, String>) mdcObj);
            }
        }
        return mdcMap;
    }

    /**
     * Retrieves the MDC map from the current thread's context.
     * <p>
     * This method tries to get MDC information from the thread-local MDC context,
     * which is typically set by MDCWebFilter in WebFlux applications. If no MDC
     * context is found, it returns an empty map.
     * <p>
     * This approach avoids blocking operations in reactive code while still
     * providing access to MDC data for logging purposes.
     *
     * @return the MDC map from context or an empty map if none exists
     */
    public static Map<String, String> getMdcMap() {
        // Check thread-local MDC context, which might have been set by MDCWebFilter
        Map<String, String> mdcMap = MDC.getCopyOfContextMap();
        if (mdcMap != null && !mdcMap.isEmpty()) {
            return mdcMap;
        }

        // If we're in a reactive context, we should have populated the MDC via
        // doOnSubscribe
        // in the MDCWebFilter, but as a fallback, return an empty map
        return new HashMap<>();
    }

    /**
     * Sets MDC values from a map into the current thread's MDC context.
     * <p>
     * This method provides a convenient way to transfer MDC values from a Reactor
     * Context into the current thread's MDC context before logging operations. It's
     * particularly useful in operators like doOnNext, doOnEach, or doOnSubscribe.
     *
     * @param mdcValues the MDC values to set
     */
    public static void setThreadLocalMdc(Map<String, String> mdcValues) {
        if (mdcValues != null && !mdcValues.isEmpty()) {
            // Save current MDC
            Map<String, String> oldMdc = MDC.getCopyOfContextMap();

            try {
                // Set MDC values for current thread
                MDC.setContextMap(mdcValues);
            } catch (Exception ex) {
                // Restore previous MDC if there was a problem
                if (oldMdc != null) {
                    MDC.setContextMap(oldMdc);
                } else {
                    MDC.clear();
                }
            }
        }
    }

    /**
     * Helper method to ensure MDC values are available when logging.
     * <p>
     * This method can be used in hooks like doOnEach or doOnNext to ensure that MDC
     * values from the reactor context are set in the current thread for logging
     * purposes.
     *
     * @param context The reactor context view to extract MDC values from
     */
    public static void setMdcFromContext(reactor.util.context.ContextView context) {
        try {
            if (context.hasKey(MDC_CONTEXT_KEY)) {
                Object mdcObj = context.get(MDC_CONTEXT_KEY);
                if (mdcObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> contextMdc = (Map<String, String>) mdcObj;
                    MDC.setContextMap(contextMdc);
                }
            }
        } catch (Exception e) {
            // Just log and continue if there's an issue with MDC
            System.err.println("Error setting MDC from context: " + e.getMessage());
        }
    }

    /**
     * Gets the MDC map from the reactor context in the given Mono chain.
     * <p>
     * This method allows you to explicitly retrieve the MDC map from within a
     * reactive chain without using blocking operations.
     *
     * @param mono the Mono to retrieve context from
     * @return a new Mono that will emit the MDC map
     */
    @SuppressWarnings("unchecked")
    public static Mono<Map<String, String>> getMdcMapFromContext(Mono<?> mono) {
        // First subscribe to the original Mono to ensure its context is used
        return mono.flatMap(value ->
        // Then use deferContextual with the same context to extract MDC data
        Mono.deferContextual(ctx -> {
            Map<String, String> mdcMap = new HashMap<>();

            if (ctx.hasKey(MDC_CONTEXT_KEY)) {
                Object mdcObj = ctx.get(MDC_CONTEXT_KEY);
                if (mdcObj instanceof Map) {
                    mdcMap.putAll((Map<String, String>) mdcObj);
                }
            }

            return Mono.just(mdcMap);
        }));
    }
}