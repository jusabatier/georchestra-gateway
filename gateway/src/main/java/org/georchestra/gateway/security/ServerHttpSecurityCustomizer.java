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
 * Extension point to assist {@link GatewaySecurityConfiguration} in configuring
 * the application security filter chain.
 * <p>
 * Implementations of this interface act as modular security configuration
 * components that can modify the {@link ServerHttpSecurity} instance during
 * application startup. These beans implement {@link Ordered}, ensuring they are
 * applied in a predictable sequence based on their defined order.
 * </p>
 * <p>
 * This interface extends {@link Customizer} with {@code ServerHttpSecurity}.
 * Implementations of the {@link Customizer#customize} method modify the
 * provided {@link ServerHttpSecurity} configuration bean as required.
 * </p>
 */
public interface ServerHttpSecurityCustomizer extends Customizer<ServerHttpSecurity>, Ordered {

    /**
     * Returns a human-readable extension name for logging purposes.
     *
     * @return the fully qualified class name of the implementing class
     */
    default String getName() {
        return getClass().getCanonicalName();
    }

    /**
     * Returns the execution order of this customizer.
     * <p>
     * By default, it returns {@code 0}. Implementations should override this method
     * if they need to apply their customizations in a specific order within the
     * security configuration process.
     * </p>
     *
     * @return the order in which this customizer should be applied
     * @see Ordered#HIGHEST_PRECEDENCE
     * @see Ordered#LOWEST_PRECEDENCE
     */
    default @Override int getOrder() {
        return 0;
    }
}
