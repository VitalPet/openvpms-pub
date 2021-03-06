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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.query;

import echopointng.DateChooser;
import echopointng.DateField;
import echopointng.model.CalendarEvent;
import echopointng.model.CalendarSelectionListener;
import echopointng.model.CalendarSelectionModel;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.DateFieldFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.style.Styles;

import java.util.Calendar;
import java.util.Date;


/**
 * A date selector that provides:
 * <ul>
 * <li>an editable date field</li>
 * <li>buttons to add/subtract 1 and 7 days from the displayed date</li>
 * <li>a button to jump to the current date</li>
 * </ul>
 *
 * @author Tim Anderson
 */
public class DateSelector {

    /**
     * The date.
     */
    private DateField date;

    /**
     * The last date processed by {@link #onDateChanged()}.
     * todo - this is a workaround for a bug/feature of the EPNG date field
     * which seems to generate 2 events for the one update to the text field.
     */
    private Date lastDate;

    /**
     * Listener to notify of date change events.
     */
    private ActionListener listener;

    /**
     * The component.
     */
    private Component component;

    /**
     * The focus group.
     */
    private FocusGroup focus;

    /**
     * Listener for calendar events.
     */
    private CalendarSelectionListener calendarListener;

    /**
     * The date navigator.
     */
    private DateNavigator navigator = DateNavigator.DAY;


    /**
     * Constructs a {@link DateSelector}.
     */
    public DateSelector() {
        date = DateFieldFactory.create();
        calendarListener = new CalendarSelectionListener() {
            public void selectedDateChange(CalendarEvent event) {
                onDateChanged();
            }

            public void displayedDateChange(CalendarEvent event) {
            }
        };
        date.getModel().addListener(calendarListener);
        focus = new FocusGroup("dateSelector");
    }

    /**
     * Set the displayed date
     *
     * @param date the date to set
     */
    public void setDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        CalendarSelectionModel model = this.date.getModel();
        model.removeListener(calendarListener);
        DateChooser dateChooser = this.date.getDateChooser();
        dateChooser.setSelectedDate(calendar);
        dateChooser.setDisplayedDate(calendar);
        model.addListener(calendarListener);
    }

    /**
     * Returns the selected date.
     *
     * @return the selected date
     */
    public Date getDate() {
        Date datetime = date.getDateChooser().getSelectedDate().getTime();
        return DateRules.getDate(datetime);
    }

    /**
     * Sets a listener to be notified of date change events.
     *
     * @param listener the listener. May be {@code null}
     */
    public void setListener(ActionListener listener) {
        this.listener = listener;
    }

    /**
     * Sets the date navigator.
     *
     * @param navigator the navigator
     */
    public void setNavigator(DateNavigator navigator) {
        this.navigator = navigator;
    }

    /**
     * Returns the component representing the date selector.
     *
     * @return the component
     */
    public Component getComponent() {
        if (component == null) {
            doLayout();
        }
        return component;
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group
     */
    public FocusGroup getFocusGroup() {
        return focus;
    }

    /**
     * Lays out the component.
     */
    private void doLayout() {
        Button prevWeek = ButtonFactory.create(null, "date.previousWeek", new ActionListener() {
            public void onAction(ActionEvent event) {
                update(navigator.getPreviousTerm(getDate()));
            }
        });
        Button prevDay = ButtonFactory.create(null, "date.previousDay", new ActionListener() {
            public void onAction(ActionEvent event) {
                update(navigator.getPrevious(getDate()));
            }
        });
        Button currentDay = ButtonFactory.create(null, "date.currentDay", new ActionListener() {
            public void onAction(ActionEvent event) {
                update(navigator.getCurrent());
            }
        });
        Button nextDay = ButtonFactory.create(null, "date.nextDay", new ActionListener() {
            public void onAction(ActionEvent event) {
                update(navigator.getNext(getDate()));
            }
        });
        Button nextWeek = ButtonFactory.create(null, "date.nextWeek", new ActionListener() {
            public void onAction(ActionEvent event) {
                update(navigator.getNextTerm(getDate()));
            }
        });

        focus.add(prevWeek);
        focus.add(prevDay);
        focus.add(date);
        focus.add(currentDay);
        focus.add(nextDay);
        focus.add(nextWeek);
        component = RowFactory.create(Styles.CELL_SPACING, prevWeek, prevDay, date, currentDay, nextDay, nextWeek);
    }

    /**
     * Invoked when the date is updated.
     */
    protected void onDateChanged() {
        Date date = getDate();
        if (!date.equals(lastDate)) {
            lastDate = date;
            if (listener != null) {
                listener.actionPerformed(new ActionEvent(this, "date"));
            }
        }
    }

    /**
     * Updates the date, notifying listeners.
     *
     * @param date the date
     */
    private void update(Date date) {
        setDate(date);
        onDateChanged();
    }

}
