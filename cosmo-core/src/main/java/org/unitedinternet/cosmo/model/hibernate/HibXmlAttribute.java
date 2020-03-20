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

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.hibernate.annotations.Type;
import org.unitedinternet.cosmo.dao.ModelValidationException;
import org.unitedinternet.cosmo.model.Attribute;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.QName;
import org.unitedinternet.cosmo.model.XmlAttribute;
import org.w3c.dom.Element;

/**
 * Hibernate persistent XMLAttribute.
 */
@Entity
@DiscriminatorValue("xml")
public class HibXmlAttribute extends HibAttribute implements XmlAttribute {

    private static final long serialVersionUID = -6431240722450099152L;

    @Column(name = "textvalue", length = 2147483647)
    @Type(type = "xml_clob")
    private Element value;

    public HibXmlAttribute() {
    }

    public HibXmlAttribute(QName qname, Element value) {
        setQName(qname);
        this.value = value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.Attribute#getValue()
     */
    public Element getValue() {
        return this.value;
    }

    public Attribute copy() {
        XmlAttribute attr = new HibXmlAttribute();
        attr.setQName(getQName().copy());
        Element clone = value != null ? (Element) value.cloneNode(true) : null;
        attr.setValue(clone);
        return attr;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.XmlAttribute#setValue(org.w3c.dom.Element)
     */
    public void setValue(Element value) {
        this.value = value;
    }

    public void setValue(Object value) {
        if (value != null && !(value instanceof Element)) {
            throw new ModelValidationException("attempted to set non-Element value");
        }
        setValue((Element) value);
    }

    /**
     * Convienence method for returning a Element value on an XmlAttribute with a given QName stored on the given item.
     * 
     * @param item  item to fetch XmlAttribute from
     * @param qname QName of attribute
     * @return Long value of XmlAttribute
     */
    public static Element getValue(Item item, QName qname) {
        XmlAttribute xa = (XmlAttribute) item.getAttribute(qname);
        if (xa == null) {
            return null;
        } else {
            return xa.getValue();
        }
    }

    /**
     * Convienence method for setting a Elementvalue on an XmlAttribute with a given QName stored on the given item.
     * 
     * @param item  item to fetch Xmlttribute from
     * @param qname QName of attribute
     * @param value value to set on XmlAttribute
     */
    public static void setValue(Item item, QName qname, Element value) {
        XmlAttribute attr = (XmlAttribute) item.getAttribute(qname);
        if (attr == null && value != null) {
            attr = new HibXmlAttribute(qname, value);
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
