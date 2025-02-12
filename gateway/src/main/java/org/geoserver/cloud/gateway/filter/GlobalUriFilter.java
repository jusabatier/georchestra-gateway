/*
 * (c) 2022 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.gateway.filter;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

import java.net.URI;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * Global filter to correct double-encoded URLs in Spring Cloud Gateway.
 * <p>
 * This filter addresses issue <a href=
 * "https://github.com/spring-cloud/spring-cloud-gateway/issues/2065">#2065</a>,
 * where request URIs may be unintentionally double-encoded during request
 * processing.
 * <p>
 * When a request URI is already encoded (i.e., it contains percent-encoded
 * characters), this filter ensures that it is not further re-encoded by the
 * {@link ReactiveLoadBalancerClientFilter}.
 * <p>
 * The filter does the following:
 * <ol>
 * <li>Checks if the incoming request URI is already encoded.</li>
 * <li>Retrieves the original {@link Route} for the request.</li>
 * <li>Overrides the incorrectly re-encoded URI by merging the original request
 * URI with the target load-balanced service URL.</li>
 * </ol>
 * 
 * @see ReactiveLoadBalancerClientFilter
 */
@Component
public class GlobalUriFilter implements GlobalFilter, Ordered {

    /**
     * Intercepts requests to check for double-encoded URIs and fixes them before
     * further processing.
     *
     * @param exchange the {@link ServerWebExchange} containing request and response
     *                 details
     * @param chain    the {@link GatewayFilterChain} to continue request processing
     * @return a {@link Mono} indicating when request processing is complete
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        URI incomingUri = exchange.getRequest().getURI();
        if (isUriEncoded(incomingUri)) {
            // Retrieve the route associated with the request
            Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
            if (route == null) {
                return chain.filter(exchange);
            }

            // Retrieve the load-balanced service URI (computed by
            // ReactiveLoadBalancerClientFilter)
            URI balanceUrl = exchange.getRequiredAttribute(GATEWAY_REQUEST_URL_ATTR);

            // Construct the corrected URI to prevent double encoding
            URI mergedUri = createUri(incomingUri, balanceUrl);

            // Override the wrongly encoded URI in the exchange attributes
            exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, mergedUri);
        }

        return chain.filter(exchange);
    }

    /**
     * Creates a correctly formatted URI by merging the incoming request URI with
     * the load-balanced service URL.
     * <p>
     * This method ensures that the original request's query parameters and path
     * remain intact while applying the proper host and scheme from the load
     * balancer.
     *
     * @param incomingUri the original request URI
     * @param balanceUrl  the load-balanced target service URI
     * @return a corrected {@link URI} with proper encoding and formatting
     */
    private URI createUri(URI incomingUri, URI balanceUrl) {
        final var port = balanceUrl.getPort() != -1 ? ":" + balanceUrl.getPort() : "";
        final var rawPath = balanceUrl.getRawPath() != null ? balanceUrl.getRawPath() : "";
        final var query = incomingUri.getRawQuery() != null ? "?" + incomingUri.getRawQuery() : "";
        return URI.create(balanceUrl.getScheme() + "://" + balanceUrl.getHost() + port + rawPath + query);
    }

    /**
     * Checks if a URI is already encoded by looking for percent-encoded characters
     * in the path or query.
     *
     * @param uri the {@link URI} to check
     * @return {@code true} if the URI contains percent-encoded characters,
     *         otherwise {@code false}
     */
    private static boolean isUriEncoded(URI uri) {
        return (uri.getRawQuery() != null && uri.getRawQuery().contains("%"))
                || (uri.getRawPath() != null && uri.getRawPath().contains("%"));
    }

    /**
     * Defines the order of execution for this filter.
     * <p>
     * This filter runs immediately after the
     * {@link ReactiveLoadBalancerClientFilter} to correct double-encoded URIs
     * before they are processed further.
     *
     * @return the filter execution order, which is one position after
     *         {@link ReactiveLoadBalancerClientFilter#LOAD_BALANCER_CLIENT_FILTER_ORDER}
     */
    @Override
    public int getOrder() {
        return ReactiveLoadBalancerClientFilter.LOAD_BALANCER_CLIENT_FILTER_ORDER + 1;
    }
}
