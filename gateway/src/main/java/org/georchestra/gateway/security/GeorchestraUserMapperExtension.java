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

package org.georchestra.gateway.security;

import java.util.Optional;

import org.georchestra.security.model.GeorchestraUser;
import org.springframework.core.Ordered;
import org.springframework.security.core.Authentication;

/**
 * Extension point to decouple the authentication origin from the logic to
 * convey geOrchestra-specific HTTP security request headers to back-end
 * services.
 * <p>
 * Beans of this type will be asked by {@link GeorchestraUserMapper} to obtain a
 * {@link GeorchestraUser} from the current request authentication token. An
 * instance that knows how to perform such mapping based on the kind of
 * authentication represented by the token shall return a non-empty user.
 */
public interface GeorchestraUserMapperExtension extends Ordered {

    /**
     * @return the mapped {@link GeorchestraUser} based on the provided auth token,
     *         or {@link Optional#empty()} if this instance can't perform such
     *         mapping.
     */
    Optional<GeorchestraUser> resolve(Authentication authToken);

    default int getOrder() {
        return 0;
    }
}
