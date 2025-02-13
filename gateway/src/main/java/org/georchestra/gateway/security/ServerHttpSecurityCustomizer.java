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

import org.springframework.core.Ordered;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;

/**
 * Extension point to aid {@link GatewaySecurityConfiguration} in initializing
 * the application security filter chain.
 * <p>
 * Spring beans of this type implement {@link Ordered}, and will be called in
 * sequence adhering to each bean's defined order.
 * <p>
 * This interface extends {@link Customizer Customizer<ServerHttpSecurity>}. The
 * {@link Customizer#customize customize(ServerHttpSecurity)} shall modify the
 * provided server HTTP security configuration bean in whatever way needed.
 */
public interface ServerHttpSecurityCustomizer extends Customizer<ServerHttpSecurity>, Ordered {

    /**
     * @return user friendly extension name for logging purposes
     */
    default String getName() {
        return getClass().getCanonicalName();
    }

    /**
     * {@inheritDoc}
     * 
     * @return {@code 0} as default order, implementations should override as needed
     *         in case they need to apply their customizations to
     *         {@link ServerHttpSecurity} in a specific order.
     * @see Ordered#HIGHEST_PRECEDENCE
     * @see Ordered#LOWEST_PRECEDENCE
     */
    default @Override int getOrder() {
        return 0;
    }
}
