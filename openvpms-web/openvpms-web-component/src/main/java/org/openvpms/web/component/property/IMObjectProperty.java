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

package org.openvpms.web.component.property;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.archetype.descriptor.DescriptorException;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.component.model.archetype.AssertionDescriptor;
import org.openvpms.component.service.archetype.ValidationError;
import org.openvpms.web.component.im.util.ObjectHelper;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * Represents a property of an {@link IMObject}.
 *
 * @author Tim Anderson
 */
public class IMObjectProperty extends AbstractProperty
        implements CollectionProperty {

    /**
     * The object that the property belongs to.
     */
    private final IMObject object;

    /**
     * The property descriptor.
     */
    private final NodeDescriptor descriptor;

    /**
     * Current validation errors.
     */
    private List<ValidatorError> validationErrors;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(IMObjectProperty.class);


    /**
     * Constructs an {@link IMObjectProperty}.
     *
     * @param object     the object that the property belongs to
     * @param descriptor the property descriptor
     */
    public IMObjectProperty(IMObject object, NodeDescriptor descriptor) {
        this.object = object;
        this.descriptor = descriptor;
    }

    /**
     * Returns the parent object.
     *
     * @return the parent object
     */
    public IMObject getParent() {
        return object;
    }

    /**
     * Returns the property descriptor.
     *
     * @return the property descriptor
     */
    public NodeDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Returns the value of the property.
     *
     * @return the property value
     */
    public Object getValue() {
        return descriptor.getValue(object);
    }

    /**
     * Sets the value of the property.
     * The value will only be set if it is valid, and different to the existing
     * value. If the value is set, any listeners will be notified.
     *
     * @param value the property value
     * @return {@code true} if the value was set, {@code false} if it
     *         cannot be set due to error, or is the same as the existing value
     */
    public boolean setValue(Object value) {
        boolean set = false;
        checkModifiable();
        try {
            value = getTransformer().apply(value);
            if (!ObjectHelper.equals(getValue(), value)) {
                descriptor.setValue(object, value);
                set = true;
                refresh();
            } else if (validationErrors != null) {
                // a previous set triggered an error, and didn't update the value. If a new update occurs
                // but has the same value, need to clear any errors
                refresh();
            }
        } catch (DescriptorException exception) {
            invalidate(exception);
        } catch (ValidationException exception) {
            invalidate(exception);
        } catch (PropertyException exception) {
            invalidate(exception);
        }
        return set;
    }

    /**
     * Returns the property name.
     *
     * @return the property name
     */
    public String getName() {
        return descriptor.getName();
    }

    /**
     * Returns the property display name.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return descriptor.getDisplayName();
    }

    /**
     * Returns the property description.
     *
     * @return the description. May be {@code null}
     */
    public String getDescription() {
        return descriptor.getDescription();
    }

    /**
     * Returns the minimum length of the property.
     *
     * @return the minimum length
     */
    public int getMinLength() {
        return descriptor.getMinLength();
    }

    /**
     * Returns the maximum length of the property.
     *
     * @return the maximum length, or {@code -1} if it is unbounded
     */
    public int getMaxLength() {
        return descriptor.getMaxLength();
    }

    /**
     * Returns the property type.
     *
     * @return the property type
     */
    public Class getType() {
        return descriptor.getClazz();
    }

    /**
     * Determines if the property is a boolean.
     *
     * @return {@code true} if it is a boolean
     */
    public boolean isBoolean() {
        return descriptor.isBoolean();
    }

    /**
     * Determines if the property is a string.
     *
     * @return {@code true} if it is a string
     */
    public boolean isString() {
        return descriptor.isString();
    }

    /**
     * Determines if the property is numeric.
     *
     * @return {@code true} if it is numeric
     */
    public boolean isNumeric() {
        return descriptor.isNumeric();
    }

    /**
     * Determines if the property is a date.
     *
     * @return {@code true} if it is a date
     */
    public boolean isDate() {
        return descriptor.isDate();
    }

    /**
     * Determines if the property is a money property.
     *
     * @return {@code true} it is a money property
     */
    public boolean isMoney() {
        return descriptor.isMoney();
    }

    /**
     * Determines if the property is an object reference.
     *
     * @return {@code true} if it is an object reference
     */
    public boolean isObjectReference() {
        return descriptor.isObjectReference();
    }

    /**
     * Determines if the property is a lookup.
     *
     * @return {@code true} if it is a lookup
     */
    public boolean isLookup() {
        return descriptor.isLookup();
    }

    /**
     * Determines if the property is a password.
     *
     * @return {@code true} if it is a password
     */
    @Override
    public boolean isPassword() {
        return descriptor.containsAssertionType("password");
    }

    /**
     * Determines if the property is a collection.
     *
     * @return {@code true} if it is a collection
     */
    public boolean isCollection() {
        return descriptor.isCollection();
    }

    /**
     * Returns the archetype short names that this collection may support.
     * <p/>
     * Wildcards are expanded.
     *
     * @return the archetype short names
     * @throws ArchetypeServiceException for any error
     */
    public String[] getArchetypeRange() {
        return DescriptorHelper.getShortNames(descriptor);
    }

    /**
     * Determines if the property value is derived from an expression.
     *
     * @return {@code true} if the value is derived, otherwise {@code false}
     */
    public boolean isDerived() {
        return descriptor.isDerived();
    }

    /**
     * Determines if the property is read-only.
     *
     * @return {@code true} if the property is read-only
     */
    public boolean isReadOnly() {
        return descriptor.isReadOnly();
    }

    /**
     * Determines if the property is hidden.
     *
     * @return {@code true} if the property is hidden; otherwise {@code false}
     */
    public boolean isHidden() {
        return descriptor.isHidden();
    }

    /**
     * Determines if the property is required.
     *
     * @return {@code true} if the property is required; otherwise {@code false}
     */
    public boolean isRequired() {
        return descriptor.isRequired();
    }

    /**
     * Returns the collection.
     *
     * @return the collection
     */
    public List getValues() {
        List<IMObject> values = null;
        try {
            values = descriptor.getChildren(object);
            if (values != null) {
                values = Collections.unmodifiableList(values);
            }
        } catch (OpenVPMSException exception) {
            log.error(exception, exception);
        }
        return values;
    }

    /**
     * Returns the no. of elements in the collection
     *
     * @return the no. of elements in the collection
     */
    public int size() {
        List<IMObject> values = descriptor.getChildren(object);
        return values != null ? values.size() : 0;
    }

    /**
     * Add a value.
     *
     * @param value the value to add
     */
    public void add(Object value) {
        checkModifiable();
        try {
            value = getTransformer().apply(value);
            descriptor.addChildToCollection(object, value);
            refresh();
        } catch (ValidationException exception) {
            invalidate(exception);
        } catch (DescriptorException exception) {
            invalidate(exception);
        } catch (PropertyException exception) {
            invalidate(exception);
        }
    }

    /**
     * Remove a value.
     *
     * @param value the value to remove
     * @return {@code true} if the value was removed
     */
    public boolean remove(Object value) {
        boolean result = false;
        checkModifiable();
        try {
            int size = size();
            value = getTransformer().apply(value);
            descriptor.removeChildFromCollection(object, value);
            if (size != size()) {
                result = true;
                refresh();
            }
        } catch (ValidationException exception) {
            invalidate(exception);
        } catch (DescriptorException exception) {
            invalidate(exception);
        } catch (PropertyException exception) {
            invalidate(exception);
        }
        return result;
    }

    /**
     * Returns the minimum cardinality.
     *
     * @return the minimum cardinality
     */
    public int getMinCardinality() {
        return descriptor.getMinCardinality();
    }

    /**
     * Returns the maximum cardinality.
     *
     * @return the maximum cardinality, or {@code -1} if it is unbounded
     */
    public int getMaxCardinality() {
        if (descriptor.getMaxCardinality() == NodeDescriptor.UNBOUNDED) {
            return -1;
        }
        return descriptor.getMaxCardinality();
    }

    /**
     * Determines the relationship of the elements of the collection to the
     * object.
     *
     * @return {@code true} if the objects are children of the parent object,
     *         or {@code false} if they are its peer
     */
    public boolean isParentChild() {
        return descriptor.isParentChild();
    }

    /**
     * Returns the object that the property belongs to.
     *
     * @return the object
     */
    public IMObject getObject() {
        return object;
    }

    /**
     * Resets the cached validity state of the object.
     *
     * @param descendants if {@code true} reset the validity state of any descendants as well.
     */
    @Override
    protected void resetValid(boolean descendants) {
        super.resetValid(descendants);
        validationErrors = null;
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    protected boolean doValidation(Validator validator) {
        List<ValidatorError> errors = null;
        if (validationErrors == null || isDerived()) {
            validationErrors = null;
            // determine if this is valid
            int minSize = getMinCardinality();
            if (minSize == 1 && getValue() == null) {
                addError("property.error.required",
                         descriptor.getDisplayName());
            } else if (descriptor.isCollection()) {
                Collection values = getValues();
                int size = values.size();
                int maxSize = getMaxCardinality();
                if (minSize != -1 && size < minSize) {
                    addError("property.error.minSize",
                             descriptor.getDisplayName(), minSize);
                } else if (maxSize != -1 && size > maxSize) {
                    addError("property.error.maxSize",
                             descriptor.getDisplayName(), maxSize);
                } else if (size != 0) {
                    // don't cache any validation errors from collection objects
                    // as these may be corrected without updating the status
                    // of this property
                    IArchetypeService service
                            = ServiceHelper.getArchetypeService();
                    for (Object value : getValues()) {
                        IMObject object = (IMObject) value;
                        errors = ValidationHelper.validate(object, service);
                        if (errors != null) {
                            break;
                        }
                    }
                }
            } else if (validateAssertions()) {
                PropertyTransformer transformer = getTransformer();
                try {
                    transformer.apply(getValue());
                } catch (PropertyException exception) {
                    invalidate(exception);
                }
            }
        }
        if (validationErrors != null) {
            errors = validationErrors;
        }
        if (errors != null) {
            validator.add(this, errors);
        }
        return (errors == null);
    }

    /**
     * Validates any assertions linked to the property.
     *
     * @return {@code true} if the assertions are valid
     */
    private boolean validateAssertions() {
        boolean result = true;
        Collection<AssertionDescriptor> assertions = descriptor.getAssertionDescriptors().values();
        Object value = getValue();
        if (value != null && !assertions.isEmpty()) {
            for (AssertionDescriptor assertion : assertions) {
                try {
                    if (!assertion.validate(value, object, descriptor)) {
                        String message = assertion.getErrorMessage();
                        if (message == null) {
                            message = "Validation failed for assertion " + assertion.getName();
                        }
                        addError(message);
                        result = false;
                        break;
                    }
                } catch (Exception exception) {
                    log.error("Assertion " + assertion.getName() + " failed for node " + descriptor, exception);
                    addError(exception.getMessage());
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Invoked when an update fails. Marks this as invalid.
     *
     * @param exception the reason for the failure
     */
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    private void invalidate(DescriptorException exception) {
        log.warn(exception.getMessage(), exception);
        resetErrors();
        Throwable cause = ExceptionUtils.getRootCause(exception);
        if (cause != null) {
            addError(cause.getMessage());
        } else {
            addError(exception.getMessage());
        }
    }

    /**
     * Invoked when an update fails. Marks this as invalid.
     *
     * @param exception the reason for the failure
     */
    private void invalidate(ValidationException exception) {
        resetErrors();
        if (!exception.getErrors().isEmpty()) {
            for (ValidationError error : exception.getErrors()) {
                addError(new ValidatorError(error));
            }
        } else {
            addError(exception.getMessage());
        }
    }

    /**
     * Invoked when an update fails. Marks this as invalid.
     *
     * @param exception the reason for the failure
     */
    private void invalidate(PropertyException exception) {
        resetErrors();
        Throwable cause = ExceptionUtils.getRootCause(exception);
        if (cause != null) {
            addError(cause.getMessage());
        } else {
            addError(exception.getMessage());
        }
    }

    /**
     * Removes any validation errors.
     */
    private void resetErrors() {
        if (validationErrors != null && !validationErrors.isEmpty()) {
            validationErrors.clear();
        }
    }

    /**
     * Adds a validation error.
     *
     * @param message the error message
     */
    private void addError(String message) {
        addError(new ValidatorError(object.getArchetypeId().getShortName(), descriptor.getName(), message));
    }

    /**
     * Adds a validation error.
     *
     * @param error the validation error
     */
    private void addError(ValidatorError error) {
        resetValid();
        if (validationErrors == null) {
            validationErrors = new ArrayList<>();
        }
        validationErrors.add(error);
    }

    /**
     * Adds a validation error.
     *
     * @param message the key of the message
     * @param args    an array of arguments to be inserted into the message
     */
    private void addError(String message, Object... args) {
        message = Messages.format(message, args);
        addError(message);
    }

}
