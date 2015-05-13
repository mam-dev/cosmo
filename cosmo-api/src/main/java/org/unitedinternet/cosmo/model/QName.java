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
 * Represents a qualified name. A qualified name 
 * consists of a namespace and a local name.
 * 
 * Attribtues are indexed by a qualified name.
 */
public interface QName {

    /**
     * Get local name
     * @return local name
     */
    public String getLocalName();

    /**
     * Set local name
     * @param localName local name
     */
    public void setLocalName(String localName);

    /**
     * Get namespace
     * @return namespace
     */
    public String getNamespace();

    /**
     * Set namespace
     * @param namespace namespace
     */
    public void setNamespace(String namespace);

    /**
     * Create copy of QName object.
     * @return copy of current QName object
     */
    public QName copy();

}