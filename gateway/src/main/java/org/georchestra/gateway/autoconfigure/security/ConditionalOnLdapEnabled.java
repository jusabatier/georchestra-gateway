/*
 * Copyright (C) 2022 by the geOrchestra PSC
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
package org.georchestra.gateway.autoconfigure.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Conditional;
import org.springframework.ldap.core.LdapTemplate;

/**
 * Condition that enables a bean if LDAP support is available and at least one
 * LDAP data source is enabled.
 * <p>
 * This annotation ensures that a component is only loaded when:
 * <ul>
 * <li>The {@link LdapTemplate} class is present on the classpath.</li>
 * <li>At least one LDAP configuration is enabled, as determined by
 * {@link AtLeastOneLdapDatasourceEnabledCondition}.</li>
 * </ul>
 * </p>
 *
 * @see AtLeastOneLdapDatasourceEnabledCondition
 * @see LdapTemplate
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConditionalOnClass(LdapTemplate.class)
@Conditional(AtLeastOneLdapDatasourceEnabledCondition.class)
public @interface ConditionalOnLdapEnabled {

}
