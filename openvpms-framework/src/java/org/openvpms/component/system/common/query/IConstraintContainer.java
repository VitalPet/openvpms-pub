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
 *  $Id: IConstraintContainer.java 716 2006-04-05 06:11:15Z jalateras $
 */


package org.openvpms.component.system.common.query;

/**
 * An interface for managing collection of constraints
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate: 2006-04-05 06:11:15 +0000 (Wed, 05 Apr 2006) $
 */
public interface IConstraintContainer extends IConstraint {
    /**
     * Add the specified constraint to the container
     * 
     * @param constraint
     *            the constraint to add
     * @return IConstraintContainer            
     */
    public IConstraintContainer add(IConstraint constraint);
    
    /**
     * Remove the specified constraint from the container
     * 
     * @param constraint
     *            the constraint to remove
     * @return IConstraintContainer            
     */
    public IConstraintContainer remove(IConstraint constraint);
}
