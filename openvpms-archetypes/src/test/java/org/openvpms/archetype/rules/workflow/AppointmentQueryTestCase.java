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

package org.openvpms.archetype.rules.workflow;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.openvpms.archetype.test.TestHelper.getDatetime;


/**
 * Tests the {@link AppointmentQuery} class.
 *
 * @author Tim Anderson
 */
public class AppointmentQueryTestCase extends ArchetypeServiceTest {

    /**
     * Tests the {@link AppointmentQuery#query()} method.
     */
    @Test
    public void testQuery() {
        final int count = 10;
        Party schedule = ScheduleTestHelper.createSchedule(TestHelper.createLocation());
        Date from = new Date();
        Act[] appointments = new Act[count];
        Date[] startTimes = new Date[count];
        Date[] endTimes = new Date[count];
        Date[] arrivalTimes = new Date[count];
        Party[] customers = new Party[count];
        Party[] patients = new Party[count];
        User[] clinicians = new User[count];
        Entity[] appointmentTypes = new Entity[count];
        for (int i = 0; i < count; ++i) {
            Date startTime = new Date();
            Date arrivalTime = (i % 2 == 0) ? new Date() : null;
            Date endTime = new Date();
            Party customer = TestHelper.createCustomer();
            Party patient = TestHelper.createPatient();
            User clinician = TestHelper.createClinician();
            User author = TestHelper.createClinician();
            Act appointment = ScheduleTestHelper.createAppointment(
                    startTime, endTime, schedule, customer, patient);
            ActBean bean = new ActBean(appointment);
            bean.addParticipation("participation.clinician", clinician);
            bean.addParticipation("participation.author", author);
            bean.setValue("arrivalTime", arrivalTime);
            appointments[i] = appointment;
            startTimes[i] = getTimestamp(startTime);
            arrivalTimes[i] = arrivalTime;
            endTimes[i] = getTimestamp(endTime);
            appointmentTypes[i] = bean.getNodeParticipant("appointmentType");
            customers[i] = customer;
            patients[i] = patient;
            clinicians[i] = clinician;
            bean.save();
        }
        Date to = new Date();

        AppointmentQuery query = new AppointmentQuery(schedule, from, to, getArchetypeService(), getLookupService());
        IPage<ObjectSet> page = query.query();
        assertNotNull(page);
        List<ObjectSet> results = page.getResults();
        assertEquals(count, results.size());
        for (int i = 0; i < results.size(); ++i) {
            ObjectSet set = results.get(i);
            assertEquals(appointments[i].getObjectReference(),
                         set.get(ScheduleEvent.ACT_REFERENCE));
            assertEquals(startTimes[i],
                         set.get(ScheduleEvent.ACT_START_TIME));
            assertEquals(endTimes[i], set.get(ScheduleEvent.ACT_END_TIME));
            assertEquals(appointments[i].getStatus(),
                         set.get(ScheduleEvent.ACT_STATUS));
            assertEquals(appointments[i].getReason(),
                         set.get(ScheduleEvent.ACT_REASON));
            assertEquals(appointments[i].getDescription(),
                         set.get(ScheduleEvent.ACT_DESCRIPTION));
            assertEquals(customers[i].getObjectReference(),
                         set.get(ScheduleEvent.CUSTOMER_REFERENCE));
            assertEquals(customers[i].getName(),
                         set.get(ScheduleEvent.CUSTOMER_NAME));
            assertEquals(patients[i].getObjectReference(),
                         set.get(ScheduleEvent.PATIENT_REFERENCE));
            assertEquals(patients[i].getName(),
                         set.get(ScheduleEvent.PATIENT_NAME));
            assertEquals(clinicians[i].getObjectReference(),
                         set.get(ScheduleEvent.CLINICIAN_REFERENCE));
            assertEquals(clinicians[i].getName(),
                         set.get(ScheduleEvent.CLINICIAN_NAME));
            assertEquals(schedule.getObjectReference(), set.get(ScheduleEvent.SCHEDULE_REFERENCE));
            assertEquals(schedule.getName(), set.get(ScheduleEvent.SCHEDULE_NAME));
            assertEquals(appointmentTypes[i].getObjectReference(),
                         set.get(ScheduleEvent.SCHEDULE_TYPE_REFERENCE));
            assertEquals(appointmentTypes[i].getName(),
                         set.get(ScheduleEvent.SCHEDULE_TYPE_NAME));
            assertEquals(arrivalTimes[i],
                         set.get(ScheduleEvent.ARRIVAL_TIME));
        }
    }

    /**
     * Verifies that appointments that intersect the query date range are returned.
     */
    @Test
    public void testDateRanges() {
        Date from = getDatetime("2015-01-03 10:30:00");
        Date to = getDatetime("2015-01-03 12:30:00");
        Party location = TestHelper.createLocation();
        Party schedule1 = ScheduleTestHelper.createSchedule(location);
        Party schedule2 = ScheduleTestHelper.createSchedule(location);

        // create some appointments before the query range
        createAppointment(schedule1, "2015-01-01 10:00:00", "2015-01-01 10:30:00");
        createAppointment(schedule1, "2015-01-02 11:00:00", "2015-01-03 10:30:00");
        createAppointment(schedule1, "2015-01-03 10:00:00", "2015-01-03 10:30:00");

        // create some appointments that intersect the query range
        Act act0 = createAppointment(schedule1, "2015-01-01 10:30:00", "2015-01-04 12:30:00"); // overlaps start,end
        Act act1 = createAppointment(schedule1, "2015-01-03 10:00:00", "2015-01-03 11:00:00"); // intersect start
        Act act2 = createAppointment(schedule1, "2015-01-03 10:30:00", "2015-01-03 11:00:00"); // at start
        Act act3 = createAppointment(schedule1, "2015-01-03 10:30:00", "2015-01-03 12:30:00"); // equals start,end
        Act act4 = createAppointment(schedule1, "2015-01-03 11:00:00", "2015-01-03 12:00:00"); // between
        Act act5 = createAppointment(schedule1, "2015-01-03 12:00:00", "2015-01-03 12:30:00"); // at end
        Act act6 = createAppointment(schedule1, "2015-01-03 12:00:00", "2015-01-03 13:00:00"); // intersect end

        // create some appointments after the query range
        createAppointment(schedule1, "2015-01-03 12:30:00", "2015-01-03 13:30:00");
        createAppointment(schedule1, "2015-01-03 12:30:00", "2015-01-04 10:30:00");
        createAppointment(schedule1, "2015-01-04 10:00:00", "2015-01-04 10:30:00");

        // create some appointments that intersect the range, but for a different schedule
        createAppointment(schedule2, "2015-01-01 10:30:00", "2015-01-04 12:30:00"); // overlaps start,end
        createAppointment(schedule2, "2015-01-03 10:00:00", "2015-01-03 11:00:00"); // intersect start
        createAppointment(schedule2, "2015-01-03 10:30:00", "2015-01-03 11:00:00"); // at start

        AppointmentQuery query = new AppointmentQuery(schedule1, from, to, getArchetypeService(), getLookupService());
        IPage<ObjectSet> page = query.query();
        List<ObjectSet> appointments = page.getResults();
        assertEquals(7, appointments.size());

        assertEquals(act0.getObjectReference(), appointments.get(0).get(ScheduleEvent.ACT_REFERENCE));
        assertEquals(act1.getObjectReference(), appointments.get(1).get(ScheduleEvent.ACT_REFERENCE));
        assertEquals(act2.getObjectReference(), appointments.get(2).get(ScheduleEvent.ACT_REFERENCE));
        assertEquals(act3.getObjectReference(), appointments.get(3).get(ScheduleEvent.ACT_REFERENCE));
        assertEquals(act4.getObjectReference(), appointments.get(4).get(ScheduleEvent.ACT_REFERENCE));
        assertEquals(act5.getObjectReference(), appointments.get(5).get(ScheduleEvent.ACT_REFERENCE));
        assertEquals(act6.getObjectReference(), appointments.get(6).get(ScheduleEvent.ACT_REFERENCE));
    }

    /**
     * Helper to remove any seconds from a time, as the database may not
     * store them.
     *
     * @param timestamp the timestamp
     * @return the timestamp with seconds and milliseconds removed
     */
    private Date getTimestamp(Date timestamp) {
        return DateUtils.truncate(new Date(timestamp.getTime()), Calendar.SECOND);
    }

    /**
     * Helper to create an appointment.
     *
     * @param schedule the schedule
     * @param from     the appointment start time
     * @param to       the appointment end time
     * @return a new appointment
     */
    private Act createAppointment(Entity schedule, String from, String to) {
        Act appointment = ScheduleTestHelper.createAppointment(getDatetime(from), getDatetime(to), schedule);
        save(appointment);
        return appointment;
    }

}