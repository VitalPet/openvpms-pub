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

package org.openvpms.web.workspace.patient.mr;

import org.openvpms.web.component.im.layout.LayoutContext;

/**
 * Table model for <em>act.patientReminder</em> acts.
 *
 * @author Tim Anderson
 */
public class ReminderActTableModel extends PatientRecordActTableModel {

    /**
     * Constructs a {@code ReminderActTableModel}.
     *
     * @param shortNames the act archetype short names
     * @param context    the layout context
     */
    public ReminderActTableModel(String[] shortNames, LayoutContext context) {
        super(shortNames, context);
    }

    /**
     * Returns a list of descriptor names to include in the table.
     *
     * @return the list of descriptor names to include in the table
     */
    @Override
    protected String[] getNodeNames() {
        return new String[]{"createdTime", "endTime", "status", "reminderType", "product", "reminderCount",
                            "startTime"};
    }

}
