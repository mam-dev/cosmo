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

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.unitedinternet.cosmo.dao.ModelValidationException;
import org.unitedinternet.cosmo.model.Attribute;
import org.unitedinternet.cosmo.model.IntegerAttribute;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.QName;

/**
 * Hibernate persistent IntegerAtttribute.
 */
@Entity
@DiscriminatorValue("integer")
public class HibIntegerAttribute extends HibAttribute implements IntegerAttribute {

    /**
     * 
     */
    private static final long serialVersionUID = -7110319771835652090L;

    @Column(name = "intvalue")
    private Long value;

    /** default constructor */
    public HibIntegerAttribute() {
    }

    public HibIntegerAttribute(QName qname, Long value) {
        setQName(qname);
        this.value = value;
    }

    // Property accessors
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Attribute#getValue()
     */
    public Long getValue() {
        return this.value;
    }

    public Attribute copy() {
        IntegerAttribute attr = new HibIntegerAttribute();
        attr.setQName(getQName().copy());
        attr.setValue(value);
        return attr;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.IntegerAttribute#setValue(java.lang.Long)
     */
    public void setValue(Long value) {
        this.value = value;
    }

    public void setValue(Object value) {
        if (value != null && !(value instanceof Long)) {
            throw new ModelValidationException(
                    "attempted to set non Long value on attribute");
        }
        setValue((Long) value);
    }
    
    /**
     * Convienence method for returning a Long value on a IntegerAttribute
     * with a given QName stored on the given item.
     * @param item item to fetch IntegerAttribute from
     * @param qname QName of attribute
     * @return Long value of IntegerAttribute
     */
    public static Long getValue(Item item, QName qname) {
        IntegerAttribute ia = (IntegerAttribute) item.getAttribute(qname);
        if(ia==null) {
            return null;
        }
        else {
            return ia.getValue();
        }
    }
    
    /**
     * Convienence method for setting a Long value on a IntegerAttribute
     * with a given QName stored on the given item.
     * @param item item to fetch IntegerAttribute from
     * @param qname QName of attribute
     * @param value value to set on IntegerAttribute
     */
    public static void setValue(Item item, QName qname, Long value) {
        IntegerAttribute attr = (IntegerAttribute) item.getAttribute(qname);
        if(attr==null && value!=null) {
            attr = new HibIntegerAttribute(qname,value);
            item.addAttribute(attr);
            return;
        }
        if(value==null) {
            item.removeAttribute(qname);
        }
        else {
            attr.setValue(value);
        }
    }

    @Override
    public void validate() {
        
    }

    @Override
    public String calculateEntityTag() {
        return "";
    }

}
