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
package org.georchestra.gateway.logging.mdc.config;

import java.util.Optional;

import org.slf4j.MDC;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.env.Environment;

import lombok.Data;

/**
 * Configuration properties for controlling which Spring Environment information
 * is included in the MDC.
 * <p>
 * These properties determine what application and environment information is
 * added to the MDC (Mapped Diagnostic Context) during request processing.
 * Including this information in the MDC makes it available to all logging
 * statements, providing valuable context for debugging, monitoring, and audit
 * purposes.
 * <p>
 * The properties are configured using the prefix
 * {@code logging.mdc.include.app} in the application properties or YAML files.
 * <p>
 * Example configuration in YAML:
 * 
 * <pre>
 * logging:
 *   mdc:
 *     include:
 *       app:
 *         name: true
 *         profile: true
 *         version: true
 *         instance-id: true
 * </pre>
 */
@Data
@ConfigurationProperties(prefix = "logging.mdc.include.app")
public class SpringEnvironmentMdcConfigProperties {

    /** Whether to append the application.name MDC property. */
    private boolean name = true;

    /** Whether to append the application.profile (active profiles) MDC property. */
    private boolean profile = true;

    /**
     * Whether to append the application.instance-id MDC property, useful in
     * environments with multiple instances of the same application.
     */
    private boolean instanceId = false;

    /** Whether to append the application.version MDC property. */
    private boolean version = true;

    /**
     * Adds application environment properties to the MDC based on configuration.
     * <p>
     * This method extracts information from the Spring Environment and
     * BuildProperties and adds it to the MDC if enabled by configuration.
     * Information that can be added includes:
     * <ul>
     * <li>Application name</li>
     * <li>Application version</li>
     * <li>Active profiles</li>
     * <li>Instance ID</li>
     * </ul>
     *
     * @param env             the Spring Environment
     * @param buildProperties optional BuildProperties that may contain application
     *                        info
     */
    public void addEnvironmentProperties(Environment env, Optional<BuildProperties> buildProperties) {
        if (env != null) {
            if (isName()) {
                String appName = env.getProperty("spring.application.name", "unknown-application-name");
                MDC.put("application.name", appName);
            }
            if (isProfile()) {
                String profiles = String.join(",", env.getActiveProfiles());
                if (profiles.isEmpty()) {
                    profiles = String.join(",", env.getDefaultProfiles());
                }
                MDC.put("application.profile", profiles);
            }

            if (isInstanceId()) {
                MDC.put("application.instance-id", env.getProperty("spring.application.instance-id", "null"));
            }
        }

        if (buildProperties.isPresent()) {
            BuildProperties props = buildProperties.orElseThrow();
            String ver = props.getVersion();
            if (isVersion() && ver != null) {
                MDC.put("application.version", ver);
            }
        }
    }
}