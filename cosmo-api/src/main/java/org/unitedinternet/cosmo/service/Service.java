/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
 * Service interface. Services provide facades that encapsulate business logic and interact with data access objects
 * (DAOs) to execute business operations.
 */
public interface Service {

    /**
     * Initializes the service, sanity checking required properties and defaulting optional properties.
     */
    public void init();

    /**
     * Readies the service for garbage collection, shutting down any resources used.
     */

    public void destroy();
}
