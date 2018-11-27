/*
 * BufferedContentTypeDescriptor.java Feb 24, 2012
 * 
 * Copyright (c) 2012 1&1 Internet AG. All rights reserved.
 * 
 * $Id: BufferedContentTypeDescriptor.java 10925 2015-02-06 11:07:33Z izidaru $
 */
package org.unitedinternet.cosmo.hibernate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.SQLException;

import org.hibernate.engine.jdbc.BinaryStream;
import org.hibernate.engine.jdbc.internal.BinaryStreamImpl;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;
import org.hibernate.type.descriptor.java.DataHelper;
import org.hibernate.type.descriptor.java.MutabilityPlan;
import org.hibernate.type.descriptor.java.PrimitiveByteArrayTypeDescriptor;
import org.unitedinternet.cosmo.CosmoIOException;
import org.unitedinternet.cosmo.util.BufferedContent;

/**
 * Type descriptor from creating BufferedContent instances.
 * 
 * @author iulia
 *
 */
public class BufferedContentTypeDescriptor extends AbstractTypeDescriptor<BufferedContent> {

    private static final long serialVersionUID = -1055420379476000710L;
    public static final BufferedContentTypeDescriptor INSTANCE = new BufferedContentTypeDescriptor();

    protected BufferedContentTypeDescriptor() {
        super(BufferedContent.class, BufferedContentMutabilityPlan.INSTANCE);
    }

    public static class BufferedContentMutabilityPlan implements MutabilityPlan<BufferedContent> {

        private static final long serialVersionUID = 4332020476637486780L;
        public static final BufferedContentMutabilityPlan INSTANCE = new BufferedContentMutabilityPlan();

        public boolean isMutable() {
            return true;
        }

        public BufferedContent deepCopy(BufferedContent value) {
            try {
                return new BufferedContent(value.getInputStream());
            } catch (IOException e) {
                throw new CosmoIOException("error in deepcopy of BufferedContent", e);
            }
        }

        public Serializable disassemble(BufferedContent value) {
            throw new UnsupportedOperationException("Blobs are not cacheable");
        }

        public BufferedContent assemble(Serializable cached) {
            throw new UnsupportedOperationException("Blobs are not cacheable");
        }
    }

    @Override
    public BufferedContent fromString(String string) {
        BufferedContent bufferedContent = null;
        try {
            new BufferedContent(new ByteArrayInputStream(string.getBytes("UTF-8")));
        } catch (IOException e) {
            throw new CosmoIOException(e);
        }
        return bufferedContent;
    }

    @Override
    public String toString(BufferedContent value) {
        final byte[] bytes;
        bytes = DataHelper.extractBytes(value.getInputStream());
        return PrimitiveByteArrayTypeDescriptor.INSTANCE.toString(bytes);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <X> X unwrap(BufferedContent value, Class<X> type, WrapperOptions options) {
        if (value == null) {
            return null;
        }
        if (BufferedContent.class.isAssignableFrom(type)) {
            return (X) value;
        }
        if (BinaryStream.class.isAssignableFrom(type)) {
            return (X) new BinaryStreamImpl(DataHelper.extractBytes(value.getInputStream()));
        }
        throw unknownUnwrap(type);
    }

    @Override
    public <X> BufferedContent wrap(X value, WrapperOptions options) {
        if (value == null) {
            return null;
        }
        if (BufferedContent.class.isInstance(value)) {
            return (BufferedContent) value;
        }
        if (InputStream.class.isInstance(value)) {
            try {
                return new BufferedContent((InputStream) value);
            } catch (IOException e) {
                throw new CosmoIOException(e);
            }
        }
        if (Blob.class.isInstance(value)) {
            try {
                return new BufferedContent(((Blob) value).getBinaryStream());
            } catch (IOException | SQLException e) {
                throw new CosmoIOException(e);
            }
        }
        throw unknownWrap(value.getClass());
    }

}
