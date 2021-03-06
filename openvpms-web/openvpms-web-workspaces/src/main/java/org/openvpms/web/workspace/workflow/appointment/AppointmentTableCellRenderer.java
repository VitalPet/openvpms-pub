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

package org.openvpms.web.workspace.workflow.appointment;

import echopointng.layout.TableLayoutDataEx;
import echopointng.xhtml.XhtmlFragment;
import nextapp.echo2.app.Table;
import org.openvpms.web.echo.table.TableHelper;
import org.openvpms.web.workspace.workflow.scheduling.Schedule;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleEventGrid.Availability;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleTableModel;


/**
 * TableCellRender that assigns blocks of appointments in different hours a different style.
 *
 * @author Tim Anderson
 */
public class AppointmentTableCellRenderer extends AbstractAppointmentTableCellRender {

    /**
     * Constructs an {@link AppointmentTableCellRenderer}.
     *
     * @param model the model
     */
    public AppointmentTableCellRenderer(AppointmentTableModel model) {
        super(model);
    }

    /**
     * Returns a {@code XhtmlFragment} that will be displayed as the
     * content at the specified coordinate in the table.
     *
     * @param table  the {@code Table} for which the rendering is occurring
     * @param value  the value retrieved from the {@code TableModel} for the specified coordinate
     * @param column the column index to render
     * @param row    the row index to render
     * @return a {@code XhtmlFragment} representation of the value
     */
    public XhtmlFragment getTableCellRendererContent(Table table, Object value, int column, int row) {
        XhtmlFragment result = new XhtmlFragment();
        TableLayoutDataEx layout;
        AppointmentTableModel model = getModel();

        Availability avail = model.getAvailability(column, row);
        String style = getStyle(avail, model, row);
        layout = TableHelper.getTableLayoutDataEx(style);

        if (layout != null && avail == Availability.UNAVAILABLE) {
            Schedule schedule = model.getSchedule(column, row);
            int span = model.getGrid().getUnavailableSlots(schedule, row);
            layout.setRowSpan(span);
        }

        result.setLayoutData(layout);
        return result;
    }

    /**
     * Returns the table model.
     *
     * @return the table model
     */
    @Override
    protected AppointmentTableModel getModel() {
        return (AppointmentTableModel) super.getModel();
    }

    /**
     * Returns the style for a free row.
     *
     * @param model the appointment table model
     * @param row   the row
     * @return a style for the row
     */
    @Override
    protected String getFreeStyle(ScheduleTableModel model, int row) {
        AppointmentTableModel m = (AppointmentTableModel) model;
        int hour = m.getHour(row);
        return (hour % 2 == 0) ? "ScheduleTable.Even" : "ScheduleTable.Odd";
    }

}

