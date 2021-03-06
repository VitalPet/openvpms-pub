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

package org.openvpms.web.component.print;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.list.DefaultListModel;
import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.bound.SpinBox;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.SelectFieldFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


/**
 * Print dialog.
 *
 * @author Tim Anderson
 */
public class PrintDialog extends PopupDialog {

    /**
     * The printers.
     */
    private final SelectField printers;

    /**
     * Determines if the preview button should be added.
     */
    private final boolean preview;

    /**
     * Determines if the mail button should be added.
     */
    private final boolean mail;

    /**
     * The preview button identifier.
     */
    private static final String PREVIEW_ID = "preview";

    /**
     * The mail button identifier.
     */
    private static final String MAIL_ID = "mail";

    /**
     * The no. of copies to print.
     */
    private SpinBox copies;


    /**
     * Constructs a {@link PrintDialog}.
     *
     * @param title    the window title
     * @param preview  if {@code true} add a 'preview' button
     * @param mail     if {@code true} add a 'mail' button
     * @param skip     if {@code true} display a 'skip' button that simply closes the dialog
     * @param location the current practice location. May be {@code null}
     * @param help     the help context. May be {@code null}
     */
    public PrintDialog(String title, boolean preview, boolean mail, boolean skip, Party location, HelpContext help) {
        super(title, "PrintDialog", (skip) ? OK_SKIP_CANCEL : OK_CANCEL, help);
        setModal(true);
        copies = new SpinBox(1, 99);
        DefaultListModel model = new DefaultListModel(getPrinters(location));
        printers = SelectFieldFactory.create(model);
        this.preview = preview;
        this.mail = mail;
    }

    /**
     * Sets the default printer.
     *
     * @param name the default printer name. May be {@code null}
     */
    public void setDefaultPrinter(String name) {
        DefaultListModel model = (DefaultListModel) printers.getModel();
        int index = model.indexOf(name);
        if (index != -1) {
            printers.setSelectedIndex(index);
        }
    }

    /**
     * Returns the selected printer.
     *
     * @return the selected printer, or {@code null} if none is selected
     */
    public String getPrinter() {
        return (String) printers.getSelectedItem();
    }

    /**
     * Sets the number of copies to print.
     *
     * @param copies the number of copies to print
     */
    public void setCopies(int copies) {
        this.copies.setValue(copies);
    }

    /**
     * Returns the number of copies to print.
     *
     * @return the number of copies to print
     */
    public int getCopies() {
        return copies.getValue();
    }

    /**
     * Lays out the component prior to display.
     */
    @Override
    protected void doLayout() {
        Column column = ColumnFactory.create("WideCellSpacing");
        doLayout(column);
        getLayout().add(ColumnFactory.create("Inset", column));
    }

    /**
     * Lays out the dialog.
     *
     * @param container the container
     */
    protected void doLayout(Component container) {
        if (preview) {
            addButton(PREVIEW_ID, new ActionListener() {
                public void onAction(ActionEvent e) {
                    onPreview();
                }
            });
        }
        if (mail) {
            addButton(MAIL_ID, new ActionListener() {
                public void onAction(ActionEvent e) {
                    onMail();
                }
            });
        }

        FocusGroup parent = getFocusGroup();
        FocusGroup child = new FocusGroup("PrintDialog");
        child.add(printers);
        child.add(copies.getFocusGroup());
        parent.add(0, child); // insert before buttons

        Grid grid = GridFactory.create(2);
        grid.add(LabelFactory.create("printdialog.printer"));
        grid.add(printers);
        grid.add(LabelFactory.create("printdialog.copies"));
        grid.add(copies);

        setFocus(copies);

        container.add(grid);
    }

    /**
     * Invoked when the preview button is pressed.
     * This implementation does nothing.
     */
    protected void onPreview() {
    }

    /**
     * Invoked when the mail button is pressed.
     * This implementation does nothing.
     */
    protected void onMail() {
    }

    /**
     * Returns the printers available at a practice location.
     *
     * @param location the practice location. May be {@code null}
     * @return the printers
     */
    protected String[] getPrinters(Party location) {
        String[] available = PrintHelper.getPrinters();
        String[] result;
        if (location != null) {
            LocationRules rules = ServiceHelper.getBean(LocationRules.class);
            Collection<String> printerNames = rules.getPrinterNames(location);
            if (!printerNames.isEmpty()) {
                Set<String> set = new HashSet<>(Arrays.asList(available));
                String defaultPrinter = rules.getDefaultPrinter(location);
                if (defaultPrinter != null) {
                    // add the default printer, if one is defined, but not listed as an available printer
                    printerNames = new ArrayList<>(printerNames);
                    printerNames.add(defaultPrinter);
                }
                set.retainAll(printerNames);
                result = set.toArray(new String[set.size()]);
            } else {
                result = available;
            }
        } else {
            result = available;
        }
        return result;
    }

}
