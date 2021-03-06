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

package org.openvpms.component.business.service.archetype;

import org.apache.commons.lang.mutable.MutableInt;
import org.junit.Before;
import org.junit.Test;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.AbstractArchetypeServiceTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Tests archetype service event notification via {@link IArchetypeServiceListener}.
 *
 * @author Tim Anderson
 */
@ContextConfiguration("archetype-service-appcontext.xml")
public class ArchetypeServiceListenerTestCase extends AbstractArchetypeServiceTest {

    /**
     * The transaction template.
     */
    private TransactionTemplate template;


    /**
     * Verifies that callbacks are invoked on save.
     */
    @Test
    public void testSave() {
        IArchetypeService service = getArchetypeService();
        Party person1 = createPerson();
        Party person2 = createPerson();
        Listener listener = new Listener();
        service.addListener("party.customerperson", listener);
        service.save(person1);
        service.save(person2);

        Set<IMObject> saved = listener.getSaved();
        Set<IMObject> saving = listener.getSaving();

        assertEquals(2, saved.size());
        assertTrue(saving.equals(saved));
    }

    /**
     * Verifies that callbacks are invoked on transaction commit.
     */
    @Test
    public void testTransactionSave() {
        final IArchetypeService service = getArchetypeService();
        final Party person1 = createPerson();
        final Party person2 = createPerson();
        final Listener listener = new Listener();
        service.addListener("party.customerperson", listener);

        template.execute(new TransactionCallback<Object>() {
            public Object doInTransaction(TransactionStatus status) {
                listener.setInTransaction(true);
                service.save(person1);
                assertTrue(listener.getSaving().contains(person1));
                assertFalse(listener.getSaved().contains(person1));

                service.save(person2);
                assertTrue(listener.getSaving().contains(person2));
                assertFalse(listener.getSaved().contains(person2));

                listener.setInTransaction(false);
                return null;
            }
        });

        Set<IMObject> saved = listener.getSaved();
        Set<IMObject> saving = listener.getSaving();

        assertEquals(2, saved.size());
        assertTrue(saving.equals(saved));
    }

    /**
     * Verifies that callbacks are invoked on collection save.
     */
    @Test
    public void testCollectionSave() {
        IArchetypeService service = getArchetypeService();
        Party person1 = createPerson();
        Party person2 = createPerson();
        Listener listener = new Listener();
        service.addListener("party.customerperson", listener);
        service.save(Arrays.asList(person1, person2));

        Set<IMObject> saved = listener.getSaved();
        Set<IMObject> saving = listener.getSaving();

        assertEquals(2, saved.size());
        assertTrue(saving.equals(saved));
    }

    /**
     * Verifies that callbacks are invoked on remove.
     */
    @Test
    public void testRemove() {
        IArchetypeService service = getArchetypeService();
        Party person1 = createPerson();
        Party person2 = createPerson();
        service.save(person1);
        service.save(person2);

        Listener listener = new Listener();
        service.addListener("party.customerperson", listener);
        service.remove(person1);
        service.remove(person2);

        Set<IMObject> removed = listener.getRemoved();
        Set<IMObject> removing = listener.getRemoving();

        assertEquals(2, removed.size());
        assertTrue(removing.equals(removed));
    }

    /**
     * Verifies that callbacks are invoked on transaction commit.
     */
    @Test
    public void testTransactionRemove() {
        final IArchetypeService service = getArchetypeService();
        final Party person1 = createPerson();
        final Party person2 = createPerson();
        service.save(person1);
        service.save(person2);

        final Listener listener = new Listener();
        service.addListener("party.customerperson", listener);
        template.execute(new TransactionCallback<Object>() {
            public Object doInTransaction(TransactionStatus status) {
                listener.setInTransaction(true);
                service.remove(person1);
                assertTrue(listener.getRemoving().contains(person1));
                assertFalse(listener.getRemoved().contains(person1));

                service.remove(person2);
                assertTrue(listener.getRemoving().contains(person2));
                assertFalse(listener.getRemoved().contains(person2));

                listener.setInTransaction(false);
                return null;
            }
        });

        Set<IMObject> removed = listener.getRemoved();
        Set<IMObject> removing = listener.getRemoving();

        assertEquals(2, removed.size());
        assertTrue(removing.equals(removed));
    }

    /**
     * Verifies that callbacks are invoked on transaction rollback.
     */
    @Test
    public void testTransactionRollback() {
        final IArchetypeService service = getArchetypeService();
        final Party person1 = createPerson();
        final Party person2 = createPerson();
        final Listener listener = new Listener();
        service.addListener("party.customerperson", listener);

        try {
            template.execute(new TransactionCallback<Object>() {
                public Object doInTransaction(TransactionStatus status) {
                    listener.setInTransaction(true);
                    service.save(person1);
                    assertTrue(listener.getSaving().contains(person1));

                    service.remove(person2);
                    assertTrue(listener.getRemoving().contains(person2));
                    listener.setInTransaction(false);
                    throw new RuntimeException("Force rollback");
                }
            });
        } catch (RuntimeException ignore) {
            // do nothing
        }

        Set<IMObject> saving = listener.getSaving();
        Set<IMObject> removing = listener.getRemoving();

        assertEquals(1, saving.size());
        assertEquals(1, removing.size());

        assertTrue(listener.getSaved().isEmpty());
        assertTrue(listener.getRemoved().isEmpty());

        Set<IMObject> rolledBack = listener.getRolledBack();
        assertEquals(2, rolledBack.size());
        assertTrue(rolledBack.contains(person1));
        assertTrue(rolledBack.contains(person2));
    }

    /**
     * Verifies that the {@link IMObject#getVersion()} is correctly reported within an {@link IArchetypeServiceListener}
     * in the context of a transaction.
     */
    @Test
    public void testVersion() {
        final MutableInt version = new MutableInt();
        final IArchetypeService service = getArchetypeService();

        final Party person = createPerson();
        service.addListener("party.customerperson", new AbstractArchetypeServiceListener() {
            @Override
            public void saved(IMObject object) {
                version.setValue(object.getVersion());
            }
        });

        template.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                save(person);
                return null;
            }
        });
        assertEquals(0, person.getVersion());
        assertEquals(0, version.intValue());

        person.getDetails().put("lastName", "Gum");
        template.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                save(person);
                return null;
            }
        });
        assertEquals(1, person.getVersion());
        assertEquals(1, version.intValue());
    }

    /**
     * Helper to create a person.
     *
     * @return a new person
     */
    private Party createPerson() {
        Party person = (Party) create("party.customerperson");
        person.getDetails().put("lastName", "Foo");
        person.getDetails().put("firstName", "Bar");
        person.getDetails().put("title", "MR");
        return person;
    }

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        PlatformTransactionManager txnManager = (PlatformTransactionManager) applicationContext.getBean("txnManager");
        template = new TransactionTemplate(txnManager);
    }

    private class Listener extends AbstractArchetypeServiceListener {

        private Set<IMObject> saving = new HashSet<IMObject>();

        private Set<IMObject> saved = new HashSet<IMObject>();

        private Set<IMObject> removing = new HashSet<IMObject>();

        private Set<IMObject> removed = new HashSet<IMObject>();

        private Set<IMObject> rollback = new HashSet<IMObject>();

        private boolean inTransaction;


        /**
         * Invoked prior to an object being saved.
         *
         * @param object the object being saved
         */
        @Override
        public void save(IMObject object) {
            assertFalse(saving.contains(object));
            saving.add(object);
        }

        /**
         * Invoked prior to an object being removed.
         *
         * @param object the object being removed
         */
        @Override
        public void remove(IMObject object) {
            assertFalse(removing.contains(object));
            removing.add(object);
        }

        /**
         * Determines if a transactions is in progress.
         *
         * @param inTransaction if <tt>true</tt> a transaction is in progress
         */
        public void setInTransaction(boolean inTransaction) {
            this.inTransaction = inTransaction;
        }

        /**
         * Invoked after an object has been saved and the transaction committed.
         *
         * @param object the saved object
         */
        @Override
        public void saved(IMObject object) {
            assertFalse(inTransaction); // shouldn't be invoked in a transaction
            assertTrue(saving.contains(object));
            saved.add(object);
        }

        /**
         * Invoked after an object has been removed and the transaction
         * committed.
         *
         * @param object the removed object
         */
        @Override
        public void removed(IMObject object) {
            assertFalse(inTransaction); // shouldn't be invoked in a transaction
            assertTrue(removing.contains(object));
            removed.add(object);
        }

        /**
         * Invoked on transaction rollback.
         *
         * @param object the object that was rolled back
         */
        @Override
        public void rollback(IMObject object) {
            assertFalse(inTransaction); // shouldn't be invoked in a transaction
            rollback.add(object);
        }

        /**
         * Returns the objects passed to the {@link #save(IMObject)} method.
         *
         * @return the objects being saved
         */
        public Set<IMObject> getSaving() {
            return saving;
        }

        /**
         * Returns the objects passed to the {@link #saved(IMObject)} method.
         *
         * @return the saved objects
         */
        public Set<IMObject> getSaved() {
            return saved;
        }

        /**
         * Returns the objects passed to the {@link #remove(IMObject)} method.
         *
         * @return the objects being removed
         */
        public Set<IMObject> getRemoving() {
            return removing;
        }

        /**
         * Returns the objects passed to the {@link #removed(IMObject)} method.
         *
         * @return the removed objects
         */
        public Set<IMObject> getRemoved() {
            return removed;
        }

        /**
         * Returns the rolled back objects.
         *
         * @return the rolled back objects
         */
        public Set<IMObject> getRolledBack() {
            return rollback;
        }
    }
}
