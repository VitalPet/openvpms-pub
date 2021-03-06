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
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.component.im.edit.act;


import org.openvpms.component.business.domain.im.product.Product;

/**
 * Notifies changes to act products.
 *
 * @author Tim Anderson
 */
public interface ProductListener {

    /**
     * Invoked when an act product changes.
     *
     * @param editor  the editor
     * @param product the product. May be {@code null}
     */
    void productChanged(ActItemEditor editor, Product product);
}