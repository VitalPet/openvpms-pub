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

package org.openvpms.web.workspace.workflow.appointment;

import net.sf.jasperreports.engine.util.ObjectUtils;
import org.openvpms.archetype.rules.prefs.PreferenceArchetypes;
import org.openvpms.archetype.rules.prefs.Preferences;
import org.openvpms.archetype.rules.workflow.AppointmentRules;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.util.PropertySet;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleBrowser;
import org.openvpms.web.workspace.workflow.scheduling.ScheduleCRUDWindow;
import org.openvpms.web.workspace.workflow.scheduling.SchedulingWorkspace;

import java.util.Date;

/**
 * Appointment workspace.
 *
 * @author Tim Anderson
 */
public class AppointmentWorkspace extends SchedulingWorkspace {

    /**
     * Constructs an {@link AppointmentWorkspace}.
     *
     * @param context     the context
     * @param preferences user preferences
     */
    public AppointmentWorkspace(Context context, Preferences preferences) {
        super("workflow.scheduling", Archetypes.create("entity.organisationScheduleView", Entity.class), context,
              preferences, PreferenceArchetypes.SCHEDULING);
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be {@code null}
     */
    @Override
    public void setObject(Entity object) {
        ScheduleBrowser browser = getBrowser();
        PropertySet marked = null;
        boolean isCut = false;
        if (browser != null) {
            marked = browser.getMarked();
            isCut = browser.isCut();
        }
        getContext().setScheduleView(object);
        super.setObject(object);
        browser = getBrowser();
        if (browser != null && marked != null) {
            browser.setMarked(marked, isCut);
        }
    }

    /**
     * Determines if the workspace can be updated with instances of the specified archetype.
     *
     * @param shortName the archetype's short name
     * @return {@code true} if the workspace can be updated by the archetype; otherwise {@code false}
     * @see #update
     */
    @Override
    public boolean canUpdate(String shortName) {
        return super.canUpdate(shortName) || ScheduleArchetypes.APPOINTMENT.equals(shortName);
    }

    /**
     * Updates the workspace with the specified object.
     *
     * @param object the object to update the workspace with
     */
    @Override
    public void update(IMObject object) {
        if (TypeHelper.isA(object, ScheduleArchetypes.SCHEDULE_VIEW)) {
            if (!ObjectUtils.equals(getObject(), object)) {
                setObject((Entity) object);
            }
        } else if (TypeHelper.isA(object, ScheduleArchetypes.APPOINTMENT)) {
            Act act = (Act) object;
            if (!ObjectUtils.equals(getCRUDWindow().getObject(), act)) {
                ActBean bean = new ActBean(act);
                Entity schedule = bean.getNodeParticipant("schedule");
                Party location = getContext().getLocation();
                if (schedule != null && location != null) {
                    AppointmentRules rules = ServiceHelper.getBean(AppointmentRules.class);
                    Entity view = rules.getScheduleView(location, schedule);
                    if (view != null) {
                        setScheduleView(view, act.getActivityStartTime());
                        ScheduleBrowser scheduleBrowser = getBrowser();
                        scheduleBrowser.setSelected(scheduleBrowser.getEvent(act));
                        getCRUDWindow().setObject(act);
                    }
                }
            }
        }
    }

    /**
     * Sets the schedule view and date.
     *
     * @param view the schedule view
     * @param date the date to view
     */
    @Override
    protected void setScheduleView(Entity view, Date date) {
        // TODO - this is icky. Should be done via setObject(), but for SchedulingWorkspace.setScheduleView()
        // invoking super.setObject() to avoid unwanted side-effects
        getContext().setScheduleView(view);
        super.setScheduleView(view, date);
    }

    /**
     * Creates a new browser.
     *
     * @return a new browser
     */
    protected ScheduleBrowser createBrowser() {
        Context context = getContext();
        return new AppointmentBrowser(context.getLocation(), new DefaultLayoutContext(getContext(), getHelpContext()));
    }

    /**
     * Creates a new CRUD window.
     *
     * @return a new CRUD window
     */
    protected ScheduleCRUDWindow createCRUDWindow() {
        return new AppointmentCRUDWindow((AppointmentBrowser) getBrowser(), getContext(), getHelpContext());
    }

    /**
     * Invoked when events are queried.
     * <p/>
     * This implementation updates the context with the selected schedule date and schedule.
     */
    @Override
    protected void onQuery() {
        Context context = getContext();
        ScheduleBrowser browser = getBrowser();
        context.setScheduleDate(browser.getDate());
        context.setSchedule((Party) browser.getSelectedSchedule());
        Act act = browser.getAct(browser.getSelected());
        getCRUDWindow().setObject(act);
    }

    /**
     * Invoked when an event is selected.
     *
     * @param event the event. May be {@code null}
     */
    @Override
    protected void eventSelected(PropertySet event) {
        // update the context schedule
        updateContext();
        super.eventSelected(event);
        getContext().setAppointment(getCRUDWindow().getObject());
    }

    /**
     * Invoked to edit an event.
     *
     * @param event the event
     */
    @Override
    protected void onEdit(PropertySet event) {
        updateContext();
        super.onEdit(event);
    }

    /**
     * Returns the latest version of the current schedule view context object.
     *
     * @return the latest version of the schedule view context object, or {@link #getObject()} if they are the same
     */
    @Override
    protected Entity getLatest() {
        return getLatest(getContext().getScheduleView());
    }

    /**
     * Returns the default schedule view for the specified practice location.
     *
     * @param location the practice location
     * @param prefs    user preferences
     * @return the default schedule view, or {@code null} if there is no default
     */
    protected Entity getDefaultView(Party location, Preferences prefs) {
        return new AppointmentSchedules(location, prefs).getDefaultScheduleView();
    }

    /**
     * Updates the context with the selected schedule.
     */
    private void updateContext() {
        Party schedule = (Party) getBrowser().getSelectedSchedule();
        getContext().setSchedule(schedule);
    }

}
