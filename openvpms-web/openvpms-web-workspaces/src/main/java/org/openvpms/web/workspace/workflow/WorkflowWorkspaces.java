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

package org.openvpms.web.workspace.workflow;

import org.openvpms.archetype.rules.prefs.Preferences;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.PracticeMailContext;
import org.openvpms.web.component.workspace.AbstractWorkspaces;
import org.openvpms.web.workspace.workflow.appointment.AppointmentWorkspace;
import org.openvpms.web.workspace.workflow.investigation.InvestigationsWorkspace;
import org.openvpms.web.workspace.workflow.messaging.MessagingWorkspace;
import org.openvpms.web.workspace.workflow.order.CustomerOrderWorkspace;
import org.openvpms.web.workspace.workflow.worklist.TaskWorkspace;


/**
 * Workflow workspaces.
 *
 * @author Tim Anderson
 */
public class WorkflowWorkspaces extends AbstractWorkspaces {

    /**
     * Constructs a {@link WorkflowWorkspaces}.
     *
     * @param context     the context
     * @param preferences user preferences
     */
    public WorkflowWorkspaces(Context context, Preferences preferences) {
        super("workflow");
        addWorkspace(new AppointmentWorkspace(context, preferences));
        addWorkspace(new TaskWorkspace(context, preferences));
        addWorkspace(new MessagingWorkspace(context));
        PracticeMailContext mailContext = new PracticeMailContext(context);
        addWorkspace(new InvestigationsWorkspace(context, mailContext, preferences));
        addWorkspace(new CustomerOrderWorkspace(context, mailContext));
    }
}
