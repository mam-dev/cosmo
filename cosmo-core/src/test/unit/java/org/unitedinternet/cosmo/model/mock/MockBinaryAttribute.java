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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.unitedinternet.cosmo.CosmoIOException;
import org.unitedinternet.cosmo.dao.ModelValidationException;
import org.unitedinternet.cosmo.model.Attribute;
import org.unitedinternet.cosmo.model.BinaryAttribute;
import org.unitedinternet.cosmo.model.DataSizeException;
import org.unitedinternet.cosmo.model.QName;

/**
 * Attribute with a binary value.
 */
public class MockBinaryAttribute extends MockAttribute implements java.io.Serializable, BinaryAttribute {

    /**
     * 
     */
    private static final long serialVersionUID = 6296196539997344427L;
    
    private byte[] value;

    public static final long MAX_BINARY_ATTR_SIZE = 100 * 1024 * 1024;
    
    
    /** default constructor */
    public MockBinaryAttribute() {
    }

    /**
     * Constructor.
     * @param qname The qname.
     * @param value The value.
     */
    public MockBinaryAttribute(QName qname, byte[] value) {
        setQName(qname);
        this.value = value;
    }
    
    /**
     * Construct BinaryAttribute and initialize data using
     * an InputStream
     * @param qname The qname.
     * @param value The value.
     */
    public MockBinaryAttribute(QName qname, InputStream value) {
        setQName(qname);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            IOUtils.copy(value, bos);
        } catch (IOException e) {
           throw new CosmoIOException("error reading input stream", e);
        }
        this.value = bos.toByteArray();
    }

    // Property accessors
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBinaryAttribute#getValue()
     */
    /**
     * Gets value.
     * @return byte[].
     */
    public byte[] getValue() {
        return this.value;
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBinaryAttribute#setValue(byte[])
     */
    /**
     * Sets value.
     * @param value The value.
     */
    public void setValue(byte[] value) {
        this.value = value;
    }

    /**
     * Sets value.
     * {@inheritDoc}
     * @param value The value.
     */
    public void setValue(Object value) {
        if (value != null && !(value instanceof byte[])) {
            throw new ModelValidationException("attempted to set non binary value on attribute");
        }
        setValue((byte[]) value);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBinaryAttribute#getLength()
     */
    /**
     * Gets length.
     * @return The length.
     */
    public int getLength() {
        if (value != null) {
            return value.length;
        }
        else {
            return 0; 
        }
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.copy.InterfaceBinaryAttribute#getInputStream()
     */
    /**
     * Gets input stream.
     * @return The input stream.
     */
    public InputStream getInputStream() {
        if (value!=null) {
            return new ByteArrayInputStream(value);
        }
        else {
            return null;
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.unitedinternet.cosmo.model.Attribute#copy()
     */
    /**
     * Copy.
     * @return The attribute.
     */
    public Attribute copy() {
        BinaryAttribute attr = new MockBinaryAttribute();
        attr.setQName(getQName().copy());
        if (value != null) {
            attr.setValue(value.clone());
        }
        return attr;
    }
    
    @Override
    public void validate() {
        if (value!=null && value.length > MAX_BINARY_ATTR_SIZE) {
            throw new DataSizeException("Binary attribute " + getQName() + " too large");
        }
    }

    @Override
    public String calculateEntityTag() {
       return "";
    }

}
