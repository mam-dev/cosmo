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

import java.io.IOException;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.unitedinternet.cosmo.CosmoIOException;
import org.unitedinternet.cosmo.CosmoParseException;
import org.unitedinternet.cosmo.CosmoXMLStreamException;
import org.unitedinternet.cosmo.dao.ModelValidationException;
import org.unitedinternet.cosmo.model.Attribute;
import org.unitedinternet.cosmo.model.QName;
import org.unitedinternet.cosmo.model.XmlAttribute;
import org.unitedinternet.cosmo.util.DomReader;
import org.unitedinternet.cosmo.util.DomWriter;
import org.w3c.dom.Element;

/**
 * Hibernate persistent XMLAttribute.
 */
@Entity
@DiscriminatorValue("xml")
public class HibXmlAttribute extends HibAttribute implements XmlAttribute {

    private static final long serialVersionUID = -6431240722450099152L;

    @Column(name = "textvalue", length = 2147483647)
    private String textValue;

    public HibXmlAttribute() {

    }

    public HibXmlAttribute(QName qName, Element element) {
        setQName(qName);
        setValue(element);
    }

    public String getTextValue() {
        return textValue;
    }

    public void setTextValue(String textValue) {
        from(textValue);
        this.textValue = textValue;
    }

    private static Element from(String value) {
        try {
            return (Element) DomReader.read(value);
        } catch (ParserConfigurationException e) {
            throw new CosmoParseException(e);
        } catch (XMLStreamException e) {
            throw new CosmoXMLStreamException(e);
        } catch (IOException e) {
            throw new CosmoIOException(e);
        }
    }

    @Override
    public Element getValue() {
        return from(this.textValue);
    }

    @Override
    public void setValue(Element value) {
        try {
            this.textValue = DomWriter.write(value);
        } catch (XMLStreamException e) {
            throw new CosmoXMLStreamException(e);
        } catch (IOException e) {
            throw new CosmoIOException(e);
        }
    }
    
    @Override
    public void setValue(Object value) {
        if (value != null && !(value instanceof Element)) {
            throw new ModelValidationException("attempted to set non-Element value");
        }
        setValue((Element) value);
    }

    public Attribute copy() {
        HibXmlAttribute attr = new HibXmlAttribute();
        attr.setQName(getQName().copy());
        attr.setTextValue(this.textValue);
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
