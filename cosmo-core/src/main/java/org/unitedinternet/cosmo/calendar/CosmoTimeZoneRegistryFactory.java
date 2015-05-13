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
package org.unitedinternet.cosmo.calendar;

import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;

/**
 * Factory implementation for timezone registries.  Simply
 * creates and returns a CosmoICUTimeZoneRegistry instance.  This
 * allows Cosmo to provide its own timezone registiry to plug
 * into ical4j.
 */
public class CosmoTimeZoneRegistryFactory extends TimeZoneRegistryFactory {

    /**
     * Constructor.
     */
    public CosmoTimeZoneRegistryFactory() {}
    
    /* (non-Javadoc)
     * @see net.fortuna.ical4j.model.TimeZoneRegistryFactory#createRegistry()
     */
    /**
     * Creates registry.
     * @return The timezone registry.
     */
    public TimeZoneRegistry createRegistry() {
        return new CosmoICUTimeZoneRegistry();
    }
}
