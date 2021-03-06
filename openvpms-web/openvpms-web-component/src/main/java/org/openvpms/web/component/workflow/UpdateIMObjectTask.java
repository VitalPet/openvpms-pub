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

package org.openvpms.web.component.workflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.im.edit.SaveHelper;


/**
 * Task to update an {@link IMObject}.
 *
 * @author Tim Anderson
 */
public class UpdateIMObjectTask extends SynchronousTask {

    /**
     * The object short name.
     */
    private String shortName;

    /**
     * The object to update.
     */
    private IMObject object;

    /**
     * The reference of the object to update. Resolved at time of update.
     */
    private IMObjectReference reference;

    /**
     * Properties to populate the created object with.
     */
    private final TaskProperties properties;

    /**
     * Determines if the object should be saved.
     */
    private final boolean save;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(UpdateIMObjectTask.class);


    /**
     * Constructs an {@link UpdateIMObjectTask}.
     * The object is saved on update.
     *
     * @param shortName  the short name of the object to update
     * @param properties properties to populate the object with
     */
    public UpdateIMObjectTask(String shortName, TaskProperties properties) {
        this(shortName, properties, true);
    }

    /**
     * Constructs an {@link UpdateIMObjectTask}.
     *
     * @param shortName  the short name of the object to update
     * @param properties properties to populate the object with
     * @param save       determines if the object should be saved
     */
    public UpdateIMObjectTask(String shortName, TaskProperties properties,
                              boolean save) {
        this.shortName = shortName;
        this.properties = properties;
        this.save = save;
    }

    /**
     * Constructs an {@link UpdateIMObjectTask}.
     * The object is saved on update.
     *
     * @param object     the object to update
     * @param properties properties to populate the object with
     */
    public UpdateIMObjectTask(IMObject object, TaskProperties properties) {
        this(object, properties, true);
    }

    /**
     * Constructs an {@link UpdateIMObjectTask}.
     *
     * @param object     the object to update
     * @param properties properties to populate the object with
     * @param save       determines if the object should be saved
     */
    public UpdateIMObjectTask(IMObject object, TaskProperties properties,
                              boolean save) {
        this.object = object;
        this.properties = properties;
        this.save = save;
    }

    /**
     * Constructs an {@link UpdateIMObjectTask}.
     * The object is saved on update.
     *
     * @param reference  reference to the object to update
     * @param properties properties to populate the object with
     */
    public UpdateIMObjectTask(IMObjectReference reference,
                              TaskProperties properties) {
        this(reference, properties, true);
    }

    /**
     * Constructs an {@link UpdateIMObjectTask}.
     *
     * @param reference  reference to the object to update
     * @param properties properties to populate the object with
     * @param save       determines if the object should be saved
     */
    public UpdateIMObjectTask(IMObjectReference reference,
                              TaskProperties properties, boolean save) {
        this.reference = reference;
        this.properties = properties;
        this.save = save;
    }

    /**
     * Executes the task.
     *
     * @throws OpenVPMSException for any error
     */
    public void execute(TaskContext context) {
        if (object == null) {
            object = getObject(context);
        }
        if (object != null) {
            populate(object, context);
            if (save) {
                if (!SaveHelper.save(object)) {
                    notifyCancelled();
                }
            }
        } else {
            log.error("Cannot update object: no object with shortName=" +
                      shortName + ", reference=" + reference);
            notifyCancelled();
        }
    }

    /**
     * Populates an object.
     *
     * @param object  the object to populate
     * @param context the task context
     */
    protected void populate(IMObject object, TaskContext context) {
        populate(object, properties, context);
    }

    /**
     * Returns the object.
     *
     * @param context the task context
     * @return the object, or {@code null} if none is found
     */
    protected IMObject getObject(TaskContext context) {
        IMObject result;
        if (reference != null) {
            IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
            result = service.get(reference);
        } else {
            result = context.getObject(shortName);
        }
        return result;
    }
}
