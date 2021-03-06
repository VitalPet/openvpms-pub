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

package org.openvpms.web.component.im.edit;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.property.AbstractModifiable;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.ErrorListener;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.ValidationHelper;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Abstract implementation of the {@link CollectionPropertyEditor} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractCollectionPropertyEditor extends AbstractModifiable implements CollectionPropertyEditor {

    /**
     * The property being edited.
     */
    private final CollectionProperty property;

    /**
     * The set of edited objects.
     */
    private final Set<IMObject> edited = new HashSet<>();

    /**
     * The editors. Where present, these will be responsible for saving/removing
     * the associated object.
     */
    private Map<IMObject, IMObjectEditor> editors = new HashMap<>();

    /**
     * Indicates if any object has been saved.
     */
    private boolean saved;

    /**
     * The remove handler.
     */
    private RemoveHandler handler;


    /**
     * Constructs an {@link AbstractCollectionPropertyEditor}.
     *
     * @param property the collection property
     */
    public AbstractCollectionPropertyEditor(CollectionProperty property) {
        this.property = property;
    }

    /**
     * Returns the collection property.
     *
     * @return the property
     */
    public CollectionProperty getProperty() {
        return property;
    }

    /**
     * Returns the range of archetypes that the collection may contain.
     * Any wildcards are expanded.
     *
     * @return the range of archetypes
     */
    public String[] getArchetypeRange() {
        return property.getArchetypeRange();
    }

    /**
     * Adds an object to the collection, if it doesn't exist.
     *
     * @param object the object to add
     * @return {@code true} if the object was added, otherwise {@code false}
     */
    public boolean add(IMObject object) {
        boolean added = false;
        if (!property.getValues().contains(object)) {
            resetValid();
            property.add(object);
            added = true;
        }
        addEdited(object);
        return added;
    }

    /**
     * Associates an object in the collection with an editor. The editor
     * will be responsible for saving/removing it.
     *
     * @param object the object
     * @param editor the editor. Use {@code null} to remove an association
     */
    public void setEditor(IMObject object, IMObjectEditor editor) {
        resetValid();
        if (editor == null) {
            editors.remove(object);
        } else {
            editors.put(object, editor);
        }
    }

    /**
     * Returns the editor associated with an object in the collection.
     *
     * @param object the object
     * @return the associated editor, or {@code null} if none is found
     */
    public IMObjectEditor getEditor(IMObject object) {
        return editors.get(object);
    }

    /**
     * Returns the editors.
     * <p>
     * There may be fewer editors than there are objects in the collection,
     * as objects may not have an associated editor.
     *
     * @return the editors
     */
    public Collection<IMObjectEditor> getEditors() {
        return editors.values();
    }

    /**
     * Removes an object from the collection.
     * This removes any associated editor.
     *
     * @param object the object to remove
     * @return {@code true} if the object was removed
     */
    public boolean remove(IMObject object) {
        boolean result = property.getValues().contains(object);
        result |= removeEdited(object);
        if (result) {
            resetValid();
            property.remove(object); // will notify listeners, so invoke last
        }
        return result;
    }

    /**
     * Determines if the collection has been modified.
     *
     * @return {@code true} if the collection has been modified
     */
    public boolean isModified() {
        boolean modified = property.isModified();
        if (!modified) {
            for (IMObjectEditor editor : editors.values()) {
                if (editor.isModified()) {
                    modified = true;
                    break;
                }
            }
        }
        return modified;
    }

    /**
     * Clears the modified status of the object.
     */
    public void clearModified() {
        property.clearModified();
        for (IMObjectEditor editor : editors.values()) {
            editor.clearModified();
        }
    }

    /**
     * Adds a listener to be notified when this changes.
     *
     * @param listener the listener to add
     */
    public void addModifiableListener(ModifiableListener listener) {
        property.addModifiableListener(listener);
    }

    /**
     * Adds a listener to be notified when this changes, specifying the order of the listener.
     *
     * @param listener the listener to add
     * @param index    the index to add the listener at. The 0-index listener is notified first
     */
    public void addModifiableListener(ModifiableListener listener, int index) {
        property.addModifiableListener(listener, index);
    }

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    public void removeModifiableListener(ModifiableListener listener) {
        property.removeModifiableListener(listener);
    }

    /**
     * Sets a listener to be notified of errors.
     *
     * @param listener the listener to register. May be {@code null}
     */
    @Override
    public void setErrorListener(ErrorListener listener) {
        property.setErrorListener(listener);
    }

    /**
     * Returns the listener to be notified of errors.
     *
     * @return the listener. May be {@code null}
     */
    @Override
    public ErrorListener getErrorListener() {
        return property.getErrorListener();
    }

    /**
     * Saves any edits.
     */
    public void save() {
        doSave();
        clearModified();
    }

    /**
     * Determines if any edits have been saved.
     *
     * @return {@code true} if edits have been saved.
     */
    public boolean isSaved() {
        return saved;
    }


    /**
     * Returns the objects in the collection.
     *
     * @return the objects in the collection
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends IMObject> List<T> getObjects() {
        List<T> objects = Collections.emptyList();
        List values = property.getValues();
        int size = values.size();
        if (size != 0) {
            objects = new ArrayList<>();
            for (Object value : values) {
                objects.add((T) value);
            }
        }
        return objects;
    }

    /**
     * Returns the objects in the collection, selected by a predicate.
     *
     * @return the selected objects
     */
    @Override
    public <T extends IMObject> List<T> getObjects(Predicate<T> predicate) {
        List<T> result;
        List<T> objects = getObjects();
        if (!objects.isEmpty()) {
            result = new ArrayList<>();
            CollectionUtils.select(objects, predicate, result);
        } else {
            result = Collections.emptyList();
        }
        return result;
    }

    /**
     * Returns the minimum cardinality.
     *
     * @return the minimum cardinality
     */
    public int getMinCardinality() {
        return property.getMinCardinality();
    }

    /**
     * Returns the maximum cardinality.
     *
     * @return the maximum cardinality, or {@code -1} if it is unbounded
     */
    public int getMaxCardinality() {
        return property.getMaxCardinality();
    }

    /**
     * Registers a handler to be notified to remove an object from the collection.
     * <p>
     * The handler is only invoked when the collection is saved. It takes on the responsibility of object removal.
     *
     * @param handler the handler. May be {@code null}
     */
    @Override
    public void setRemoveHandler(RemoveHandler handler) {
        this.handler = handler;
    }

    /**
     * Returns the removal handler.
     *
     * @return the handler. May be {@code null}
     */
    @Override
    public RemoveHandler getRemoveHandler() {
        return handler;
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    protected boolean doValidation(Validator validator) {
        boolean result = validator.validate(property);
        IArchetypeService service = ServiceHelper.getArchetypeService();
        for (IMObject object : getObjects()) {
            if (!doValidation(object, validator, service)) {
                result = false;
                break;
            }
        }
        return result;
    }

    /**
     * Validates an object.
     *
     * @param object    the object to validate
     * @param validator the  validator
     * @param service   the archetype service
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    protected boolean doValidation(IMObject object, Validator validator, IArchetypeService service) {
        boolean result = true;
        IMObjectEditor editor = getEditor(object);
        if (editor != null) {
            if (!validator.validate(editor)) {
                result = false;
            }
        } else {
            List<ValidatorError> errors = ValidationHelper.validate(object, service);
            if (errors != null) {
                validator.add(property, errors);
                result = false;
            }
        }
        return result;
    }

    /**
     * Resets the cached validity state of the object.
     *
     * @param descendants if {@code true} reset the validity state of any descendants as well.
     */
    @Override
    protected void resetValid(boolean descendants) {
        super.resetValid(descendants);
        if (descendants) {
            for (IMObjectEditor editor : editors.values()) {
                editor.resetValid();
            }
        }
    }

    /**
     * Saves the collection.
     *
     * @throws OpenVPMSException if the save fails
     */
    protected void doSave() {
        saved = false;
        if (!edited.isEmpty() || !editors.isEmpty()) {
            for (IMObjectEditor editor : editors.values()) {
                editor.save();
                edited.remove(editor.getObject());
                saved = true;
            }

            // now save objects with no associated editor
            IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
            IMObject[] edited = this.edited.toArray(new IMObject[this.edited.size()]);
            for (IMObject object : edited) {
                save(object, service);
                this.edited.remove(object);
                saved = true;
            }
        }
    }

    /**
     * Saves an object.
     *
     * @param object  the object to save
     * @param service the archetype service
     */
    protected void save(IMObject object, IArchetypeService service) {
        service.save(object);
    }

    /**
     * Sets the saved state.
     *
     * @param saved if {@code true} indicates that this has been saved
     */
    protected void setSaved(boolean saved) {
        this.saved = saved;
    }

    /**
     * Adds an object to the set of objects to save when the collection is
     * saved.
     *
     * @param object the edited object
     */
    protected void addEdited(IMObject object) {
        if (edited.add(object)) {
            resetValid(false);
        }
    }

    /**
     * Removes an object from the the set of objects to save.
     * This removes any associated editor.
     *
     * @param object the object to remove
     * @return {@code true} if the the object was being edited
     */
    protected boolean removeEdited(IMObject object) {
        boolean result = editors.remove(object) != null;
        result |= edited.remove(object);
        if (result) {
            resetValid(false);
        }
        return result;
    }
}
