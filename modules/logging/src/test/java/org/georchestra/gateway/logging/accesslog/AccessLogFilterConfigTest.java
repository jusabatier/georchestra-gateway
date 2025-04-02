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
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.georchestra.gateway.logging.accesslog;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccessLogFilterConfigTest {

    private AccessLogFilterConfig config;

    @BeforeEach
    void setUp() {
        config = new AccessLogFilterConfig();
    }

    @Test
    void shouldLogReturnsFalseWhenURIIsNull() {
        // Execute
        boolean shouldLog = config.shouldLog(null);

        // Verify
        assertThat(shouldLog).isFalse();
    }

    @Test
    void shouldLogReturnsFalseWhenNoPatternsDefined() throws Exception {
        // Setup - no patterns added to config
        URI uri = new URI("http://example.com/any/path");

        // Execute
        boolean shouldLog = config.shouldLog(uri);

        // Verify
        assertThat(shouldLog).isFalse();
    }

    @Test
    void shouldLogReturnsTrueWhenURIMatchesInfoPattern() throws Exception {
        // Setup
        config.setInfo(createPatterns(".*\\/api\\/.*"));
        URI uri = new URI("http://example.com/api/users");

        // Execute
        boolean shouldLog = config.shouldLog(uri);

        // Verify
        assertThat(shouldLog).isTrue();
    }

    @Test
    void shouldLogReturnsTrueWhenURIMatchesDebugPattern() throws Exception {
        // Setup
        config.setDebug(createPatterns(".*\\/admin\\/.*"));
        URI uri = new URI("http://example.com/admin/users");

        // Execute
        boolean shouldLog = config.shouldLog(uri);

        // Verify
        assertThat(shouldLog).isTrue();
    }

    @Test
    void shouldLogReturnsTrueWhenURIMatchesTracePattern() throws Exception {
        // Setup
        config.setTrace(createPatterns(".*\\/debug\\/.*"));
        URI uri = new URI("http://example.com/debug/test");

        // Execute
        boolean shouldLog = config.shouldLog(uri);

        // Verify
        assertThat(shouldLog).isTrue();
    }

    @Test
    void shouldLogReturnsFalseWhenURIDoesNotMatchAnyPattern() throws Exception {
        // Setup
        config.setInfo(createPatterns(".*\\/api\\/.*"));
        config.setDebug(createPatterns(".*\\/admin\\/.*"));
        config.setTrace(createPatterns(".*\\/debug\\/.*"));
        URI uri = new URI("http://example.com/other/path");

        // Execute
        boolean shouldLog = config.shouldLog(uri);

        // Verify
        assertThat(shouldLog).isFalse();
    }

    private List<Pattern> createPatterns(String... patterns) {
        return Arrays.stream(patterns).map(Pattern::compile).collect(Collectors.toList());
    }
}