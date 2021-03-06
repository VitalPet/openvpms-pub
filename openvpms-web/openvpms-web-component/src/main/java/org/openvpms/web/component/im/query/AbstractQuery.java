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

package org.openvpms.web.component.im.query;

import nextapp.echo2.app.Extent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.StyleSheetHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Abstract implementation of the {@link Query} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractQuery<T> implements Query<T> {

    /**
     * The type that this query returns.
     */
    private final Class type;

    /**
     * Archetype short names to match on.
     */
    private final String[] shortNames;

    /**
     * The name being queried on.
     */
    private String name;

    /**
     * Determines if substring searches should be performed by default.
     */
    private boolean contains;

    /**
     * Determines if the query should be run automatically.
     */
    private boolean auto;

    /**
     * The minimum length of the name field, before queries can be performed.
     */
    private int valueMinLength;

    /**
     * Determines if duplicate rows should be filtered.
     */
    private boolean distinct;

    /**
     * The maximum no. of results to return per page.
     */
    private int maxResults = 20;

    /**
     * The default sort constraints. May be {@code null}
     */
    private SortConstraint[] sort;

    /**
     * The event listener list.
     */
    private List<QueryListener> listeners = new ArrayList<>();

    /**
     * Additional constraints to associate with the query. May be {@code null}.
     */
    private IConstraint constraints;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(AbstractQuery.class);


    /**
     * Construct a new {@code AbstractQuery} that queries objects with
     * the specified primary short names.
     *
     * @param shortNames the archetype short names
     * @param type       the type that this query returns
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    public AbstractQuery(String[] shortNames, Class type) {
        this(shortNames, true, type);
    }

    /**
     * Construct a new {@code AbstractQuery} that queries objects with
     * the specified short names.
     *
     * @param shortNames  the archetype short names
     * @param primaryOnly if {@code true} only include primary archetypes
     * @param type        the type that this query returns
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    @SuppressWarnings("unchecked")
    public AbstractQuery(String[] shortNames, boolean primaryOnly, Class type) {
        this.shortNames = DescriptorHelper.getShortNames(shortNames, primaryOnly);
        this.type = type;
        if (IMObject.class.isAssignableFrom(type)) {
            // verify that the specified type matches what the query actually // returns
            Class actual = IMObjectHelper.getType(this.shortNames);
            if (!type.isAssignableFrom(actual)) {
                throw new QueryException(QueryException.ErrorCode.InvalidType, type, actual);

            }
        }
    }

    /**
     * Construct a new {@code AbstractQuery} that queries objects with
     * the specified short names.
     *
     * @param shortNames  the archetype short names
     * @param primaryOnly if {@code true} only include primary archetypes
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    public AbstractQuery(String[] shortNames, boolean primaryOnly) {
        this.shortNames = DescriptorHelper.getShortNames(shortNames, primaryOnly);
        type = IMObjectHelper.getType(this.shortNames);
    }

    /**
     * Returns the type that this query returns.
     *
     * @return the type
     */
    public Class getType() {
        return type;
    }

    /**
     * Sets the maximum no. of results to return per page.
     *
     * @param maxResults the maxiomum no. of rows per page
     */
    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    /**
     * Returns the maximum no. of results to return per page.
     *
     * @return the maximum no. of results to return per page
     */
    public int getMaxResults() {
        return maxResults;
    }

    /**
     * Sets the default sort constraint.
     *
     * @param sort the default sort constraint. May be {@code null}
     */
    public void setDefaultSortConstraint(SortConstraint[] sort) {
        this.sort = sort;
    }

    /**
     * Returns the default sort constraint
     *
     * @return the default sort constraint. May be {@code null}
     */
    public SortConstraint[] getDefaultSortConstraint() {
        return sort;
    }

    /**
     * Performs the query using the default sort constraint (if any).
     *
     * @return the query result set. May be {@code null}
     * @throws ArchetypeServiceException for any error
     */
    public ResultSet<T> query() {
        return query(sort);
    }

    /**
     * Determines if the query selects a particular object.
     * <p/>
     * This implementation performs a linear search.
     *
     * @param object the object to check
     * @return {@code true} if the object is selected by the query
     */
    public boolean selects(T object) {
        long start = System.currentTimeMillis();
        for (T next : this) {
            if (next.equals(object)) {
                return true;
            }
        }
        long end = System.currentTimeMillis();
        if ((end - start) > 1000) {
            // If it takes more than a second then optimization is required.
            // Could argue that a second is too long.
            log.warn("Slow query: " + getClass().getName() + " performing linear search");
        }
        return false;
    }

    /**
     * Performs the query using the default sort constraint, and adapts the
     * results to an iterator.
     *
     * @param sort the sort constraint. May be {@code null}
     * @return an iterator over the results.
     * @throws ArchetypeServiceException if the query fails
     */
    public Iterator<T> iterator(SortConstraint[] sort) {
        ResultSet<T> set = query(sort);
        if (set == null) {
            set = new EmptyResultSet<>(10);
        }
        return new ResultSetIterator<>(set);
    }

    /**
     * Performs the query using the default sort constraint, and adapts the
     * results to an iterator.
     *
     * @return an iterator over the results.
     * @throws ArchetypeServiceException if the query fails
     */
    public Iterator<T> iterator() {
        return iterator(sort);
    }

    /**
     * The archetype short names being queried.
     * Any wildcards are expanded.
     *
     * @return the short names being queried
     */
    public String[] getShortNames() {
        return shortNames;
    }

    /**
     * Sets the value to query on.
     *
     * @param value the value. May contain wildcards, or be {@code null}
     */
    public void setValue(String value) {
        this.name = value;
    }

    /**
     * Returns the name being queried on.
     *
     * @return the name. May contain wildcards, or be {@code null}
     */
    public String getValue() {
        return name;
    }

    /**
     * Determines if substring searches should be performed by default.
     *
     * @param contains if {@code true}, perform substring searches, otherwise only perform them if wildcards are present
     */
    @Override
    public void setContains(boolean contains) {
        this.contains = contains;
    }

    /**
     * Determines if substring searches should be performed by default.
     *
     * @return {@code true} to perform substring searches, {@code false} to only perform them if wildcards are present
     */
    @Override
    public boolean isContains() {
        return contains;
    }

    /**
     * Sets the minimum length of a value before queries can be performed.
     *
     * @param length the minimum length
     */
    public void setValueMinLength(int length) {
        valueMinLength = length;
    }

    /**
     * Returns the minimum length of a value before queries can be performed.
     *
     * @return the minimum length
     */
    public int getValueMinLength() {
        return valueMinLength;
    }

    /**
     * Determines if the query should be run automatically.
     *
     * @param auto if {@code true} the query should be run automatically
     */
    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    /**
     * Determines if the query should be run automatically.
     *
     * @return {@code true} if the query should be run automatically;
     * otherwise {@code false}
     */
    public boolean isAuto() {
        return auto;
    }

    /**
     * Determines if duplicate rows should be filtered.
     *
     * @param distinct if true, remove duplicate rows
     */
    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    /**
     * Determines if duplicate rows should be filtered.
     *
     * @return {@code true} if duplicate rows should be removed;
     * otherwise {@code false}
     */
    public boolean isDistinct() {
        return distinct;
    }

    /**
     * Add a listener for query events.
     *
     * @param listener the listener to add
     */
    public void addQueryListener(QueryListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a listener.
     *
     * @param listener the listener to remove
     */
    public void removeQueryListener(QueryListener listener) {
        listeners.remove(listener);
    }


    /**
     * Set query constraints.
     *
     * @param constraints the constraints. May be {@code null}
     */
    public void setConstraints(IConstraint constraints) {
        this.constraints = constraints;
    }

    /**
     * Returns query contraints.
     *
     * @return the constraints. May be {@code null}
     */
    public IConstraint getConstraints() {
        return constraints;
    }

    /**
     * Returns the query state.
     * <p/>
     * This implementation returns {@code null}.
     *
     * @return {@code null}
     */
    public QueryState getQueryState() {
        return null;
    }

    /**
     * Sets the query state.
     *
     * @param state the query state
     */
    public void setQueryState(QueryState state) {
    }

    /**
     * Returns the preferred height of the query when rendered.
     *
     * @return the preferred height, or {@code null} if it has no preferred height
     */
    @Override
    public Extent getHeight() {
        return getHeight(1);
    }

    /**
     * Notify listeners to perform a query.
     */
    protected void onQuery() {
        QueryListener[] listeners = this.listeners.toArray(new QueryListener[this.listeners.size()]);
        for (QueryListener listener : listeners) {
            listener.query();
        }
    }

    /**
     * Helper to return the query height in pixels, based on a factor of the query.height property.
     *
     * @param factor the factor
     * @return {@code $query.height * factor}, or {@code -1} if the property is not defined
     */
    protected Extent getHeight(int factor) {
        int height = StyleSheetHelper.getProperty("query.height", -1);
        return height > 0 ? new Extent(height * factor) : null;
    }

}
