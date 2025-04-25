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

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.georchestra.gateway.logging.mdc.config.AuthenticationMdcConfigProperties;
import org.georchestra.gateway.logging.mdc.webflux.ReactorContextHolder;
import org.georchestra.gateway.model.*;
import org.georchestra.gateway.security.ResolveGeorchestraUserGlobalFilter;
import org.georchestra.security.model.GeorchestraUser;
import org.georchestra.security.model.Organization;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * A {@link GlobalFilter} that adds user and org-related MDC (Mapping Diagnostic Context) if respectively
 * logging.mdc.include.user.id = true
 * and
 * logging.mdc.include.user.org= true
 * <p>
 * This filter executes after user and org resolution in
 * {@link ResolveGeorchestraUserGlobalFilter}.
 * </p>
 */
@RequiredArgsConstructor
@Slf4j
public class MdcUserAndOrgGlobalFilter implements GlobalFilter, Ordered {

    /**
     * The execution order of this filter, ensuring it runs after user/org
     * resolution
     */
    public static final int ORDER = ResolveGeorchestraUserGlobalFilter.ORDER + 2;

    private final @NonNull AuthenticationMdcConfigProperties authConfig;

    public static final String MDC_CONTEXT_KEY = "MDC";

    /**
     * Ensures that this filter runs after the matched {@link Route} has been set as
     * an attribute in the {@link ServerWebExchange}.
     *
     * @return the execution order of this filter
     */
    @Override
    public int getOrder() {
        return MdcUserAndOrgGlobalFilter.ORDER;
    }

    /**
     * Adds some MDC related to user and org auth information, if enabled.
     *
     * @param exchange the current server exchange
     * @param chain    the gateway filter chain
     * @return a {@link Mono} that proceeds with the filter chain execution
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.debug("Activated GeorchestraUserMdcGlobalFilter");
        return chain.filter(exchange).contextWrite(context -> {
            Map<String, String> mdcMap = context.getOrEmpty(ReactorContextHolder.MDC_CONTEXT_KEY)
                    .map(map -> new HashMap<>((Map<String, String>) map)).orElseGet(HashMap::new);
            if (authConfig.isOrg()) {
                // Add custom MDC attributes
                Optional<Organization> opt_org = GeorchestraOrganizations.resolve(exchange);
                opt_org.ifPresent(org -> {
                    mdcMap.put("enduser.org.uuid", org.getId());
                    mdcMap.put("enduser.org.id", org.getShortName());
                    if (authConfig.isExtras()) {
                        mdcMap.put("enduser.org.fullname", org.getName());
                    }
                });
            }
            if (authConfig.isId()) {
                // Add custom MDC attributes
                Optional<GeorchestraUser> opt_user = GeorchestraUsers.resolve(exchange);
                opt_user.ifPresent(user -> {
                    mdcMap.put("enduser.uuid", user.getId());
                    if (authConfig.isExtras()) {
                        mdcMap.put("enduser.firstname", user.getFirstName());
                        mdcMap.put("enduser.lastname", user.getLastName());
                    }
                });
            }
            return context.put(ReactorContextHolder.MDC_CONTEXT_KEY, mdcMap);
        });
    }
}
