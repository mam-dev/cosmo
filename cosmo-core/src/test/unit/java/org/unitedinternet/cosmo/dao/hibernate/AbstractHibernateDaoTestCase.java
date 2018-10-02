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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.hibernate.Session;
import org.junit.Before;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;

/**
 * Abstract Hibernate Dao Test Case. 
 *
 */
@Transactional
public abstract class AbstractHibernateDaoTestCase extends AbstractSpringDaoTestCase {

    @PersistenceContext
    private EntityManager entityManager;
    
    protected Session session;
    
    protected HibernateTestHelper helper;
    
    @Before
    public void setUp() {
        this.session = this.getSession();
        this.helper = new HibernateTestHelper();
    }
    
    
    protected Session getSession() {
        return (Session) this.entityManager.getDelegate();
    }
    
    /**
     * Override onteadDownAfterTransaction.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @AfterTransaction
    public void onTearDownAfterTransaction() throws Exception {
        this.entityManager.clear();        
        this.entityManager.close();
        
    }

    /**
     * Clears session.
     */
    public void clearSession() {
        this.entityManager.flush();
        this.entityManager.clear();
        this.entityManager.close();
    }

    /**
     * SetsUp before transaction.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @BeforeTransaction
    public void onSetUpBeforeTransaction() throws Exception {
        // Unbind session from TransactionManager
//        session = sessionFactory.openSession();
//        TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
    }
}
