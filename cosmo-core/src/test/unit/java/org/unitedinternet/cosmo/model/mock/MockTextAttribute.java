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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.unitedinternet.cosmo.CosmoIOException;
import org.unitedinternet.cosmo.dao.ModelValidationException;
import org.unitedinternet.cosmo.model.Attribute;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.QName;
import org.unitedinternet.cosmo.model.TextAttribute;


/**
 * Represents an attribute with a text value.  The difference
 * between StringAttribute and TextAttribute is that
 * TextAttribute can store much larger values than StringAttribute.
 */
public class MockTextAttribute extends MockAttribute implements
        java.io.Serializable, TextAttribute {
    
    
    /**
     * 
     */
    private static final long serialVersionUID = 2417093506524504993L;
    
   
    private String value;

    // Constructors

    /** default constructor */
    public MockTextAttribute() {
    }

    /**
     * Constructor.
     * @param qname The name of the attribute.
     * @param value The value of the attribute.
     */
    public MockTextAttribute(QName qname, String value) {
        setQName(qname);
        this.value = value;
    }
    
    /**
     * Construct TextAttribute from Reader
     * @param qname The name of the attribute.
     * @param reader The reader.
     */
    public MockTextAttribute(QName qname, Reader reader) {
        setQName(qname);
        this.value = read(reader);
    }

    // Property accessors
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceTextAttribute#getValue()
     */
    /**
     * Gets value.
     * @return The value.
     */
    public String getValue() {
        return this.value;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceTextAttribute#setValue(java.lang.String)
     */
    /**
     * Sets value.
     * @param value The value.
     */
    public void setValue(String value) {
        this.value = value;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceTextAttribute#getReader()
     */
    /**
     * Gets reader.
     * @return The reader.
     */
    public Reader getReader() {
        if (value!=null) {
            return new StringReader(value);
        }
        else {
            return null;
        }
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceTextAttribute#getLength()
     */
    /**
     * Gets length.
     * @return The length.
     */
    public int getLength() {
        if (value != null) {
            return value.length();
        }
        else {
            return 0;
        }
    }
    
    /**
     * Sets value.
     * {@inheritDoc}
     * @param value The value.
     */
    public void setValue(Object value) {
        if (value != null && !(value instanceof String) &&
            !(value instanceof Reader)) {
            throw new ModelValidationException(
                    "attempted to set non String or Reader value on attribute");
        }
        if (value instanceof Reader) {
            setValue(read((Reader) value));
        } else {
            setValue((String) value);
        }
    }
    
    /**
     * Copy.
     * {@inheritDoc}
     * @return The attribute.
     */
    public Attribute copy() {
        TextAttribute attr = new MockTextAttribute();
        attr.setQName(getQName().copy());
        attr.setValue(getValue());
        return attr;
    }

    /**
     * Read.
     * @param reader The reader.
     * @return The reader.
     */
    private String read(Reader reader) {
        if (reader == null) {
            return null;
        }
        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(reader, writer);
        } catch (IOException e) {
            throw new CosmoIOException("error reading stream", e);
        }
        return writer.toString();
    }
    
    /**
     * Convienence method for returning a String value on a TextAttribute
     * with a given QName stored on the given item.
     * @param item item to fetch TextAttribute from
     * @param qname QName of attribute
     * @return String value of TextAttribute
     */
    public static String getValue(Item item, QName qname) {
        TextAttribute ta = (TextAttribute) item.getAttribute(qname);
        if (ta == null) {
            return null;
        }
        else {
            return ta.getValue();
        }
    }
    
    /**
     * Convienence method for setting a String value on a TextAttribute
     * with a given QName stored on the given item.
     * @param item item to fetch TextAttribute from
     * @param qname QName of attribute
     * @param value value to set on TextAttribute
     */
    public static void setValue(Item item, QName qname, String value) {
        TextAttribute attr = (TextAttribute) item.getAttribute(qname);
        if(attr==null && value!=null) {
            attr = new MockTextAttribute(qname,value);
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
     * Convienence method for setting a Reader value on a TextAttribute
     * with a given QName stored on the given item.
     * @param item item to fetch TextAttribute from
     * @param qname QName of attribute
     * @param value value to set on TextAttribute
     */
    public static void setValue(Item item, QName qname, Reader value) {
        TextAttribute attr = (TextAttribute) item.getAttribute(qname);
        if(attr==null && value!=null) {
            attr = new MockTextAttribute(qname,value);
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
