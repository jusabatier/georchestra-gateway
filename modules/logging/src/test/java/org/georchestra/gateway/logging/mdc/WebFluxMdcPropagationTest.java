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
package org.georchestra.gateway.logging.mdc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.georchestra.gateway.logging.mdc.config.AuthenticationMdcConfigProperties;
import org.georchestra.gateway.logging.mdc.config.HttpRequestMdcConfigProperties;
import org.georchestra.gateway.logging.mdc.config.SpringEnvironmentMdcConfigProperties;
import org.georchestra.gateway.logging.mdc.webflux.MDCWebFilter;
import org.georchestra.gateway.logging.mdc.webflux.ReactorContextHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.MDC;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.Environment;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class WebFluxMdcPropagationTest {

    private HttpRequestMdcConfigProperties httpConfig;
    private AuthenticationMdcConfigProperties authConfig;
    private SpringEnvironmentMdcConfigProperties appConfig;
    private MDCWebFilter filter;
    private MockServerHttpRequest request;
    private MockServerWebExchange exchange;
    private TestWebFilterChain filterChain;

    @BeforeEach
    void setUp() {
        // Clear MDC before each test
        MDC.clear();

        // Initialize config with default values
        httpConfig = new HttpRequestMdcConfigProperties();
        authConfig = new AuthenticationMdcConfigProperties();
        appConfig = new SpringEnvironmentMdcConfigProperties();

        // Create mock environment
        Environment mockEnv = Mockito.mock(Environment.class);
        when(mockEnv.getProperty(eq("spring.application.name"), anyString())).thenReturn("test-app");
        when(mockEnv.getActiveProfiles()).thenReturn(new String[] { "test", "development" });
        when(mockEnv.getDefaultProfiles()).thenReturn(new String[] { "default" });
        when(mockEnv.getProperty(eq("spring.application.instance-id"), anyString())).thenReturn("instance-001");

        // Create mock build properties
        BuildProperties mockBuildProps = Mockito.mock(BuildProperties.class);
        when(mockBuildProps.getName()).thenReturn("test-app");
        when(mockBuildProps.getVersion()).thenReturn("1.0.0-SNAPSHOT");

        // Create filter with mocks
        filter = new MDCWebFilter(httpConfig, authConfig, appConfig, mockEnv, Optional.of(mockBuildProps));

        // Create mock request and exchange
        request = MockServerHttpRequest.get("http://example.com/test").header("X-Request-ID", "test-request-id")
                .build();
        exchange = MockServerWebExchange.from(request);

        // Create test filter chain
        filterChain = new TestWebFilterChain();
    }

    @Test
    void shouldPropagateHttpMdcThroughReactorChain() {
        // Configure what HTTP properties we want to capture
        httpConfig.setId(true);
        httpConfig.setMethod(true);
        httpConfig.setPath(true);
        httpConfig.setRemoteAddr(true);

        // Run the filter
        Mono<Void> result = filter.filter(exchange, filterChain);

        // Verify execution completes successfully
        StepVerifier.create(result).verifyComplete();

        // Check that MDC was propagated to the filter chain
        Map<String, String> capturedMdc = filterChain.getCapturedMdc();
        assertThat(capturedMdc).isNotEmpty().containsKey("http.request.id").containsKey("http.request.method")
                .containsKey("http.request.path").containsEntry("http.request.id", "test-request-id")
                .containsEntry("http.request.method", "GET").containsEntry("http.request.path", "/test");
    }

    @Test
    void shouldPropagateApplicationEnvironmentMdc() {
        // Configure app properties
        appConfig.setName(true);
        appConfig.setVersion(true);
        appConfig.setProfile(true);
        appConfig.setInstanceId(true);

        // Run the filter
        Mono<Void> result = filter.filter(exchange, filterChain);

        // Verify execution completes successfully
        StepVerifier.create(result).verifyComplete();

        // Check that MDC was propagated to the filter chain
        Map<String, String> capturedMdc = filterChain.getCapturedMdc();
        assertThat(capturedMdc).isNotEmpty().containsKey("application.name").containsKey("application.version")
                .containsKey("application.profile").containsKey("application.instance-id")
                .containsEntry("application.name", "test-app").containsEntry("application.version", "1.0.0-SNAPSHOT")
                .containsEntry("application.profile", "test,development")
                .containsEntry("application.instance-id", "instance-001");
    }

    @Test
    void shouldPropagateAuthenticationMdc() {
        // Configure authentication properties
        authConfig.setId(true);
        authConfig.setRoles(true);
        authConfig.setAuthMethod(true);

        // Create mock authentication
        SimpleGrantedAuthority userRole = new SimpleGrantedAuthority("ROLE_USER");
        SimpleGrantedAuthority adminRole = new SimpleGrantedAuthority("ROLE_ADMIN");
        TestingAuthenticationToken mockAuth = new TestingAuthenticationToken("testuser", null,
                List.of(userRole, adminRole));
        mockAuth.setAuthenticated(true);

        // Set up the exchange with the mock authentication
        exchange = spy(MockServerWebExchange.from(request));
        doReturn(Mono.just(mockAuth)).when(exchange).getPrincipal();

        // Run the filter
        Mono<Void> result = filter.filter(exchange, filterChain);

        // Verify execution completes successfully
        StepVerifier.create(result).verifyComplete();

        // Check that MDC was propagated to the filter chain
        Map<String, String> capturedMdc = filterChain.getCapturedMdc();
        assertThat(capturedMdc).isNotEmpty().containsKey("enduser.id").containsKey("enduser.roles")
                .containsKey("enduser.auth-method").containsEntry("enduser.id", "testuser")
                .containsEntry("enduser.roles", "ROLE_USER,ROLE_ADMIN")
                .containsEntry("enduser.auth-method", "TestingAuthenticationToken");
    }

    /**
     * Test implementation of WebFilterChain that captures MDC context during
     * execution
     */
    static class TestWebFilterChain implements WebFilterChain {
        private Map<String, String> capturedMdc;

        @Override
        public Mono<Void> filter(org.springframework.web.server.ServerWebExchange exchange) {
            // Capture MDC at filter execution time
            capturedMdc = MDC.getCopyOfContextMap();
            if (capturedMdc == null) {
                capturedMdc = new HashMap<>();
            }

            // Use the new extractMdcMapFromContext method to get MDC from reactor context
            return Mono.deferContextual(ctx -> {
                // Use the new helper method to extract MDC map
                capturedMdc = ReactorContextHolder.extractMdcMapFromContext(ctx);
                return Mono.empty();
            });
        }

        public Map<String, String> getCapturedMdc() {
            return capturedMdc;
        }
    }
}