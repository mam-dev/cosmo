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

import java.text.ParseException;
import java.util.Calendar;

import org.unitedinternet.cosmo.dao.ModelValidationException;
import org.unitedinternet.cosmo.model.Attribute;
import org.unitedinternet.cosmo.model.CalendarAttribute;
import org.unitedinternet.cosmo.model.QName;
import org.unitedinternet.cosmo.util.DateUtil;

/**
 * Represents an attribute with a java.util.Calendar value.
 */
public class MockCalendarAttribute extends MockAttribute implements
        java.io.Serializable, CalendarAttribute {

    private Calendar value;

    /** default constructor */
    public MockCalendarAttribute() {
    }

    /**
     * @param qname qualified name
     * @param value initial value
     */
    public MockCalendarAttribute(QName qname, Calendar value) {
        setQName(qname);
        this.value = value;
    }
    
    /**
     * @param qname qualified name
     * @param value String representation of Calendar
     */
    public MockCalendarAttribute(QName qname, String value) {
        setQName(qname);
        setValue(value);
    }

    // Property accessors
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceCalendarAttribute#getValue()
     */
    /**
     * Gets value.
     * @return The calendar.
     */
    public Calendar getValue() {
        return this.value;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceCalendarAttribute#setValue(java.util.Calendar)
     */
    /**
     * Sets value.
     * @param value The value.
     */
    public void setValue(Calendar value) {
        this.value = value;
    }
    
    /**
     * Sets value.
     * {@inheritDoc}
     * @param value
     */
    public void setValue(Object value) {
        if (value != null && !(value instanceof Calendar) && !(value instanceof String)) {
            throw new ModelValidationException("attempted to set non Calendar value on attribute");
        }
        
        if (value instanceof Calendar) {
            setValue((Calendar) value);
        }
        else {
            if (value != null) {
                setValue((String) value);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceCalendarAttribute#setValue(java.lang.String)
     */
    /**
     * Sets value.
     * @param value The value.
     */
    public void setValue(String value) {
        try {
            this.value = DateUtil.parseRfc3339Calendar(value);
        } catch (ParseException e) {
            throw new ModelValidationException("invalid date format: " + value);
        }
    }
    
    /**
     * Copy.
     * {@inheritDoc}
     * @return The attribute.
     */
    public Attribute copy() {
        CalendarAttribute attr = new MockCalendarAttribute();
        attr.setQName(getQName().copy());
        if (attr!=null) {
            attr.setValue(value.clone());
        }
        return attr;
    }
    
    /**
     * Return Calendar representation in RFC 3339 format.
     * @return The string.
     */
    public String toString() {
        if (value == null) {
            return "null";
        }
        return DateUtil.formatRfc3339Calendar(value);
    }

    @Override
    public void validate() {
        
    }

    @Override
    public String calculateEntityTag() {
        return null;
    }
}
