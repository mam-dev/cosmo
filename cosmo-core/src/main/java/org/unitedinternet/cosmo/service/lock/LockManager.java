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

import org.unitedinternet.cosmo.model.CollectionItem;

/**
 * It is recommended practice to always immediately follow a call 
 * to lock with a try block.  For example:
 * <code>
 *     if (lockManager.lockCollection(collection,timeout)==true) {
 *         try {
 *            ....
 *         } finally {
 *             lockManager.unlockCollection(collection);
 *         }
 *     } else {
 *         throw new LockedException();
 *     }
 * </code>
 * 
 * This makes sure that a collection is unlocked in
 * the event of an unexpected error.
 */
public interface LockManager {

   
    /**
     * Obtain an exclusive lock to a collection.  This method
     * will block until lock has been acquired.
     * @param collection collection to lock
     */
    void lockCollection(CollectionItem collection);
    
  
    /**
     * Obtain an exclusive lock to a collection.  This method
     * will block until lock has been acquired, or until the
     * specified timeout period has passed.
     * 
     * @param collection collection to lock
     * @param timeout Time in milliseconds to timeout waiting on lock.
     *                A value of 0 results in no waiting.  A negative
     *                value is wait forever.
     * @return true if lock has been acquired, false if lock could
     *         not be obtained within a specified ammount of time
     */
    boolean lockCollection(CollectionItem collection, long timeout);

    /**
     * Release lock for a collection.  The thread calling this method
     * must own the lock.
     * 
     * @param collection
     *            collection to unlock
     */
    void unlockCollection(CollectionItem collection);
}
