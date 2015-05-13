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

import org.unitedinternet.cosmo.model.QName;

/**
 * Filter that matches Items with a TextAttribute.
 *
 */
public class TextAttributeFilter extends AttributeFilter {
    
    FilterCriteria value = null;
    
    public TextAttributeFilter() {
    }
    
    public TextAttributeFilter(QName qname) {
        this.setQname(qname);
    }

    public FilterCriteria getValue() {
        return value;
    }

    /**
     * Match a TextAttribute with a string
     * @param value
     */
    public void setValue(FilterCriteria value) {
        this.value = value;
    }
}
