/*
 * Copyright 2006 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unitedinternet.cosmo.dao.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.unitedinternet.cosmo.dao.ServerPropertyDao;
import org.unitedinternet.cosmo.model.ServerProperty;
import org.unitedinternet.cosmo.model.hibernate.HibServerProperty;
import org.springframework.orm.hibernate4.SessionFactoryUtils;


/**
 * Implementation of ServerPropertyDao using Hibernate persistent objects.
 */
public class ServerPropertyDaoImpl extends AbstractDaoImpl implements
        ServerPropertyDao {
    @SuppressWarnings("unused")
    private static final Log LOG = LogFactory.getLog(ServerPropertyDaoImpl.class);

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.dao.ServerPropertyDao#getServerProperty(java.lang.String)
     */
    public String getServerProperty(String property) {
        try {
            ServerProperty prop = (ServerProperty) getSession().createQuery(
                    "from HibServerProperty where name=:name").setParameter(
                    "name", property).uniqueResult();
            if (prop != null) {
                return prop.getValue();
            } else {
                return null;
            }
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.dao.ServerPropertyDao#setServerProperty(java.lang.String, java.lang.String)
     */
    public void setServerProperty(String property, String value) {
        try {

            ServerProperty prop = (ServerProperty) getSession().createQuery(
                    "from HibServerProperty where name=:name").setParameter(
                    "name", property).uniqueResult();
            if (prop != null) {
                prop.setValue(value);
                getSession().update(prop);
            } else {
                prop = new HibServerProperty(property, value);
                getSession().save(prop);
            }

            getSession().flush();

        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    public void destroy() {
        // nothing
    }

    public void init() {
        // nothing
    }

}
