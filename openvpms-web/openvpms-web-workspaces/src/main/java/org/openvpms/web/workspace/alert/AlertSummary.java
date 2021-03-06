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

package org.openvpms.web.workspace.alert;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.layout.RowLayoutData;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;

import java.util.Collections;
import java.util.List;


/**
 * Provides a summary of customer/patient alerts.
 *
 * @author Tim Anderson
 */
public class AlertSummary {

    /**
     * The alerts.
     */
    private final List<Alert> alerts;

    /**
     * The no. of alerts to show.
     */
    private int showCount = 4;

    /**
     * The context.
     */
    private final Context context;

    /**
     * The help context.
     */
    private final HelpContext help;

    /**
     * Constructs an {@link AlertSummary}.
     *
     * @param alerts  the alerts
     * @param context the context
     * @param help    the help context
     */
    public AlertSummary(List<Alert> alerts, Context context, HelpContext help) {
        this.alerts = alerts;
        this.context = context;
        this.help = help.subtopic("alert");
    }

    /**
     * Sets the number of alerts to display.
     *
     * @param count the no. of alerts to display. Defaults to {@code 4}.
     */
    public void setShowCount(int count) {
        showCount = count;
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    public Component getComponent() {
        Column result = ColumnFactory.create();
        Collections.sort(alerts);

        Row title = RowFactory.create(LabelFactory.create("alerts.title"));
        result.add(title);
        for (int i = 0; i < alerts.size() && i < showCount; ++i) {
            Alert element = alerts.get(i);
            result.add(getButton(element));
        }
        Button viewAll = ButtonFactory.create("alerts.viewall", "small", new ActionListener() {
            public void onAction(ActionEvent event) {
                onShowAll();
            }
        });
        Row right = RowFactory.create(viewAll);

        RowLayoutData rightLayout = new RowLayoutData();
        rightLayout.setAlignment(Alignment.ALIGN_RIGHT);
        rightLayout.setWidth(new Extent(100, Extent.PERCENT));
        right.setLayoutData(rightLayout);

        Row row;
        if (alerts.size() > showCount) {
            row = RowFactory.create(Styles.WIDE_CELL_SPACING, title, right);
            result.add(row, 0);
        }

        return result;
    }

    /**
     * Displays a dialog with all alerts.
     */
    protected void onShowAll() {
        AlertsViewer viewer = new AlertsViewer(alerts, context, help);
        viewer.show();
    }

    /**
     * Returns a button to render the alerts.
     *
     * @param alert the alerts
     * @return a new button
     */
    protected Button getButton(final Alert alert) {
        Button result = ButtonFactory.create(null, "small");
        result.setText(alert.getName());
        Color colour = alert.getColour();
        if (colour != null) {
            result.setBackground(colour);
            result.setForeground(alert.getTextColour());
        }

        result.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                AlertsViewer viewer = new AlertsViewer(alert, context, help);
                viewer.show();
            }
        });
        return result;
    }

}
