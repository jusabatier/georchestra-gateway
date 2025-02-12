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
package org.georchestra.gateway.app;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

/**
 * Controller that provides the geOrchestra UI style configuration.
 * <p>
 * This controller exposes an endpoint that returns style-related settings, such
 * as the stylesheet URL and logo URL, used for customizing the user interface.
 * The values are loaded from the geOrchestra data directory's
 * {@code default.properties}.
 * </p>
 */
@RestController
public class StyleConfigController {

    /** The URL of the custom stylesheet for geOrchestra, if defined. */
    private final String georchestraStylesheet;

    /** The URL of the logo used in geOrchestra, if defined. */
    private final String logoUrl;

    /**
     * Constructs a {@code StyleConfigController} with style configuration values.
     *
     * @param georchestraStylesheet the URL of the geOrchestra stylesheet, usually
     *                              loaded from {@code default.properties}
     * @param logoUrl               the URL of the geOrchestra logo, usually loaded
     *                              from {@code default.properties}
     */
    public StyleConfigController(@Value("${georchestraStylesheet:}") String georchestraStylesheet,
            @Value("${logoUrl:}") String logoUrl) {
        this.georchestraStylesheet = georchestraStylesheet;
        this.logoUrl = logoUrl;
    }

    /**
     * Provides the geOrchestra UI style configuration as a JSON object.
     * <p>
     * This endpoint returns a JSON response containing the configured stylesheet
     * URL and logo URL.
     * </p>
     *
     * <p>
     * <b>Example Response:</b>
     * </p>
     *
     * <pre>
     * {
     *   "stylesheet": "https://example.com/custom.css",
     *   "logo": "https://example.com/logo.png"
     * }
     * </pre>
     *
     * @return a reactive {@link Mono} containing a map with style configuration
     *         properties
     */
    @GetMapping(path = "/style-config", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> styleConfig() {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("stylesheet", georchestraStylesheet);
        config.put("logo", logoUrl);
        return Mono.just(config);
    }
}