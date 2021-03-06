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

package org.openvpms.web.workspace.workflow.appointment.boarding;

import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.workspace.workflow.appointment.AbstractMultiDayScheduleGrid;
import org.openvpms.web.workspace.workflow.scheduling.Schedule;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A grid that shows all schedules with appointments ending on the date.
 *
 * @author Tim Anderson
 */
public class CheckOutScheduleGrid extends AbstractMultiDayScheduleGrid {

    /**
     * Constructs a {@link CheckOutScheduleGrid}.
     *
     * @param scheduleView the schedule view
     * @param date         the date
     * @param days         the number of days to display
     * @param events       the events
     * @param rules        the appointment rules
     */
    public CheckOutScheduleGrid(Entity scheduleView, Date date, int days, Map<Entity, List<PropertySet>> events,
                                AppointmentRules rules) {
        super(scheduleView, date, days, filterEvents(events, date), rules);
    }

    /**
     * Filters appointments so that they only include those checking out on or after the specified date.
     * <p/>
     * If a schedule has no appointments checking out on the date, then all of its appointments are excluded.
     *
     * @param events the appointments
     * @param date   the date
     * @return the filtered appointments
     */
    private static Map<Entity, List<PropertySet>> filterEvents(Map<Entity, List<PropertySet>> events, Date date) {
        Map<Entity, List<PropertySet>> map = new LinkedHashMap<>();
        for (Map.Entry<Entity, List<PropertySet>> eventsBySchedule : events.entrySet()) {
            Date midnight = DateRules.getNextDate(date);
            if (!eventsBySchedule.getValue().isEmpty()) {
                List<PropertySet> onDate = new ArrayList<>();        // check-outs on date
                List<PropertySet> afterDate = new ArrayList<>();     // check-outs after date
                for (PropertySet event : eventsBySchedule.getValue()) {
                    Date endTime = event.getDate(ScheduleEvent.ACT_END_TIME);
                    Date endDate = DateRules.getDate(endTime);
                    if (endDate.compareTo(date) == 0 || DateRules.compareTo(midnight, endTime) == 0) {
                        // consider appointments checking out at midnight to be for the same day
                        if (!Schedule.isBlockingEvent(event) || !onDate.isEmpty()) {
                            // only add blocking events on the date if there is an appointment before it, to provide
                            // context
                            onDate.add(event);
                        }
                    } else if (endDate.compareTo(date) > 0) {
                        afterDate.add(event);
                    }
                }
                if (!onDate.isEmpty()) {
                    onDate.addAll(afterDate);                   // add all check-outs after the date to provide context
                    map.put(eventsBySchedule.getKey(), onDate);
                }
            }
        }
        return map;
    }
}
