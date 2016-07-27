/*
 * Copyright 2007 Open Source Applications Foundation
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
import org.springframework.orm.hibernate5.SessionFactoryUtils;
import org.springframework.orm.hibernate5.SessionHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Helps manage transactions for testing.  This is handled by Spring
 * in production.
 */
public class HibernateTransactionHelper {
    
    PlatformTransactionManager txManager = null;
    SessionFactory sessionFactory = null;
    
    /**
     * Constructor.
     * @param txManager Transaction Manager.
     * @param sessionFactory Session factory.
     */
    public HibernateTransactionHelper(PlatformTransactionManager txManager, SessionFactory sessionFactory) {
        this.txManager = txManager;
        this.sessionFactory = sessionFactory;
    }
    
    /**
     * Starts new transaction.
     * @return transaction status.
     */
    public TransactionStatus startNewTransaction() {
        Session session = sessionFactory.openSession();
        TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
        TransactionStatus transactionStatus = txManager.getTransaction(new DefaultTransactionDefinition());
        return transactionStatus;
    }
    
    /**
     * End transaction.
     * @param ts Transaction status.
     * @param rollback rollback.
     */
    public void endTransaction(TransactionStatus ts, boolean rollback) {
        if (rollback) {
            txManager.rollback(ts);
        }
        else {
            txManager.commit(ts);
        }
        
        SessionHolder holder = (SessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
        Session s = holder.getSession(); 
        TransactionSynchronizationManager.unbindResource(sessionFactory);
        SessionFactoryUtils.closeSession(s);
    }
    
}
