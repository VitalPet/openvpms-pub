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

package org.openvpms.web.workspace.admin.archetype;

import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.filetransfer.UploadEvent;
import nextapp.echo2.app.filetransfer.UploadListener;
import org.openvpms.archetype.component.processor.BatchProcessorListener;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptorWriter;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptors;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptors;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.descriptor.cache.ArchetypeDescriptorCacheDB;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.tools.archetype.comparator.ArchetypeChange;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.doc.AbstractUploadListener;
import org.openvpms.web.component.im.doc.UploadDialog;
import org.openvpms.web.component.im.edit.EditResultSetDialog;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.workspace.ResultSetCRUDWindow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.servlet.DownloadServlet;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * Archetype CRUD window, providing facilities to import and export archetype
 * descriptors.
 *
 * @author Tim Anderson
 */
public class ArchetypeCRUDWindow extends ResultSetCRUDWindow<ArchetypeDescriptor> {

    /**
     * The import button identifier.
     */
    private static final String IMPORT_ID = "button.import";

    /**
     * The export button identifier.
     */
    private static final String EXPORT_ID = "button.export";

    /**
     * The reload identifier.
     */
    private static final String RELOAD_ID = "button.reload";

    /**
     * The export mime type.
     */
    private static final String MIME_TYPE = "text/xml";


    /**
     * Constructs an {@code ArchetypeCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create
     * @param query      the query. May be {@code null}
     * @param set        the result set. May be {@code null}
     * @param context    the context
     * @param help       the help context
     */
    public ArchetypeCRUDWindow(Archetypes<ArchetypeDescriptor> archetypes, Query<ArchetypeDescriptor> query,
                               ResultSet<ArchetypeDescriptor> set, Context context, HelpContext help) {
        super(archetypes, query, set, context, help);
    }

    /**
     * Creates a new result set dialog for editing.
     *
     * @param object the first object to edit
     * @param title  the dialog title
     * @return a new dialog
     */
    @Override
    protected EditResultSetDialog<ArchetypeDescriptor> createEditResultSetDialog(ArchetypeDescriptor object,
                                                                                 String title) {
        return new ArchetypeEditDialog(title, object, getResultSet(), getActions(), getContext(), getHelpContext());
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        buttons.add(IMPORT_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onImport();
            }
        });
        buttons.add(EXPORT_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onExport();
            }
        });
        buttons.add(RELOAD_ID, new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onReload();
            }
        });
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        super.enableButtons(buttons, enable);
        buttons.setEnabled(EXPORT_ID, enable);
    }

    /**
     * Invoked when the import button is pressed.
     */
    private void onImport() {
        UploadListener listener = new AbstractUploadListener() {
            public void fileUpload(UploadEvent event) {
                InputStream stream = event.getInputStream();
                upload(stream, event.getFileName());
            }
        };
        UploadDialog dialog = new UploadDialog(listener, getHelpContext());
        dialog.show();
    }

    /**
     * Invoked when the export button is pressed.
     */
    private void onExport() {
        ArchetypeDescriptor descriptor = getObject();
        try {
            ArchetypeDescriptors descriptors = new ArchetypeDescriptors();
            descriptors.setArchetypeDescriptorsAsArray(new ArchetypeDescriptor[]{descriptor});
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            new ArchetypeDescriptorWriter(false, true).write(descriptors, stream);
            String name = descriptor.getShortName() + ".adl";
            DocumentHandlers handlers = ServiceHelper.getDocumentHandlers();
            DocumentHandler handler = handlers.get(name, MIME_TYPE);
            byte[] buffer = stream.toByteArray();
            Document document = handler.create(name, new ByteArrayInputStream(buffer), MIME_TYPE, buffer.length);
            DownloadServlet.startDownload(document);
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Invoked when the reload button is pressed.
     */
    private void onReload() {
        String title = Messages.get("archetype.reload.title");
        String message = Messages.get("archetype.reload.message");
        ConfirmationDialog dialog = new ConfirmationDialog(title, message, ConfirmationDialog.YES_NO);
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onYes() {
                ArchetypeDescriptorCacheDB cache = ServiceHelper.getBean(ArchetypeDescriptorCacheDB.class);
                cache.refresh();
            }
        });
        dialog.show();
    }

    /**
     * Uploads a file.
     *
     * @param stream   the file stream
     * @param fileName the file name
     */
    private void upload(InputStream stream, String fileName) {
        try {
            if (fileName == null || fileName.toLowerCase().endsWith(".adl")) {
                loadArchetypes(stream);
            } else {
                loadAssertions(stream);
            }
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Loads archetype descriptors from the supplied stream.
     *
     * @param stream the stream to read
     * @throws OpenVPMSException for any error
     */
    private void loadArchetypes(InputStream stream) {
        ArchetypeDescriptors descriptors
                = ArchetypeDescriptors.read(stream);

        final BatchArchetypeLoader loader = new BatchArchetypeLoader(descriptors);
        loader.setListener(new BatchProcessorListener() {
            public void completed() {
                uploaded(loader.getChanges());
            }

            public void error(Throwable exception) {
                ErrorHelper.show(exception);
            }
        });

        loader.process();
    }

    /**
     * Loads assertion type descriptors from the supplied stream.
     *
     * @param stream the stream to read
     * @throws org.openvpms.component.exception.OpenVPMSException
     *          for any error
     */
    private void loadAssertions(InputStream stream) {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        AssertionTypeDescriptors descriptors = AssertionTypeDescriptors.read(stream);
        for (AssertionTypeDescriptor descriptor : descriptors.getAssertionTypeDescriptors().values()) {
            service.save(descriptor);
        }
    }

    /**
     * Invoked when uploading is completed.
     *
     * @param changes the changes
     */
    private void uploaded(List<ArchetypeChange> changes) {
        if (!changes.isEmpty()) {
            super.onSaved(changes.get(0).getNewVersion(), true);
            List<ArchetypeChange> update = new ArrayList<ArchetypeChange>();
            for (ArchetypeChange change : changes) {
                if (change.hasChangedDerivedNodes() || change.hasAddedAssertions(BatchArchetypeUpdater.ASSERTIONS)) {
                    update.add(change);
                }
            }
            if (!update.isEmpty()) {
                ConfirmingBatchArchetypeUpdater updater = new ConfirmingBatchArchetypeUpdater();
                updater.confirmUpdate(update);
            }
        }
    }


}
