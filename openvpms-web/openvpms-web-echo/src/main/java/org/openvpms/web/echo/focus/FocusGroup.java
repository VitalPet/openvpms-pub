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

package org.openvpms.web.echo.focus;

import echopointng.DateField;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.TextField;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;


/**
 * Manages the focus traversal indexes of a set of components.
 *
 * @author Tim Anderson
 */
public class FocusGroup {

    /**
     * The symbolic name for the group. May be {@code null}.
     */
    private final String name;

    /**
     * The parent. May be {@code null}.
     */
    private FocusGroup parent;

    /**
     * Dirty flag used to indicate that re-indexing is required.
     */
    private boolean dirty = true;

    /**
     * The focus components.
     */
    private List<Object> components = new ArrayList<Object>();

    /**
     * The first focus traversal index.
     */
    private int first = -1;

    /**
     * The next focus traversal index to assign.
     */
    private int next = -1;

    /**
     * The default component to focus on.
     */
    private Component defaultFocus;


    /**
     * Constructs a {@link FocusGroup}.
     *
     * @param name a symbolic name for the group
     */
    public FocusGroup(String name) {
        this(name, -1);
    }

    /**
     * Constructs a {@link FocusGroup}.
     *
     * @param name  a symbolic name for the group
     * @param first the first focus traversal index
     */
    public FocusGroup(String name, int first) {
        this.name = name;
        this.first = first;
    }

    /**
     * Constructs a {@link FocusGroup}.
     *
     * @param name       a symbolic name for the group
     * @param components the components to add
     */
    public FocusGroup(String name, Component... components) {
        this(name);
        for (Component component : components) {
            add(component);
        }
    }

    /**
     * Returns a symbolic name for the group.
     *
     * @return a symbolic name for the group. May be {@code null}
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the parent group.
     *
     * @param group the parent group. May be {@code null}
     */
    public void setParent(FocusGroup group) {
        parent = group;
        setDirty(true);
    }

    /**
     * Returns the parent group.
     *
     * @return the parent group. May be {@code null}
     */
    public FocusGroup getParent() {
        return parent;
    }

    /**
     * Sets the default component to focus on.
     *
     * @param component the component. May be {@code null}
     */
    public void setDefault(Component component) {
        defaultFocus = component;
    }

    /**
     * Returns the default component to focus on.
     *
     * @return the component. May be {@code null}
     */
    public Component getDefaultFocus() {
        return defaultFocus;
    }

    /**
     * Determines if reindexing is required.
     *
     * @return {@code true} if reindexing is required, otherwise
     *         {@code false}
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Sets the dirty flag.
     *
     * @param dirty if {@code true}  indicates reindexing is required
     */
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    /**
     * Returns a stringified form of this.
     *
     * @return a stringified form of this
     */
    public String toString() {
        return "[" + getFirst() + "," + getLast() + "] - " + getName();
    }

    /**
     * Adds a component.
     *
     * @param component the component to add
     */
    public void add(Component component) {
        add(components.size(), component);
    }

    /**
     * Inserts a new component.
     *
     * @param index     index at which the component is to be inserted
     * @param component the component to insert
     */
    public void add(int index, Component component) {
        components.add(index, component);
        setDirty(true);
        reindex();
    }

    /**
     * Adds a new focus group.
     *
     * @param group the group to add
     */
    public void add(FocusGroup group) {
        add(components.size(), group);
    }

    /**
     * Inserts a new focus group.
     *
     * @param index index at which the set is to be inserted
     * @param group the group to insert
     */
    public void add(int index, FocusGroup group) {
        components.add(index, group);
        group.setParent(this);
        setDirty(true);
        reindex();
    }

    /**
     * Returns the index of a focus group.
     *
     * @param group the group
     * @return the index of the group, or {@code -1} if it doesn't exist
     */
    public int indexOf(FocusGroup group) {
        return components.indexOf(group);
    }

    /**
     * Removes a focus group.
     *
     * @param group the group to remove
     */
    public void remove(FocusGroup group) {
        if (components.remove(group)) {
            group.setParent(null);
        }
    }

    /**
     * Removes a component.
     *
     * @param component the component to remove
     */
    public void remove(Component component) {
        components.remove(component);
    }

    /**
     * Returns the number of immediate children in the tree.
     *
     * @return the number of immediate children in the tree
     */
    public int size() {
        return components.size();
    }

    /**
     * Returns the components.
     *
     * @return a list containing {@code Component} and {@link FocusGroup}
     *         instances.
     */
    public List<Object> getComponents() {
        return components;
    }

    /**
     * Returns the first focus traversal index.
     *
     * @return the first focus traversal index, or {@code -1} if there are
     *         no indexes
     */
    public int getFirst() {
        return first;
    }

    /**
     * Returns the last focus traversal index.
     *
     * @return the last focus traversal index, or {@code -1} if there are
     *         no indexes
     */
    public int getLast() {
        return (next != first) ? next - 1 : first;
    }

    /**
     * Resets the focus traversal indexes.
     */
    public void reindex() {
        int index = (first <= 0) ? 1 : first;
        int last = getLast();
        reindex(index);
        if (last != getLast()) {
            notifyParent();
        }
    }

    /**
     * Resets the focus traversal indexes.
     *
     * @param index the first index
     * @return the last index
     */
    public int reindex(int index) {
        if (first != index || isDirty()) {
            first = index;
            next = index;
            for (Object object : components) {
                if (object instanceof FocusGroup) {
                    FocusGroup group = (FocusGroup) object;
                    if (group.isDirty() || next > group.getFirst()) {
                        index = group.reindex(next);
                    } else {
                        index = group.getLast();
                    }
                    next = index + 1;
                } else {
                    Component component = (Component) object;
                    setIndex(component, ++next);
                }
            }
            index = next;
            setDirty(false);
        } else {
            index = getLast();
        }
        return index;
    }

    /**
     * Returns the first component that may have focus set.
     *
     * @return the first component that may have focus set, or
     *         {@code null} if none may have focus set
     */
    public Component getFocusable() {
        Component result = null;
        for (Object object : getComponents()) {
            if (object instanceof Component) {
                result = FocusHelper.getFocusable((Component) object);
            } else {
                result = ((FocusGroup) object).getFocusable();
            }
            if (result != null) {
                break;
            }
        }
        return result;
    }

    /**
     * Sets the focus.
     * <p/>
     * This will set the focus on the {@link #setDefault default} if present; otherwise it will set it on the first
     * component that may have focus set.
     */
    public void setFocus() {
        Component focus = (defaultFocus != null) ? defaultFocus : getFocusable();
        FocusHelper.setFocus(focus);
    }

    /**
     * Helper to print the state of the group.
     *
     * @param stream the stream to print to
     */
    public void print(PrintStream stream) {
        for (Object component : components) {
            if (component instanceof FocusGroup) {
                dump((FocusGroup) component, 0, stream);
            }
        }
    }

    /**
     * Helper to print the state of the group.
     *
     * @param group  the group
     * @param depth  the group depth
     * @param stream the stream to print to
     */
    protected void dump(FocusGroup group, int depth, PrintStream stream) {
        for (int i = 0; i < depth; ++i) {
            stream.print(".");
        }
        stream.println(group);
        for (Object child : group.components) {
            if (child instanceof FocusGroup) {
                dump((FocusGroup) child, depth + 1, stream);
            }
        }
    }

    /**
     * Notify the parent that it needs to reindex.
     */
    protected void notifyParent() {
        if (parent != null) {
            parent.setDirty(true);
            parent.reindex();
        }
    }

    /**
     * Sets the focus traversal index for a component.
     *
     * @param component the component
     * @param index     the =index
     */
    private void setIndex(Component component, int index) {
        component.setFocusTraversalIndex(index);
        if (component instanceof DateField) {
            // @todo workaround for dates.
            TextField text = ((DateField) component).getTextField();
            text.setFocusTraversalIndex(index);
        }
    }

}
