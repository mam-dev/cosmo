package org.unitedinternet.cosmo.hibernate;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Clob;
import java.sql.SQLException;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.validate.ValidationException;

import org.hibernate.engine.jdbc.CharacterStream;
import org.hibernate.engine.jdbc.internal.CharacterStreamImpl;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;
import org.hibernate.type.descriptor.java.MutableMutabilityPlan;
import org.unitedinternet.cosmo.CosmoIOException;
import org.unitedinternet.cosmo.CosmoParseException;
import org.unitedinternet.cosmo.CosmoSqlException;
import org.unitedinternet.cosmo.CosmoValidationException;
import org.unitedinternet.cosmo.calendar.util.CalendarUtils;

/**
 * Adapter for Calendar type descriptor.
 * 
 * @author cristina coman
 * 
 */
public class CosmoCalendarTypeDescriptor extends AbstractTypeDescriptor<Calendar> {
    private static final long serialVersionUID = -6135150975606583510L;
    
    public static final CosmoCalendarTypeDescriptor INSTANCE = new CosmoCalendarTypeDescriptor();

    public static class CalendarMutabilityPlan extends MutableMutabilityPlan<Calendar> {
        
        private static final long serialVersionUID = 3323888185833891112L;
        
        public static final CalendarMutabilityPlan INSTANCE = new CalendarMutabilityPlan();

        public Calendar deepCopyNotNull(Calendar value) {
            String representation;
            try {
                representation = value.toString();
                return new CalendarBuilder().build(new StringReader(representation));
            } catch (IOException e) {
                throw new CosmoIOException(e);
            } catch (ParserException e) {
                throw new CosmoParseException(e);
            }
        }
    }
    
    protected CosmoCalendarTypeDescriptor() {
        super(Calendar.class, CalendarMutabilityPlan.INSTANCE);
    }

    @Override
    public Calendar fromString(String value) {
        CalendarBuilder builder = new CalendarBuilder();
        Calendar calendar = null;
        try {
            calendar = builder.build(new StringReader(value));
        } catch (IOException e) {
            throw new CosmoIOException("can not happen with StringReader", e);
        } catch (ParserException e) {
            throw new CosmoParseException(e);
        }
        return calendar;
    }

    @Override
    public String toString(Calendar value) {
        String calendar = null;
        try {
            calendar = CalendarUtils.outputCalendar(value);
        } catch (ValidationException e) {
            throw new CosmoValidationException(e);
        } catch (IOException e) {
            throw new CosmoIOException(e);
        }
        return calendar;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <X> X unwrap(Calendar value, Class<X> type, WrapperOptions options) {
        if (value == null) {
            return null;
        }
        if (Calendar.class.isAssignableFrom(type)) {
            return (X) value;
        }
        if (CharacterStream.class.isAssignableFrom(type)) {
                return (X) new CharacterStreamImpl(value.toString());
        }
        throw unknownUnwrap(type);
    }

    @Override
    public <X> Calendar wrap(X value, WrapperOptions options) {
        Calendar calendar = null;
        if (value == null) {
            calendar = null;
        }
        if (Calendar.class.isInstance(value)) {
            calendar = (Calendar) value;
        }
        if(Clob.class.isInstance(value)){
            try {
                calendar = CalendarUtils.parseCalendar(((Clob)value).getCharacterStream());
            } catch (ParserException e) {
                throw new CosmoParseException(e);
            } catch (IOException e) {
                throw new CosmoIOException(e);
            } catch (SQLException e) {
                throw new CosmoSqlException(e);
            }
        }        
        return calendar;
    }

}
