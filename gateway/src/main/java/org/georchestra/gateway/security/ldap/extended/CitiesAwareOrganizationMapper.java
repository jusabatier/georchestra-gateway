/*
 * Copyright (C) 2026 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra. If not, see <http://www.gnu.org/licenses/>.
 */
package org.georchestra.gateway.security.ldap.extended;

import java.util.List;
import java.util.Objects;

import org.georchestra.ds.orgs.Org;
import org.georchestra.ds.security.OrganizationMapper;
import org.georchestra.security.model.Organization;

/**
 * Preserves LDAP organization cities when mapping to the security model.
 * <p>
 * The security {@link Organization} model has no cities property. For
 * compatibility with consumers of {@code sec-organization}, non-empty city
 * identifiers are exposed as a comma-separated description.
 * </p>
 */
final class CitiesAwareOrganizationMapper implements OrganizationMapper {

    private final OrganizationMapper delegate;

    CitiesAwareOrganizationMapper(OrganizationMapper delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public Organization map(Org source) {
        Organization target = delegate.map(source);
        if (source != null && target != null) {
            List<String> cities = source.getCities();
            if (cities != null && !cities.isEmpty()) {
                target.setDescription(String.join(",", cities));
            }
        }
        return target;
    }
}
