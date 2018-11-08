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

package org.openvpms.hl7.io;

import org.openvpms.archetype.rules.doc.AbstractTextDocumentHandler;
import org.openvpms.component.business.service.archetype.IArchetypeService;

/**
 * Handler for HL7 message documents.
 *
 * @author Tim Anderson
 */
public class HL7DocumentHandler extends AbstractTextDocumentHandler {

    /**
     * Constructs an {@link HL7DocumentHandler}.
     *
     * @param service the archetype service
     */
    public HL7DocumentHandler(IArchetypeService service) {
        super("document.HL7", service);
    }

}
