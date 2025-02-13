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

import java.util.List;
import java.util.Optional;

import org.georchestra.gateway.security.exceptions.DuplicatedEmailFoundException;
import org.georchestra.security.model.GeorchestraUser;
import org.springframework.security.core.Authentication;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Resolves a {@link GeorchestraUser} from an {@link Authentication} token by
 * delegating to available {@link GeorchestraUserMapperExtension}
 * implementations.
 * <p>
 * This class acts as an abstraction layer that allows multiple authentication
 * strategies to provide user resolution mechanisms, such as LDAP, OAuth2, or
 * custom authentication providers.
 * </p>
 * <p>
 * Once a user is successfully resolved, any registered
 * {@link GeorchestraUserCustomizerExtension} implementations are applied in
 * order to modify or enrich the user attributes.
 * </p>
 * <p>
 * This component is primarily used by
 * {@link ResolveGeorchestraUserGlobalFilter} to extract user details from
 * authentication tokens in the request lifecycle.
 * </p>
 * 
 * @see GeorchestraUserMapperExtension
 * @see GeorchestraUserCustomizerExtension
 * @see ResolveGeorchestraUserGlobalFilter
 */
@RequiredArgsConstructor
public class GeorchestraUserMapper {

    /**
     * Ordered list of user mapper extensions responsible for resolving a
     * {@link GeorchestraUser} from an {@link Authentication} token.
     */
    private final @NonNull List<GeorchestraUserMapperExtension> resolvers;

    /**
     * Ordered list of user customizer extensions that apply modifications to a
     * resolved {@link GeorchestraUser}.
     */
    private final @NonNull List<GeorchestraUserCustomizerExtension> customizers;

    /**
     * Default constructor for use when no resolvers or customizers are provided.
     */
    GeorchestraUserMapper() {
        this(List.of(), List.of());
    }

    /**
     * Constructor for initializing only with user resolvers.
     * 
     * @param resolvers the list of {@link GeorchestraUserMapperExtension} instances
     */
    GeorchestraUserMapper(List<GeorchestraUserMapperExtension> resolvers) {
        this(resolvers, List.of());
    }

    /**
     * Attempts to resolve a {@link GeorchestraUser} from the provided
     * authentication token.
     * <p>
     * Each {@link GeorchestraUserMapperExtension} is queried in order until one
     * successfully resolves a user. If no extension handles the authentication
     * token, an empty result is returned.
     * </p>
     * <p>
     * If a user is resolved, it is then processed through all registered
     * {@link GeorchestraUserCustomizerExtension} instances in order.
     * </p>
     * 
     * @param authToken the authentication token to resolve
     * @return an optional {@link GeorchestraUser} if resolution is successful
     * @throws DuplicatedEmailFoundException if multiple users with the same email
     *                                       are found
     */
    public Optional<GeorchestraUser> resolve(@NonNull Authentication authToken) throws DuplicatedEmailFoundException {
        return resolvers.stream().map(resolver -> resolver.resolve(authToken)).filter(Optional::isPresent)
                .map(Optional::orElseThrow).map(mapped -> customize(authToken, mapped)).findFirst();
    }

    /**
     * Applies registered {@link GeorchestraUserCustomizerExtension} instances to
     * the resolved user.
     * <p>
     * This allows for modifications such as role mappings, attribute enrichment, or
     * other custom transformations based on the authentication context.
     * </p>
     * 
     * @param authToken the authentication token associated with the user
     * @param mapped    the resolved {@link GeorchestraUser} instance
     * @return the customized {@link GeorchestraUser} after all modifications are
     *         applied
     * @throws DuplicatedEmailFoundException if an issue occurs during customization
     */
    private GeorchestraUser customize(@NonNull Authentication authToken, GeorchestraUser mapped)
            throws DuplicatedEmailFoundException {
        GeorchestraUser customized = mapped;
        for (GeorchestraUserCustomizerExtension customizer : customizers) {
            customized = customizer.apply(authToken, customized);
        }
        return customized;
    }
}
