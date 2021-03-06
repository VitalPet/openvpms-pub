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

package org.openvpms.web.workspace.admin.hl7;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.hl7.laboratory.Laboratories;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.system.ServiceHelper;

/**
 * An editor for <em>entity.HL7ServiceLaboratoryGroup</em> archetypes.
 * <p/>
 * This ensures that laboratories have different practice locations.
 *
 * @author Tim Anderson
 */
public class LaboratoryGroupEditor extends HL7ServiceGroupEditor {

    /**
     * Constructs a {@link {LaboratoryGroupEditor}}.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context
     */
    public LaboratoryGroupEditor(Entity object, IMObject parent, LayoutContext layoutContext) {
        super(object, parent, ServiceHelper.getBean(Laboratories.class), layoutContext);
    }

}


