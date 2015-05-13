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
package org.unitedinternet.cosmo.model.hibernate;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MapKeyColumn;

import org.unitedinternet.cosmo.dao.ModelValidationException;
import org.unitedinternet.cosmo.model.Attribute;
import org.unitedinternet.cosmo.model.DictionaryAttribute;
import org.unitedinternet.cosmo.model.QName;


/**
 * Hibernate persistent DictionaryAtttribute.
 */
@Entity
@DiscriminatorValue("dictionary")
public class HibDictionaryAttribute extends HibAttribute
        implements DictionaryAttribute {

    /**
     * 
     */
    private static final long serialVersionUID = 3713980765847199175L;
    
    @ElementCollection
    @JoinTable(
            name="dictionary_values",
            joinColumns = @JoinColumn(name="attributeid")
    )
    @MapKeyColumn(name="keyname", length=255)
    @Column(name="stringvalue", length=2048)
    private Map<String, String> value = new HashMap<String,String>(0);

    /** default constructor */
    public HibDictionaryAttribute() {
    }

    public HibDictionaryAttribute(QName qname, Map<String, String> value)
    {
        setQName(qname);
        this.value = value;
    }

    // Property accessors
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Attribute#getValue()
     */
    public Map<String, String> getValue() {
        return this.value;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.DictionaryAttribute#setValue(java.util.Map)
     */
    public void setValue(Map<String, String> value) {
        this.value = value;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Attribute#setValue(java.lang.Object)
     */
    public void setValue(Object value) {
        if (value != null && !(value instanceof Map)) {
            throw new ModelValidationException(
                    "attempted to set non Map value on attribute");
        }
        setValue((Map<String, String>) value);
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.hibernate.HibAttribute#copy()
     */
    public Attribute copy() {
        DictionaryAttribute attr = new HibDictionaryAttribute();
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
