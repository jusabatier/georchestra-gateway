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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.georchestra.ds.orgs.Org;
import org.georchestra.ds.security.OrganizationMapper;
import org.georchestra.security.model.Organization;
import org.junit.jupiter.api.Test;

class CitiesAwareOrganizationMapperTest {

    @Test
    void mapsCitiesToCommaSeparatedDescription() {
        Org source = new Org();
        source.setCities(List.of("43001", "43002", "43003"));
        Organization target = new Organization();
        target.setDescription("Organization description");
        OrganizationMapper delegate = ignored -> target;

        Organization result = new CitiesAwareOrganizationMapper(delegate).map(source);

        assertThat(result).isSameAs(target);
        assertThat(result.getDescription()).isEqualTo("43001,43002,43003");
    }

    @Test
    void preservesMappedDescriptionWhenCitiesAreEmpty() {
        Org source = new Org();
        source.setCities(List.of());
        Organization target = new Organization();
        target.setDescription("Organization description");
        OrganizationMapper delegate = ignored -> target;

        Organization result = new CitiesAwareOrganizationMapper(delegate).map(source);

        assertThat(result).isSameAs(target);
        assertThat(result.getDescription()).isEqualTo("Organization description");
    }
}
