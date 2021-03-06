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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.finance.account;

import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.JoinConstraint;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.OrConstraint;
import org.openvpms.component.system.common.query.ShortNameConstraint;


/**
 * Helper to create queries for customer account acts.
 *
 * @author Tim Anderson
 */
public class CustomerAccountQueryFactory {

    /**
     * Creates an object set query for unallocated acts for the specified
     * customer. Returns only the amount, allocatedAmount and credit nodes,
     * named <em>a.amount</em>, <em>a.allocatedAmount</em> and <em>a.credit</em>
     * respectively.
     *
     * @param customer   the customer
     * @param shortNames the act short names
     * @return a new query
     */
    public static ArchetypeQuery createUnallocatedObjectSetQuery(
            Party customer, String[] shortNames) {
        ArchetypeQuery query = createUnallocatedQuery(customer, shortNames,
                                                      null);
        query.add(new NodeSelectConstraint("amount"));
        query.add(new NodeSelectConstraint("allocatedAmount"));
        query.add(new NodeSelectConstraint("credit"));
        return query;
    }

    /**
     * Creates a query for unallocated acts for the specified customer.
     *
     * @param customer   the customer
     * @param shortNames the act short names
     * @param exclude    the act to exclude. May be <tt>null</tt>
     * @return a new query
     */
    public static ArchetypeQuery createUnallocatedQuery(Party customer,
                                                        String[] shortNames,
                                                        Act exclude) {
        ArchetypeQuery query = createBalanceParticipationQuery(customer,
                                                               shortNames,
                                                               exclude);
        query.add(Constraints.eq("status", FinancialActStatus.POSTED));
        return query;
    }

    /**
     * Creates a query for unbilled acts for the specified customer.
     *
     * @param customer   the customer
     * @param shortNames the act short names
     * @return a new query
     */
    public static ArchetypeQuery createUnbilledObjectSetQuery(
            Party customer, String[] shortNames) {
        ArchetypeQuery query = createBalanceParticipationQuery(
                customer, shortNames, null);
        query.add(Constraints.ne("status", FinancialActStatus.POSTED));
        query.add(new NodeSelectConstraint("amount"));
        query.add(new NodeSelectConstraint("allocatedAmount"));
        query.add(new NodeSelectConstraint("credit"));
        return query;
    }

    /**
     * Creates an object set query for acts for the specified customer.
     * <p/>
     * Returns only the amount, allocatedAmount and credit nodes,
     * named <em>a.amount</em>, <em>a.allocatedAmount</em> and <em>a.credit</em>
     * respectively.
     * <p/>
     * Results are ordered on descending <em>startTime</em>.
     *
     * @param customer   the customer
     * @param shortNames the act short names
     * @return a new query
     */
    public static ArchetypeQuery createObjectSetQuery(Party customer,
                                                      String[] shortNames) {
        return createObjectSetQuery(customer, shortNames, false);
    }

    /**
     * Creates an object set query for acts for the specified customer.
     * <p/>
     * Returns only the amount, allocatedAmount and credit nodes,
     * named <em>a.amount</em>, <em>a.allocatedAmount</em> and <em>a.credit</em>
     * respectively.
     *
     * @param customer      the customer
     * @param shortNames    the act short names
     * @param sortAscending if <tt>true</tt>, sort on ascending
     *                      <em>startTime</em>; otherwise sort descending
     * @return a new query
     */
    public static ArchetypeQuery createObjectSetQuery(Party customer,
                                                      String[] shortNames,
                                                      boolean sortAscending) {
        ArchetypeQuery query = createQuery(customer, shortNames);
        query.add(Constraints.sort("startTime", sortAscending));
        query.add(Constraints.sort("id", true));
        query.add(new NodeSelectConstraint("amount"));
        query.add(new NodeSelectConstraint("allocatedAmount"));
        query.add(new NodeSelectConstraint("credit"));
        return query;
    }

    /**
     * Creates a query for all acts matching the specified short names, for a customer.
     *
     * @param customer   the customer
     * @param shortNames the act archetype short names
     * @return the corresponding query
     */
    public static ArchetypeQuery createQuery(Party customer, String[] shortNames) {
        return createQuery(customer.getObjectReference(), shortNames);
    }

    /**
     * Creates a query for all acts matching the specified short names, for a customer.
     *
     * @param customer   the customer reference
     * @param shortNames the act archetype short names
     * @return the corresponding query
     */
    public static ArchetypeQuery createQuery(IMObjectReference customer, String[] shortNames) {
        ShortNameConstraint archetypes = Constraints.shortName("act", shortNames, false);
        ArchetypeQuery query = new ArchetypeQuery(archetypes);
        JoinConstraint join = Constraints.join("customer").add(Constraints.eq("entity", customer));
        OrConstraint or = new OrConstraint();

        // re-specify the act short names, this time on the participation act
        // node. Ideally wouldn't specify them on the acts at all, but this
        // is not supported by ArchetypeQuery.
        for (String shortName : shortNames) {
            ArchetypeId id = new ArchetypeId(shortName);
            or.add(Constraints.eq("customer.act", id));
        }
        query.add(join);
        query.add(or);
        return query;
    }

    /**
     * Creates an account balance participation query.
     * <p/>
     * This sorts acts on ascending startTime and id.
     *
     * @param customer   the customer
     * @param shortNames the act archetype short names
     * @param exclude    the act to exclude fromj the result. May be <tt>null</tt>
     * @return the corresponding query
     */
    private static ArchetypeQuery createBalanceParticipationQuery(Party customer, String[] shortNames, Act exclude) {
        ShortNameConstraint archetypes = Constraints.shortName("act", shortNames, false);
        ArchetypeQuery query = new ArchetypeQuery(archetypes);
        JoinConstraint join = Constraints.join("accountBalance").add(Constraints.eq("entity", customer));
        if (exclude != null) {
            join.add(Constraints.ne("act", exclude));
        }
        query.add(join);
        query.add(Constraints.sort("startTime"));
        query.add(Constraints.sort("id"));
        return query;
    }

}
