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

import java.util.List;

import org.hibernate.HibernateException;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.unitedinternet.cosmo.dao.ServerPropertyDao;
import org.unitedinternet.cosmo.model.ServerProperty;
import org.unitedinternet.cosmo.model.hibernate.HibServerProperty;

/**
 * Implementation of ServerPropertyDao using Hibernate persistent objects.
 */
public class ServerPropertyDaoImpl extends AbstractDaoImpl implements ServerPropertyDao {

    @Override
    public String getServerProperty(String property) {
        try {
            List<ServerProperty> propertyList = getSession()
                    .createQuery("from HibServerProperty where name=:name", ServerProperty.class)
                    .setParameter("name", property).getResultList();
            return propertyList.size() > 0 ? propertyList.get(0).getValue() : null;
        } catch (HibernateException e) {
            throw SessionFactoryUtils.convertHibernateAccessException(e);
        }
    }

    @Override
    public void setServerProperty(String property, String value) {
        try {
            List<ServerProperty> propertyList = getSession()
                    .createQuery("from HibServerProperty where name=:name", ServerProperty.class)
                    .setParameter("name", property).getResultList();
            ServerProperty prop = null;
            if (propertyList.size() > 0) {
                prop = propertyList.get(0);
                prop.setValue(value);
            } else {
                prop = new HibServerProperty(property, value);
            }
            getSession().saveOrUpdate(prop);
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
