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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.CosmoInterruptedException;
import org.unitedinternet.cosmo.model.CollectionItem;

/**
 * And implementation of <code>LockManager</code> that supports locking within a single JVM. Once a thread obtains a
 * lock, it owns the lock until the thread unlocks it. A thread that attempts to unlock something it doesn't own will
 * result in a RuntimeException.
 * 
 * Once a lock is released by a thread, it stays in memory. Unused locks are cleared from memory after maxLocks are in
 * memory to prevent memory leaks.
 *
 * @see LockManager
 */
@Component
public class SingleVMLockManager implements LockManager {
    
    private static final Logger LOG = LoggerFactory.getLogger(SingleVMLockManager.class);

    private int maxLocks = 10000;

    /**
     * Cache of locks, mapped by uid
     */
    protected HashMap<String, CollectionLock> locks = new HashMap<String, CollectionLock>();

    /**
     * @return number of maximum locks allowed in memory
     */
    public int getMaxLocks() {
        return maxLocks;
    }

    /**
     * Check if a collection is locked
     * 
     * @param collection The collection.
     * @return true if collection is locked
     */
    public boolean isLocked(CollectionItem collection) {
        CollectionLock lock = locks.get(collection.getUid());
        if (lock == null) {
            return false;
        } else {
            return lock.isLocked();
        }
    }

    /**
     * Return the number of threads waiting on collection lock
     * 
     * @param collection The collection.
     * @return number of threads waiting on collection lock
     */
    public int getNumWaitingThreads(CollectionItem collection) {
        CollectionLock lock = locks.get(collection.getUid());
        if (lock == null) {
            return 0;
        } else {
            return lock.getQueueLength();
        }
    }

    /**
     * Set the maximum number of locks allowed in memory
     * 
     * @param maxLocks The maximum number of locks.
     */
    public void setMaxLocks(int maxLocks) {
        this.maxLocks = maxLocks;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.service.LockService#lockCollection(org.unitedinternet.cosmo.model.CollectionItem)
     */
    public void lockCollection(CollectionItem collection) {
        lockCollection(collection, -1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.service.LockService#lockCollection(org.unitedinternet.cosmo.model.CollectionItem,
     * long)
     */
    public boolean lockCollection(CollectionItem collection, long timeout) {

        CollectionLock lock = null;

        synchronized (this) {
            lock = locks.get(collection.getUid());

            // If lock is null, then we need to create one
            if (lock == null) {

                // check locks to prevent memory leaks
                checkAndCleanLocks();

                // create lock and add to map with uid as the key
                lock = new CollectionLock();
                locks.put(collection.getUid(), lock);
            }

            // mark lock as inUse, preventing thread running cleanup
            // from removing it
            lock.inUse = true;
        }

        // Attempt to acquire the lock.
        // This will block until thread can acquire the lock, or
        // until timeout milliseconds have passed if timeout is > 0
        try {
            if (timeout < 0) {
                lock.lock();
                LOG.info("Lock collection: {}", collection.getUid());
            } else {
                if (lock.tryLock(timeout, TimeUnit.MILLISECONDS) == false) {
                    return false;
                }
            }
        } catch (InterruptedException e) {
            throw new CosmoInterruptedException("thread interrupted, no lock acquired", e);
        } finally {
            // done calling lock(), so clear inUse flag
            lock.inUse = false;
        }

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.mc.LockManager#unnlockCollection(org.unitedinternet.cosmo.model.CollectionItem)
     */
    public void unlockCollection(CollectionItem collection) {

        synchronized (this) {
            CollectionLock lock = locks.get(collection.getUid());

            // unlock if there is a lock to unlock
            if (lock != null) {
                if (!lock.isHeldByCurrentThread()) {
                    throw new CosmoException("Current thread does not own lock", new CosmoException());
                }
                lock.unlock();
                LOG.info("Unlock collection: {}", collection.getUid());
            }
        }
    }

    /**
     * Return the current number of locks in memory.
     * 
     * @return number of locks currently in memory
     */
    public int getNumLocksInMemory() {
        return locks.size();
    }

    /**
     * Verify that the maximum number of locks hasn't been reached. If the maximum number of locks has been reached,
     * then cleanup unused locks to prevent memory leaks.
     */
    private void checkAndCleanLocks() {

        // First check to see if we have reached the maximum number
        // of locks held in memory.
        if (locks.size() >= maxLocks) {

            LOG.info("max locks reached({}) cleaning...", maxLocks);

            // If so, then cleanup locks
            for (Iterator<Entry<String, CollectionLock>> it = locks.entrySet().iterator(); it.hasNext();) {
                Entry<String, CollectionLock> entry = it.next();
                // remove the lock entry if its not currently held or in use
                if (!entry.getValue().isLocked() && !entry.getValue().inUse) {
                    it.remove();
                }
            }

            // If every lock in memory is in use after cleanup, then throw
            // exception
            if (locks.size() >= maxLocks) {
                throw new CosmoException("Maximum ammount of locks in memeory reached", new CosmoException());
            }
        }
    }

    /**
     * Subclass ReentrantLock and add boolean variable to prevent race condition where lock is removed from map by
     * another thread running cleanup before being acquired by current thread. Unlikely, but technically possible.
     */
    static class CollectionLock extends ReentrantLock {

        private static final long serialVersionUID = 2596344954700953608L;
        /**
         * Flag denoting that the lock is currently being used by lockCollection(). This is used to prevent a thread
         * running cleanup from deleting a lock that isn't currently held, but about to be locked by another thread.
         */
        boolean inUse = false;
    }

}
