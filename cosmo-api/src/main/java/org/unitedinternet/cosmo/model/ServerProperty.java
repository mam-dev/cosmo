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
package org.unitedinternet.cosmo.model;

/**
 * Represents a Cosmo Server Property, which is a simple
 * key value/pair.
 */
public interface ServerProperty {

    public static final String PROP_SCHEMA_VERSION = "cosmo.schemaVersion";
    
    public String getName();

    public void setName(String name);

    public String getValue();

    public void setValue(String value);

}