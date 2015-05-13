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
package org.unitedinternet.cosmo.model.mock;

import java.util.HashMap;
import java.util.Map;

import org.unitedinternet.cosmo.dao.ModelValidationException;
import org.unitedinternet.cosmo.model.Attribute;
import org.unitedinternet.cosmo.model.DictionaryAttribute;
import org.unitedinternet.cosmo.model.QName;


/**
 * Attribute that contains a Map<String,String> as its
 * value.
 */
public class MockDictionaryAttribute extends MockAttribute
        implements java.io.Serializable, DictionaryAttribute {

    /**
     * 
     */
    private static final long serialVersionUID = 3713980765847199175L;
    
    private Map<String, String> value = new HashMap<String,String>(0);

    /** default constructor */
    public MockDictionaryAttribute() {
    }

    /**
     * Constructor.
     * @param qname The name.
     * @param value The value.
     */
    public MockDictionaryAttribute(QName qname, Map<String, String> value)
    {
        setQName(qname);
        this.value = value;
    }

    // Property accessors
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceDictionaryAttribute#getValue()
     */
    /**
     * Gets value.
     * @return values.
     */
    public Map<String, String> getValue() {
        return this.value;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceDictionaryAttribute#setValue(java.util.Map)
     */
    /**
     * Sets value.
     * @param value The value.
     */
    public void setValue(Map<String, String> value) {
        this.value = value;
    }
    
    /**
     * Sets value.
     * {@inheritDoc}
     * @param value The value.
     */
    @SuppressWarnings("unchecked")
    public void setValue(Object value) {
        if (value != null && !(value instanceof Map)) {
            throw new ModelValidationException("attempted to set non Map value on attribute");
        }
        setValue((Map<String, String>) value);
    }
    
    /**
     * Copy.
     * {@inheritDoc}
     * @return The attribute.
     */
    public Attribute copy() {
        DictionaryAttribute attr = new MockDictionaryAttribute();
        attr.setQName(getQName().copy());
        attr.setValue(new HashMap<String, String>(value));
        return attr;
    }

    @Override
    public void validate() {
        
    }

    @Override
    public String calculateEntityTag() {
       return "";
    }

}
