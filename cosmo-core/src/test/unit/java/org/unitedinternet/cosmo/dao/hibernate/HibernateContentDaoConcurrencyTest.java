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

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.CosmoInterruptedException;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.FileItem;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.hibernate.HibFileItem;
import org.unitedinternet.cosmo.model.hibernate.HibQName;
import org.unitedinternet.cosmo.model.hibernate.HibStringAttribute;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

/**
 * Test concurrent modification of an item.  Since cosmo uses
 * optimistic locking, a concurrent modification will fail
 * when the second thread updates the item.  It goes something
 * like this:
 *
 * 1. Thread 1 reads item version 1
 * 2. Thread 2 reads item version 1
 * 3. Thread 1 updates item version 1 to item version 2
 * 4. Thread 2 tries to update item version 1, sees that item is
 *    no longer version 1, and throws exception
 *
 */
@Ignore
public class HibernateContentDaoConcurrencyTest extends AbstractHibernateDaoTestCase {

    protected UserDaoImpl userDao = null;
    protected ContentDaoImpl contentDao;
    protected DataSource jdbcDataSource;
    protected HibernateTransactionManager transactionManager;
    
    /**
     * onSetUp.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Before
    protected void onSetUp() throws Exception {
    }

    /**
     * Tests concurrent content dao update.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testConcurrentContentDaoUpdateContent() throws Exception {
        
        TransactionThread txThread1 = new TransactionThread(transactionManager,sessionFactory);
        TransactionThread txThread2 = new TransactionThread(transactionManager,sessionFactory);
        TransactionThread txThread3 = new TransactionThread(transactionManager,sessionFactory);
        
        cleanupDb();
        
        // create item to be updated concurrently
        txThread1.addRunnable("1", new TxRunnable() {
            public Object run() {
                User user = getUser(userDao, "testuser");
                CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

                ContentItem item = generateTestContent();
                item.setUid("test");

                ContentItem newItem = contentDao.createContent(root, item);
                return newItem;
            }
        });
        
        // read item by thread 2
        txThread2.addRunnable("1", new TxRunnable() {
            public Object run() {
                
                ContentItem item = (ContentItem) contentDao.findItemByUid("test");
                return item;
            }
        });
        
        // read item by thread 3
        txThread3.addRunnable("1", new TxRunnable() {
            public Object run() {
                
                ContentItem item = (ContentItem) contentDao.findItemByUid("test");
                return item;
            }
        });
        
        // create item
        txThread1.start();
        txThread1.commit();
        txThread1.join();
        
        // read item at the same time
        txThread2.start();
        txThread3.start();
        
        // wait till reads are done
        while(txThread2.getRunnableResults("1")==null)
            Thread.sleep(50);
        while(txThread3.getRunnableResults("1")==null)
            Thread.sleep(50);
        
        // results of the read (should be same item)
        final ContentItem item1 = (ContentItem) txThread2.getRunnableResults("1");
        final ContentItem item2 = (ContentItem) txThread3.getRunnableResults("1");
        
        // write item by thread 2
        txThread2.addRunnable("2", new TxRunnable() {
            public Object run() {
                
                contentDao.updateContent(item1);
                return item1;
            }
        });
        
        // wait for write to complete
        while (txThread2.getRunnableResults("2")==null) {
            Thread.sleep(50);
        }
        
        // thread 2 wins with the commit
        txThread2.commit();
        txThread2.join();
        
        // now try to write item by thread 3, should fail
        txThread3.addRunnable("2", new TxRunnable() {
            /**
             * Run.
             * {@inheritDoc}
             */
            public Object run() {
                
                contentDao.updateContent(item2);
                return item2;
            }
        });
        
        txThread3.commit();
        txThread3.join();

        // results should be OptimisticLockingFailureException
        Assert.assertTrue(txThread3.getRunnableResults("2") instanceof OptimisticLockingFailureException);
       
        cleanupDb();
    }
    
    /**
     * Tests concurrent content dao delete content.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testConcurrentContentDaoDeleteContent() throws Exception {
        
        TransactionThread txThread1 = new TransactionThread(transactionManager,sessionFactory);
        TransactionThread txThread2 = new TransactionThread(transactionManager,sessionFactory);
        TransactionThread txThread3 = new TransactionThread(transactionManager,sessionFactory);
        
        cleanupDb();
        
        // create item to be updated concurrently
        txThread1.addRunnable("1", new TxRunnable() {
            public Object run() {
                User user = getUser(userDao, "testuser");
                CollectionItem root = (CollectionItem) contentDao.getRootItem(user);

                ContentItem item = generateTestContent();
                item.setUid("test");

                ContentItem newItem = contentDao.createContent(root, item);
                return newItem;
            }
        });
        
        // read item by thread 2
        txThread2.addRunnable("1", new TxRunnable() {
            public Object run() {
                
                ContentItem item = (ContentItem) contentDao.findItemByUid("test");
                return item;
            }
        });
        
        // read item by thread 3
        txThread3.addRunnable("1", new TxRunnable() {
            /**
             * Run.
             * {@inheritDoc}
             */
            public Object run() {
                
                ContentItem item = (ContentItem) contentDao.findItemByUid("test");
                return item;
            }
        });
        
        
        // create item
        txThread1.start();
        txThread1.commit();
        txThread1.join();
        
        // read item at the same time
        txThread2.start();
        txThread3.start();
        
        // wait till reads are done
        while (txThread2.getRunnableResults("1")==null) {
            Thread.sleep(50);
        }
        while (txThread3.getRunnableResults("1")==null) {
            Thread.sleep(50);
        }
        
        // results of the read (should be same item)
        final ContentItem item1 = (ContentItem) txThread2.getRunnableResults("1");
        final ContentItem item2 = (ContentItem) txThread3.getRunnableResults("1");
        
        // delete item by thread 2
        txThread2.addRunnable("2", new TxRunnable() {
            public Object run() {
                
                contentDao.removeContent(item1);
                return item1;
            }
        });
        
        // wait for delete to complete
        while (txThread2.getRunnableResults("2")==null) {
            Thread.sleep(50);
        }
        
        // thread 2 wins with the commit
        txThread2.commit();
        txThread2.join();
        
        // now try to delete item by thread 3, should fail
        txThread3.addRunnable("2", new TxRunnable() {
            /**
             * Run.
             * {@inheritDoc}
             */
            public Object run() {
                
                contentDao.removeContent(item2);
                return item2;
            }
        });
        
        txThread3.commit();
        txThread3.join();

        // results should be DataRetrievalFailureException
        Assert.assertTrue(txThread3.getRunnableResults("2") instanceof DataRetrievalFailureException);
       
        cleanupDb();
    }

    /**
     * Gets user.
     * @param userDao The user dao.
     * @param username The username.
     * @return The user.
     */
    private User getUser(UserDao userDao, String username) {
        return helper.getUser(userDao, contentDao, username);
    }
    
    /**
     * Generates test content.
     * @return The file item.
     */
    private FileItem generateTestContent() {
        return generateTestContent("test", "testuser");
    }

    /**
     * Generates test content.
     * @param name The name.
     * @param owner The owner.
     * @return The file item.
     */
    private FileItem generateTestContent(String name, String owner)
             {
        FileItem content = new HibFileItem();
        content.setName(name);
        content.setDisplayName(name);
        try {
            content.setContent(helper.getBytes("testdata1.txt"));
        } catch (Exception e) {
            throw new CosmoException(e);
        }
        content.setContentLanguage("en");
        content.setContentEncoding("UTF8");
        content.setContentType("text/text");
        content.setOwner(getUser(userDao, owner));
        content.addAttribute(new HibStringAttribute(new HibQName("customattribute"),
                "customattributevalue"));
        return content;
    }
    
    
    /**
     * The cleanup DB.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    protected void cleanupDb () throws Exception {
        Connection conn = jdbcDataSource.getConnection();
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("delete from event_stamp");
        stmt.executeUpdate("delete from stamp");
        stmt.executeUpdate("delete from attribute");
        stmt.executeUpdate("delete from collection_item");
        stmt.executeUpdate("delete from tombstones");
        stmt.executeUpdate("delete from item");
        stmt.executeUpdate("delete from content_data");
        stmt.executeUpdate("delete from users");
        
        conn.commit();
    }
    
    /**
     * Transaction thread.
     *
     */
    static class TransactionThread extends Thread {
        
        private List<RunContext> toRun = new ArrayList<RunContext>();
        private Map<String, Object> doneSet = Collections.synchronizedMap(new HashMap<String, Object>());
        private boolean commit = false;
        HibernateTransactionHelper txHelper = null;
        
        TransactionThread(PlatformTransactionManager ptm, SessionFactory sf) {
            txHelper = new HibernateTransactionHelper(ptm, sf);
        }
        
        /**
         * Add code to be run inside transaction.
         * @param key identifier to use when checkign results of run
         * @param runnable code to run
         */
        public void addRunnable(String key, TxRunnable runnable) {
            RunContext rc = new RunContext();
            rc.key = key;
            rc.runnable = runnable;

            toRun.add(rc);
        }
        
        /**
         * Return results from runnable
         * @param key identifier
         * @return return value from runnable
         */
        public Object getRunnableResults(String key) {
            return doneSet.get(key);
        }
      
        /**
         * Run.
         * {@inheritDoc}
         */
        public void run() {
            TransactionStatus ts = txHelper.startNewTransaction();
            
            while(!commit || toRun.size()>0) {
                RunContext rc = null;
                
                if (toRun.size()>0) {
                    rc = toRun.remove(0); 
                }
                
                if(rc==null) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        throw new CosmoInterruptedException(e);
                    }
                    continue;
                }

                Object returnValue = null;
                
                try {
                    returnValue = rc.runnable.run();
                } catch (Exception e) {
                    doneSet.put(rc.key, e);
                    txHelper.endTransaction(ts, true);
                    return;
                }
                
               
                doneSet.put(rc.key, returnValue);
            }
            
           
            txHelper.endTransaction(ts, false);
        }

        /**
         * Commit.
         */
        public void commit() {
            commit = true;
        }
        
        /**
         * Run context.
         */
        class RunContext {
            String key;
            TxRunnable runnable;
        }
        
    }
    /**
     * Interface TxRunnable.
     */
    interface TxRunnable {
        /**
         * Run.
         * @return The object.
         */
        public Object run();
    }
}
