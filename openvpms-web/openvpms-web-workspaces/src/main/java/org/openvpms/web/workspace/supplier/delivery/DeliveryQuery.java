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

package org.openvpms.web.workspace.supplier.delivery;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Row;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ActResultSet;
import org.openvpms.web.component.im.query.ActStatuses;
import org.openvpms.web.component.im.query.ParticipantConstraint;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.workspace.supplier.SupplierActQuery;


/**
 * Query for <em>act.supplierDelivery</em> and <em>act.supplierReturn</em> acts.
 *
 * @author Tim Anderson
 */
public class DeliveryQuery extends SupplierActQuery<FinancialAct> {

    /**
     * The act statuses.
     */
    private static final ActStatuses STATUSES = new ActStatuses("act.supplierDelivery");


    /**
     * Constructs a {@link DeliveryQuery}.
     *
     * @param shortNames the act short names to query
     * @param context    the layout context
     */
    public DeliveryQuery(String[] shortNames, LayoutContext context) {
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
        return createResultSet(sort);
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
     * Lays out the component in a container, and sets focus on the instance
     * name.
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
        addDateRange(row2);

        container.add(ColumnFactory.create(Styles.CELL_SPACING, row1, row2));
    }

    /**
     * Creates a new result set.
     *
     * @param participants the participant constraints
     * @param sort         the sort criteria
     * @return a new result set
     */
    protected ActResultSet<FinancialAct> createResultSet(ParticipantConstraint[] participants, SortConstraint[] sort) {
        return new ActResultSet<>(getArchetypeConstraint(), participants, getFrom(), getTo(), getStatuses(),
                                  excludeStatuses(), getConstraints(), getMaxResults(), sort);
    }

}
