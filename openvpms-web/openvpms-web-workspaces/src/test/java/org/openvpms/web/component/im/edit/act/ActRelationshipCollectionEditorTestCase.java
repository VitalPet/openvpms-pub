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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.edit.act;

import org.junit.Test;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.PatientHistoryChanges;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.archetype.rules.supplier.SupplierTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.test.AbstractAppTest;
import org.openvpms.web.workspace.customer.charge.ChargeItemRelationshipCollectionEditor;
import org.openvpms.web.workspace.customer.charge.ChargeSaveContext;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActItemEditor;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link ActRelationshipCollectionEditor} class.
 *
 * @author Tim Anderson
 */
public class ActRelationshipCollectionEditorTestCase extends AbstractAppTest {

    /**
     * Verifies an act relationship is added between an invoice and invoice item.
     */
    @Test
    public void testAddRelationship() {
        final FinancialAct invoice = createInvoice();
        final ActRelationshipCollectionEditor itemsEditor = createActRelationshipCollectionEditor(invoice, 0);
        itemsEditor.add(); // add a single item

        CustomerChargeActItemEditor editor1 = (CustomerChargeActItemEditor) itemsEditor.getCurrentEditor();
        assertNotNull(editor1);
        editor1.setEditorQueue(null); // disable popups
        Product product = TestHelper.createProduct();
        Party patient = TestHelper.createPatient();
        editor1.setProduct(product);
        editor1.setPatient(patient);

        save(invoice, itemsEditor);
        checkItems(invoice, 1);     // verify the invoice has a single item

        itemsEditor.add();       // add another item
        CustomerChargeActItemEditor editor2 = (CustomerChargeActItemEditor) itemsEditor.getCurrentEditor();
        assertNotNull(editor2);
        editor2.setProduct(product);
        editor2.setPatient(patient);

        save(invoice, itemsEditor);
        checkItems(invoice, 2);     // verify the invoice now has two items
    }

    /**
     * Verifies that a new item with default values is excluded from a collection when the minimum cardinality is zero.
     */
    @Test
    public void testExcludeDefaultItemsForMinCardinalityZero() {
        final FinancialAct invoice = createInvoice();

        final ActRelationshipCollectionEditor editor = createActRelationshipCollectionEditor(invoice, 0);
        editor.add(); // add a single item

        assertTrue(editor.isValid());
        save(invoice, editor);
        checkItems(invoice, 0);     // verify the invoice has no items
    }

    /**
     * Verifies that a new item with default values is not excluded when
     * {@link ActRelationshipCollectionEditor#setExcludeDefaultValueObject(boolean)} is false.
     */
    @Test
    public void testDisableExcludeDefaultItemsForMinCardinalityZero() {
        final FinancialAct invoice = createInvoice();

        final ActRelationshipCollectionEditor editor = createActRelationshipCollectionEditor(invoice, 0);
        editor.setExcludeDefaultValueObject(false);
        editor.add(); // add a single item

        assertFalse(editor.isValid());  // if the object was excluded, the editor would be valid
    }

    /**
     * Verifies that a new item with default values is excluded from a collection when the minimum cardinality is zero.
     */
    @Test
    public void testIncludeDefaultItemsForMinCardinalityOne() {
        final FinancialAct delivery1 = createDelivery();
        final ActRelationshipCollectionEditor editor1 = createActRelationshipCollectionEditor(delivery1, 1);
        editor1.getComponent();
        editor1.add(); // add a single item

        save(delivery1, editor1);
        checkItems(delivery1, 1);  // verify the delivery has a single item

        // now test with 2 items. The second item with default values should be discarded as the min cardinality
        // constraints have been met.
        final FinancialAct delivery2 = createDelivery();
        final ActRelationshipCollectionEditor editor2 = createActRelationshipCollectionEditor(delivery2, 1);
        editor2.getComponent();
        editor2.add();
        FinancialAct first = (FinancialAct) editor2.getCurrentEditor().getObject();
        editor2.add();
        FinancialAct second = (FinancialAct) editor2.getCurrentEditor().getObject();

        save(delivery2, editor2);
        List<FinancialAct> items = checkItems(delivery2, 1);
        assertTrue(items.contains(first));
        assertFalse(items.contains(second));
    }

    /**
     * Verifies that a template with no products included is replaced with a null when expanded.
     * <p/>
     * This ensures that template products are not saved as charge items.
     */
    @Test
    public void testTemplateExpansionForTemplateWithNoIncludes() {
        final FinancialAct invoice = createInvoice();

        final ActRelationshipCollectionEditor itemsEditor = createActRelationshipCollectionEditor(invoice, 0);
        itemsEditor.setExcludeDefaultValueObject(false);
        itemsEditor.add(); // add a single item

        CustomerChargeActItemEditor editor = (CustomerChargeActItemEditor) itemsEditor.getCurrentEditor();
        assertNotNull(editor);
        Party patient = TestHelper.createPatient();
        Product product = TestHelper.createProduct(ProductArchetypes.TEMPLATE, null);
        editor.setPatient(patient);
        editor.setProduct(product);
        assertFalse(itemsEditor.isValid());
        assertNull(editor.getProductRef()); // template product should be replaced with null
        assertNull(editor.getTemplateRef());
    }

    /**
     * Verifies a parent act has the correct no. of act items.
     *
     * @param parent   the parent act
     * @param expected the expected no. of items
     * @return the items
     */
    private List<FinancialAct> checkItems(FinancialAct parent, int expected) {
        FinancialAct reloaded = get(parent);
        assertNotNull(reloaded);
        ActBean bean = new ActBean(reloaded);
        List<FinancialAct> items = bean.getNodeActs("items", FinancialAct.class);
        assertEquals(expected, items.size());
        return items;
    }

    /**
     * Saves a parent act and its items.
     *
     * @param parent      the parent
     * @param itemsEditor the items editor
     */
    private void save(final FinancialAct parent, final ActRelationshipCollectionEditor itemsEditor) {
        assertTrue(itemsEditor.isValid());
        // NOTE: may trigger addition of relationship to parent if not already present

        TransactionTemplate template = new TransactionTemplate(ServiceHelper.getTransactionManager());
        boolean result = template.execute(new TransactionCallback<Boolean>() {
            public Boolean doInTransaction(TransactionStatus status) {
                ChargeSaveContext context = null;
                if (itemsEditor instanceof ChargeItemRelationshipCollectionEditor) {
                    PatientHistoryChanges changes = new PatientHistoryChanges(null, null, getArchetypeService());
                    context = ((ChargeItemRelationshipCollectionEditor) itemsEditor).getSaveContext();
                    assertNotNull(context);
                    context.setHistoryChanges(changes);
                }
                boolean saved = SaveHelper.save(parent);
                itemsEditor.save();
                if (context != null) {
                    if (saved) {
                        context.save();
                    }
                    context.setHistoryChanges(null);
                }
                return saved;
            }
        });
        assertTrue(result);
    }

    /**
     * Creates a new editor.
     *
     * @param parent         the parent act
     * @param minCardinality the expected minimum cardinality of the "items" node
     * @return a new editor
     */
    private ActRelationshipCollectionEditor createActRelationshipCollectionEditor(FinancialAct parent,
                                                                                  int minCardinality) {
        User user = TestHelper.createUser();
        LayoutContext context = new DefaultLayoutContext(new LocalContext(), new HelpContext("foo", null));
        context.getContext().setPractice(TestHelper.getPractice());
        context.getContext().setUser(user);
        PropertySet set = new PropertySet(parent, context);
        CollectionProperty items = (CollectionProperty) set.get("items");
        assertNotNull(items);
        assertEquals(minCardinality, items.getMinCardinality());
        ActRelationshipCollectionEditor result;
        if (TypeHelper.isA(parent, CustomerAccountArchetypes.INVOICE)) {
            // required as invoice item editors share a CustomerChargeEditContext
            result = new ChargeItemRelationshipCollectionEditor(items, parent, context);
        } else {
            result = new ActRelationshipCollectionEditor(items, parent, context);
        }
        result.getComponent(); // ensure it is rendered
        return result;
    }

    /**
     * Helper to create an invoice.
     *
     * @return a new invoice
     */
    private FinancialAct createInvoice() {
        final FinancialAct invoice = (FinancialAct) create(CustomerAccountArchetypes.INVOICE);
        ActBean bean = new ActBean(invoice);
        Party customer = TestHelper.createCustomer();
        Party location = TestHelper.createLocation();
        bean.addNodeParticipation("customer", customer);
        bean.addNodeParticipation("location", location);
        return invoice;
    }

    /**
     * Helper to create a delivery.
     *
     * @return a new delivery
     */
    private FinancialAct createDelivery() {
        final FinancialAct delivery = (FinancialAct) create(SupplierArchetypes.DELIVERY);
        ActBean bean = new ActBean(delivery);
        Party supplier = TestHelper.createSupplier();
        Party stockLocation = SupplierTestHelper.createStockLocation();

        bean.addNodeParticipation("supplier", supplier);
        bean.addNodeParticipation("stockLocation", stockLocation);
        return delivery;
    }

}
