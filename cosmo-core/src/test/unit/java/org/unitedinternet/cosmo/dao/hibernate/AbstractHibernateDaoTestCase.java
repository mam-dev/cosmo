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

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.orm.hibernate5.SessionHolder;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Abstract Hibernate Dao Test Case. 
 *
 */
public abstract class AbstractHibernateDaoTestCase extends AbstractSpringDaoTestCase {

    protected HibernateTestHelper helper;
    protected Session session;
    @Autowired(required=true)
    protected SessionFactory sessionFactory ;
    
    /**
     * Constructor.
     */
    public AbstractHibernateDaoTestCase() {
        super();
        helper = new HibernateTestHelper();
    }
    
    /**
     * Override onteadDownAfterTransaction.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @AfterTransaction
    public void onTearDownAfterTransaction() throws Exception {
        
        // Get a reference to the Session and bind it to the TransactionManager
        SessionHolder holder = (SessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
        Session s = holder.getSession(); 
        TransactionSynchronizationManager.unbindResource(sessionFactory);
      //  s.clear();
     //   clearSession();
        SessionFactoryUtils.closeSession(s);
    }

    /**
     * Clears session.
     */
    public void clearSession() {
        //session.flush();
        session.clear();
    }

    /**
     * SetsUp before transaction.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @BeforeTransaction
    public void onSetUpBeforeTransaction() throws Exception {
        // Unbind session from TransactionManager
        session = sessionFactory.openSession();
        TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
    }
}
