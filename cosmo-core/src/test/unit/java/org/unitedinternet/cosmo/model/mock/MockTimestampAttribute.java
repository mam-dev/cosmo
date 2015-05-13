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

import java.util.Date;

import org.unitedinternet.cosmo.dao.ModelValidationException;
import org.unitedinternet.cosmo.model.Attribute;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.QName;
import org.unitedinternet.cosmo.model.TimestampAttribute;

/**
 * Represents an attribute with a timestamp.  A timestamp
 * is a java.util.Date, containing the number of milliseconds
 * since the epoch (Jan 1, 1970 GMT).
 */
public class MockTimestampAttribute extends MockAttribute implements
        java.io.Serializable, TimestampAttribute {

    /**
     * 
     */
    private static final long serialVersionUID = 5263977785074085449L;
    
    
    private Date value;

    /** default constructor */
    public MockTimestampAttribute() {
    }

    /**
     * Contructor.
     * @param qname The name of the attribute.
     * @param value The date.
     */
    public MockTimestampAttribute(QName qname, Date value) {
        setQName(qname);
        this.value = value;
    }

    // Property accessors
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceTimestampAttribute#getValue()
     */
    /**
     * Gets value.
     * @return The date.
     */
    public Date getValue() {
        return this.value;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceTimestampAttribute#setValue(java.util.Date)
     */
    /**
     * Sets value.
     * @param value The date.
     */
    public void setValue(Date value) {
        this.value = value;
    }
    
    /**
     * Sets value.
     * {@inheritDoc}
     * @param value The value.
     */
    public void setValue(Object value) {
        if (value != null && !(value instanceof Date)) {
            throw new ModelValidationException("attempted to set non Date value on attribute");
        }
        setValue((Date) value);
    }
    
    /**
     * Convienence method for returning a Date value on a TimestampAttribute
     * with a given QName stored on the given item.
     * @param item item to fetch TextAttribute from
     * @param qname QName of attribute
     * @return Date value of TextAttribute
     */
    public static Date getValue(Item item, QName qname) {
        TimestampAttribute ta = (TimestampAttribute) item.getAttribute(qname);
        if (ta == null) {
            return null;
        }
        else {
            return ta.getValue();
        }
    }
    
    /**
     * Convienence method for setting a Date value on a TimestampAttribute
     * with a given QName stored on the given item.
     * @param item item to fetch TimestampAttribute from
     * @param qname QName of attribute
     * @param value value to set on TextAttribute
     */
    public static void setValue(Item item, QName qname, Date value) {
        TimestampAttribute attr = (TimestampAttribute) item.getAttribute(qname);
        if(attr==null && value!=null) {
            attr = new MockTimestampAttribute(qname,value);
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
    
    /**
     * Copy.
     * {@inheritDoc}
     * @return The attribute.
     */
    public Attribute copy() {
        TimestampAttribute attr = new MockTimestampAttribute();
        attr.setQName(getQName().copy());
        attr.setValue(value.clone());
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
