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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: PropertyCollection.java 320 2005-12-05 11:53:29Z jalateras $
 */


package org.openvpms.component.business.domain.im.datatypes.property;

// java core
import java.util.Collection;
/**
 * Marker interface for a class that holds a collection of properties
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate: 2005-12-05 11:53:29 +0000 (Mon, 05 Dec 2005) $
 */
public interface PropertyCollection {
    /**
     * Return the collection
     * 
     * @return Collection
     */
    public Collection values();
}
