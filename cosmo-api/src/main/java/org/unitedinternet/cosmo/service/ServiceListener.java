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
package org.unitedinternet.cosmo.service;

/**
 * <p>
 * An interface for objects that respond to service operations.
 * </p>
 * <p>
 * A service listener implements only two methods for handling any
 * type of service operation. This decouples the listener interface
 * and implementations from the set of events published by the various
 * services. Listeners should be coded to respond to only the events
 * they care about, as distinguished by an event's id.
 * </p>
 */
public interface ServiceListener {

    /**
     * <p>
     * Executed just before a service operation occurs.
     * </p>
     * <p>
     * Subclasses should throw <code>RuntimeException</code>s to
     * signal processing errors.
     * </p>
     */
    public void before(ServiceEvent event);

    /**
     * <p>
     * Executed just after a service operation occurs.
     * </p>
     * <p>
     * Subclasses should throw <code>RuntimeException</code>s to
     * signal processing errors.
     * </p>
     */
    public void after(ServiceEvent event);
}
