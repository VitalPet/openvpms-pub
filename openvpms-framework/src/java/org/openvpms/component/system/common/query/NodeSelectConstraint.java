/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.system.common.query;


/**
 * Select constraint for a node.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class NodeSelectConstraint extends SelectConstraint {

    /**
     * Constructs a new <code>NodeSelectConstraint</code>.
     *
     * @param name the name. May be a node name or a qualified name
     */
    public NodeSelectConstraint(String name) {
        this(getAlias(name), getNodeName(name));
    }

    /**
     * Constructs a new <code>NodeSelectConstraint</code>.
     *
     * @param alias    the type alias
     * @param nodeName the node name
     */
    public NodeSelectConstraint(String alias, String nodeName) {
        super(alias, nodeName);
    }

    /**
     * Returns the qualified name.
     *
     * @return the qualified name
     */
    public String getName() {
        String alias = getAlias();
        String nodeName = getNodeName();
        return alias != null ? alias + "." + nodeName : nodeName;
    }

    /**
     * Helper to return the alias from a (potentially) qualified name.
     *
     * @param name the name. May be qualified
     * @return the alias from the name, or <code>null</code> if the name is
     *         unqualified
     */
    private static String getAlias(String name) {
        int index = name.indexOf('.');
        return (index == -1) ? null : name.substring(0, index);
    }

    /**
     * Helper to return the node name from a (potentially) qualified name.
     *
     * @param name the name. May be qualified
     * @return the node name
     */
    private static String getNodeName(String name) {
        int index = name.indexOf('.');
        return (index == -1) ? name : name.substring(index + 1);
    }
}
