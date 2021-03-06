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

package org.openvpms.web.component.bound;

import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.factory.ComponentFactory;
import org.openvpms.web.echo.style.Styles;

/**
 * Factory for {@link BoundDateTimeField}s.
 *
 * @author Tim Anderson
 */
public class BoundDateTimeFieldFactory extends ComponentFactory {

    /**
     * Creates a new bound date-time field with the default style.
     *
     * @param property the property to bind
     * @return a new bound date-time field
     */
    public static BoundDateTimeField create(Property property) {
        BoundDateTimeField field = new BoundDateTimeField(property);
        field.setStyleName(Styles.DEFAULT);
        return field;
    }

}
