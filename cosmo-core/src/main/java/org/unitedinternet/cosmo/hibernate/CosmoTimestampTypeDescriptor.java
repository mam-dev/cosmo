package org.unitedinternet.cosmo.hibernate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;
import org.hibernate.type.descriptor.java.JdbcTimestampTypeDescriptor.TimestampMutabilityPlan;
import org.unitedinternet.cosmo.CosmoParseException;

/**
 * Adapter for Timestamp type descriptor.
 * 
 * @author ccoman
 * 
 */
@SuppressWarnings("serial")
public class CosmoTimestampTypeDescriptor extends AbstractTypeDescriptor<Date> {

    public static final CosmoTimestampTypeDescriptor INSTANCE = new CosmoTimestampTypeDescriptor();

    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";

    protected CosmoTimestampTypeDescriptor() {
        super(Date.class, TimestampMutabilityPlan.INSTANCE);
    }

    @Override
    public Date fromString(String value) {
        try {
            return new SimpleDateFormat(TIMESTAMP_FORMAT).parse(value);
        } catch (ParseException e) {
            throw new CosmoParseException(e);
        }
    }

    @Override
    public String toString(Date value) {
        return new SimpleDateFormat(TIMESTAMP_FORMAT).format(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <X> X unwrap(Date value, Class<X> type, WrapperOptions options) {
        if (value == null) {
            return null;
        }
        if (Long.class.isAssignableFrom(type)) {
            return (X) Long.valueOf(value.getTime());
        }
        throw unknownUnwrap(type);
    }

    @Override
    public <X> Date wrap(X value, WrapperOptions options) {
        if (value == null) {
            return null;
        }
        if (Long.class.isAssignableFrom(value.getClass())) {
            return new Date((Long) value);
        }
        throw unknownWrap(value.getClass());
    }

}
