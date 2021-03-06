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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.etl.load;


/**
 * Listener for {@link Loader} errors.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface ErrorListener {

    /**
     * Invoked when an error occurs for a particular row.
     *
     * @param rowId     the identifier of the row that triggered the error
     * @param message   the error message
     * @param exception the exception
     */
    void error(String rowId, String message, Throwable exception);

    /**
     * Invoked when an error occurs.
     *
     * @param message   the error message
     * @param exception the exception
     */
    void error(String message, Throwable exception);
}
