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

import java.util.function.BiFunction;

import org.georchestra.security.model.GeorchestraUser;
import org.springframework.core.Ordered;
import org.springframework.security.core.Authentication;

/**
 * Extension point to customize the {@link GeorchestraUser} after it has been
 * resolved from an authentication provider.
 * <p>
 * This interface allows modifying the {@link GeorchestraUser} instance based on
 * authentication details, such as applying additional role mappings, setting
 * organization attributes, or enriching the user with external metadata.
 * </p>
 * <p>
 * Implementations are executed in the order defined by {@link #getOrder()}.
 * </p>
 *
 * @see GeorchestraUserMapper
 * @see GeorchestraUserMapperExtension
 */
public interface GeorchestraUserCustomizerExtension
        extends Ordered, BiFunction<Authentication, GeorchestraUser, GeorchestraUser> {

    /**
     * Defines the execution order of this extension when multiple customizers are
     * available.
     *
     * @return the order in which this customizer is applied, defaults to {@code 0}.
     */
    default int getOrder() {
        return 0;
    }
}
