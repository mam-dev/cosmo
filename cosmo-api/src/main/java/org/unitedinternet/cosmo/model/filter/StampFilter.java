/*
 * Copyright 2005-2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.model.filter;


/**
 * Represents a filter that matches an Item with
 * a specified Stamp.
 *
 */
public class StampFilter {
  
    private boolean isMissing = false;
    private Class<?> stampClass = null;
    
    public StampFilter() {
        
    }
    
    public StampFilter(Class<?> stampClass, boolean isMissing) {
        this.stampClass = stampClass;
        this.isMissing = isMissing;
    }
    
    public boolean isMissing() {
        return isMissing;
    }
    
    /**
     * If true, match Items that do not have the
     * specified Stamp.
     * @param isMissing
     */
    public void setMissing(boolean isMissing) {
        this.isMissing = isMissing;
    }
    
    public Class<?> getStampClass() {
        return stampClass;
    }
    
    /**
     * Match Items that contain the specified Stamp
     * type.
     * @param stampClass
     */
    public void setStampClass(Class<?> stampClass) {
        this.stampClass = stampClass;
    }
}
