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

package org.openvpms.archetype.component.processor;

import org.openvpms.component.exception.OpenVPMSException;


/**
 * A processor that takes an object, and performs operations on it.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 */
public interface Processor<Type> {

    /**
     * Process an object.
     *
     * @param object the object to process
     * @throws OpenVPMSException for any error
     */
    void process(Type object);

}
