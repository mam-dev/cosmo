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

/**
 * Abstract simple expression criteria.
 */
public abstract class FilterExpression implements FilterCriteria {
    
    private Object value = null;
    private boolean negated = false;
    
    public FilterExpression(Object value) {
        super();
        this.value = value;
    }
    public boolean isNegated() {
        return negated;
    }
    public void setNegated(boolean negated) {
        this.negated = negated;
    }
    public Object getValue() {
        return value;
    }
    public void setValue(Object value) {
        this.value = value;
    }
    
    
}
