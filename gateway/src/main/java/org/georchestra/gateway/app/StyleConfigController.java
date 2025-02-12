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
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.georchestra.gateway.app;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
public class StyleConfigController {

    private String georchestraStylesheet;
    private String logoUrl;

    /**
     * @param georchestraStylesheet defined in georchestra datadir's
     *                              default.properties
     * @param logoUrl               defined in georchestra datadir's
     *                              default.properties
     */
    public StyleConfigController(@Value("${georchestraStylesheet:}") String georchestraStylesheet,
            @Value("${logoUrl:}") String logoUrl) {
        this.georchestraStylesheet = georchestraStylesheet;
        this.logoUrl = logoUrl;
    }

    @GetMapping(path = "/style-config", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, Object>> styleConfig() {

        Map<String, Object> ret = new LinkedHashMap<>();
        ret.put("stylesheet", georchestraStylesheet);
        ret.put("logo", logoUrl);
        return Mono.just(ret);

    }
}
