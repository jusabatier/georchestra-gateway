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

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.georchestra.gateway.security.GeorchestraGatewaySecurityConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.util.unit.DataSize;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties(GeorchestraGatewaySecurityConfigProperties.class)
public class GeorchestraGatewayApplication {

    private @Autowired RouteLocator routeLocator;
    private @Value("${spring.messages.basename:}") String messagesBasename;

    public static void main(String[] args) {
        SpringApplication.run(GeorchestraGatewayApplication.class, args);
    }

    /**
     * REVISIT: why do we need to define this bean in the Application class and not
     * in a configuration that depends on whether rabbit is enabled?
     */
    @Bean
    MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames(("classpath:messages/login," + messagesBasename).split(","));
        messageSource.setCacheSeconds(600);
        messageSource.setUseCodeAsDefaultMessage(true);
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        return messageSource;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent e) {
        Environment env = e.getApplicationContext().getEnvironment();
        String datadir = env.getProperty("georchestra.datadir");
        if (null != datadir) {
            datadir = new File(datadir).getAbsolutePath();
        }
        String app = env.getProperty("spring.application.name");
        String instanceId = env.getProperty("info.instance-id");
        int cpus = Runtime.getRuntime().availableProcessors();
        String maxMem = getMaxMem();

        Long routeCount = routeLocator.getRoutes().count().block();
        log.info("{} ready. Data dir: {}. Routes: {}. Instance-id: {}, cpus: {}, max memory: {}", app, datadir,
                routeCount, instanceId, cpus, maxMem);
    }

    private String getMaxMem() {
        String maxMem;
        DataSize maxMemBytes = DataSize.ofBytes(Runtime.getRuntime().maxMemory());
        double value = maxMemBytes.toKilobytes() / 1024d;
        String unit = "MB";
        if (maxMemBytes.toGigabytes() > 0) {
            value = value / 1024d;
            unit = "GB";
        }
        maxMem = String.format("%.2f %s", value, unit);
        return maxMem;
    }
}
