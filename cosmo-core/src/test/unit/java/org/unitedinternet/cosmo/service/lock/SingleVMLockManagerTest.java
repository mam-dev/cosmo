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
package org.unitedinternet.cosmo.service.lock;

import org.junit.Assert;
import org.junit.Test;
import org.unitedinternet.cosmo.CosmoInterruptedException;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;

/**
 * Test StandardLockManager
 */
public class SingleVMLockManagerTest {
    
    SingleVMLockManager lockManager = new SingleVMLockManager();
    
    /**
     * Tests standard lock manager.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testStandardLockManager() throws Exception{
        lockManager.setMaxLocks(3);
        
        LockTestThread t1;
        LockTestThread t2;
        LockTestThread t3;
        LockTestThread t4;
        
        t1 = new LockTestThread();
        t2 = new LockTestThread();
        t3 = new LockTestThread();
        t4 = new LockTestThread();
        
        // Get 3 locks, all waiting indef
        t1.uid = "1";
        t1.wait = -1;
        t1.start();
        t2.uid = "2";
        t2.wait = -1;
        t2.start();
        t3.uid = "3";
        t3.wait = -1;
        t3.start();
        
        Thread.sleep(300);
        
        // t4 should wait on col 3 because t3 has 
        // the lock
        t4.uid = "3";
        t4.wait = -1;
        t4.start();
        
        // Give threads chance to acquire locks
        Thread.sleep(300);
        
        // All 3 threads should have lock
        Assert.assertTrue(t1.hasLock);
        Assert.assertTrue(t2.hasLock);
        Assert.assertTrue(t3.hasLock);
        
        // t4 should be waiting
        Assert.assertFalse(t4.hasLock);
        
        CollectionItem col = new HibCollectionItem();
        col.setUid("3");
        
        Assert.assertEquals(1, lockManager.getNumWaitingThreads(col));
        Assert.assertTrue(lockManager.isLocked(col));
        
        col.setUid("1");
        
        // Should not be able to obtain lock to 1
        Assert.assertFalse(lockManager.lockCollection(col,100));
        
        // should be 3 locks in memory
        Assert.assertEquals(3, lockManager.getNumLocksInMemory());
        
        // Should not be able to obtain lock to 4 because there are
        // already 3 locks (max) in memory
        col.setUid("4");
        try {
            lockManager.lockCollection(col);
            Assert.fail("able to create more than maxLocks!");
        } catch (RuntimeException e) {}
        
        // increase max to 4
        lockManager.setMaxLocks(4);
        
        // now we should be ok
        Assert.assertTrue(lockManager.lockCollection(col,100));
        lockManager.unlockCollection(col);
        
        Assert.assertEquals(4, lockManager.getNumLocksInMemory());
        
        // this should trigger a clean
        col.setUid("5");
        Assert.assertTrue(lockManager.lockCollection(col,100));
        lockManager.unlockCollection(col);
        Assert.assertEquals(4, lockManager.getNumLocksInMemory());
        
        // another clean
        col.setUid("6");
        Assert.assertTrue(lockManager.lockCollection(col,100));
        Assert.assertEquals(4, lockManager.getNumLocksInMemory());
        
        // should not be able to unlock something we don't own
        col.setUid("1");
        try {
            lockManager.unlockCollection(col);
            Assert.fail("able to unlock something we don't own");
        } catch (RuntimeException e) { 
        }        
    }
    
    /**
     * Lock test thread class.
     *
     */
    class LockTestThread extends Thread {
        
        String uid = null;
        int wait = 0;
        boolean hasLock = false;

        /**
         * Constructor.
         */
        public LockTestThread() {
        }
        
        /**
         * Run.
         */
        public void run() {
            CollectionItem collection = new HibCollectionItem();
            collection.setUid(uid);
            lockManager.lockCollection(collection);
            hasLock = true;
            if(wait>=0) {
                try {
                    Thread.sleep(wait);
                } catch (InterruptedException e) {
                    throw new CosmoInterruptedException("thread interrupted!", e);
                }
                lockManager.unlockCollection(collection);
                hasLock = false;
            }
            
        }
    }
    
}
