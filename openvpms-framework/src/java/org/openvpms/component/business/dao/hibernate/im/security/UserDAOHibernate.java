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


package org.openvpms.component.business.dao.hibernate.im.security;

import org.hibernate.Query;
import org.hibernate.Session;
import org.openvpms.component.business.dao.hibernate.im.common.CompoundAssembler;
import org.openvpms.component.business.dao.hibernate.im.common.Context;
import org.openvpms.component.business.dao.hibernate.im.entity.EntityLinkAssembler;
import org.openvpms.component.business.dao.hibernate.im.entity.EntityRelationshipAssembler;
import org.openvpms.component.business.dao.hibernate.im.entity.IMObjectResultCollector;
import org.openvpms.component.business.dao.hibernate.im.lookup.LookupAssembler;
import org.openvpms.component.business.dao.hibernate.im.lookup.LookupRelationshipAssembler;
import org.openvpms.component.business.dao.hibernate.im.party.ContactAssembler;
import org.openvpms.component.business.dao.im.security.IUserDAO;
import org.openvpms.component.business.dao.im.security.UserDAOException;
import org.openvpms.component.business.domain.im.security.User;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.List;


/**
 * This is an implementation of the IUserDAO DAO for hibernate. It uses the
 * Spring Framework's template classes.
 *
 * @author Jim Alateras
 */
public class UserDAOHibernate extends HibernateDaoSupport implements IUserDAO {

    /**
     * The assembler.
     */
    private final CompoundAssembler assembler;

    /**
     * The archetype short names to filter on.
     */
    private String[] shortNames;

    /**
     * The query string.
     */
    private String userQuery = QUERY;

    /**
     * User query.
     */
    private static final String QUERY =
            "from " + UserDOImpl.class.getName() + " as user where user.username = ?";

    /**
     * Constructs an <tt>UserDAOHibernate</tt>.
     */
    public UserDAOHibernate() {
        assembler = new Assembler();
    }

    /**
     * Constrains users to the specified archetype short names.
     *
     * @param shortNames the user archetype short names
     */
    public void setUserArchetypes(String... shortNames) {
        this.shortNames = shortNames;
        if (shortNames != null && shortNames.length != 0) {
            StringBuilder query = new StringBuilder(QUERY);
            query.append(" and user.archetypeId.shortName in (");
            for (int i = 0; i < shortNames.length; ++i) {
                if (i > 0) {
                    query.append(',');
                }
                query.append('?');
            }
            query.append(")");
            userQuery = query.toString();
        } else {
            userQuery = QUERY;
        }
    }

    /**
     * (non-Javadoc)
     *
     * @see IUserDAO#getByUserName(String)
     */
    @SuppressWarnings("unchecked")
    public List<User> getByUserName(String name) {
        List<User> results = null;
        Session session
                = getHibernateTemplate().getSessionFactory().openSession();
        try {
            Query query = session.createQuery(userQuery);
            query.setString(0, name);
            if (shortNames != null && shortNames.length > 0) {
                for (int i = 0; i < shortNames.length; ++i) {
                    query.setString(i + 1, shortNames[i]);
                }
            }
            Context context = Context.getContext(assembler, session);
            IMObjectResultCollector collector = new IMObjectResultCollector();
            collector.setContext(context);
            for (Object object : query.list()) {
                collector.collect(object);
            }
            context.resolveDeferredReferences();
            List objects;
            objects = collector.getPage().getResults();
            results = objects;
        } catch (Exception exception) {
            throw new UserDAOException(
                    UserDAOException.ErrorCode.FailedToFindUserRecordByName,
                    new Object[]{name}, exception);
        } finally {
            session.close();
        }

        return results;
    }

    private static class Assembler extends CompoundAssembler {

        public Assembler() {
            addAssembler(new ArchetypeAuthorityAssembler());
            addAssembler(new EntityLinkAssembler());
            addAssembler(new EntityRelationshipAssembler());
            addAssembler(new LookupAssembler());
            addAssembler(new LookupRelationshipAssembler());
            addAssembler(new SecurityRoleAssembler());
            addAssembler(new UserAssembler());
            addAssembler(new ContactAssembler());
        }
    }
}
