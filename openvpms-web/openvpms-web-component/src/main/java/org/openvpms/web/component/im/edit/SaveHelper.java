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

package org.openvpms.web.component.im.edit;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.im.delete.DefaultIMObjectDeletionListener;
import org.openvpms.web.component.im.delete.IMObjectDeletionListener;
import org.openvpms.web.component.im.util.DefaultIMObjectSaveListener;
import org.openvpms.web.component.im.util.IMObjectSaveListener;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;


/**
 * Helper for saving {@link IMObject}s.
 *
 * @author Tim Anderson
 */
public class SaveHelper {

    /**
     * Saves an editor in a transaction.
     *
     * @param editor the editor to save
     * @return {@code true} if the object was saved successfully
     */
    public static boolean save(final IMObjectEditor editor) {
        IMObjectEditorSaver saver = new IMObjectEditorSaver();
        return saver.save(editor);
    }

    /**
     * Saves an editor and invokes a callback within a single transaction.
     *
     * @param editor   the editor
     * @param callback the callback
     * @return {@code true} if the object was saved and the callback returned {@code true}
     */
    public static boolean save(final IMObjectEditor editor, final TransactionCallbackWithoutResult callback) {
        IMObjectEditorSaver saver = new IMObjectEditorSaver() {
            @Override
            protected void save(IMObjectEditor editor, TransactionStatus status) {
                super.save(editor, status);
                callback.doInTransaction(status);
            }
        };
        return saver.save(editor);
    }

    /**
     * Saves an object.
     *
     * @param object the object to save
     * @return {@code true} if the object was saved; otherwise {@code false}
     */
    public static boolean save(IMObject object) {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        return save(object, service);
    }

    /**
     * Saves an object.
     *
     * @param object  the object to save
     * @param service the archetype service
     * @return {@code true} if the object was saved; otherwise {@code false}
     */
    public static boolean save(IMObject object, IArchetypeService service) {
        return save(Collections.singletonList(object), service);
    }

    /**
     * Saves a collection of objects.
     *
     * @param objects the objects to save
     * @return {@code true} if the objects were saved; otherwise {@code false}
     */
    public static boolean save(IMObject... objects) {
        return save(Arrays.asList(objects));
    }

    /**
     * Saves a collection of objects.
     *
     * @param objects the objects to save
     * @return {@code true} if the objects were saved; otherwise {@code false}
     */
    public static boolean save(Collection<? extends IMObject> objects) {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        return save(objects, service);
    }

    /**
     * Saves an object.
     *
     * @param objects the objects to save
     * @param service the archetype service
     * @return {@code true} if the objects were saved; otherwise {@code false}
     */
    public static boolean save(Collection<? extends IMObject> objects, IArchetypeService service) {
        return save(objects, DefaultIMObjectSaveListener.INSTANCE, service);
    }

    /**
     * Saves a collection of objects.
     *
     * @param objects  the objects to save
     * @param listener the listener to notify
     */
    public static boolean save(Collection<? extends IMObject> objects, IMObjectSaveListener listener) {
        return save(objects, listener, ServiceHelper.getArchetypeService());
    }

    /**
     * Saves a collection of objects.
     *
     * @param objects  the objects to save
     * @param listener the listener to notify
     */
    public static boolean save(Collection<? extends IMObject> objects, IMObjectSaveListener listener,
                               IArchetypeService service) {
        boolean saved = false;
        try {
            service.save(objects);
            listener.saved(objects);
            saved = true;
        } catch (Throwable exception) {
            listener.error(objects, exception);
        }
        return saved;
    }

    /**
     * Removes an object.
     *
     * @param object the object to remove
     * @return {@code true} if the object was removed; otherwise {@code false}
     */
    public static boolean delete(IMObject object) {
        return delete(object, new DefaultIMObjectDeletionListener<>());
    }

    /**
     * Removes an object.
     *
     * @param object   the object to remove
     * @param listener the listener to notify
     * @return {@code true} if the object was removed; otherwise {@code false}
     */
    public static <T extends IMObject> boolean delete(T object, IMObjectDeletionListener<T> listener) {
        return delete(object, listener, ServiceHelper.getArchetypeService());
    }

    /**
     * Removes an object.
     *
     * @param object   the object to remove
     * @param listener the listener to notify
     * @param service  the archetype service
     * @return {@code true} if the object was removed; otherwise {@code false}
     */
    public static <T extends IMObject> boolean delete(T object, IMObjectDeletionListener<T> listener,
                                                      IArchetypeService service) {
        boolean removed = false;
        try {
            service.remove(object);
            removed = true;
        } catch (Throwable exception) {
            listener.failed(object, exception);
        }
        return removed;
    }

    /**
     * Replace an object, by deleting one instance and inserting another.
     *
     * @param delete the object to delete
     * @param insert the object to insert
     * @return {@code true} if the operation was successful
     */
    public static boolean replace(final IMObject delete, final IMObject insert) {
        Boolean result = null;
        try {
            TransactionTemplate template = new TransactionTemplate(ServiceHelper.getTransactionManager());
            result = template.execute(new TransactionCallback<Boolean>() {
                public Boolean doInTransaction(TransactionStatus status) {
                    IArchetypeService service = ServiceHelper.getArchetypeService();
                    service.remove(delete);
                    service.save(insert);
                    return true;
                }
            });
        } catch (Throwable exception) {
            String title = Messages.get("imobject.replace.failed.title");
            ErrorHelper.show(title, exception);
        }
        return (result != null) && result;
    }

}
