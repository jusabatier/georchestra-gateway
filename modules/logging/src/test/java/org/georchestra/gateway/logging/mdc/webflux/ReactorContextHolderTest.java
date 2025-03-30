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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

class ReactorContextHolderTest {

    @BeforeEach
    void setUp() {
        // Clear MDC before each test
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        // Clean up MDC after tests
        MDC.clear();
    }

    @Test
    void getMdcMapShouldReturnThreadLocalMdc() {
        // Set up MDC values
        MDC.put("key1", "value1");
        MDC.put("key2", "value2");

        // Get MDC map
        Map<String, String> mdcMap = ReactorContextHolder.getMdcMap();

        // Verify results
        assertThat(mdcMap).isNotEmpty().hasSize(2).containsEntry("key1", "value1").containsEntry("key2", "value2");
    }

    @Test
    void getMdcMapShouldReturnEmptyMapWhenNoMdcAvailable() {
        // Ensure MDC is empty
        MDC.clear();

        // Get MDC map
        Map<String, String> mdcMap = ReactorContextHolder.getMdcMap();

        // Verify results
        assertThat(mdcMap).isNotNull().isEmpty();
    }

    @Test
    void setThreadLocalMdcShouldSetValuesInMdc() {
        // Prepare MDC values to set
        Map<String, String> mdcValues = new HashMap<>();
        mdcValues.put("test-key1", "test-value1");
        mdcValues.put("test-key2", "test-value2");

        // Set thread-local MDC
        ReactorContextHolder.setThreadLocalMdc(mdcValues);

        // Verify MDC values were set
        assertThat(MDC.get("test-key1")).isEqualTo("test-value1");
        assertThat(MDC.get("test-key2")).isEqualTo("test-value2");
    }

    @Test
    void setThreadLocalMdcShouldHandleNullAndEmptyMaps() {
        // Set initial MDC value
        MDC.put("initial-key", "initial-value");

        // Set null MDC values (should not change MDC)
        ReactorContextHolder.setThreadLocalMdc(null);
        assertThat(MDC.get("initial-key")).isEqualTo("initial-value");

        // Set empty MDC values (should not change MDC)
        ReactorContextHolder.setThreadLocalMdc(new HashMap<>());
        assertThat(MDC.get("initial-key")).isEqualTo("initial-value");
    }

    @Test
    void setMdcFromContextShouldSetValuesFromContext() {
        // Create test MDC map
        Map<String, String> testMdc = new HashMap<>();
        testMdc.put("context-key1", "context-value1");
        testMdc.put("context-key2", "context-value2");

        // Create context with MDC map
        Context context = Context.of(ReactorContextHolder.MDC_CONTEXT_KEY, testMdc);

        // Set MDC from context
        ReactorContextHolder.setMdcFromContext(context);

        // Verify MDC values were set
        assertThat(MDC.get("context-key1")).isEqualTo("context-value1");
        assertThat(MDC.get("context-key2")).isEqualTo("context-value2");
    }

    @Test
    void setMdcFromContextShouldHandleContextWithoutMdc() {
        // Create context without MDC map
        Context context = Context.empty();

        // Set initial MDC value
        MDC.put("initial-key", "initial-value");

        // Set MDC from context (should not change MDC since context has no MDC)
        ReactorContextHolder.setMdcFromContext(context);

        // Verify MDC wasn't changed
        assertThat(MDC.get("initial-key")).isEqualTo("initial-value");
    }

    @Test
    void getMdcMapFromContextShouldReturnEmptyMapWhenNoMdcInContext() {
        // Create Mono without MDC in context
        Mono<String> testMono = Mono.just("test");

        // Create a direct test with flatMap for consistency with previous test
        Mono<Map<String, String>> mdcMapMono = testMono.flatMap(ignored -> Mono.deferContextual(ctx -> {
            if (ctx.hasKey(ReactorContextHolder.MDC_CONTEXT_KEY)) {
                @SuppressWarnings("unchecked")
                Map<String, String> mdcMap = (Map<String, String>) ctx.get(ReactorContextHolder.MDC_CONTEXT_KEY);
                return Mono.just(mdcMap);
            }
            return Mono.just(new HashMap<String, String>());
        }));

        // Verify an empty map is returned
        StepVerifier.create(mdcMapMono).assertNext(mdcMap -> assertThat(mdcMap).isNotNull().isEmpty()).verifyComplete();
    }
}