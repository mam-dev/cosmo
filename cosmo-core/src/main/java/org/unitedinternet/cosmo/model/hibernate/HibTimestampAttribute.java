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
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.QName;
import org.unitedinternet.cosmo.model.TimestampAttribute;

/**
 * Hibernate persistent TimestampAtttribute.
 */
@Entity
@DiscriminatorValue("timestamp")
public class HibTimestampAttribute extends HibAttribute implements TimestampAttribute {

    
    private static final long serialVersionUID = 5263977785074085449L;
    
    @Column(name = "intvalue")
    private Long value;

    public HibTimestampAttribute() {
    }

    public HibTimestampAttribute(QName qname, Long value) {
        setQName(qname);
        this.value = value;
    }

    public Long getValue() {
        return this.value;
    }

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
     * Convenience method for returning a Date value on a TimestampAttribute
     * with a given QName stored on the given item.
     * 
     * @param item item to fetch TextAttribute from
     * @param qname QName of attribute
     * @return Date value of TextAttribute
     */
    public static Long getValue(Item item, QName qname) {
        TimestampAttribute attibute = (TimestampAttribute) item.getAttribute(qname);
        if(attibute == null) {
            return null;
        }
        return attibute.getValue();        
    }
    
    /**
     * Convenience method for setting a Date value on a TimestampAttribute
     * with a given QName stored on the given item.
     * @param item item to fetch TimestampAttribute from
     * @param qname QName of attribute
     * @param value value to set on TextAttribute
     */
    public static void setValue(Item item, QName qname, Long value) {
        TimestampAttribute attr = (TimestampAttribute) item.getAttribute(qname);
        if(attr==null && value!=null) {
            attr = new HibTimestampAttribute(qname, value);
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
    
    public Attribute copy() {
        TimestampAttribute attr = new HibTimestampAttribute();
        attr.setQName(getQName().copy());
        attr.setValue(value);
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
