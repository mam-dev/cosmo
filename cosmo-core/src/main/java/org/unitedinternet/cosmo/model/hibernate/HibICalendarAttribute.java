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
package org.unitedinternet.cosmo.model.hibernate;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.CosmoIOException;
import org.unitedinternet.cosmo.calendar.util.CalendarUtils;
import org.unitedinternet.cosmo.dao.ModelValidationException;
import org.unitedinternet.cosmo.model.Attribute;
import org.unitedinternet.cosmo.model.ICalendarAttribute;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.QName;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.validate.ValidationException;

/**
 * Hibernate persistent ICalendarAtttribute.
 */
@Entity
@DiscriminatorValue("icalendar")
public class HibICalendarAttribute extends HibAttribute implements ICalendarAttribute {

    private static final long serialVersionUID = -4714651259305593990L;

    @Column(name = "textvalue", length = 2147483647)
    private String textValue;

    public HibICalendarAttribute() {
        // Default
    }

    /**
     * 
     * @param qname
     * @param value
     */
    public HibICalendarAttribute(QName qname, Calendar value) {
        setQName(qname);
        setValue(value);
    }

    public String getTextValue() {
        return textValue;
    }

    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }

    @Override
    public Calendar getValue() {
        if (this.textValue == null) {
            return null;
        }
        try {
            return new CalendarBuilder().build(new ByteArrayInputStream(this.textValue.getBytes()));
        } catch (IOException | ParserException e) {
            throw new CosmoIOException(e);
        }
    }

    @Override
    public void setValue(Calendar value) {
        if (value != null) {
            try {
                this.textValue = CalendarUtils.outputCalendar(value);
            } catch (ValidationException | IOException e) {
                throw new CosmoIOException(e);
            }
        } else {
            this.textValue = null;
        }
    }

    @Override
    public void setValue(Object value) {
        if (value != null && !(value instanceof Calendar)) {
            throw new ModelValidationException("attempted to set non Calendar value on attribute");
        }
        setValue((Calendar) value);
    }

    public Attribute copy() {
        HibICalendarAttribute attr = new HibICalendarAttribute();
        attr.setQName(getQName().copy());
        try {
            attr.setTextValue(this.textValue);
        } catch (Exception e) {
            throw new CosmoException("Error copying ICalendar attribute", e);
        }

        return attr;
    }

    /**
     * Convienence method for returning a Calendar value on a ICalendarAttribute with a given QName stored on the given
     * item.
     * 
     * @param item  item to fetch ICalendarAttribute from
     * @param qname QName of attribute
     * @return Date value of ICalendarAttribute
     */
    public static Calendar getValue(Item item, QName qname) {
        ICalendarAttribute attr = (ICalendarAttribute) item.getAttribute(qname);
        if (attr == null) {
            return null;
        } else {
            return attr.getValue();
        }
    }

    /**
     * Convienence method for setting a Calendar value on a ICalendarpAttribute with a given QName stored on the given
     * item.
     * 
     * @param item  item to fetch ICalendarpAttribute from
     * @param qname QName of attribute
     * @param value value to set on ICalendarpAttribute
     */
    public static void setValue(Item item, QName qname, Calendar value) {
        ICalendarAttribute attr = (ICalendarAttribute) item.getAttribute(qname);
        if (attr == null && value != null) {
            attr = new HibICalendarAttribute(qname, value);
            item.addAttribute(attr);
            return;
        }
        if (value == null) {
            item.removeAttribute(qname);
        } else {
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
