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
package org.georchestra.gateway.filter.global;

import static com.github.tomakehurst.wiremock.stubbing.StubMapping.buildFrom;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import org.georchestra.gateway.app.GeorchestraGatewayApplication;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

@SpringBootTest(classes = GeorchestraGatewayApplication.class, //
        webEnvironment = WebEnvironment.RANDOM_PORT, //
        properties = { //
                "server.error.whitelabel.enabled=false", //
                "georchestra.gateway.global-access-rules[0].intercept-url=/**", //
                "georchestra.gateway.global-access-rules[0].anonymous=true" //
        })
@WireMockTest
class ApplicationErrorGatewayFilterFactoryTest {

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
        ApplicationErrorGatewayFilterFactoryTest.wmRuntimeInfo = runtimeInfo;
    }

    /**
     * Set up a gateway route that proxies all requests to the wiremock server
     */
    @DynamicPropertySource
    static void registerRoutes(DynamicPropertyRegistry registry) {
        String targetUrl = wmRuntimeInfo.getHttpBaseUrl();

        registry.add("spring.cloud.gateway.routes[0].id", () -> "mockeduproute");
        registry.add("spring.cloud.gateway.routes[0].uri", () -> targetUrl);
        registry.add("spring.cloud.gateway.routes[0].predicates[0]", () -> "Path=/**");
    }

    @Autowired
    TestRestTemplate testRestTemplate;

    @SpyBean
    ApplicationErrorGatewayFilterFactory factory;

    @BeforeEach
    void setUp(WireMockRuntimeInfo runtimeInfo) throws Exception {
        StubMapping defaultResponse = buildFrom("""
                {
                    "priority": 100,
                    "request": {"method": "ANY","urlPattern": ".*"},
                    "response": {
                        "status": 418,
                        "jsonBody": { "status": "Error", "message": "I'm a teapot" },
                        "headers": {"Content-Type": "application/json"}
                    }
                }
                """);

        WireMock wireMock = runtimeInfo.getWireMock();
        wireMock.register(defaultResponse);
    }

    @Test
    void testNonIdempotentHttpMethodsIgnored(WireMockRuntimeInfo runtimeInfo) {
        StubMapping mapping = buildFrom("""
                {
                    "priority": 1,
                    "request": {
                        "method": "POST",
                        "url": "/geonetwork",
                        "headers": {
                            "Accept": {"contains": "text/html"}
                        }
                    },
                    "response": {
                        "status": 400,
                        "body": "Bad request from downstream",
                        "headers": {
                            "Content-Type": "text/plain",
                            "X-Frame-Options": "ALLOW-FROM *.test.com",
                            "X-Content-Type-Options": "nosniff",
                            "Referrer-Policy": "same-origin"
                        }
                   }
                }
                """);
        runtimeInfo.getWireMock().register(mapping);

        ResponseEntity<String> response = testRestTemplate.postForEntity("/geonetwork",
                withHeaders("Accept", "text/html"), String.class);

        verify(factory, times(1)).canFilter(any());
        verify(factory, never()).decorate(any());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, String> headers = response.getHeaders().toSingleValueMap();
        Map<String, String> expected = Map.of(//
                "Content-Type", "text/plain", //
                "X-Frame-Options", "ALLOW-FROM *.test.com", //
                "X-Content-Type-Options", "nosniff", //
                "Referrer-Policy", "same-origin"//

        );
        assertThat(headers).as("response does not contain all original headers").containsAllEntriesOf(expected);
        assertThat(response.getBody()).isEqualTo("Bad request from downstream");
    }

    @Test
    void testNonHtmlAcceptRquestIgnored(WireMockRuntimeInfo runtimeInfo) {
        StubMapping mapping = buildFrom("""
                {
                    "priority": 1,
                    "request": {
                        "method": "GET",
                        "url": "/geonetwork",
                        "headers": {
                            "Accept": {"contains": "application/json"}
                        }
                    },
                    "response": {
                        "status": 500,
                        "body": "Internal server error from downstream",
                        "headers": {
                            "Content-Type": "text/plain",
                            "X-Frame-Options": "ALLOW-FROM *.test.com",
                            "X-Content-Type-Options": "nosniff",
                            "Referrer-Policy": "same-origin"
                        }
                   }
                }
                """);
        runtimeInfo.getWireMock().register(mapping);

        RequestEntity<Void> req = RequestEntity.get("/geonetwork").header("Accept", "application/json").build();
        ResponseEntity<String> response = testRestTemplate.exchange(req, String.class);

        verify(factory, times(1)).canFilter(any());
        verify(factory, never()).decorate(any());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        Map<String, String> headers = response.getHeaders().toSingleValueMap();
        Map<String, String> expected = Map.of(//
                "Content-Type", "text/plain", //
                "X-Frame-Options", "ALLOW-FROM *.test.com", //
                "X-Content-Type-Options", "nosniff", //
                "Referrer-Policy", "same-origin"//

        );
        assertThat(headers).as("response does not contain all original headers").containsAllEntriesOf(expected);
        assertThat(response.getBody()).isEqualTo("Internal server error from downstream");
    }

    @Test
    void testApplicationErrorToCustomErrorPageMapping(WireMockRuntimeInfo runtimeInfo) {
        runtimeInfo.getWireMock().register(buildFrom("""
                {
                    "priority": 1,
                    "request": {
                        "method": "GET",
                        "url": "/geonetwork",
                        "headers": {
                            "Accept": {"contains": "text/html"}
                        }
                    },
                    "response": {
                        "status": 500,
                        "body": "Internal server error from downstream",
                        "headers": {
                            "Content-Type": "text/plain",
                            "X-Frame-Options": "ALLOW-FROM *.test.com",
                            "X-Content-Type-Options": "nosniff",
                            "Referrer-Policy": "same-origin"
                        }
                   }
                }
                """));

        RequestEntity<Void> req = RequestEntity.get("/geonetwork").header("Accept", "text/html").build();
        ResponseEntity<String> response = testRestTemplate.exchange(req, String.class);

        verify(factory, times(1)).canFilter(any());
        verify(factory, times(1)).decorate(any());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getHeaders().getContentType().isCompatibleWith(MediaType.TEXT_HTML))
                .as("Expected content type text/html").isTrue();

        Map<String, String> headers = response.getHeaders().toSingleValueMap();
        Map<String, String> expected = Map.of(//
                "X-Frame-Options", "ALLOW-FROM *.test.com", //
                "X-Content-Type-Options", "nosniff", //
                "Referrer-Policy", "same-origin"//

        );
        assertThat(headers).as("response does not contain all original headers").containsAllEntriesOf(expected);
    }

    private HttpEntity<?> withHeaders(String... headersKvp) {
        assertThat(headersKvp.length % 2).isZero();
        HttpHeaders headers = new HttpHeaders();
        Iterator<String> it = Stream.of(headersKvp).iterator();
        while (it.hasNext()) {
            headers.add(it.next(), it.next());
        }
        return new HttpEntity<>(headers);
    }
}
