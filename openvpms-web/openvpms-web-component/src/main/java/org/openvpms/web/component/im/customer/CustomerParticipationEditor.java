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

package org.openvpms.web.component.im.customer;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ContextHelper;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.act.ParticipationEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.patient.PatientParticipationEditor;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.property.Property;


/**
 * Participation editor for customer. This updates the context with the selected
 * customer.
 *
 * @author Tim Anderson
 */
public class CustomerParticipationEditor extends ParticipationEditor<Party> {

    /**
     * The associated patient participation editor. May be {@code null}.
     */
    private PatientParticipationEditor patientEditor;


    /**
     * Constructs a {@link CustomerParticipationEditor}.
     *
     * @param participation the object to edit
     * @param parent        the parent object
     * @param layout        the layout context. May be {@code null}
     */
    public CustomerParticipationEditor(Participation participation, Act parent, LayoutContext layout) {
        super(participation, parent, layout);
        if (!TypeHelper.isA(participation, "participation.customer")) {
            throw new IllegalArgumentException("Invalid participation type:"
                                               + participation.getArchetypeId().getShortName());
        }
        Context context = getLayoutContext().getContext();
        IMObjectReference customerRef = participation.getEntity();
        if (customerRef == null && parent.isNew()) {
            Party customer = context.getCustomer();
            setEntity(customer);
        } else {
            // add the existing customer to the context
            Party customer = (Party) getObject(customerRef);
            if (customer != null && customer != context.getCustomer()) {
                ContextHelper.setCustomer(context, customer);
            }
        }
    }

    /**
     * Associates a patient participation editor with this.
     * <p>
     * If non-null, the patient will be updated if it is selected in the customer browser.
     * <p>
     * The patient participation editor's
     * {@link PatientParticipationEditor#setCustomerParticipationEditor setCustomerParticipationEditor} method will be
     * invoked, passing this instance.
     *
     * @param editor the patient participation editor. May be {@code null}
     */
    public void setPatientParticipationEditor(PatientParticipationEditor editor) {
        patientEditor = editor;
        if (patientEditor != null) {
            patientEditor.setCustomerParticipationEditor(this);
        }
    }

    /**
     * Creates a new object reference editor.
     *
     * @param property the reference property
     * @return a new object reference editor
     */
    @Override
    protected IMObjectReferenceEditor<Party> createEntityEditor(
            Property property) {
        LayoutContext context = getLayoutContext();
        LayoutContext subContext = new DefaultLayoutContext(context, context.getHelpContext().topic("customer"));
        return new CustomerReferenceEditor(property, getParent(), subContext, true) {

            /**
             * Invoked when an object is selected from a browser.
             *
             * @param object  the selected object. May be {@code null}
             * @param browser the browser
             */
            @Override
            protected void onSelected(Party object, Browser<Party> browser) {
                super.onSelected(object, browser);
                if (patientEditor != null && browser instanceof CustomerBrowser) {
                    Party patient = ((CustomerBrowser) browser).getPatient();
                    if (patient != null) {
                        patientEditor.setEntity(patient);
                    }
                }
            }
        };
    }
}
