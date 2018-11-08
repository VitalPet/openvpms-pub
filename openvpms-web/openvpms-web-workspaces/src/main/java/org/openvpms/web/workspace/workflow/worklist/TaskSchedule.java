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

package org.openvpms.web.workspace.workflow.worklist;

import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.workspace.workflow.scheduling.Schedule;


/**
 * Task schedule.
 */
class TaskSchedule extends Schedule {

    /**
     * The maximum no. of slots for this schedule.
     */
    private final int maxSlots;


    /**
     * Constructs a {@link TaskSchedule}.
     *
     * @param schedule the schedule
     * @param rules    the appointment rules
     */
    public TaskSchedule(Entity schedule, AppointmentRules rules) {
        super(schedule, null, rules);
        IMObjectBean bean = new IMObjectBean(schedule);
        maxSlots = bean.getInt("maxSlots");
    }

    /**
     * Returns the number of slots in use.
     *
     * @return the number of slots
     */
    public int getSlots() {
        return getEvents().size();
    }

    /**
     * Returns the maximum number of slots.
     *
     * @return the maximum number of slots
     */
    public int getMaxSlots() {
        return maxSlots;
    }

}
