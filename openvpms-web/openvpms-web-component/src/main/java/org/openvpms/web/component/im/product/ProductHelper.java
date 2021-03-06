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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.product;

import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.product.PricingGroup;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Product helper methods.
 *
 * @author Tim Anderson
 */
public class ProductHelper {

    /**
     * Determines if a products are being filtered by practice location.
     *
     * @param practice the practice
     * @return {@code true} if products are being filtered by practice location
     */
    public static boolean useLocationProducts(Party practice) {
        PracticeRules rules = ServiceHelper.getBean(PracticeRules.class);
        return rules.useLocationProducts(practice);
    }

    /**
     * Determines if a products are being filtered by practice location.
     *
     * @param context the context
     * @return {@code true} if products are being filtered by practice location
     */
    public static boolean useLocationProducts(Context context) {
        Party practice = context.getPractice();
        return (practice != null) && useLocationProducts(practice);
    }

    /**
     * Returns the pricing group for the context practice location.
     *
     * @param context the context
     * @return the pricing group, or {@link PricingGroup#NONE} if the location has no pricing group
     */
    public static PricingGroup getPricingGroup(Context context) {
        Lookup result = null;
        Party location = context.getLocation();
        if (location != null) {
            LocationRules rules = ServiceHelper.getBean(LocationRules.class);
            result = rules.getPricingGroup(location);
        }
        return (result != null) ? new PricingGroup(result) : PricingGroup.NONE;
    }

    /**
     * Filters prices on pricing group.
     *
     * @param prices       the prices to filter
     * @param pricingGroup the pricing group to filter on. May be {@code null}, to indicate prices with no pricing
     *                     groups
     * @return the filtered prices
     */
    public static List<IMObject> filterPrices(List<IMObject> prices, PricingGroup pricingGroup) {
        List<IMObject> result = new ArrayList<IMObject>();
        for (IMObject object : prices) {
            IMObjectBean bean = new IMObjectBean(object);
            List<Lookup> groups = bean.getValues("pricingGroups", Lookup.class);
            if (pricingGroup == null) {
                if (groups.isEmpty()) {
                    result.add(object);
                }
            } else if (pricingGroup.matches(groups)) {
                result.add(object);
            }
        }
        return result;
    }

    /**
     * Determines if pricing groups have been configured.
     *
     * @return {@code true} if pricing groups have been configured
     */
    public static boolean pricingGroupsConfigured() {
        return !getPricingGroups().isEmpty();
    }

    /**
     * Returns the pricing groups.
     *
     * @return the pricing groups
     */
    public static Collection<Lookup> getPricingGroups() {
        return ServiceHelper.getLookupService().getLookups("lookup.pricingGroup");
    }

    /**
     * Determines if a price has pricing groups.
     *
     * @param price the price
     * @return {@code true} if the price has pricing groups
     */
    public static boolean hasPricingGroups(ProductPrice price) {
        ProductPriceRules rules = ServiceHelper.getBean(ProductPriceRules.class);
        return !rules.getPricingGroups(price).isEmpty();
    }
}
