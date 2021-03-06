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

package org.openvpms.archetype.rules.workflow;


/**
 * Message archetype short names.
 *
 * @author Tim Anderson
 */
public class MessageArchetypes {

    /**
     * The user message archetype short name.
     */
    public static String USER = "act.userMessage";

    /**
     * The system message archetype short name.
     */
    public static String SYSTEM = "act.systemMessage";

    /**
     * The audit message archetype short name.
     */
    public static final String AUDIT = "act.auditMessage";

    /**
     * All system message archetypes.
     */
    public static String SYSTEM_MESSAGES = "act.systemMessage*";

}