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

import java.util.HashSet;
import java.util.Set;

import org.unitedinternet.cosmo.dao.ModelValidationException;
import org.unitedinternet.cosmo.model.Attribute;
import org.unitedinternet.cosmo.model.MultiValueStringAttribute;
import org.unitedinternet.cosmo.model.QName;


/**
 * Represents attribute with a List<String> as its value
 */
public class MockMultiValueStringAttribute extends MockAttribute
        implements java.io.Serializable, MultiValueStringAttribute {

    /**
     * 
     */
    private static final long serialVersionUID = 8518583717902318228L;
    
    
    private Set<String> value = new HashSet<String>(0);

    /** default constructor */
    public MockMultiValueStringAttribute() {
    }

    /**
     * Contructor.
     * @param qname The name of the attribute.
     * @param value The value.
     */
    public MockMultiValueStringAttribute(QName qname, Set<String> value)
    {
        setQName(qname);
        this.value = value;
    }

    // Property accessors
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceMultiValueStringAttribute#getValue()
     */
    /**
     * Gets value
     * @return The value.
     */
    public Set<String> getValue() {
        return this.value;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceMultiValueStringAttribute#setValue(java.util.Set)
     */
    /**
     * Sets value.
     * @param value The value.
     */
    public void setValue(Set<String> value) {
        this.value = value;
    }
    
    /**
     * Sets value.
     * {@inheritDoc}
     * @param value The value.
     */
    @SuppressWarnings("unchecked")
    public void setValue(Object value) {
        if (value != null && !(value instanceof Set)) {
            throw new ModelValidationException("attempted to set non Set value on attribute");
        }
        setValue((Set<String>) value);
    }
    
    /**
     * Copy.
     * {@inheritDoc}
     * @return The attribute.
     */
    public Attribute copy() {
        MultiValueStringAttribute attr = new MockMultiValueStringAttribute();
        attr.setQName(getQName().copy());
        Set<String> newValue = new HashSet<String>(value);
        attr.setValue(newValue);
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
