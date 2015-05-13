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
package org.unitedinternet.cosmo.hibernate;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.TimeZone;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.TimeZoneType;
import org.hibernate.type.Type;
import org.hibernate.usertype.CompositeUserType;
import org.unitedinternet.cosmo.CosmoException;

/**
 * Custom Hibernate type that maps a java.util.Calendar 
 * to two columns.  One column stores the timestamp (datetime)
 * and the other stores the timezone.  This provides support
 * for datetimes with timezone.
 */
public class CalendarType implements CompositeUserType {

    @Override
    public Class<?> returnedClass() {
        return Calendar.class;
    }

    @Override
    public Type[] getPropertyTypes() {
        return new Type[] {org.hibernate.type.CalendarType.INSTANCE, TimeZoneType.INSTANCE};
    }

    @Override
    public String[] getPropertyNames() {
        return new String[] { "date", "timezone" };
    }

    @Override
    public Object getPropertyValue(Object component, int property){
        if(property == 0) {
            return (Calendar) component;
        }
        if(property == 1) { 
            return ((Calendar) component).getTimeZone().getID();
        }
        
        return null;
    }

    @Override
    public void setPropertyValue(Object component, int property, Object value) {
        Calendar cal = (Calendar) component;
        if(property == 0) {
            cal.setTime(((Calendar) value).getTime());
        }
        else if(property == 1) { 
            cal.setTimeZone((TimeZone) value);
        }
        else {
            throw new CosmoException("unknown property " + property, new CosmoException());
        }
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names,
            SessionImplementor session, Object owner) throws
            SQLException {
        TimeZone tz = (TimeZone) TimeZoneType.INSTANCE.nullSafeGet(rs, names[1], session);
        if(tz==null) {
            tz = TimeZone.getDefault();
        }
        Calendar cal = org.hibernate.type.CalendarType.INSTANCE.nullSafeGet(rs, names[0], session);
        cal.setTimeZone(tz);
        return cal;
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object obj, int index,
            SessionImplementor session) throws HibernateException, SQLException {
        org.hibernate.type.CalendarType.INSTANCE.nullSafeSet(st, obj, index, session);
        TimeZoneType.INSTANCE.nullSafeSet(st, obj == null ? null : ((Calendar) obj).getTimeZone(), index+1, session);
    }

    @Override
    public boolean equals(Object val1, Object val2){
        if(val1==val2) {
            return true;
        }
        
        if(val1==null || val2==null) {
            return false;
        }
        Calendar cal1 = (Calendar) val1;
        Calendar cal2 = (Calendar) val2;

        return cal1.equals(cal2);
    }

    @Override
    public int hashCode(Object obj) {
        return obj.hashCode();
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Object assemble(Serializable cached, SessionImplementor session,
            Object owner) {
        return deepCopy(cached);
    }

    @Override
    public Object deepCopy(Object obj) {
        if(obj==null) {
            return null;
        }
        return ((Calendar) obj).clone();
    }

    @Override
    public Serializable disassemble(Object value, SessionImplementor session){
        return (Serializable) deepCopy(value);
    }

    @Override
    public Object replace(Object original, Object target, SessionImplementor session,
            Object owner)  {
        return deepCopy(original);
    }

}
