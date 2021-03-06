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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.supplier.order;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.lookup.LookupField;
import org.openvpms.web.component.im.lookup.LookupFieldFactory;
import org.openvpms.web.component.im.lookup.NodeLookupQuery;
import org.openvpms.web.component.im.query.ActResultSet;
import org.openvpms.web.component.im.query.ActStatuses;
import org.openvpms.web.component.im.query.IMObjectListResultSet;
import org.openvpms.web.component.im.query.ParticipantConstraint;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.workspace.supplier.SupplierActQuery;

import java.util.ArrayList;
import java.util.List;


/**
 * Query for <em>act.supplierOrder</em> acts.
 *
 * @author Tim Anderson
 */
public class OrderQuery extends SupplierActQuery<FinancialAct> {

    /**
     * The delivery status selector.
     */
    private LookupField deliveryStatus;

    /**
     * The act statuses.
     */
    private static final ActStatuses STATUSES = new ActStatuses(SupplierArchetypes.ORDER);


    /**
     * Constructs an {@code OrderQuery}.
     *
     * @param shortNames the act short names to query
     * @param context    the context
     */
    public OrderQuery(String[] shortNames, LayoutContext context) {
        super(shortNames, STATUSES, FinancialAct.class, context);
    }

    /**
     * Performs the query.
     *
     * @param sort the sort constraint. May be {@code null}
     * @return the query result set. May be {@code null}
     * @throws ArchetypeServiceException if the query fails
     */
    @Override
    public ResultSet<FinancialAct> query(SortConstraint[] sort) {
        ResultSet<FinancialAct> result = createResultSet(sort);
        if (result != null) {
            String deliveryStatus = getDeliveryStatus();
            if (deliveryStatus != null) {
                result = filterOnDeliveryStatus(result, deliveryStatus);
            }
        }

        return result;
    }

    /**
     * Returns the preferred height of the query when rendered.
     *
     * @return the preferred height, or {@code null} if it has no preferred height
     */
    @Override
    public Extent getHeight() {
        return getHeight(2);
    }

    /**
     * Filters the result set on delivery status.
     *
     * @param set            the result set to filter
     * @param deliveryStatus the status to match on
     * @return the filtered result set
     */
    private ResultSet<FinancialAct> filterOnDeliveryStatus(ResultSet<FinancialAct> set, String deliveryStatus) {
        List<FinancialAct> matches = new ArrayList<>();
        while (set.hasNext()) {
            IPage<FinancialAct> page = set.next();
            for (FinancialAct act : page.getResults()) {
                IMObjectBean bean = new IMObjectBean(act);
                if (deliveryStatus.equals(bean.getString("deliveryStatus"))) {
                    matches.add(act);
                }
            }
        }
        return new IMObjectListResultSet<>(matches, getMaxResults(), set.getSortConstraints(), null);
    }

    /**
     * Lays out the component in a container, and sets focus on the instance name.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        Row row1 = RowFactory.create(Styles.CELL_SPACING);
        Row row2 = RowFactory.create(Styles.CELL_SPACING);

        addSupplierSelector(row1);
        addStockLocationSelector(row1);
        addStatusSelector(row1);
        addDeliveryStatus(row1);
        addDateRange(row2);

        container.add(ColumnFactory.create(Styles.CELL_SPACING, row1, row2));
    }

    /**
     * Returns the selected delivery status.
     *
     * @return the selected delivery status, or {@code null} if no status is selected
     */
    private String getDeliveryStatus() {
        return deliveryStatus.getSelectedCode();
    }

    /**
     * Adds the delivery status select field component to a container.
     *
     * @param container the container
     */
    private void addDeliveryStatus(Component container) {
        Label label = LabelFactory.create();
        String displayName = DescriptorHelper.getDisplayName(SupplierArchetypes.ORDER, "deliveryStatus");
        label.setText(displayName);
        NodeLookupQuery source = new NodeLookupQuery(SupplierArchetypes.ORDER, "deliveryStatus");
        deliveryStatus = LookupFieldFactory.create(source, true);
        getFocusGroup().add(deliveryStatus);
        container.add(label);
        container.add(deliveryStatus);
    }

    /**
     * Creates a new result set.
     *
     * @param participants the participant constraints
     * @param sort         the sort criteria
     * @return a new result set
     */
    protected ResultSet<FinancialAct> createResultSet(ParticipantConstraint[] participants, SortConstraint[] sort) {
        return new ActResultSet<>(getArchetypeConstraint(), participants, getFrom(), getTo(), getStatuses(),
                                  excludeStatuses(), getConstraints(), getMaxResults(), sort);
    }

}
