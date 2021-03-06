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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.layout;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.layout.ColumnLayoutData;
import nextapp.echo2.app.layout.RowLayoutData;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.filter.ComplexNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.RowFactory;

import java.util.List;


/**
 * Layout strategy that adds a button to expand/collapse the layout.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2013-07-17 01:58:27 +0000 (Wed, 17 Jul 2013) $
 */
public class ExpandableLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Determines if only required nodes should be shown.
     */
    private boolean _showOptional;

    /**
     * Determines if the button should be included.
     */
    private final boolean _showButton;

    /**
     * Button indicating to expand/collapse the layout.
     */
    private Button _button;


    /**
     * Construct a new <code>ExpandableLayoutStrategy</code>.
     */
    public ExpandableLayoutStrategy() {
        this(false, true);
    }

    /**
     * Construct a new <code>ExpandableLayoutStrategy</code>.
     *
     * @param showOptional if <code>true</code> show optional fields as well as
     *                     mandatory ones.
     */
    public ExpandableLayoutStrategy(boolean showOptional) {
        this(showOptional, true);
    }

    /**
     * Construct a new <code>ExpandableLayoutStrategy</code>.
     *
     * @param showOptional if <code>true</code> show optional fields as well as mandatory ones.
     * @param showButton   if <tt>true</tt> show button to expand/collapse the layout
     */
    public ExpandableLayoutStrategy(boolean showOptional, boolean showButton) {
        _showOptional = showOptional;
        _showButton = showButton;
    }

    /**
     * Apply the layout strategy.
     * <p/>
     * This renders an object in a <code>Component</code>, using a factory to
     * create the child components.
     *
     * @param object     the object to apply
     * @param properties the object's properties
     * @param parent     the parent object. May be <code>null</code>
     * @param context    the layout context
     * @return the component containing the rendered <code>object</code>
     */
    @Override
    public ComponentState apply(IMObject object, PropertySet properties,
                                IMObject parent, LayoutContext context) {
        _button = null;
        return super.apply(object, properties, parent, context);
    }

    /**
     * Returns the button to expand/collapse the layout.
     *
     * @return the layout button, or <code>null</code> if the layout cannot be
     *         expanded/collapsed
     */
    public Button getButton() {
        return _button;
    }

    /**
     * Lay out out the object in the specified container.
     *
     * @param object     the object to lay out
     * @param properties the object's properties
     * @param parent     the parent object. May be <tt>null</tt>
     * @param container  the container to use
     * @param context    the layout context
     */
    @Override
    protected void doLayout(IMObject object, PropertySet properties, IMObject parent, Component container,
                            LayoutContext context) {
        super.doLayout(object, properties, parent, container, context);
        if (_button == null && _showButton) {
            Row row = getButtonRow();
            ColumnLayoutData right = new ColumnLayoutData();
            right.setAlignment(new Alignment(Alignment.RIGHT, Alignment.TOP));
            row.setLayoutData(right);
            container.add(row);
        }
    }

    /**
     * Lays out child components in a 2x2 grid.
     *
     * @param object     the object to lay out
     * @param parent     the parent object. May be <tt>null</tt>
     * @param properties the properties
     * @param container  the container to use
     * @param context    the layout context
     */
    @Override
    protected void doSimpleLayout(IMObject object, IMObject parent, List<Property> properties,
                                  Component container, LayoutContext context) {
        if (_button != null || !_showButton) {
            super.doSimpleLayout(object, parent, properties, container,
                                 context);
        } else if (!properties.isEmpty()) {
            Row group = RowFactory.create();
            super.doSimpleLayout(object, parent, properties, group,
                                 context);
            group.add(getButtonRow());
            container.add(group);
        }
    }

    /**
     * Lays out each child component in a tabbed pane.
     *
     * @param object     the object to lay out
     * @param parent     the parent object. May be <tt>null</tt>
     * @param properties the properties
     * @param container  the container to use
     * @param context    the layout context
     */
    @Override
    protected void doComplexLayout(IMObject object, IMObject parent,
                                   List<Property> properties, Component container, LayoutContext context) {
        if (_button == null && _showButton && !properties.isEmpty()) {
            Row row = getButtonRow();
            ColumnLayoutData right = new ColumnLayoutData();
            right.setAlignment(new Alignment(Alignment.RIGHT, Alignment.TOP));
            row.setLayoutData(right);
            container.add(row);
        }
        super.doComplexLayout(object, parent, properties, container,
                              context);
    }

    /**
     * Returns a node filter.
     *
     * @param object  the object to filter nodes for
     * @param context the context
     * @return a node filter to filter nodes
     */
    @Override
    protected NodeFilter getNodeFilter(IMObject object, LayoutContext context) {
        return new ComplexNodeFilter(_showOptional, false);
    }

    /**
     * Determines if the layout button should be shown.
     *
     * @return <code>true</code> if the layout button should be included;
     *         otherwise <code>false</code>
     */
    protected boolean showButton() {
        return _showButton;
    }

    /**
     * Creates a row with the layout button in the top right.
     *
     * @return a new row
     */
    protected Row getButtonRow() {
        String key = (_showOptional) ? "minus" : "plus";
        _button = ButtonFactory.create(key);
        RowLayoutData right = new RowLayoutData();
        right.setAlignment(new Alignment(Alignment.RIGHT, Alignment.TOP));
        right.setWidth(new Extent(100, Extent.PERCENT));
        _button.setLayoutData(right);
        Row wrapper = new Row();
        wrapper.add(_button);
        wrapper.setLayoutData(right);
        return wrapper;
    }

    public boolean isShowOptional() {
        return _showOptional;
    }

    public void setShowOptional(boolean showOptional) {
        _showOptional = showOptional;
    }

}
