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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.util;

import org.junit.Test;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.archetype.ArchetypeHandler;
import org.openvpms.web.component.im.archetype.ArchetypeHandlers;
import org.openvpms.web.component.im.query.AutoQuery;
import org.openvpms.web.component.im.query.DefaultQuery;
import org.openvpms.web.component.im.query.EntityQuery;
import org.openvpms.web.component.im.query.PatientQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.test.AbstractAppTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * {@link ArchetypeHandlers} test case.
 *
 * @author Tim Anderson
 */
public class ArchetypeHandlersTestCase extends AbstractAppTest {

    /**
     * The handlers, configured from the properties file.
     */
    private ArchetypeHandlers propertiesHandlers;

    /**
     * The handlers, configured from the XML file.
     */
    private ArchetypeHandlers xmlHandlers;

    /**
     * Test properties.
     */
    private static final String PROPERTIES = "org/openvpms/web/component/im/util/ArchetypeHandlersTestCase.properties";

    /**
     * Test xml.
     */
    private static final String XML = "org/openvpms/web/component/im/util/ArchetypeHandlersTestCase.xml";

    /**
     * Verifies that {@link AutoQuery} can be created for a <em>lookup.*</em>
     * short name.
     *
     * @throws Exception for any error
     */
    @Test
    public void testCreateAutoQuery() throws Exception {
        checkCreateAutoQuery(propertiesHandlers);
        checkCreateAutoQuery(xmlHandlers);
    }

    /**
     * Verifies that {@link PatientQuery} can be created for a
     * <em>patient.*</em>short name, and that maxRows is configured for 25.
     *
     * @throws Exception for any error
     */
    @Test
    public void testCreatePatientQuery() throws Exception {
        checkCreatePatientQuery(propertiesHandlers);
        checkCreatePatientQuery(xmlHandlers);
    }

    /**
     * Verifies that no handler is returned if there is no match for the
     * specified archetypes.
     */
    @Test
    public void testNoMatch() {
        checkNoMatch(propertiesHandlers);
        checkNoMatch(xmlHandlers);
    }

    /**
     * Verifies that no handler is returned if a handler doesn't support
     * the entire range of archetypes.
     */
    @Test
    public void testNoCompleteMatch() {
        checkNoCompleteMatch(propertiesHandlers);
        checkNoCompleteMatch(xmlHandlers);
    }

    /**
     * Verifies that the correct handler is returned if multiple handlers
     * are registered with the same implementation type.
     */
    @Test
    public void testSameHandlerImplementationType() {
        checkSameHandlerImplementationType(propertiesHandlers);
        checkSameHandlerImplementationType(xmlHandlers);
    }

    /**
     * Verifies that anonymous handlers (i.e. those not associated with an archetype) can be registered.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void checkAnonymous() throws Exception {
        // check the defaults for DefaultQuery
        DefaultQuery query1 = new DefaultQuery(new String[]{"lookup.species"}, Lookup.class);
        assertFalse(query1.isAuto());
        assertFalse(query1.isContains());

        // now verify DefaultQuery can be registered with different properties
        ArchetypeHandler<Query> handler = propertiesHandlers.getHandler(DefaultQuery.class);
        assertNotNull(handler);
        assertEquals(DefaultQuery.class, handler.getType());

        Query query = handler.create(new Object[]{new String[]{"lookup.species"}, Lookup.class});
        assertTrue(query.isAuto());
        assertTrue(query.isContains());
    }

    /**
     * Sets up the test case.
     */
    @Override
    public void setUp() {
        super.setUp();
        propertiesHandlers = new ArchetypeHandlers<>(PROPERTIES, null, Query.class, "query.", getArchetypeService());
        xmlHandlers = new ArchetypeHandlers<>(XML, Query.class, getArchetypeService());
    }

    /**
     * Verifies that {@link AutoQuery} can be created for a <em>lookup.*</em> short name.
     *
     * @param handlers the handlers
     * @throws Exception for any errror
     */
    private void checkCreateAutoQuery(ArchetypeHandlers handlers)
            throws Exception {
        String[] shortNames = DescriptorHelper.getShortNames("lookup.*");
        ArchetypeHandler lookup = handlers.getHandler("lookup.*");
        assertNotNull(lookup);
        assertEquals(lookup.getType(), AutoQuery.class);
        Query query = (Query) lookup.create(new Object[]{shortNames});
        assertNotNull(query);
    }

    /**
     * Verifies that {@link PatientQuery} can be created for a
     * <em>patient.*</em>short name, and that maxRows is configured for 25.
     *
     * @param handlers the handlers
     * @throws Exception for any error
     */
    private void checkCreatePatientQuery(ArchetypeHandlers handlers)
            throws Exception {
        String[] shortNames = DescriptorHelper.getShortNames("party.patient*");
        ArchetypeHandler patient = handlers.getHandler("party.patient*");
        assertNotNull(patient);
        assertEquals(patient.getType(), PatientQuery.class);
        Query query = (Query) patient.create(
                new Object[]{shortNames, new LocalContext()});
        assertNotNull(query);
        assertEquals(25, query.getMaxResults());
    }

    /**
     * Verifies that no handler is returned if there is no match for the
     * specified archetypes.
     *
     * @param handlers the handlers
     */
    private void checkNoMatch(ArchetypeHandlers handlers) {
        ArchetypeHandler handler1 = handlers.getHandler("act.*");
        assertNull(handler1);

        ArchetypeHandler handler2 = handlers.getHandler(
                new String[]{"act.*", "actRelationship.*"});
        assertNull(handler2);
    }

    /**
     * Verifies that no handler is returned if a handler doesn't support
     * the entire range of archetypes.
     *
     * @param handlers the handlers
     */
    private void checkNoCompleteMatch(ArchetypeHandlers handlers) {
        ArchetypeHandler handler1 = handlers.getHandler("*.*");
        assertNull(handler1);

        ArchetypeHandler handler2 = handlers.getHandler(
                new String[]{"party.patient*", "lookup.*"});
        assertNull(handler2);
    }

    /**
     * Verifies that the correct handler is returned if multiple handlers
     * are registered with the same implementation type.
     *
     * @param handlers the handlers
     */
    private void checkSameHandlerImplementationType(
            ArchetypeHandlers handlers) {
        // make sure the AutoQuery class is returned for lookup.*,
        // security.*
        ArchetypeHandler handler = handlers.getHandler(
                new String[]{"lookup.*", "security.*"});
        assertNotNull(handler);
        assertEquals(handler.getType(), AutoQuery.class);

        // make sure the AutoQuery class is returned for party.organisation*
        ArchetypeHandler org = handlers.getHandler(
                new String[]{"party.organisation*"});
        assertNotNull(org);
        assertEquals(org.getType(), AutoQuery.class);

        // make sure the EntityQuery class is returned for party.customer*,
        // party.organisationOTC
        ArchetypeHandler entity = handlers.getHandler(
                new String[]{"party.organisationOTC", "party.customer*"});
        assertNotNull(entity);
        assertEquals(entity.getType(), EntityQuery.class);

        // make sure no handleris returned for lookup.*, security.*,
        // party.organisation* as the party.organisation* line has a different
        // configuration
        handler = handlers.getHandler(
                new String[]{"lookup.*", "security.*",
                             "party.organisation*"});
        assertNull(handler);
    }

}
