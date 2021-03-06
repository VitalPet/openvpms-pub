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
 */

package org.openvpms.web.component.im.doc;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;


/**
 * Layout strategy for <em>participation.documentTemplate</em> participation
 * relationships. This navigates the entity and its corresponding
 * {@code DocumentAct}, enabling any associated document do be downloaded.
 *
 * @author Tim Anderson
 */
public class DocumentTemplateParticipationLayoutStrategy
    implements IMObjectLayoutStrategy {

    /**
     * Pre-registers a component for inclusion in the layout.
     * <p/>
     * This implementation is a no-op.
     *
     * @param state the component state
     */
    public void addComponent(ComponentState state) {
        // do nothing
    }

    /**
     * Apply the layout strategy.
     * <p/>
     * This renders an object in a {@code Component}, using a factory to
     * create the child components.
     *
     * @param object     the object to apply
     * @param properties the object's properties
     * @param parent     the parent object. May be {@code null}
     * @param context    the layout context
     * @return the component containing the rendered {@code object}
     */
    public ComponentState apply(IMObject object, PropertySet properties,
                                IMObject parent, LayoutContext context) {
        Property property = properties.get("entity");
        IMObjectReference ref = (IMObjectReference) property.getValue();
        IMObjectReferenceViewer viewer = new IMObjectReferenceViewer(ref, context.getContextSwitchListener(),
                                                                     context.getContext());
        return new ComponentState(viewer.getComponent());
    }

}
