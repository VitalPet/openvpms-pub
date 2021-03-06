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

package org.openvpms.web.component.im.edit.act;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.openvpms.archetype.rules.act.ActCopyHandler;
import org.openvpms.archetype.rules.math.Weight;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopier;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.CollectionPropertyEditor;
import org.openvpms.web.component.im.edit.CollectionResultSetFactory;
import org.openvpms.web.component.im.edit.DefaultCollectionResultSetFactory;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.MultipleRelationshipCollectionTargetEditor;
import org.openvpms.web.component.im.view.ReadOnlyComponentFactory;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.system.ServiceHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.COUNTER_ITEM;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.CREDIT_ITEM;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.INVOICE_ITEM;
import static org.openvpms.archetype.rules.product.ProductArchetypes.TEMPLATE;


/**
 * Editor for collections of {@link ActRelationship}s.
 *
 * @author Tim Anderson
 */
public class ActRelationshipCollectionEditor extends MultipleRelationshipCollectionTargetEditor {

    /**
     * Listener for product change events.
     */
    private final ProductListener productListener;

    /**
     * Determines if a new object has been modified since its editor was created (i.e has been user modified).
     */
    private Map<IMObjectReference, Boolean> modified = new HashMap<>();

    /**
     * Determines if a new object with default values should be excluded from commit.
     */
    private boolean excludeDefaultValueObject = true;

    /**
     * Listener for template product expansion events. May be {@code null}
     */
    private TemplateProductListener templateProductListener;


    /**
     * Constructs an {@link ActRelationshipCollectionEditor}.
     *
     * @param property the collection property
     * @param act      the parent act
     * @param context  the layout context
     */
    public ActRelationshipCollectionEditor(CollectionProperty property, Act act, LayoutContext context) {
        this(property, act, context, DefaultCollectionResultSetFactory.INSTANCE);
    }

    /**
     * Constructs an {@link ActRelationshipCollectionEditor}.
     *
     * @param property the collection property
     * @param act      the parent act
     * @param context  the layout context
     * @param factory  the result set factory
     */
    public ActRelationshipCollectionEditor(CollectionProperty property, Act act, LayoutContext context,
                                           CollectionResultSetFactory factory) {
        super(new ActRelationshipCollectionPropertyEditor(property, act), act, context, factory);
        productListener = new ProductListener() {
            public void productChanged(ActItemEditor editor, Product product) {
                onProductChanged(editor, product);
            }
        };
    }

    /**
     * Returns the set of acts being edited.
     *
     * @return the set of acts being edited.
     */
    public List<Act> getActs() {
        ActRelationshipCollectionPropertyEditor collection = getEditor();
        return new ArrayList<>(collection.getActs().keySet());
    }

    /**
     * Returns the set of acts being edited, filtered by a predicate.
     *
     * @return the filtered acts
     */
    public List<Act> getActs(Predicate<Act> predicate) {
        List<Act> result = new ArrayList<>();
        return CollectionUtils.select(getActs(), predicate, result);
    }

    /**
     * Returns the set of acts being edited, including that of the {@link #getCurrentEditor()}.
     *
     * @return the set of acts being edited
     */
    public List<Act> getCurrentActs() {
        Set<Act> result = new LinkedHashSet<>(getActs());
        IMObjectEditor current = getCurrentEditor();
        if (current != null) {
            result.add((Act) current.getObject());
        }
        return new ArrayList<>(result);
    }

    /**
     * Returns the set of acts being edited, including that of the {@link #getCurrentEditor()}, filtered by a predicate.
     *
     * @return the filtered acts
     */
    public List<Act> getCurrentActs(Predicate<Act> predicate) {
        List<Act> result = new ArrayList<>();
        return CollectionUtils.select(getCurrentActs(), predicate, result);
    }

    /**
     * Registers a listener for template product expansion events.
     *
     * @param listener the listener
     */
    public void setTemplateProductListener(TemplateProductListener listener) {
        templateProductListener = listener;
    }

    /**
     * Returns the listener for template product expansion events.
     *
     * @return the listener. May be {@code null}
     */
    public TemplateProductListener getTemplateProductListener() {
        return templateProductListener;
    }

    /**
     * Creates a new editor.
     *
     * @param object  the object to edit
     * @param context the layout context
     * @return an editor to edit {@code object}
     */
    @Override
    public IMObjectEditor createEditor(IMObject object, LayoutContext context) {
        IMObjectEditor editor = super.createEditor(object, context);
        if (editor instanceof ActItemEditor) {
            ((ActItemEditor) editor).setProductListener(productListener);
        }
        return editor;
    }

    /**
     * Adds the object being edited to the collection, if it doesn't exist.
     * <p>
     * The object will be selected.
     *
     * @param editor the editor
     * @return {@code true} if the object was added, otherwise {@code false}
     */
    @Override
    public boolean addEdited(IMObjectEditor editor) {
        boolean result = false;
        if (needsTemplateExpansion(editor)) {
            Product product = ((ActItemEditor) editor).getProduct();
            if (TypeHelper.isA(product, TEMPLATE)) {
                result = expandTemplate((ActItemEditor) editor, product, BigDecimal.ONE);
            }
        } else {
            result = super.addEdited(editor);
        }
        return result;
    }

    /**
     * Determines if a new object in the collection that has default values should be removed prior to save.
     * <p>
     * This only applies to the object currently being edited.
     * <p>
     * An object is considered to have default values if it hasn't been modified since the editor was created.
     * <p>
     * This is so that incomplete objects that contain no user-entered data can be excluded from commits.
     * An object will only be removed if its removal won't invalidate the collection's minimum cardinality.
     * <p>
     * Defaults to {@code true}.
     *
     * @param exclude if {@code true} exclude objects with
     */
    public void setExcludeDefaultValueObject(boolean exclude) {
        excludeDefaultValueObject = exclude;
    }

    /**
     * Determines if an act has been modified.
     *
     * @param act the act
     * @return {@code true} if the act has been modified
     */
    public boolean isModified(Act act) {
        Boolean result = modified.get(act.getObjectReference());
        return result != null && result;
    }

    /**
     * Sets the modified state of an act.
     *
     * @param act      the act
     * @param modified determines if the act has been modified
     */
    public void setModified(Act act, boolean modified) {
        this.modified.put(act.getObjectReference(), modified);
    }

    /**
     * Returns the collection property editor.
     *
     * @return the collection property editor
     */
    public ActRelationshipCollectionPropertyEditor getEditor() {
        return (ActRelationshipCollectionPropertyEditor) getCollectionPropertyEditor();
    }

    /**
     * Creates an act item for each product referred to in its template.
     *
     * @param editor   the editor
     * @param template the template. May be {@code null}
     * @param quantity the quantity
     * @return {@code true} if the template was expanded; otherwise {@code false}
     */
    public boolean expandTemplate(ActItemEditor editor, Product template, BigDecimal quantity) {
        boolean result = false;
        if (template != null) {
            List<Act> items = createTemplateActs(editor, template, quantity);
            result = !items.isEmpty();
            if (result && templateProductListener != null) {
                templateProductListener.expanded(template);
            }
        }
        if (!result) {
            // template failed to expand. Clear the product reference so its not saved.
            setTemplateProduct(editor, null, null);
        } else {
            populateTable();
            // template act is replaced with the first product in the template, so try and select it in the table
            IMObject object = editor.getObject();
            setSelected(object);
        }
        return result;
    }

    /**
     * Adds any object being edited to the collection, if it is valid.
     *
     * @param validator the validator
     * @return {@code true} if the object is valid, otherwise {@code false}
     */
    @Override
    protected boolean addCurrentEdits(Validator validator) {
        boolean valid = true;
        IMObjectEditor editor = getCurrentEditor();
        if (editor != null) {
            valid = validator.validate(editor);
            if (valid) {
                boolean checkValid = needsTemplateExpansion(editor);
                addEdited(editor);
                if (checkValid) {
                    valid = validator.validate(editor);
                }
            }
        }
        return valid;
    }

    /**
     * Validates the object.
     * <p>
     * This validates the current object being edited, and if valid, the collection.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        boolean valid;
        if (!excludeObjectWithDefaultValues()) {
            // validate both the current editor and the collection
            valid = super.doValidation(validator);
        } else {
            // validate just the collection
            valid = getCollectionPropertyEditor().validate(validator);
        }
        return valid;
    }

    /**
     * Saves any current edits.
     *
     * @throws OpenVPMSException if the save fails
     */
    @Override
    protected void doSave() {
        if (!excludeObjectWithDefaultValues()) {
            // save the current editor and collection
            super.doSave();
        } else {
            // save the collection, excluding the current editor
            getCollectionPropertyEditor().save();
        }
    }

    /**
     * Adds a new editor for an object.
     * <p>
     * This flags the object as unmodified, if no modified flag already exists.
     *
     * @param object the object
     * @param editor the editor for the object
     */
    @Override
    protected void addEditor(IMObject object, IMObjectEditor editor) {
        super.addEditor(object, editor);
        if (modified.get(object.getObjectReference()) == null) {
            // default to unmodified
            setModified((Act) object, false);
        }
    }

    /**
     * Invoked when the current editor is modified.
     */
    @Override
    protected void onCurrentEditorModified() {
        IMObjectEditor editor = getCurrentEditor();
        if (editor != null) {
            // flag the editor as modified before delegating to super, otherwise it may be needlessly unmapped from the
            // collection by excludeObjectWithDefaultValues()
            Act object = (Act) editor.getObject();
            setModified(object, true);
        }
        super.onCurrentEditorModified();
    }

    /**
     * Copies an act item for each product referred to in its template.
     *
     * @param editor   the editor
     * @param template the product template
     * @param quantity the quantity
     * @return the acts generated from the template
     */
    protected List<Act> createTemplateActs(ActItemEditor editor, Product template, BigDecimal quantity) {
        List<Act> result = new ArrayList<>();
        ActRelationshipCollectionPropertyEditor collection = getEditor();

        IMObjectCopier copier = new IMObjectCopier(new ActItemCopyHandler());
        Collection<TemplateProduct> includes = getProductIncludes(template, editor.getPatient(), quantity);
        Act act = editor.getObject();
        Act copy = act; // replace the existing act with the first
        Date startTime = act.getActivityStartTime();

        // during template expansion, any child acts need to inherit the clinician of the item.
        Context context = getContext().getContext();
        User currentClinician = context.getClinician();
        context.setClinician(editor.getClinician());

        for (TemplateProduct include : includes) {
            if (copy == null) {
                // copy the act, and associate the product
                List<IMObject> objects = copier.apply(act);
                copy = (Act) objects.get(0);
                LayoutContext layoutContext = new DefaultLayoutContext(getContext());
                layoutContext.setComponentFactory(new ReadOnlyComponentFactory(layoutContext));
                editor = (ActItemEditor) createEditor(copy, layoutContext);

                // reset the start-time, which may have been set by the editor
                copy.setActivityStartTime(startTime);

                // create the component - must do this to ensure that the product editor is created
                editor.getComponent();
            }
            setTemplateProduct(editor, template, include);

            collection.add(copy);
            collection.setEditor(copy, editor);
            setModified(copy, true);
            result.add(copy);
            copy = null;
        }

        // after template expansion is complete, any new child acts should pick up the current clinician
        context.setClinician(currentClinician);

        return result;
    }

    /**
     * Populates an editor from a template product.
     *
     * @param editor   the editor
     * @param template the template. May be {@code null}
     * @param product  the template product. May be {@code null}
     */
    protected void setTemplateProduct(ActItemEditor editor, Product template, TemplateProduct product) {
        editor.setProduct(product, template);
    }

    /**
     * Expands a product template.
     *
     * @param template the template to expand
     * @param patient  the patient. May be {@code null}
     * @param quantity the quantity to include
     * @return a collection of included products
     */
    protected Collection<TemplateProduct> getProductIncludes(Product template, Party patient, BigDecimal quantity) {
        Weight weight = Weight.ZERO;
        if (patient != null) {
            PatientRules rules = ServiceHelper.getBean(PatientRules.class);
            weight = rules.getWeight(patient);
        }
        ProductTemplateExpander expander = getProductTemplateExpander();
        return expander.expand(template, weight, quantity, getContext().getCache());
    }

    /**
     * Creates a new product template expander.
     *
     * @return a new product template expander
     */
    protected ProductTemplateExpander getProductTemplateExpander() {
        return new ProductTemplateExpander(false, null, null);
    }

    /**
     * Returns the listener for product change events.
     *
     * @return the product listener
     */
    protected ProductListener getProductListener() {
        return productListener;
    }

    /**
     * Invoked when a product changes.
     * <p>
     * This expands any template products
     *
     * @param editor  the editor
     * @param product the product.
     */
    private void onProductChanged(ActItemEditor editor, Product product) {
        if (TypeHelper.isA(product, ProductArchetypes.TEMPLATE)) {
            expandTemplate(editor, product, BigDecimal.ONE);
        }
    }

    /**
     * Excludes the current object being edited from the collection if:
     * <ul>
     * <li>excludeWithDefaultValues is {@code true}
     * <li>it has default values; and
     * <li>excluding the object won't invalidate the collection's minimum cardinality
     * </ul>
     * <p>
     * This is so that incomplete objects that contain no user-entered data can be excluded from commits.
     *
     * @return {@code true} if an incomplete object was excluded; otherwise {@code false}
     */
    private boolean excludeObjectWithDefaultValues() {
        IMObjectEditor editor = getCurrentEditor();
        boolean excluded = false;
        if (excludeDefaultValueObject && editor != null) {
            CollectionPropertyEditor collection = getCollectionPropertyEditor();
            IMObject object = editor.getObject();
            List<IMObject> list = collection.getObjects();
            if (hasDefaultValues(editor)) {
                // NOTE: object not in the collection until added, but its editor will still be registered
                boolean inList = list.contains(object);
                int inListSize = inList ? list.size() : list.size() + 1;  // size of the collection with the obj present
                // only exclude if excluding it doesn't invalidate cardinality constraints
                if (inListSize - 1 >= collection.getMinCardinality()) {
                    collection.remove(object);
                    excluded = true;
                } else if (inListSize - 1 < collection.getMinCardinality()) {
                    if (!inList) {
                        // add it if its required to get closer to min cardinality
                        addEdited(editor);
                    }
                } else {
                    // can exclude
                    excluded = true;
                }
            } else {
                // user has changed the object, so ensure it is added
                addEdited(editor);
            }
        }
        return excluded;
    }

    /**
     * Determines if an object associated with an editor contains default values.
     * <p>
     * This only applies to new objects. These are considered to have default values if they haven't been modified
     * since the editor was created (the editor typically initialises default values within its constructor).
     *
     * @param editor the editor
     * @return {@code true} if the object has default values
     */
    private boolean hasDefaultValues(IMObjectEditor editor) {
        boolean result = false;
        IMObject object = editor.getObject();
        if (object.isNew()) {
            Boolean changed = modified.get(object.getObjectReference());
            if (changed != null && !changed) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Determines if an editor has a template product that needs expanding.
     *
     * @param editor the editor
     * @return {@code true} iif the editor has a template product
     */
    private boolean needsTemplateExpansion(IMObjectEditor editor) {
        if (editor instanceof ActItemEditor) {
            IMObjectReference product = ((ActItemEditor) editor).getProductRef();
            return TypeHelper.isA(product, TEMPLATE);
        }
        return false;
    }

    private class ActItemCopyHandler extends ActCopyHandler {

        /**
         * Determines how {@link IMObjectCopier} should treat an object.
         *
         * @param object  the source object
         * @param service the archetype service
         * @return {@code object} if the object shouldn't be copied,
         * {@code null} if it should be replaced with
         * {@code null}, or a new instance if the object should be
         * copied
         */
        public IMObject getObject(IMObject object, IArchetypeService service) {
            IMObject result;
            if (object instanceof Participation) {
                Participation participant = (Participation) object;
                if (TypeHelper.isA(participant, ProductArchetypes.PRODUCT_PARTICIPATION)) {
                    // don't copy the product participation, as these will be populated when expanding the template.
                    result = null;
                } else {
                    result = super.getObject(object, service);
                }
            } else {
                result = super.getObject(object, service);
            }
            return result;
        }

        /**
         * Helper to determine if a node is copyable.
         * <p>
         * For charge items, this only copies the <em>quantity</em>,
         * <em>patient</em>, <em>product</em>, <em>author</em> and
         * <em>clinician<em> nodes.
         *
         * @param archetype the archetype descriptor
         * @param node      the node descriptor
         * @param source    if {@code true} the node is the source; otherwise its
         *                  the target
         * @return {@code true} if the node is copyable; otherwise {@code false}
         */
        @Override
        protected boolean isCopyable(ArchetypeDescriptor archetype,
                                     NodeDescriptor node, boolean source) {
            boolean result = super.isCopyable(archetype, node, source);
            if (result && TypeHelper.isA(archetype, INVOICE_ITEM, CREDIT_ITEM, COUNTER_ITEM)) {
                String name = node.getName();
                result = "quantity".equals(name) || "patient".equals(name)
                         || "product".equals(name) || "author".equals(name)
                         || "clinician".equals(name);
            }
            return result;
        }
    }
}
