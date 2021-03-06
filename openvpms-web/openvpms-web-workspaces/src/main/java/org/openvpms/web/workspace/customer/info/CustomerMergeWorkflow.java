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

package org.openvpms.web.workspace.customer.info;

import org.openvpms.archetype.rules.party.CustomerRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.ObjectRefConstraint;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.query.CustomerQuery;
import org.openvpms.web.component.workflow.SelectIMObjectTask;
import org.openvpms.web.component.workflow.SynchronousTask;
import org.openvpms.web.component.workflow.Task;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.workflow.merge.MergeWorkflow;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;


/**
 * Customer merge workflow.
 *
 * @author Tim Anderson
 */
class CustomerMergeWorkflow extends MergeWorkflow<Party> {

    /**
     * Constructs a {@link CustomerMergeWorkflow}.
     *
     * @param customer the customer to merge to
     * @param help     the help context
     */
    public CustomerMergeWorkflow(Party customer, HelpContext help) {
        super(customer, help);
        init();
    }

    /**
     * Creates a task to select the object to merge.
     *
     * @param context the context
     * @return a new select task
     */
    @Override
    protected SelectIMObjectTask<Party> createSelectTask(Context context) {
        Party customer = getObject();
        String shortName = customer.getArchetypeId().getShortName();
        CustomerQuery query = new CustomerQuery(new String[]{shortName});

        // exclude the customer being merged from the search
        query.setConstraints(Constraints.not(new ObjectRefConstraint("customer", customer.getObjectReference())));
        return new SelectIMObjectTask<>(query, getHelpContext().topic("customer"));
    }

    /**
     * Returns the merge confirmation message.
     *
     * @return the merge confirmation message
     */
    @Override
    protected String getConfirmationMessage() {
        Party customerTo = getObject();
        Party customerFrom = getContext().getCustomer();
        return Messages.format("workflow.merge.message", customerTo.getName(), customerTo.getId(),
                               customerFrom.getName(), customerFrom.getId());
    }

    /**
     * Creates the task to perform the merge.
     *
     * @return a new task
     */
    protected Task createMergeTask() {
        return new SynchronousTask() {
            public void execute(TaskContext context) {
                Party from = context.getCustomer();
                merge(from);
            }
        };
    }

    /**
     * Merges from the specified customer.
     *
     * @param from the customer to merge from
     */
    private void merge(final Party from) {
        TransactionTemplate template = new TransactionTemplate(
                ServiceHelper.getTransactionManager());
        template.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status) {
                CustomerRules rules = new CustomerRules(ServiceHelper.getArchetypeService(),
                                                        ServiceHelper.getLookupService());
                rules.mergeCustomers(from, getObject());
                return true;
            }
        });
    }
}
