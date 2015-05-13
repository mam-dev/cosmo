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
package org.unitedinternet.cosmo.model.mock;

import java.io.IOException;
import java.io.InputStream;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;

import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.calendar.util.CalendarUtils;
import org.unitedinternet.cosmo.dao.ModelValidationException;
import org.unitedinternet.cosmo.model.Attribute;
import org.unitedinternet.cosmo.model.ICalendarAttribute;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.QName;

/**
 * Represents an attribute with a net.fortuna.ical4j.model.Calendar value.
 */
public class MockICalendarAttribute extends MockAttribute implements
        java.io.Serializable, ICalendarAttribute {

    private Calendar value;

    /** default constructor */
    public MockICalendarAttribute() {
    }

    /**
     * @param qname qualified name
     * @param value initial value
     */
    public MockICalendarAttribute(QName qname, Calendar value) {
        setQName(qname);
        this.value = value;
    }
    
    /**
     * @param qname qualified name
     * @param value calendar
     */
    public MockICalendarAttribute(QName qname, String value) {
        setQName(qname);
        setValue(value);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceICalendarAttribute#getValue()
     */
    /**
     * Gets calendar value.
     * @return The calendar.
     */
    public Calendar getValue() {
        return this.value;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceICalendarAttribute#setValue(net.fortuna.ical4j.model.Calendar)
     */
    /**
     * Sets value.
     * @param value The calendar value.
     */
    public void setValue(Calendar value) {
        this.value = value;
    }
    
    /**
     * Sets value.
     * {@inheritDoc}
     * @pa value The value.
     */
    public void setValue(Object value) {
        if (value != null && !(value instanceof Calendar) && !(value instanceof String) 
                && !(value instanceof InputStream)) {
            throw new ModelValidationException("attempted to set non Calendar value on attribute");
        }
        
        if (value instanceof Calendar) {
            setValue((Calendar) value);
        }
        else if(value instanceof InputStream) {
            setValue((InputStream) value);
        }
        else {
            setValue((String) value);
        }
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceICalendarAttribute#setValue(java.lang.String)
     */
    /**
     * Sets value.
     * @param value The value.
     */
    public void setValue(String value) {
        try {
            this.value = CalendarUtils.parseCalendar(value);
        } catch (ParserException e) {
            throw new ModelValidationException("invalid calendar: " + value);
        } catch (IOException ioe) {
            throw new ModelValidationException("error parsing calendar");
        }
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceICalendarAttribute#setValue(java.io.InputStream)
     */
    /**
     * Sets value.
     * @param is The input stream.
     */
    public void setValue(InputStream is) {
        try {
            this.value = CalendarUtils.parseCalendar(is);
        } catch (ParserException e) {
            throw new ModelValidationException("invalid calendar: "
                    + e.getMessage());
        } catch (IOException ioe) {
            throw new ModelValidationException("error parsing calendar: "
                    + ioe.getMessage());
        }
    }
    
    /**
     * Copy.
     * {@inheritDoc}
     * @return The attribute.
     */
    public Attribute copy() {
        ICalendarAttribute attr = new MockICalendarAttribute();
        attr.setQName(getQName().copy());
        if(attr!=null) {
            try {
                attr.setValue(new Calendar(value));
            } catch (Exception e) {
                throw new CosmoException("Error copying ICalendar attribute", e);
            }
        }
        return attr;
    }
    
    /**
     * Convienence method for returning a Calendar value on 
     * a ICalendarAttribute with a given QName stored on the given item.
     * @param item item to fetch ICalendarAttribute from
     * @param qname QName of attribute
     * @return Date value of ICalendarAttribute
     */
    public static Calendar getValue(Item item, QName qname) {
        ICalendarAttribute attr = (ICalendarAttribute) item.getAttribute(qname);
        if (attr == null) {
            return null;
        }
        else {
            return attr.getValue();
        }
    }
    
    /**
     * Convienence method for setting a Calendar value on a 
     * ICalendarpAttribute with a given QName stored on the given item.
     * @param item item to fetch ICalendarpAttribute from
     * @param qname QName of attribute
     * @param value value to set on ICalendarpAttribute
     */
    public static void setValue(Item item, QName qname, Calendar value) {
        ICalendarAttribute attr = (ICalendarAttribute) item.getAttribute(qname);
        if(attr==null && value!=null) {
            attr = new MockICalendarAttribute(qname,value);
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
