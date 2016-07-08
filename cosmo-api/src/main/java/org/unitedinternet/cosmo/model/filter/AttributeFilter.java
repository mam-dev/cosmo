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
package org.unitedinternet.cosmo.model.filter;

import org.unitedinternet.cosmo.model.Attribute;
import org.unitedinternet.cosmo.model.QName;

/**
 * A filter that matches an Item with a given Attribute.
 */
public class AttributeFilter {
    
    private QName qname;
    private boolean isMissing = false;
    private Object value;
    
    public AttributeFilter() {}
    
    public AttributeFilter(Attribute attribute) {
        this.qname = attribute.getQName();
        this.value = attribute.getValue();
    }

    public QName getQname() {
        return qname;
    }

    /**
     * Match attribute with given qualified name.
     * @param qname qualified name to match
     */
    public void setQname(QName qname) {
        this.qname = qname;
    }
    
    
    public Object getValue() {
        return value;
    }
    
    public boolean isMissing() {
        return isMissing;
    }
    
    /**
     * If true, this filter matches items that do not 
     * contain the specified Attribute.
     * @param isMissing
     */
    public void setMissing(boolean isMissing) {
        this.isMissing = isMissing;
    }
}
