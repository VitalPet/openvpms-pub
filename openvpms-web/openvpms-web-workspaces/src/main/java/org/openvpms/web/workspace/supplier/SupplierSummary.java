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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.supplier;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.openvpms.archetype.rules.supplier.account.SupplierAccountRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.format.NumberFormatter;
import org.openvpms.web.system.ServiceHelper;

import java.math.BigDecimal;


/**
 * Renders supplier summary information.
 *
 * @author Tim Anderson
 */
public class SupplierSummary {

    /**
     * Returns summary information for a supplier.
     *
     * @param supplier the supplier. May be {@code null}
     * @return a summary component, or {@code null} if there is no summary
     */
    public static Component getSummary(Party supplier) {
        Component result = null;
        if (supplier != null) {
            Label title = LabelFactory.create("supplier.account.balance");
            Label balance = LabelFactory.create();
            SupplierAccountRules rules = ServiceHelper.getBean(SupplierAccountRules.class);
            BigDecimal value = rules.getBalance(supplier);
            balance.setText(NumberFormatter.format(value));
            result = ColumnFactory.create("PartySummary", RowFactory.create(Styles.CELL_SPACING, title, balance));
        }
        return result;
    }

}
