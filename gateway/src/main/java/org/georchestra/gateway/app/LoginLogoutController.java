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

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.tuple.Pair;
import org.georchestra.gateway.security.GeorchestraGatewaySecurityConfigProperties;
import org.georchestra.gateway.security.GeorchestraGatewaySecurityConfigProperties.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginLogoutController {

    private @Autowired(required = false) GeorchestraGatewaySecurityConfigProperties georchestraGatewaySecurityConfigProperties;

    private boolean ldapEnabled;

    private @Autowired(required = false) OAuth2ClientProperties oauth2ClientConfig;
    private @Value("${georchestra.gateway.headerEnabled:true}") boolean headerEnabled;

    // defined in georchestra datadir's default.properties
    private @Value("${georchestraStylesheet:}") String georchestraStylesheet;
    private @Value("${useLegacyHeader:false}") boolean useLegacyHeader;
    private @Value("${headerUrl:/header/}") String headerUrl;
    private @Value("${headerConfigFile:}") String headerConfigFile;
    private @Value("${headerHeight:80}") int headerHeight;
    private @Value("${logoUrl:}") String logoUrl;
    private @Value("${headerScript:https://cdn.jsdelivr.net/gh/georchestra/header@dist/header.js}") String headerScript;

    @PostConstruct
    void initialize() {
        if (georchestraGatewaySecurityConfigProperties != null) {
            ldapEnabled = georchestraGatewaySecurityConfigProperties.getLdap().values().stream()
                    .anyMatch((Server::isEnabled));
        }
    }

    @GetMapping(path = "/logout")
    public String logout(Model mdl) {
        setHeaderAttributes(mdl);
        return "logout";
    }

    @GetMapping(path = "/login")
    public String loginPage(@RequestParam Map<String, String> allRequestParams, Model mdl) {
        Map<String, Pair<String, String>> oauth2LoginLinks = new HashMap<>();
        if (oauth2ClientConfig != null) {
            oauth2ClientConfig.getRegistration().forEach((k, v) -> {
                String clientName = Optional.ofNullable(v.getClientName()).orElse(k);

                String providerPath = Paths.get("login/img/", k + ".png").toString();
                String logo = new ClassPathResource("static/" + providerPath).exists() ? providerPath
                        : "login/img/default.png";
                oauth2LoginLinks.put("/oauth2/authorization/" + k, Pair.of(clientName, logo));
            });
        }

        if (oauth2LoginLinks.size() == 1 && !ldapEnabled) {
            return "redirect:" + oauth2LoginLinks.keySet().stream().findFirst().orElseThrow();
        }

        setHeaderAttributes(mdl);
        mdl.addAttribute("ldapEnabled", ldapEnabled);
        mdl.addAttribute("oauth2LoginLinks", oauth2LoginLinks);
        boolean expired = "expired_password".equals(allRequestParams.get("error"));
        mdl.addAttribute("passwordExpired", expired);
        boolean invalidCredentials = "invalid_credentials".equals(allRequestParams.get("error"));
        mdl.addAttribute("invalidCredentials", invalidCredentials);
        boolean duplicateAccount = "duplicate_account".equals(allRequestParams.get("error"));
        mdl.addAttribute("duplicateAccount", duplicateAccount);
        return "login";
    }

    private void setHeaderAttributes(Model mdl) {
        mdl.addAttribute("georchestraStylesheet", georchestraStylesheet);
        mdl.addAttribute("useLegacyHeader", useLegacyHeader);
        mdl.addAttribute("headerUrl", headerUrl);
        mdl.addAttribute("headerHeight", headerHeight);
        mdl.addAttribute("logoUrl", logoUrl);
        mdl.addAttribute("headerConfigFile", headerConfigFile);
        mdl.addAttribute("headerEnabled", headerEnabled);
        mdl.addAttribute("headerScript", headerScript);
    }
}
