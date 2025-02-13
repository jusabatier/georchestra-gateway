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

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.HEAD;
import static org.springframework.http.HttpMethod.OPTIONS;
import static org.springframework.http.HttpMethod.TRACE;

import java.util.List;
import java.util.Set;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.RedirectToGatewayFilterFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.server.ServerWebExchange;

import com.google.common.annotations.VisibleForTesting;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * {@link GatewayFilterFactory} that redirects to {@literal /login} if the
 * request's query string contains a {@literal login} parameter and the request
 * is not already authenticated.
 * <p>
 * Sample usage:
 * 
 * <pre>
 * <code>
 * spring:
 *   cloud:
 *    gateway:
 *      routes:
 *      - id: routeid
 *        uri: ...
 *        filters:
 *        - LoginParamRedirect
 * </code>
 * </pre>
 * 
 */
@Slf4j
public class LoginParamRedirectGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    private static final Set<HttpMethod> redirectMethods = Set.of(GET, HEAD, OPTIONS, TRACE);

    @Override
    public LoginParamRedirectGatewayFilter apply(Object config) {
        RedirectToGatewayFilterFactory.Config redirectConfig = new RedirectToGatewayFilterFactory.Config();
        redirectConfig.setStatus("302");
        redirectConfig.setUrl("/login");
        GatewayFilter delegate = new RedirectToGatewayFilterFactory().apply(redirectConfig);
        return new LoginParamRedirectGatewayFilter(delegate);
    }

    @RequiredArgsConstructor
    public static class LoginParamRedirectGatewayFilter implements GatewayFilter {

        private static final Authentication UNAUTHENTICATED = new AnonymousAuthenticationToken("nobody", "nobody",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));

        private final @NonNull GatewayFilter delegate;

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            HttpMethod method = exchange.getRequest().getMethod();
            if (redirectMethods.contains(method) && containsLoginQueryParam(exchange)) {
                log.info("Applying ?login query param redirect filter for {} {}", method,
                        exchange.getRequest().getURI());
                return redirectToLoginIfNotAuthenticated(exchange, chain);
            }
            return chain.filter(exchange);
        }

        private Mono<Void> redirectToLoginIfNotAuthenticated(ServerWebExchange exchange, GatewayFilterChain chain) {

            return getAuthentication()//
                    .filter(Authentication::isAuthenticated)//
                    .switchIfEmpty(Mono.just(UNAUTHENTICATED))//
                    .flatMap(authentication -> {
                        // delegate to the redirect filter otherwise
                        if (authentication instanceof AnonymousAuthenticationToken) {
                            log.info("redirecting to /login: {}", exchange.getRequest().getURI());
                            return delegate.filter(exchange, chain);
                        }
                        // proceed if already authenticated
                        log.info("already authenticated ({}), proceeding without redirection to /login",
                                authentication.getName());
                        return chain.filter(exchange);
                    });
        }

        @VisibleForTesting
        public Mono<Authentication> getAuthentication() {
            return ReactiveSecurityContextHolder.getContext().map(SecurityContext::getAuthentication);
        }

        private boolean containsLoginQueryParam(ServerWebExchange exchange) {
            ServerHttpRequest request = exchange.getRequest();
            return request.getQueryParams().containsKey("login");
        }

    }
}
