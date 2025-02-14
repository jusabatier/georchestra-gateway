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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.georchestra.gateway.security.GeorchestraGatewaySecurityConfigProperties;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionMessage.ItemsBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.type.AnnotatedTypeMetadata;

import com.google.common.collect.Streams;

/**
 * {@link Condition} that matches if at least one LDAP configuration is enabled.
 * <p>
 * This condition checks the externalized configuration properties:
 * {@code georchestra.gateway.security.ldap.<configName>.enabled}. If at least
 * one LDAP configuration is enabled, the condition evaluates to {@code true}.
 * </p>
 *
 * @see GeorchestraGatewaySecurityConfigProperties
 */
class AtLeastOneLdapDatasourceEnabledCondition extends SpringBootCondition {

    /**
     * Determines whether the condition matches based on the presence of at least
     * one enabled LDAP data source.
     *
     * @param context  the condition evaluation context
     * @param metadata the metadata of the annotated component
     * @return a {@link ConditionOutcome} indicating whether the condition matches
     */
    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Set<String> enabledDatasourceNames = findEnabled(context);
        boolean anyEnabled = !enabledDatasourceNames.isEmpty();

        if (anyEnabled) {
            return ConditionOutcome.match();
        }

        ItemsBuilder itemsBuilder = ConditionMessage.forCondition(ConditionalOnLdapEnabled.class)
                .didNotFind("any enabled LDAP configuration");

        ConditionMessage message = enabledDatasourceNames.isEmpty() ? itemsBuilder.atAll()
                : itemsBuilder.items(enabledDatasourceNames);

        return ConditionOutcome.noMatch(message);
    }

    /**
     * Finds enabled LDAP configurations based on environment properties.
     *
     * @param context the condition evaluation context
     * @return a set of enabled LDAP data source names
     */
    static Set<String> findEnabled(ConditionContext context) {
        Environment environment = context.getEnvironment();
        MutablePropertySources propertySources = ((AbstractEnvironment) environment).getPropertySources();

        final String regex = "georchestra\\.gateway\\.security\\.ldap\\.(.*)\\.enabled";
        final Pattern pattern = Pattern.compile(regex);

        List<String> propertyNames = findMatchingPropertyNames(propertySources, regex);
        Set<String> names = new HashSet<>();
        for (String property : propertyNames) {
            String value = environment.getProperty(property);
            if (Boolean.parseBoolean(value)) {
                Matcher matcher = pattern.matcher(property);
                if (matcher.matches()) {
                    names.add(matcher.group(1));
                }
            }
        }
        return names;
    }

    /**
     * Retrieves property names that match the specified regular expression.
     *
     * @param propertySources the available property sources
     * @param regex           the regular expression to match property names
     * @return a list of matching property names
     */
    static List<String> findMatchingPropertyNames(MutablePropertySources propertySources, final String regex) {
        final Pattern pattern = Pattern.compile(regex);
        final Predicate<String> filter = pattern.asMatchPredicate();
        return Streams.stream(propertySources).filter(EnumerablePropertySource.class::isInstance)
                .map(EnumerablePropertySource.class::cast).map(EnumerablePropertySource::getPropertyNames)
                .flatMap(Stream::of).filter(filter).toList();
    }
}
