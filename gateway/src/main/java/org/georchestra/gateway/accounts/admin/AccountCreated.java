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
package org.georchestra.gateway.accounts.admin;

import org.georchestra.security.model.GeorchestraUser;

import lombok.NonNull;
import lombok.Value;

/**
 * Event published when a new {@link GeorchestraUser} account is created.
 * <p>
 * This event is triggered whenever a new user is successfully registered in the
 * system. It can be used to listen for account creation and trigger additional
 * actions such as logging, notifications, or audits.
 * </p>
 *
 * <p>
 * This class is immutable and thread-safe, though the attached
 * {@link GeorchestraUser} is mutable, so make sure not to modify it.
 * </p>
 *
 * @see GeorchestraUser
 */
@Value
public class AccountCreated {

    /** The newly created user account. */
    private @NonNull GeorchestraUser user;
}