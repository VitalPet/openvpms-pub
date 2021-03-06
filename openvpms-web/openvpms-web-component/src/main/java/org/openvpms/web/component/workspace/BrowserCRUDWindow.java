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

package org.openvpms.web.component.workspace;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserListener;
import org.openvpms.web.echo.util.DoubleClickMonitor;

/**
 * Links a {@link Browser} to a {@link CRUDWindow}.
 *
 * @author Tim Anderson
 */
public class BrowserCRUDWindow<T extends IMObject> {

    /**
     * The browser.
     */
    private Browser<T> browser;

    /**
     * The CRUD window.
     */
    private AbstractCRUDWindow<T> window;

    /**
     * Helper to monitor double clicks. When an act is double clicked, an edit dialog is displayed
     */
    private DoubleClickMonitor click = new DoubleClickMonitor();


    /**
     * Constructs a {@code BrowserCRUDWindow}.
     */
    protected BrowserCRUDWindow() {

    }

    /**
     * Constructs a {@link BrowserCRUDWindow}.
     *
     * @param browser the browser
     * @param window  the window
     */
    public BrowserCRUDWindow(Browser<T> browser, AbstractCRUDWindow<T> window) {
        setBrowser(browser);
        setWindow(window);
    }

    /**
     * Returns the browser.
     *
     * @return the browser
     */
    public Browser<T> getBrowser() {
        return browser;
    }

    /**
     * Returns the CRUD window.
     *
     * @return the window
     */
    public CRUDWindow<T> getWindow() {
        return window;
    }

    /**
     * Sets the selected object.
     *
     * @param object the selected object
     */
    public void setSelected(T object) {
        browser.setSelected(object);
        if (window != null) {
            select(browser.getSelected());
        }
    }

    /**
     * Registers the browser.
     *
     * @param browser the browser
     */
    protected void setBrowser(Browser<T> browser) {
        this.browser = browser;
        browser.addBrowserListener(new BrowserListener<T>() {
            @Override
            public void query() {
                onQuery();
            }

            public void selected(T object) {
                onSelected(object);
            }

            @Override
            public void browsed(T object) {
                onBrowsed(object);
            }

        });
        if (window != null) {
            select(browser.getSelected());
        }
    }

    /**
     * Registers the window.
     *
     * @param window the window
     */
    protected void setWindow(AbstractCRUDWindow<T> window) {
        this.window = window;
        window.setListener(new CRUDWindowListener<T>() {
            public void saved(T object, boolean isNew) {
                onSaved(object, isNew);
            }

            public void deleted(T object) {
                onDeleted(object);
            }

            public void refresh(T object) {
                onRefresh(object);
            }
        });
        if (browser != null) {
            window.setObject(browser.getSelected());
        }
    }

    /**
     * Invoked when a query is performed.
     * <p/>
     * This implementation is a no-op.
     */
    protected void onQuery() {

    }

    /**
     * Selects the current object. If the object is double clicked, invokes {@link #onDoubleClick()}.
     *
     * @param object the selected object
     */
    protected void onSelected(T object) {
        select(object);
        if (click.isDoubleClick(object.getId())) {
            onDoubleClick();
        }
    }

    /**
     * Selects the current object.
     *
     * @param object the selected object
     */
    protected void select(T object) {
        if (window.getObject() != object) {
            window.setObject(object);
        }
    }

    /**
     * Invoked when an object is double clicked.
     * <p/>
     * This implementation edits the object
     */
    protected void onDoubleClick() {
        window.edit();
    }

    /**
     * Invoked when a browser object is viewed (aka 'browsed').
     * <p/>
     * This implementation sets the object in the CRUD window.
     *
     * @param object the selected object
     */
    protected void onBrowsed(T object) {
        select(object);
    }

    /**
     * Invoked when an object is saved.
     *
     * @param object the saved object
     * @param isNew  determines if the object is a new instance
     */
    protected void onSaved(T object, boolean isNew) {
        refreshBrowser(object);
    }

    /**
     * Invoked when an object is deleted.
     *
     * @param object the deleted object
     */
    protected void onDeleted(T object) {
        refreshBrowser(null);
    }

    /**
     * Invoked when the parent needs to refresh an object.
     *
     * @param object the object to refresh
     */
    protected void onRefresh(T object) {
        if (object.isNew()) {
            // object not persistent, so don't attempt to reselect after refresh
            refreshBrowser(null);
        } else {
            refreshBrowser(object);
        }
    }

    /**
     * Refresh the browser.
     *
     * @param object the object to select. May be {@code null}
     */
    private void refreshBrowser(T object) {
        browser.query();
        if (browser.setSelected(object)) {
            if (window.getObject() != object) {
                window.setObject(object);
            }
        } else {
            window.setObject(null);
        }
    }

}
