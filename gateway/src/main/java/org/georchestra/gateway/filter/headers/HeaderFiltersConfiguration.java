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

import java.util.List;
import java.util.function.BooleanSupplier;

import org.georchestra.gateway.filter.headers.providers.GeorchestraOrganizationHeadersContributor;
import org.georchestra.gateway.filter.headers.providers.GeorchestraUserHeadersContributor;
import org.georchestra.gateway.filter.headers.providers.JsonPayloadHeadersContributor;
import org.georchestra.gateway.filter.headers.providers.SecProxyHeaderContributor;
import org.georchestra.gateway.model.GatewayConfigProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link Configuration} for security-related header filters in the gateway.
 * <p>
 * This configuration defines various {@link GatewayFilterFactory} beans for
 * handling geOrchestra security headers in proxied requests.
 * </p>
 *
 * @see AddSecHeadersGatewayFilterFactory
 * @see RemoveHeadersGatewayFilterFactory
 * @see RemoveSecurityHeadersGatewayFilterFactory
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(GatewayConfigProperties.class)
public class HeaderFiltersConfiguration {

    /**
     * {@link GatewayFilterFactory} to append all necessary {@code sec-*} request
     * headers to proxied requests.
     *
     * @param providers the list of configured {@link HeaderContributor}s in the
     *                  {@link ApplicationContext}
     * @return the configured {@link AddSecHeadersGatewayFilterFactory}
     * @see #secProxyHeaderProvider(GatewayConfigProperties)
     * @see #userSecurityHeadersProvider()
     * @see #organizationSecurityHeadersProvider()
     * @see #jsonPayloadHeadersContributor()
     */
    @Bean
    AddSecHeadersGatewayFilterFactory addSecHeadersGatewayFilterFactory(List<HeaderContributor> providers) {
        return new AddSecHeadersGatewayFilterFactory(providers);
    }

    /**
     * {@link GatewayFilterFactory} that modifies the affinity of a cookie by
     * rewriting its path.
     *
     * @return the configured {@link CookieAffinityGatewayFilterFactory}
     */
    @Bean
    CookieAffinityGatewayFilterFactory cookieAffinityGatewayFilterFactory() {
        return new CookieAffinityGatewayFilterFactory();
    }

    /**
     * {@link HeaderContributor} that appends geOrchestra user-related {@code sec-*}
     * request headers.
     *
     * @return the configured {@link GeorchestraUserHeadersContributor}
     */
    @Bean
    GeorchestraUserHeadersContributor userSecurityHeadersProvider() {
        return new GeorchestraUserHeadersContributor();
    }

    /**
     * {@link HeaderContributor} that appends the {@code sec-proxy} request header
     * when enabled in the gateway configuration.
     *
     * @param configProps the gateway security configuration properties
     * @return the configured {@link SecProxyHeaderContributor}
     */
    @Bean
    SecProxyHeaderContributor secProxyHeaderProvider(GatewayConfigProperties configProps) {
        BooleanSupplier secProxyEnabledSupplier = () -> configProps.getDefaultHeaders().getProxy().orElse(false);
        return new SecProxyHeaderContributor(secProxyEnabledSupplier);
    }

    /**
     * {@link HeaderContributor} that appends geOrchestra organization-related
     * {@code sec-*} request headers.
     *
     * @return the configured {@link GeorchestraOrganizationHeadersContributor}
     */
    @Bean
    GeorchestraOrganizationHeadersContributor organizationSecurityHeadersProvider() {
        return new GeorchestraOrganizationHeadersContributor();
    }

    /**
     * {@link HeaderContributor} that appends {@code sec-user} and
     * {@code sec-organization} Base64-encoded JSON payloads.
     *
     * @return the configured {@link JsonPayloadHeadersContributor}
     */
    @Bean
    JsonPayloadHeadersContributor jsonPayloadHeadersContributor() {
        return new JsonPayloadHeadersContributor();
    }

    /**
     * General-purpose {@link GatewayFilterFactory} to remove incoming HTTP request
     * headers based on a Java regular expression.
     *
     * @return the configured {@link RemoveHeadersGatewayFilterFactory}
     */
    @Bean
    RemoveHeadersGatewayFilterFactory removeHeadersGatewayFilterFactory() {
        return new RemoveHeadersGatewayFilterFactory();
    }

    /**
     * {@link GatewayFilterFactory} to remove incoming {@code sec-*} HTTP request
     * headers to prevent impersonation from external sources.
     *
     * @return the configured {@link RemoveSecurityHeadersGatewayFilterFactory}
     */
    @Bean
    RemoveSecurityHeadersGatewayFilterFactory removeSecurityHeadersGatewayFilterFactory() {
        return new RemoveSecurityHeadersGatewayFilterFactory();
    }

}
