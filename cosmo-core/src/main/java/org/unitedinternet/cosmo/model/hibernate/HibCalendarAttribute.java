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

import java.text.ParseException;
import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.hibernate.annotations.Columns;
import org.hibernate.annotations.Type;
import org.unitedinternet.cosmo.dao.ModelValidationException;
import org.unitedinternet.cosmo.model.Attribute;
import org.unitedinternet.cosmo.model.CalendarAttribute;
import org.unitedinternet.cosmo.model.QName;
import org.unitedinternet.cosmo.util.DateUtil;

/**
 * Hibernate persistent CalendarAtttribute.
 */
@Entity
@DiscriminatorValue("calendar")
public class HibCalendarAttribute extends HibAttribute implements CalendarAttribute {

    private static final long serialVersionUID = 4409224462434581867L;
    @Columns(columns = { 
            @Column(name = "datevalue"), @Column(name = "tzvalue", length=32) })
    @Type(type="composite_calendar")
    private Calendar value;

    /** default constructor */
    public HibCalendarAttribute() {
    }

    /**
     * @param qname qualified name
     * @param value initial value
     */
    public HibCalendarAttribute(QName qname, Calendar value) {
        setQName(qname);
        this.value = value;
    }
    
    /**
     * @param qname qualified name
     * @param value String representation of Calendar
     */
    public HibCalendarAttribute(QName qname, String value) {
        setQName(qname);
        setValue(value);
    }

    // Property accessors
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Attribute#getValue()
     */
    public Calendar getValue() {
        return this.value;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.CalendarAttribute#setValue(java.util.Calendar)
     */
    public void setValue(Calendar value) {
        this.value = value;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Attribute#setValue(java.lang.Object)
     */
    public void setValue(Object value) {
        if (value != null && !(value instanceof Calendar)
                && !(value instanceof String)) {
            throw new ModelValidationException(
                    "attempted to set non Calendar value on attribute");
        }
        
        if(value instanceof Calendar) {
            setValue((Calendar) value);
        }
        else {
            if (value != null) {
                setValue(value.toString());
            }
        }
    }
    
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.CalendarAttribute#setValue(java.lang.String)
     */
    public void setValue(String value) {
        try {
            this.value = DateUtil.parseRfc3339Calendar(value);
        } catch (ParseException e) {
            throw new ModelValidationException("invalid date format: " + value);
        }
    }
    
    public Attribute copy() {
        CalendarAttribute attr = new HibCalendarAttribute();
        attr.setQName(getQName().copy());
        attr.setValue(value.clone());
        
        return attr;
    }
    
    /**
     * Return Calendar representation in RFC 3339 format.
     */
    public String toString() {
        if(value==null) {
            return "null";
        }
        return DateUtil.formatRfc3339Calendar(value);
    }

    @Override
    public void validate() {
        
    }

    @Override
    public String calculateEntityTag() {
        return "";
    }
}
