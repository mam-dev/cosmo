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

import org.unitedinternet.cosmo.dao.ModelValidationException;
import org.unitedinternet.cosmo.model.Attribute;
import org.unitedinternet.cosmo.model.IntegerAttribute;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.QName;

/**
 * Represents attribute with an integer value.
 */
public class MockIntegerAttribute extends MockAttribute implements java.io.Serializable, IntegerAttribute {

    /**
     * 
     */
    private static final long serialVersionUID = -7110319771835652090L;

    private Long value;

    /** default constructor */
    public MockIntegerAttribute() {
    }

    /**
     * Contructor.
     * @param qname The qname.
     * @param value The value.
     */
    public MockIntegerAttribute(QName qname, Long value) {
        setQName(qname);
        this.value = value;
    }

    // Property accessors
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceIntegerAttribute#getValue()
     */
    /**
     * Gets value.
     * @return The value.
     */
    public Long getValue() {
        return this.value;
    }

    /**
     * Copy.
     * {@inheritDoc}
     * @return The attribute.
     */
    public Attribute copy() {
        IntegerAttribute attr = new MockIntegerAttribute();
        attr.setQName(getQName().copy());
        attr.setValue(Long.valueOf(value));
        return attr;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceIntegerAttribute#setValue(java.lang.Long)
     */
    /**
     * Sets value.
     * @param value The value.
     */
    public void setValue(Long value) {
        this.value = value;
    }

    /**
     * Sets value.
     * {@inheritDoc}
     * @param value The value.
     */
    public void setValue(Object value) {
        if (value != null && !(value instanceof Long)) {
            throw new ModelValidationException("attempted to set non Long value on attribute");
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
        if (ia == null) {
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
            attr = new MockIntegerAttribute(qname,value);
            item.addAttribute(attr);
            return;
        }
        if (value == null) {
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
