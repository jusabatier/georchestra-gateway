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

package org.georchestra.gateway.autoconfigure.accounts;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.georchestra.gateway.autoconfigure.security.ConditionalOnLdapEnabled;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * Meta-annotation that enables a bean only if the default geOrchestra LDAP
 * integration is enabled.
 * <p>
 * This annotation is used to conditionally activate beans when both of the
 * following conditions are met:
 * <ul>
 * <li>LDAP is enabled for geOrchestra (via
 * {@link ConditionalOnLdapEnabled}).</li>
 * <li>The property {@code georchestra.gateway.security.ldap.default.enabled} is
 * set to {@code true}.</li>
 * </ul>
 * </p>
 *
 * <p>
 * If the property is missing or explicitly set to {@code false}, the annotated
 * bean will not be created.
 * </p>
 *
 * @see ConditionalOnLdapEnabled
 * @see ConditionalOnProperty
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConditionalOnLdapEnabled
@ConditionalOnProperty(name = "georchestra.gateway.security.ldap.default.enabled", havingValue = "true", matchIfMissing = false)
public @interface ConditionalOnDefaultGeorchestraLdapEnabled {

}
