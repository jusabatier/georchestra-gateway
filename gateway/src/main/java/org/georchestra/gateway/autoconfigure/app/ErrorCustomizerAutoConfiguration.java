/*
 * Copyright (C) 2023 by the geOrchestra PSC
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
package org.georchestra.gateway.autoconfigure.app;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for customizing error handling in geOrchestra Gateway
 * <p>
 * This configuration replaces the default error attributes provided by
 * {@link ErrorWebFluxAutoConfiguration} with {@link CustomErrorAttributes},
 * which modifies error responses for specific exceptions.
 * </p>
 *
 * <p>
 * This ensures that certain network-related failures (e.g., DNS resolution
 * errors) return an HTTP 503 (Service Unavailable) instead of HTTP 500.
 * </p>
 *
 * <p>
 * The customized error attributes are injected into the
 * {@link ErrorWebExceptionHandler}, affecting how errors are represented in the
 * application's responses.
 * </p>
 *
 * @see CustomErrorAttributes
 * @see ErrorWebFluxAutoConfiguration
 * @see ErrorWebExceptionHandler
 */
@AutoConfiguration(before = ErrorWebFluxAutoConfiguration.class)
public class ErrorCustomizerAutoConfiguration {

    /**
     * Registers {@link CustomErrorAttributes} to override default error handling
     * <p>
     * This bean ensures that network-related exceptions and access control errors
     * are properly mapped to HTTP 503 and HTTP 403 respectively.
     * </p>
     *
     * @return an instance of {@link CustomErrorAttributes} for handling errors
     */
    @Bean
    CustomErrorAttributes customErrorAttributes() {
        return new CustomErrorAttributes();
    }
}
