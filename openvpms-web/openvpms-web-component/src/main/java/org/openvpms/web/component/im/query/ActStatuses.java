/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.query;

import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.web.component.im.lookup.LookupFilter;
import org.openvpms.web.component.im.lookup.LookupQuery;
import org.openvpms.web.component.im.lookup.NodeLookupQuery;

import java.util.ArrayList;
import java.util.List;


/**
 * Helper to retrieve act status lookups for selection by {@link ActQuery}.
 *
 * @author Tim Anderson
 */
public class ActStatuses implements LookupQuery {

    /**
     * The lookup source.
     */
    private final LookupQuery lookups;

    /**
     * The act status to exclude.
     */
    private final String exclude;

    /**
     * Overrides the default lookup.
     */
    private Lookup defaultLookup;

    /**
     * Indicates to use defaultLookup instead of that returned by
     * {@link #getDefault()}.
     */
    private boolean useDefault;


    /**
     * Constructs an {@link ActStatuses} selecting all lookups from the status node of the specified archetype.
     *
     * @param shortName the archetype short name
     */
    public ActStatuses(String shortName) {
        this(shortName, null);
    }

    /**
     * Constructs an {@link ActStatuses} selecting lookups from the status node of the specified archetype.
     *
     * @param shortName the archetype short name
     * @param exclude   the act status to exclude. May be {@code null}
     */
    public ActStatuses(String shortName, String exclude) {
        this(new NodeLookupQuery(shortName, "status"), exclude);
    }

    /**
     * Constructs an {@link ActStatuses}.
     *
     * @param lookups the lookups
     */
    public ActStatuses(LookupQuery lookups) {
        this(lookups, null);
    }

    /**
     * Constructs an {@link ActStatuses}.
     *
     * @param lookups the lookups
     * @param exclude the act status to exclude. May be {@code null}
     */
    public ActStatuses(LookupQuery lookups, String exclude) {
        if (exclude != null) {
            lookups = new LookupFilter(lookups, false, exclude);
        }
        this.lookups = lookups;
        this.exclude = exclude;
    }

    /**
     * Sets the default status, overriding that returned by
     * {@link LookupQuery#getDefault()}.
     *
     * @param lookup the default lookup. May be {@code null}
     */
    public void setDefault(Lookup lookup) {
        useDefault = true;
        defaultLookup = lookup;
    }

    /**
     * Sets the default status, overriding that returned by
     * {@link LookupQuery#getDefault()}.
     *
     * @param code the default lookup code. May be {@code null}
     */
    public void setDefault(String code) {
        useDefault = true;
        Lookup lookup = null;
        if (code != null) {
            for (Lookup l : getLookups()) {
                if (l.getCode().equals(code)) {
                    lookup = l;
                }
            }
        }
        defaultLookup = lookup;
    }

    /**
     * Returns the default status.
     *
     * @return the default status, or {@code null} if none is defined
     */
    public Lookup getDefault() {
        return (useDefault) ? defaultLookup : lookups.getDefault();
    }

    /**
     * Returns the default status code.
     *
     * @return the default status code, or {@code null} if none is defined
     */
    public String getDefaultCode() {
        Lookup result = getDefault();
        return result != null ? result.getCode() : null;
    }

    /**
     * Returns the act status lookups.
     *
     * @return the act status lookups
     */
    public List<Lookup> getLookups() {
        return lookups.getLookups();
    }

    /**
     * Returns the act status lookup codes.
     *
     * @return the act status lookup codes
     */
    public List<String> getCodes() {
        List<String> result = new ArrayList<>();
        for (Lookup lookup : getLookups()) {
            result.add(lookup.getCode());
        }
        return result;
    }

    /**
     * Returns the excluded status code.
     *
     * @return the excluded status code. May be {@code null}
     */
    public String getExcluded() {
        return exclude;
    }

}
