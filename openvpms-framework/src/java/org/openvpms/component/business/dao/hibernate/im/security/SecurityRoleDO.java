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

package org.openvpms.component.business.dao.hibernate.im.security;

import org.openvpms.component.business.dao.hibernate.im.common.IMObjectDO;
import org.openvpms.component.business.domain.im.security.SecurityRole;

import java.util.Set;


/**
 * Data object interface corresponding to the {@link SecurityRole} class.
 *
 * @author Jim Alateras
 * @author Tim Anderson
 */
public interface SecurityRoleDO extends IMObjectDO {

    /**
     * Returns the archetype authorities.
     *
     * @return the authorities
     */
    Set<ArchetypeAuthorityDO> getAuthorities();

    /**
     * Adds an authority.
     *
     * @param authority the authority to add
     */
    void addAuthority(ArchetypeAuthorityDO authority);

    /**
     * Removes an authority.
     *
     * @param authority the authhority to remove
     */
    void removeAuthority(ArchetypeAuthorityDO authority);

}
