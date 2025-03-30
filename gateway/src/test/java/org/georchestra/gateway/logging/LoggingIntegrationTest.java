/*
 * Copyright (C) 2024 by the geOrchestra PSC
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
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra. If not, see <http://www.gnu.org/licenses/>.
 */
package org.georchestra.gateway.logging;

import static com.github.tomakehurst.wiremock.stubbing.StubMapping.buildFrom;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.georchestra.gateway.app.GeorchestraGatewayApplication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

/**
 * Integration test for verifying the logging functionality of the geOrchestra
 * gateway, focusing on MDC propagation and JSON log format.
 */
@ActiveProfiles({ "json-logs-test" })
@SpringBootTest(classes = GeorchestraGatewayApplication.class, properties = { //
        "server.error.whitelabel.enabled=false", //
        "georchestra.gateway.global-access-rules[0].intercept-url=/**", //
        "georchestra.gateway.global-access-rules[0].anonymous=true", //
        "logging.accesslog.enabled=true", //
        "logging.accesslog.info=.*\\/api\\/.*", //
        "logging.mdc.include.http.id=true", "logging.mdc.include.http.method=true", //
        "logging.mdc.include.http.url=true", //
        "logging.mdc.include.http.remote-addr=true", //
        "logging.mdc.include.app.name=true", //
        "logging.mdc.include.app.profile=true", //
        "logging.mdc.include.user.id=true" }, webEnvironment = WebEnvironment.RANDOM_PORT)
@WireMockTest
class LoggingIntegrationTest {

    private static final String LOG_FILE_NAME = "gateway-test-logs.json";

    /**
     * saved in {@link #setUpWireMock}, to be used on {@link #registerRoutes}
     */
    private static WireMockRuntimeInfo wmRuntimeInfo;

    /**
     * Set up stub requests for the wiremock server. WireMock is running on a random
     * port, so this method saves {@link #wmRuntimeInfo} for
     * {@link #registerRoutes(DynamicPropertyRegistry)}
     */
    @BeforeAll
    static void saveWireMock(WireMockRuntimeInfo runtimeInfo) {
        LoggingIntegrationTest.wmRuntimeInfo = runtimeInfo;
    }

    /**
     * Set up a gateway route that proxies requests to the wiremock server
     */
    @DynamicPropertySource
    static void registerRoutes(DynamicPropertyRegistry registry) {
        String targetUrl = wmRuntimeInfo.getHttpBaseUrl();

        registry.add("spring.cloud.gateway.routes[0].id", () -> "api-route");
        registry.add("spring.cloud.gateway.routes[0].uri", () -> targetUrl);
        registry.add("spring.cloud.gateway.routes[0].predicates[0]", () -> "Path=/api/**");
    }

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @TempDir
    static Path logsDir;

    private static Path logFile;

    @BeforeAll
    static void setUpFile() throws IOException {
        // Create or clear the log file
        logFile = logsDir.resolve(LOG_FILE_NAME);
        // Configure the test appender via system property
        // This will be picked up by logback-test.xml
        System.setProperty("TEST_LOG_FILE", logFile.toAbsolutePath().toString());
    }

    @BeforeEach
    void setUp(WireMockRuntimeInfo runtimeInfo) throws IOException {
        // Set up mock API endpoint
        StubMapping defaultApiResponse = buildFrom("""
                {
                    "priority": 100,
                    "request": {"method": "GET", "urlPattern": "/api/.*"},
                    "response": {
                        "status": 200,
                        "jsonBody": { "status": "OK", "message": "Test API response" },
                        "headers": {"Content-Type": "application/json"}
                    }
                }
                """);

        WireMock wireMock = runtimeInfo.getWireMock();
        wireMock.register(defaultApiResponse);
    }

    @AfterEach
    void tearDown() {
        System.clearProperty("TEST_LOG_FILE");
    }

    @Test
    @WithMockUser(username = "testuser", authorities = "ROLE_USER")
    void verifyJsonFormattedLoggingWithMdcProperties(@LocalServerPort int port) throws Exception {
        // Make a request that should trigger access logging
        RequestEntity<Void> req = RequestEntity.get("/api/test").build();
        ResponseEntity<String> response = testRestTemplate.exchange(req, String.class);

        // Verify the request was successful
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        // Wait for logs to be written to the file
        await().atMost(5, TimeUnit.SECONDS).pollInterval(Duration.ofMillis(100)).until(() -> Files.size(logFile) > 0);

        // Read the log file
        List<String> logLines = Files.readAllLines(logFile, StandardCharsets.UTF_8).stream()
                .filter(line -> !line.isBlank()).toList();

        // Check that we have at least one log entry
        assertThat(logLines).isNotEmpty();

        // Parse log entries as JSON and check for MDC properties

        for (String logLine : logLines) {
            @SuppressWarnings("unchecked")
            Map<String, Object> logEntry = objectMapper.readValue(logLine, Map.class);

            assertThat(logEntry).containsKey("http.request.id");
            assertThat(logEntry).containsEntry("http.request.method", "GET");
            assertThat(logEntry).containsEntry("http.request.url", "http://localhost:%d/api/test".formatted(port));
            assertThat(logEntry).containsEntry("application.name", "gateway-service");
            assertThat(logEntry).containsEntry("application.profile", "json-logs-test");

            // If user info is logged, verify that too
            if (logEntry.containsKey("enduser.id")) {
                assertThat(logEntry).containsEntry("enduser.id", "testuser");
            }
        }
    }
}