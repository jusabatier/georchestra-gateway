/*
 * (c) 2021 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.geoserver.cloud.gateway.filter;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.StripPrefixGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.StripPrefixGatewayFilterFactory.Config;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;

import lombok.Data;

/**
 * A {@link GatewayFilter} factory that strips a base path prefix from the
 * incoming request URI.
 * <p>
 * This filter is useful in scenarios where requests contain a base path that
 * needs to be removed for downstream processing. The base path is specified in
 * the {@link PrefixConfig} and must meet the following conditions:
 * <ul>
 * <li>The prefix must start with a '/' character.</li>
 * <li>The prefix must not end with a '/' unless it is exactly '/'.</li>
 * </ul>
 * <p>
 * This filter works by calculating how many segments of the URI need to be
 * removed based on the configured prefix. If the prefix is found in the request
 * URI, it is stripped before the request is forwarded.
 * <p>
 * For more details, see <a href=
 * "https://github.com/spring-cloud/spring-cloud-gateway/issues/1759">issue
 * #1759</a>
 */
public class StripBasePathGatewayFilterFactory
        extends AbstractGatewayFilterFactory<StripBasePathGatewayFilterFactory.PrefixConfig> {

    private StripPrefixGatewayFilterFactory stripPrefix = new StripPrefixGatewayFilterFactory();

    public StripBasePathGatewayFilterFactory() {
        super(PrefixConfig.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> shortcutFieldOrder() {
        return List.of("prefix");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GatewayFilter apply(PrefixConfig config) {
        config.checkPreconditions();
        return (exchange, chain) -> {
            final ServerHttpRequest request = exchange.getRequest();

            final String basePath = config.getPrefix();
            final String path = request.getURI().getRawPath();

            // Calculate how many parts of the path to strip based on the base path
            final int partsToRemove = resolvePartsToStrip(basePath, path);
            if (partsToRemove == 0) {
                return chain.filter(exchange); // No base path to strip, continue with the chain
            }

            // Create and apply the StripPrefix filter with the correct number of parts to
            // remove
            GatewayFilter stripFilter = stripPrefix.apply(newStripPrefixConfig(partsToRemove));
            return stripFilter.filter(exchange, chain);
        };
    }

    /**
     * Creates a new configuration for the {@link StripPrefixGatewayFilterFactory}
     * with the specified number of parts to remove from the URI.
     *
     * @param partsToRemove the number of URI path segments to strip
     * @return a new {@link Config} for the StripPrefix filter
     */
    private Config newStripPrefixConfig(int partsToRemove) {
        Config config = stripPrefix.newConfig();
        config.setParts(partsToRemove);
        return config;
    }

    /**
     * Resolves the number of URI path segments to strip based on the base path and
     * the incoming request URI.
     * 
     * @param basePath    the base path to strip
     * @param requestPath the incoming request path
     * @return the number of path segments to strip
     */
    private int resolvePartsToStrip(String basePath, String requestPath) {
        if (null == basePath) {
            return 0; // No prefix to strip
        }
        if (!requestPath.startsWith(basePath)) {
            return 0; // Base path is not part of the request URI
        }

        final int basePathSteps = StringUtils.countOccurrencesOf(basePath, "/");
        boolean isRoot = basePath.equals(requestPath);
        return isRoot ? basePathSteps - 1 : basePathSteps; // Calculate how many parts to remove
    }

    /**
     * Configuration class for the {@link StripBasePathGatewayFilterFactory}.
     * <p>
     * Defines the prefix to be stripped from the incoming URI. The prefix must meet
     * specific constraints as follows:
     * <ul>
     * <li>It must start with '/'.</li>
     * <li>If it is not '/', it must not end with '/'.</li>
     * </ul>
     */
    @Data
    public static class PrefixConfig {

        private String prefix;

        /**
         * Validates the preconditions for the {@link PrefixConfig}.
         * <p>
         * Ensures that the prefix:
         * <ul>
         * <li>Starts with '/'.</li>
         * <li>If not '/', does not end with '/'.</li>
         * </ul>
         */
        public void checkPreconditions() {
            final String prefix = getPrefix();

            // Ensure the prefix is valid
            if (prefix != null) {
                checkArgument(prefix.startsWith("/"), "StripBasePath prefix must start with /");
                checkArgument("/".equals(prefix) || !prefix.endsWith("/"), "StripBasePath prefix must not end with /");
            }
        }
    }
}
