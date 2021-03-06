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

package org.openvpms.component.business.service.archetype.helper;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.AbstractArchetypeServiceListener;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeServiceListener;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.springframework.beans.factory.DisposableBean;

/**
 * Cache that monitors updates to objects from the {@link IArchetypeService}.
 *
 * @author Tim Anderson
 */
public abstract class AbstractMonitoringIMObjectCache<T extends IMObject> implements DisposableBean {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The listener for archetype service events.
     */
    private final IArchetypeServiceListener listener;

    /**
     * The short names of the objects to cache.
     */
    private final String[] shortNames;

    /**
     * The type of objects to cache.
     */
    private final Class<T> type;


    /**
     * Constructs an {@link AbstractMonitoringIMObjectCache}.
     *
     * @param service   the archetype service
     * @param shortName the short name to cache
     * @param type      the object types
     */
    public AbstractMonitoringIMObjectCache(IArchetypeService service, String shortName, final Class<T> type) {
        this(service, new String[]{shortName}, type);
    }

    /**
     * Constructs an {@link AbstractMonitoringIMObjectCache}.
     *
     * @param service    the archetype service
     * @param shortNames the short name to cache
     * @param type       the object types
     */
    public AbstractMonitoringIMObjectCache(IArchetypeService service, String[] shortNames, final Class<T> type) {
        this.service = service;
        this.shortNames = shortNames;
        this.type = type;

        listener = new AbstractArchetypeServiceListener() {
            @Override
            public void saved(IMObject object) {
                AbstractMonitoringIMObjectCache.this.addObject(type.cast(object));
            }

            @Override
            public void removed(IMObject object) {
                AbstractMonitoringIMObjectCache.this.removeObject(type.cast(object));
            }
        };
        for (String shortName : shortNames) {
            service.addListener(shortName, listener);
        }
    }

    /**
     * Invoked by a BeanFactory on destruction of a singleton.
     */
    @Override
    public void destroy() {
        for (String shortName : shortNames) {
            service.removeListener(shortName, listener);
        }
    }

    /**
     * Returns the archetypes being monitored.
     *
     * @return the archetypes
     */
    protected String[] getArchetypes() {
        return shortNames;
    }

    /**
     * Loads objects from the archetype service.
     */
    protected void load() {
        ArchetypeQuery query = new ArchetypeQuery(shortNames, true, true);
        IMObjectQueryIterator<T> iter = new IMObjectQueryIterator<T>(service, query);
        while (iter.hasNext()) {
            addObject(iter.next());
        }
    }

    /**
     * Adds an object to the cache.
     * <p>
     * Implementations may ignore the object if it is older than any cached instance, or is inactive
     *
     * @param object the object to add
     */
    protected abstract void addObject(T object);

    /**
     * Removes an object.
     *
     * @param object the object to remove
     */
    protected abstract void removeObject(T object);

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getService() {
        return service;
    }

    /**
     * Returns the object corresponding to the reference.
     *
     * @param reference the reference
     * @return the corresponding object or {@code null} if none is found
     */
    protected T get(IMObjectReference reference) {
        return type.cast(service.get(reference));
    }
}
