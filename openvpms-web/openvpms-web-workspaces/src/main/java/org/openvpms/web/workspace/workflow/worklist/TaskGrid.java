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
import org.openvpms.archetype.rules.workflow.ScheduleEvent;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.workspace.workflow.appointment.AbstractScheduleEventGrid;
import org.openvpms.web.workspace.workflow.scheduling.Schedule;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * Task event grid.
 *
 * @author Tim Anderson
 */
public class TaskGrid extends AbstractScheduleEventGrid {

    /**
     * The no. of slots in the grid. This is calculated to be number of
     * events in the schedule containing the most events. If slots is less
     * than that schedules maxSlots, then 1 is added to indicate that there
     * are slots available.
     */
    private int slots;


    /**
     * Constructs a {@link TaskGrid}.
     *
     * @param scheduleView the schedule view
     * @param date         the date
     * @param tasks        the tasks
     * @param rules        the appointment rules
     */
    public TaskGrid(Entity scheduleView, Date date, Map<Entity, List<PropertySet>> tasks, AppointmentRules rules) {
        super(scheduleView, date, rules);

        List<Schedule> schedules = new ArrayList<>();
        for (Map.Entry<Entity, List<PropertySet>> entry : tasks.entrySet()) {
            Entity workList = entry.getKey();
            List<PropertySet> sets = entry.getValue();
            TaskSchedule schedule = new TaskSchedule(workList, rules);
            for (PropertySet set : sets) {
                schedule.addEvent(set);
            }
            schedules.add(schedule);

            // display up to events + 1 slots
            int events = schedule.getSlots() + 1;
            if (slots < events) {
                slots = (events <= schedule.getMaxSlots()) ? events : schedule.getMaxSlots();
            } else if (slots == 0) {
                slots = schedule.getSlots();
            }
        }
        setSchedules(schedules);
    }

    /**
     * Returns the no. of slots in the grid.
     *
     * @return the no. of slots
     */
    public int getSlots() {
        return slots;
    }

    /**
     * Returns the event for the specified schedule and slot.
     *
     * @param schedule the schedule
     * @param slot     the slot
     * @return the corresponding event, or <tt>null</tt> if none is found
     */
    public PropertySet getEvent(Schedule schedule, int slot) {
        List<PropertySet> events = schedule.getEvents();
        return (events.size() > slot) ? events.get(slot) : null;
    }

    /**
     * Returns the time that the specified slot starts at.
     *
     * @param schedule the schedule
     * @param slot     the slot
     * @return the start time of the specified slot. May be <tt>null</tt>
     */
    public Date getStartTime(Schedule schedule, int slot) {
        PropertySet event = getEvent(schedule, slot);
        return (event != null)
               ? event.getDate(ScheduleEvent.ACT_START_TIME) : null;
    }

    /**
     * Determines the availability of a slot for the specified schedule.
     *
     * @param schedule the schedule
     * @param slot     the slot
     * @return the availability of the schedule
     */
    public Availability getAvailability(Schedule schedule,
                                        int slot) {
        TaskSchedule s = (TaskSchedule) schedule;
        if (slot < s.getSlots()) {
            return Availability.BUSY;
        } else if (slot < s.getMaxSlots()) {
            return Availability.FREE;
        }
        return Availability.UNAVAILABLE;
    }

    /**
     * Determines how many slots are unavailable from the specified slot, for
     * a schedule.
     *
     * @param schedule the schedule
     * @param slot     the starting slot
     * @return the no. of concurrent slots that are unavailable
     */
    public int getUnavailableSlots(Schedule schedule, int slot) {
        TaskSchedule s = (TaskSchedule) schedule;
        if (slot >= s.getMaxSlots()) {
            return 0;
        }
        return getSlots() - slot;
    }

    /**
     * Returns the slot that a time falls in.
     *
     * @param time the time
     * @return the slot, or {@code -1} if the time doesn't intersect any slot
     */
    @Override
    public int getSlot(Date time) {
        return -1;
    }
}
