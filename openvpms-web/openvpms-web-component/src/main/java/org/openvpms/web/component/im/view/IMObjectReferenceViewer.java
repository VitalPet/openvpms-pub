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

package org.openvpms.web.component.im.view;

import echopointng.LabelEx;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.model.object.Reference;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ContextSwitchListener;
import org.openvpms.web.component.app.DefaultContextSwitchListener;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;


/**
 * Viewer for {@link Reference}s.
 *
 * @author Tim Anderson
 */
public class IMObjectReferenceViewer {

    /**
     * The reference to view.
     */
    private final Reference reference;

    /**
     * The name.
     */
    private final String name;

    /**
     * The listener to invoke if the hyperlink is selected. May be {@code null}
     */
    private final ActionListener linkListener;

    /**
     * The context switch listener, to notify when an object hyperlink is selected. May be {@code null}
     */
    private final ContextSwitchListener listener;

    /**
     * The context.
     */
    private final Context context;

    /**
     * The style name.
     */
    private String style;

    /**
     * The width, in pixels.
     */
    private int width = -1;


    /**
     * Constructs a {@link IMObjectReferenceViewer}.
     *
     * @param reference the reference to view. May be {@code null}
     * @param link      if {@code true} enable an hyperlink to the object
     * @param context   the context
     */
    public IMObjectReferenceViewer(Reference reference, boolean link, Context context) {
        this(reference, null, link, context);
    }

    /**
     * Constructs a {@link IMObjectReferenceViewer}.
     *
     * @param reference the reference to view. May be {@code null}
     * @param listener  the listener to notify. May be {@code null}
     * @param context   the context
     */
    public IMObjectReferenceViewer(Reference reference, ContextSwitchListener listener, Context context) {
        this(reference, null, listener, context);
    }

    /**
     * Constructs a {@link IMObjectReferenceViewer}.
     *
     * @param reference the reference to view. May be {@code null}
     * @param name      the object name. May be {@code null}
     * @param link      if {@code true} enable an hyperlink to the object
     * @param context   the context
     */
    public IMObjectReferenceViewer(Reference reference, String name, boolean link, Context context) {
        this(reference, name, (link) ? DefaultContextSwitchListener.INSTANCE : null, context);
    }

    /**
     * Constructs a {@link IMObjectReferenceViewer}.
     *
     * @param reference the reference to view. May be {@code null}
     * @param name      the object name. May be {@code null}
     * @param listener  the listener to notify. May be {@code null}
     * @param context   the context
     */
    public IMObjectReferenceViewer(Reference reference, String name, ContextSwitchListener listener, Context context) {
        this.reference = reference;
        this.name = name;
        this.listener = listener;
        if (listener != null) {
            linkListener = new ActionListener() {
                public void onAction(ActionEvent event) {
                    onView();
                }
            };
        } else {
            linkListener = null;
        }
        this.context = context;
    }


    /**
     * Constructs a {@link IMObjectReferenceViewer} that invokes an action listenerwhen the reference is selected.
     *
     * @param reference the reference to view. May be {@code null}
     * @param name      the object name. May be {@code null}
     * @param listener  the listener to notify. May be {@code null}
     * @param context   the context
     */
    public IMObjectReferenceViewer(Reference reference, String name, final ActionListener listener, Context context) {
        this.reference = reference;
        this.name = name;
        this.listener = null;
        this.linkListener = new ActionListener() {
            public void onAction(ActionEvent event) {
                event = new ActionEvent(IMObjectReferenceViewer.this, event.getActionCommand());
                listener.actionPerformed(event);
            }
        };
        this.context = context;
    }

    /**
     * Sets the style name.
     *
     * @param style name
     */
    public void setStyleName(String style) {
        this.style = style;
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    public Component getComponent() {
        Component result;
        String text = name;
        if (text == null) {
            text = IMObjectHelper.getName(reference);
            if (text != null) {
                text = Messages.format("imobject.name", text);
            } else {
                text = Messages.get("imobject.none");
            }
        }
        if (text != null) {
            if (linkListener != null) {
                if (style == null) {
                    style = "hyperlink";
                }
                Button button = ButtonFactory.create(null, style, false);
                button.setText(text);
                button.addActionListener(linkListener);
                button.setFocusTraversalParticipant(false);
                if (width != -1) {
                    button.setWidth(new Extent(width));
                    result = button;
                } else {
                    // wrap in a row so the button renders to its minimum width
                    result = RowFactory.create(button);
                }
            } else {
                result = createLabel(text);
            }
        } else {
            result = createLabel(Messages.get("imobject.none"));
        }
        return result;

    }

    /**
     * Returns the object reference.
     *
     * @return the object reference. May be {@code null}
     */
    public Reference getReference() {
        return reference;
    }

    /**
     * Sets the width of the viewer, in pixels.
     *
     * @param width the width or {@code -1} to have a dynamic width
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Returns the width of the viewer, in pixels.
     *
     * @return the width or {@code -1} indicating a dynamic width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Views the object.
     */
    protected void onView() {
        IMObject object = IMObjectHelper.getObject(reference, context);
        if (object != null) {
            listener.switchTo(object);
        }
    }

    /**
     * Creates a label.
     *
     * @param text the text
     * @return the label
     */
    private Label createLabel(String text) {
        Label result;
        if (width > 0) {
            LabelEx label = new LabelEx();
            label.setWidth(new Extent(width));
            result = label;
        } else {
            result = new Label();
        }
        result.setStyleName(style != null ? style : Styles.DEFAULT);
        result.setText(text);
        return result;
    }
}
