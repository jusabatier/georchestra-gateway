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

import static org.georchestra.gateway.logging.mdc.webflux.ReactorContextHolder.MDC_CONTEXT_KEY;

import java.security.Principal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.georchestra.gateway.logging.mdc.config.AuthenticationMdcConfigProperties;
import org.georchestra.gateway.logging.mdc.config.HttpRequestMdcConfigProperties;
import org.georchestra.gateway.logging.mdc.config.SpringEnvironmentMdcConfigProperties;
import org.slf4j.MDC;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.web.reactive.filter.OrderedWebFilter;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Logging MDC (Mapped Diagnostic Context) filter for WebFlux applications.
 * <p>
 * This filter is responsible for populating the MDC with request-specific
 * information in WebFlux reactive applications. Since WebFlux can execute
 * requests across different threads, the standard thread-local based MDC
 * approach doesn't work. Instead, this filter uses Reactor Context to propagate
 * MDC values through the reactive chain.
 * <p>
 * The filter captures information based on the configuration properties:
 * <ul>
 * <li>{@link HttpRequestMdcConfigProperties} - Controls HTTP request-related
 * MDC attributes</li>
 * <li>{@link AuthenticationMdcConfigProperties} - Controls user
 * authentication-related MDC attributes</li>
 * <li>{@link SpringEnvironmentMdcConfigProperties} - Controls application
 * environment MDC attributes</li>
 * </ul>
 * <p>
 * This filter is designed to run with {@link Ordered#HIGHEST_PRECEDENCE} to
 * ensure MDC data is available to all subsequent filters and handlers in the
 * request chain.
 */
@RequiredArgsConstructor
public class MDCWebFilter implements OrderedWebFilter {

    private final @NonNull HttpRequestMdcConfigProperties httpConfig;
    private final @NonNull AuthenticationMdcConfigProperties authConfig;
    private final @NonNull SpringEnvironmentMdcConfigProperties appConfig;
    private final @NonNull Environment env;
    private final @NonNull Optional<BuildProperties> buildProperties;

    private static final Principal ANNON = () -> "anonymous";

    /**
     * Returns the order of this filter in the filter chain.
     * <p>
     * This filter is set to {@link Ordered#HIGHEST_PRECEDENCE} to ensure it
     * executes before any other filters, making MDC data available to all
     * subsequent components in the request processing chain.
     *
     * @return the highest precedence order value
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /**
     * Main filter method that processes WebFlux requests and propagates MDC through
     * the reactive chain.
     * <p>
     * This method performs the following steps:
     * <ol>
     * <li>Saves the initial MDC state (to preserve it after request
     * processing)</li>
     * <li>Clears the current MDC</li>
     * <li>Sets MDC attributes based on the current request</li>
     * <li>Propagates the MDC map through the Reactor Context</li>
     * <li>Restores the original MDC after request processing</li>
     * </ol>
     * <p>
     * By using {@link Mono#contextWrite(reactor.util.context.ContextView)
     * contextWrite}, this filter ensures that MDC data is available throughout the
     * entire reactive chain, even across thread boundaries.
     *
     * @param exchange the current server exchange
     * @param chain    the filter chain to delegate to
     * @return a Mono completing when the request handling is done
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Store initial MDC state
        Map<String, String> initialMdc = MDC.getCopyOfContextMap();

        // Clear MDC for this request
        MDC.clear();

        return setMdcAttributes(exchange).flatMap(requestMdc -> {
            // Restore original MDC (for servlet thread reuse)
            if (initialMdc != null)
                MDC.setContextMap(initialMdc);
            else
                MDC.clear();

            // Use Reactor context to propagate MDC through the reactive chain
            return chain.filter(exchange).contextWrite(context -> context.put(MDC_CONTEXT_KEY, requestMdc))
                    // Use a hook to restore MDC during subscription
                    .doOnSubscribe(s -> {
                        // Set the MDC values for this thread when the chain is subscribed
                        MDC.setContextMap(requestMdc);
                    }).doFinally(signalType -> {
                        // Clean up
                        MDC.clear();
                        if (initialMdc != null)
                            MDC.setContextMap(initialMdc);
                    });
        });
    }

    /**
     * Sets MDC attributes based on the current request and returns the MDC map.
     * <p>
     * This method populates the MDC with information from:
     * <ul>
     * <li>Application environment (e.g., application name, instance ID)</li>
     * <li>HTTP request details (e.g., method, URI, remote address)</li>
     * <li>Authentication principal (e.g., user ID) if available</li>
     * </ul>
     * <p>
     * The attributes included are controlled by the respective configuration
     * properties. After setting all the attributes, the method captures the
     * complete MDC state and returns it as a map wrapped in a Mono.
     *
     * @param exchange the current server exchange containing request information
     * @return a Mono with the Map of MDC attributes
     */
    private Mono<Map<String, String>> setMdcAttributes(ServerWebExchange exchange) {
        // Add application environment properties
        appConfig.addEnvironmentProperties(env, buildProperties);

        // Add HTTP properties
        setHttpMdcAttributes(exchange);

        // Get principal if available
        return exchange.getPrincipal().defaultIfEmpty(ANNON).map(principal -> {
            if (principal != null && principal != ANNON) {
                setAuthenticationMdcAttributes(principal);
            }

            // Capture MDC state after setting all properties
            Map<String, String> mdcMap = MDC.getCopyOfContextMap();
            return mdcMap != null ? mdcMap : new HashMap<>();
        });
    }

    /**
     * Sets authentication-specific MDC attributes from the Principal.
     * <p>
     * This method extracts information from the authenticated Principal and adds it
     * to the MDC based on the {@link AuthenticationMdcConfigProperties}
     * configuration. Information that can be added includes:
     * <ul>
     * <li>User ID</li>
     * <li>User roles</li>
     * <li>User organization</li>
     * <li>Authentication method</li>
     * </ul>
     *
     * @param principal the authenticated principal
     */
    private void setAuthenticationMdcAttributes(Principal principal) {
        if (principal == null) {
            return;
        }

        // Add user ID
        if (authConfig.isId()) {
            MDC.put("enduser.id", principal.getName());
        }

        // Add roles if configured and principal is an Authentication
        if (authConfig.isRoles() && principal instanceof Authentication auth) {
            Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
            if (authorities != null && !authorities.isEmpty()) {
                String roles = authorities.stream().map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(","));
                MDC.put("enduser.roles", roles);
            }
        }

        // Add authentication method if configured and principal is an Authentication
        if (authConfig.isAuthMethod() && principal instanceof Authentication auth) {
            MDC.put("enduser.auth-method", auth.getClass().getSimpleName());
        }

        // Note: Organization is not added here as it's specific to geOrchestra user
        // objects
        // and would need to be handled separately based on the actual user object type
    }

    /**
     * Sets HTTP-specific MDC attributes from the ServerWebExchange.
     * <p>
     * This method extracts information from the HTTP request and adds it to the MDC
     * based on the {@link HttpRequestMdcConfigProperties} configuration.
     * Information that can be added includes:
     * <ul>
     * <li>Request ID</li>
     * <li>Remote address</li>
     * <li>HTTP method</li>
     * <li>Request URL</li>
     * <li>Query string</li>
     * <li>Request parameters</li>
     * <li>HTTP headers</li>
     * <li>Cookies</li>
     * </ul>
     * <p>
     * Note: Some attributes available in Servlet applications are not available in
     * WebFlux, such as remoteHost and sessionId (commented out in the
     * implementation).
     *
     * @param exchange the current server exchange containing HTTP request
     *                 information
     */
    public void setHttpMdcAttributes(ServerWebExchange exchange) {
        ServerHttpRequest req = exchange.getRequest();
        httpConfig.id(req::getHeaders).remoteAddr(req.getRemoteAddress()).method(() -> req.getMethod().name())
                .path(path(req)).queryString(queryString(req)).parameters(req::getQueryParams).headers(req::getHeaders)
                .cookies(req::getCookies);
    }

    /**
     * Creates a supplier for the request URI path.
     * <p>
     * This method returns a Supplier that, when invoked, will provide the raw path
     * of the request URI. Using a Supplier allows lazy evaluation of the URI path.
     *
     * @param req the HTTP request
     * @return a Supplier that provides the request URI path
     */
    private Supplier<String> path(ServerHttpRequest req) {
        return () -> req.getURI().getRawPath();
    }

    /**
     * Creates a supplier for the request query string.
     * <p>
     * This method returns a Supplier that, when invoked, will provide the raw query
     * string of the request URI. Using a Supplier allows lazy evaluation of the
     * query string.
     *
     * @param req the HTTP request
     * @return a Supplier that provides the request query string
     */
    private Supplier<String> queryString(ServerHttpRequest req) {
        return () -> req.getURI().getRawQuery();
    }
}