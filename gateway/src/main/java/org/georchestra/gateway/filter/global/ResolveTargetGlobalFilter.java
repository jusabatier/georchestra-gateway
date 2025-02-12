/*
 * Copyright (C) 2022 by the geOrchestra PSC
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

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.georchestra.gateway.model.GatewayConfigProperties;
import org.georchestra.gateway.model.GeorchestraTargetConfig;
import org.georchestra.gateway.model.HeaderMappings;
import org.georchestra.gateway.model.RoleBasedAccessRule;
import org.georchestra.gateway.model.Service;
import org.georchestra.gateway.security.ResolveGeorchestraUserGlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.RouteToRequestUrlFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;

import com.google.common.annotations.VisibleForTesting;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * A {@link GlobalFilter} that resolves and stores the
 * {@link GeorchestraTargetConfig} for the matched {@link Route}, enabling
 * subsequent filters to access configuration details such as role-based access
 * rules and HTTP header mappings.
 * <p>
 * This filter executes after user resolution in
 * {@link ResolveGeorchestraUserGlobalFilter} and before request routing in
 * {@link RouteToRequestUrlFilter}.
 * </p>
 */
@RequiredArgsConstructor
@Slf4j
public class ResolveTargetGlobalFilter implements GlobalFilter, Ordered {

    /**
     * The execution order of this filter, ensuring it runs after user resolution
     * but before request routing.
     */
    public static final int ORDER = ResolveGeorchestraUserGlobalFilter.ORDER + 1;

    private final @NonNull GatewayConfigProperties config;

    /**
     * Ensures that this filter runs after the matched {@link Route} has been set as
     * an attribute in the {@link ServerWebExchange}.
     *
     * @return the execution order of this filter
     */
    @Override
    public int getOrder() {
        return ResolveTargetGlobalFilter.ORDER;
    }

    /**
     * Resolves the {@link GeorchestraTargetConfig} for the matched {@link Route}
     * and stores it in the request exchange attributes.
     *
     * @param exchange the current server exchange
     * @param chain    the gateway filter chain
     * @return a {@link Mono} that proceeds with the filter chain execution
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Route route = (Route) exchange.getAttributes().get(GATEWAY_ROUTE_ATTR);
        Objects.requireNonNull(route, "No route matched, filter should not be executed");

        GeorchestraTargetConfig targetConfig = resolveTarget(route);
        log.debug("Storing geOrchestra target config for Route {} request context", route.getId());
        GeorchestraTargetConfig.setTarget(exchange, targetConfig);
        return chain.filter(exchange);
    }

    /**
     * Resolves the {@link GeorchestraTargetConfig} for the given route by applying
     * the service-specific or global access rules and header mappings.
     *
     * @param route the matched route
     * @return a {@link GeorchestraTargetConfig} containing the relevant
     *         configuration
     */
    @VisibleForTesting
    @NonNull
    GeorchestraTargetConfig resolveTarget(@NonNull Route route) {
        GeorchestraTargetConfig target = new GeorchestraTargetConfig();

        Optional<Service> service = findService(route);
        setAccessRules(target, service);
        setHeaderMappings(target, service);

        return target;
    }

    /**
     * Determines the applicable access rules for the target configuration.
     * <p>
     * If the matched service defines access rules, they are applied; otherwise, the
     * global access rules from {@link GatewayConfigProperties} are used.
     * </p>
     *
     * @param target  the target configuration to update
     * @param service the matched service, if available
     */
    private void setAccessRules(GeorchestraTargetConfig target, Optional<Service> service) {
        List<RoleBasedAccessRule> globalAccessRules = config.getGlobalAccessRules();
        List<RoleBasedAccessRule> targetAccessRules = service.map(Service::getAccessRules).filter(Objects::nonNull)
                .filter(l -> !l.isEmpty()).orElse(globalAccessRules);

        target.accessRules(targetAccessRules);
    }

    /**
     * Determines the applicable HTTP header mappings for the target configuration.
     * <p>
     * If the matched service defines custom header mappings, they are merged with
     * the global default headers. Otherwise, only the global defaults are applied.
     * </p>
     *
     * @param target  the target configuration to update
     * @param service the matched service, if available
     */
    private void setHeaderMappings(GeorchestraTargetConfig target, Optional<Service> service) {
        HeaderMappings defaultHeaders = config.getDefaultHeaders();
        HeaderMappings mergedHeaders = service.flatMap(Service::headers)
                .map(serviceHeaders -> merge(defaultHeaders, serviceHeaders)).orElse(defaultHeaders);

        target.headers(mergedHeaders);
    }

    /**
     * Merges the default global headers with service-specific headers.
     *
     * @param defaults the global default headers
     * @param service  the service-specific headers
     * @return a merged {@link HeaderMappings} instance
     */
    private HeaderMappings merge(HeaderMappings defaults, HeaderMappings service) {
        return defaults.copy().merge(service);
    }

    /**
     * Finds the matching service definition for the given route.
     *
     * @param route the matched route
     * @return an {@link Optional} containing the matched {@link Service}, or empty
     *         if not found
     */
    private Optional<Service> findService(@NonNull Route route) {
        final URI routeURI = route.getUri();

        for (Service service : config.getServices().values()) {
            var serviceURI = service.getTarget();
            if (Objects.equals(routeURI, serviceURI)) {
                return Optional.of(service);
            }
        }
        return Optional.empty();
    }
}
