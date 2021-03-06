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

package org.openvpms.web.workspace.workflow.messaging;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Table;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.workflow.MessageStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.IMObjectTableBrowser;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.table.EvenOddTableCellRenderer;


/**
 * Browser for <em>act.userMessage</em>,<em>act.systemMessage</em> and <em>act.auditMessage</em> acts.
 *
 * @author Tim Anderson
 */
public class MessageBrowser extends IMObjectTableBrowser<Act> {

    /**
     * Constructs a {@code MessageBrowser}.
     *
     * @param query   the query
     * @param context the layout context
     */
    public MessageBrowser(MessageQuery query, LayoutContext context) {
        super(query, context);
    }

    /**
     * Notifies listeners when an object is selected.
     * <p/>
     * This marks any <em>PENDING</em> message with the same <em>to</em> address as the signed in
     * user as <em>READ</em>.
     *
     * @param selected the selected object
     */
    @Override
    protected void notifySelected(Act selected) {
        markRead(selected);
        super.notifySelected(selected);
    }

    /**
     * Creates a new paged table.
     *
     * @param model the table model
     * @return a new paged table
     */
    @Override
    protected PagedIMTable<Act> createTable(final IMTableModel<Act> model) {
        PagedIMTable<Act> table = super.createTable(model);
        // register a renderer to make labels bold, if the act status is PENDING
        table.getTable().setDefaultRenderer(Object.class, new EvenOddTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(Table table, Object value, int column, int row) {
                Component result = super.getTableCellRendererComponent(table, value, column, row);
                if (MessageStatus.PENDING.equals(model.getObjects().get(row).getStatus())) {
                    if (result instanceof Label) {
                        result.setStyleName(Styles.BOLD);
                    }
                }
                return result;
            }
        });
        return table;
    }

    /**
     * Invoked when a message is selected.
     * <p/>
     * This updates the act status to <em>READ</em> if it is <em>PENDING</em>, and the current user is the same as
     * that that the act is addressed to
     *
     * @param message the selected object
     */
    private void markRead(Act message) {
        Entity user = getContext().getContext().getUser();
        if (user != null) {
            if (MessageStatus.PENDING.equals(message.getStatus())) {
                ActBean bean = new ActBean(message);
                if (ObjectUtils.equals(user.getObjectReference(), bean.getNodeParticipantRef("to"))) {
                    message.setStatus(MessageStatus.READ);
                    bean.save();
                    Act select = getSelected();
                    getTable().getTable().getModel().refresh();
                    setSelected(select);
                }
            }
        }
    }
}
