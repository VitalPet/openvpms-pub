/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.workspace.supplier.order;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.supplier.DeliveryStatus;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.edit.ActActions;


/**
 * Determines the operations that may be performed on <em>act.supplierOrder</em> acts.
 *
 * @author Tim Anderson
 */
public class OrderActions extends ActActions<FinancialAct> {

    /**
     * The singleton instance.
     */
    public static final OrderActions INSTANCE = new OrderActions();


    /**
     * Default constructor.
     */
    private OrderActions() {
        super(true);
    }

    /**
     * Determines if an act can be edited.
     *
     * @param act the delivery to check
     * @return {@code true} if the delivery status is <em>IN_PROGRESS</em>
     */
    @Override
    public boolean canEdit(FinancialAct act) {
        IMObjectBean bean = new IMObjectBean(act);
        return !DeliveryStatus.FULL.toString().equals(bean.getString("deliveryStatus"));
    }

    /**
     * Determines if an act can be deleted.
     *
     * @param act the delivery to check
     * @return {@code true} if the act status is <em>IN_PROGRESS</em>
     */
    @Override
    public boolean canDelete(FinancialAct act) {
        return ActStatus.IN_PROGRESS.equals(act.getStatus());
    }

}
