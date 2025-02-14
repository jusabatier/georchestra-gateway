/*
 * Copyright (C) 2021 by the geOrchestra PSC
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
package org.georchestra.gateway.model;

import java.util.Optional;

import com.google.common.annotations.VisibleForTesting;

import lombok.Data;
import lombok.Generated;

/**
 * Configuration model for geOrchestra-specific HTTP request headers.
 * <p>
 * This class defines which security-related headers should be appended to
 * proxied requests. Each header can be individually enabled or disabled.
 * </p>
 */
@Data
@Generated
public class HeaderMappings {

    ///////// User info headers ///////////////

    /** Append the standard {@literal sec-proxy=true} header to proxied requests. */
    private Optional<Boolean> proxy = Optional.empty();

    /** Append the standard {@literal sec-userid} header to proxied requests. */
    private Optional<Boolean> userid = Optional.empty();

    /**
     * Append the standard {@literal sec-lastupdated} header to proxied requests.
     */
    private Optional<Boolean> lastUpdated = Optional.empty();

    /** Append the standard {@literal sec-username} header to proxied requests. */
    private Optional<Boolean> username = Optional.empty();

    /** Append the standard {@literal sec-roles} header to proxied requests. */
    private Optional<Boolean> roles = Optional.empty();

    /** Append the standard {@literal sec-org} header to proxied requests. */
    private Optional<Boolean> org = Optional.empty();

    /** Append the standard {@literal sec-email} header to proxied requests. */
    private Optional<Boolean> email = Optional.empty();

    /** Append the standard {@literal sec-firstname} header to proxied requests. */
    private Optional<Boolean> firstname = Optional.empty();

    /** Append the standard {@literal sec-lastname} header to proxied requests. */
    private Optional<Boolean> lastname = Optional.empty();

    /** Append the standard {@literal sec-tel} header to proxied requests. */
    private Optional<Boolean> tel = Optional.empty();

    /** Append the standard {@literal sec-address} header to proxied requests. */
    private Optional<Boolean> address = Optional.empty();

    /** Append the standard {@literal sec-title} header to proxied requests. */
    private Optional<Boolean> title = Optional.empty();

    /** Append the standard {@literal sec-notes} header to proxied requests. */
    private Optional<Boolean> notes = Optional.empty();

    /**
     * Append the standard {@literal sec-user} (Base64 JSON payload) header to
     * proxied requests.
     */
    private Optional<Boolean> jsonUser = Optional.empty();

    ///////// Organization info headers ///////////////

    /** Append the standard {@literal sec-orgname} header to proxied requests. */
    private Optional<Boolean> orgname = Optional.empty();

    /** Append the standard {@literal sec-orgid} header to proxied requests. */
    private Optional<Boolean> orgid = Optional.empty();

    /**
     * Append the standard {@literal sec-org-lastupdated} header to proxied
     * requests.
     */
    private Optional<Boolean> orgLastUpdated = Optional.empty();

    /**
     * Append the standard {@literal sec-organization} (Base64 JSON payload) header
     * to proxied requests.
     */
    private Optional<Boolean> jsonOrganization = Optional.empty();

    /**
     * Enables all headers.
     *
     * @return this instance with all headers set to {@code true}
     */
    @VisibleForTesting
    public HeaderMappings enableAll() {
        this.setAll(Optional.of(Boolean.TRUE));
        return this;
    }

    /**
     * Disables all headers.
     *
     * @return this instance with all headers set to {@code false}
     */
    @VisibleForTesting
    public HeaderMappings disableAll() {
        this.setAll(Optional.of(Boolean.FALSE));
        return this;
    }

    /**
     * Sets all header options to the given value.
     *
     * @param val the value to set for all headers
     */
    private void setAll(Optional<Boolean> val) {
        this.proxy = val;
        this.userid = val;
        this.lastUpdated = val;
        this.username = val;
        this.roles = val;
        this.org = val;
        this.email = val;
        this.firstname = val;
        this.lastname = val;
        this.tel = val;
        this.address = val;
        this.title = val;
        this.notes = val;
        this.jsonUser = val;
        this.orgname = val;
        this.orgid = val;
        this.orgLastUpdated = val;
        this.jsonOrganization = val;
    }

    /**
     * Enables or disables the {@literal sec-userid} header.
     *
     * @param b {@code true} to enable, {@code false} to disable
     * @return this instance with the updated configuration
     */
    public HeaderMappings userid(boolean b) {
        setUserid(Optional.of(b));
        return this;
    }

    /**
     * Enables or disables the {@literal sec-user} (Base64 JSON) header.
     *
     * @param b {@code true} to enable, {@code false} to disable
     * @return this instance with the updated configuration
     */
    public HeaderMappings jsonUser(boolean b) {
        setJsonUser(Optional.of(b));
        return this;
    }

    /**
     * Enables or disables the {@literal sec-organization} (Base64 JSON) header.
     *
     * @param b {@code true} to enable, {@code false} to disable
     * @return this instance with the updated configuration
     */
    public HeaderMappings jsonOrganization(boolean b) {
        setJsonOrganization(Optional.of(b));
        return this;
    }

    /**
     * Creates a copy of this object.
     *
     * @return a new {@link HeaderMappings} instance with the same values
     */
    public HeaderMappings copy() {
        HeaderMappings copy = new HeaderMappings();
        copy.merge(this);
        return copy;
    }

    /**
     * Merges the non-empty fields from {@code other} into this instance.
     *
     * @param other the other {@link HeaderMappings} instance
     * @return this instance with the merged values
     */
    public HeaderMappings merge(HeaderMappings other) {
        proxy = merge(proxy, other.proxy);
        userid = merge(userid, other.userid);
        lastUpdated = merge(lastUpdated, other.lastUpdated);
        username = merge(username, other.username);
        roles = merge(roles, other.roles);
        org = merge(org, other.org);
        email = merge(email, other.email);
        firstname = merge(firstname, other.firstname);
        lastname = merge(lastname, other.lastname);
        tel = merge(tel, other.tel);
        address = merge(address, other.address);
        title = merge(title, other.title);
        notes = merge(notes, other.notes);
        jsonUser = merge(jsonUser, other.jsonUser);
        orgname = merge(orgname, other.orgname);
        orgid = merge(orgid, other.orgid);
        orgLastUpdated = merge(orgLastUpdated, other.orgLastUpdated);
        jsonOrganization = merge(jsonOrganization, other.jsonOrganization);
        return this;
    }

    /**
     * Merges two {@link Optional} values, preferring the second if present.
     *
     * @param a the first value
     * @param b the second value (preferred if present)
     * @return the merged {@link Optional} value
     */
    private Optional<Boolean> merge(Optional<Boolean> a, Optional<Boolean> b) {
        return b.isEmpty() ? a : b;
    }
}
