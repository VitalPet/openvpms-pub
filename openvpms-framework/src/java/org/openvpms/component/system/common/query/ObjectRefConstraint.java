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


package org.openvpms.component.system.common.query;

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.model.object.Reference;


/**
 * An object reference constraint is a linkId constraint on a specific
 * archetypeId.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 */
public class ObjectRefConstraint extends ArchetypeIdConstraint {

    /**
     * Default SUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The id.
     */
    private long id;


    /**
     * Construct a constraint using the specified reference.
     *
     * @param reference the object reference
     */
    public ObjectRefConstraint(IMObjectReference reference) {
        this(null, reference);
    }

    /**
     * Construct a constraint using the specified reference.
     *
     * @param alias     the type alias. May be <code>null</code>
     * @param reference the object reference
     */
    public ObjectRefConstraint(String alias, Reference reference) {
        super(alias, new ArchetypeId(reference.getArchetype()), false);
        this.id = reference.getId();
    }

    /**
     * Returns the object id.
     *
     * @return the object id
     */
    public long getId() {
        return id;
    }
}
