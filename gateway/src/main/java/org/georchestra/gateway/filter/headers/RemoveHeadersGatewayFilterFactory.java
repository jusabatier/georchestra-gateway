/*
 * Copyright (C) 2021 by the geOrchestra PSC
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
package org.georchestra.gateway.filter.headers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.georchestra.gateway.filter.headers.RemoveHeadersGatewayFilterFactory.RegExConfig;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link GatewayFilterFactory} to remove incoming HTTP request headers whose
 * names match a Java regular expression.
 * <p>
 * This filter ensures that unwanted headers are stripped from incoming requests
 * before being forwarded to backend services, improving security and request
 * integrity.
 * </p>
 * <p>
 * Usage:
 * </p>
 * <p>
 * Add a {@code RemoveHeaders=<regular expression>} filter to a route in the
 * {@code spring.cloud.gateway.routes.filters} configuration.
 * </p>
 *
 * <pre>
 * <code>
 * spring:
 *   cloud:
 *    gateway:
 *      routes:
 *      - id: root
 *        uri: http://backend-service/context
 *        filters:
 *        - RemoveHeaders=(?i)(sec-.*|Authorization) 
 * </code>
 * </pre>
 *
 * <p>
 * Since version {@code 1.2.0}, the regular expression can match both header
 * names and values. This allows filtering specific header values while
 * preserving others. For example, to strip Basic authentication headers but
 * keep Bearer tokens, the following configuration can be used:
 * </p>
 *
 * <pre>
 * <code>
 * spring:
 *   cloud:
 *    gateway:
 *      routes:
 *      - id: root
 *        uri: http://backend-service/context
 *        filters:
 *        - RemoveHeaders=(?i)^(sec-.*|Authorization:(?!\s*Bearer\s*$))
 * </code>
 * </pre>
 */
@Slf4j(topic = "org.georchestra.gateway.filter.headers")
public class RemoveHeadersGatewayFilterFactory extends AbstractGatewayFilterFactory<RegExConfig> {

    public RemoveHeadersGatewayFilterFactory() {
        super(RegExConfig.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("regEx");
    }

    @Override
    public GatewayFilter apply(RegExConfig regexConfig) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest().mutate().headers(regexConfig::removeMatching).build();
            exchange = exchange.mutate().request(request).build();
            return chain.filter(exchange);
        };
    }

    /**
     * Configuration class that holds the regular expression for header removal.
     * <p>
     * The provided regular expression is compiled and used to match both header
     * names and values. Headers that match the pattern are removed from incoming
     * requests before they are forwarded.
     * </p>
     */
    @NoArgsConstructor
    public static class RegExConfig {

        private @Getter String regEx;
        private Pattern compiled;

        /**
         * Constructs a {@link RegExConfig} with the given regular expression.
         *
         * @param regEx the regular expression for matching headers to remove
         */
        public RegExConfig(String regEx) {
            setRegEx(regEx);
        }

        /**
         * Sets the regular expression used to match header names and values.
         *
         * @param regEx the regular expression to use
         */
        public void setRegEx(String regEx) {
            Objects.requireNonNull(regEx, "Regular expression can't be null");
            this.regEx = regEx;
            this.compiled = Pattern.compile(regEx);
        }

        private Pattern pattern() {
            Objects.requireNonNull(compiled, "Regular expression is not initialized");
            return compiled;
        }

        /**
         * Checks if any headers in the given {@link HttpHeaders} match the configured
         * regular expression.
         *
         * @param httpHeaders the HTTP headers to check
         * @return {@code true} if any headers match, otherwise {@code false}
         */
        boolean anyMatches(@NonNull HttpHeaders httpHeaders) {
            return httpHeaders.keySet().stream().anyMatch(header -> matches(header, httpHeaders.get(header)));
        }

        /**
         * Checks if a given header name or its value matches the configured regular
         * expression.
         *
         * @param headerNameOrTuple the header name or header name-value pair
         * @return {@code true} if it matches, otherwise {@code false}
         */
        boolean matches(@NonNull String headerNameOrTuple) {
            return pattern().matcher(headerNameOrTuple).matches();
        }

        /**
         * Checks if a given header name and its values match the configured regular
         * expression.
         *
         * @param headerName the name of the header
         * @param values     the list of header values
         * @return {@code true} if any value matches, otherwise {@code false}
         */
        boolean matches(@NonNull String headerName, List<String> values) {
            return values.stream().map(value -> "%s: %s".formatted(headerName, value)).anyMatch(this::matches);
        }

        /**
         * Removes all headers from the given {@link HttpHeaders} that match the
         * configured regular expression.
         *
         * @param headers the HTTP headers from which matching headers should be removed
         */
        void removeMatching(@NonNull HttpHeaders headers) {
            List.copyOf(headers.entrySet()).stream().filter(entry -> matches(entry.getKey(), entry.getValue()))
                    .map(Map.Entry::getKey).peek(name -> log.trace("Removing header {}", name))
                    .forEach(headers::remove);
        }
    }
}
