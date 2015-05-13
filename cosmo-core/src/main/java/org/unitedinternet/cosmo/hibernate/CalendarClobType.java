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

import net.fortuna.ical4j.model.Calendar;

import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.descriptor.sql.ClobTypeDescriptor;


/**
 * Custom Hibernate type that persists ical4j Calendar object
 * to CLOB field in database.
 */
public class CalendarClobType extends AbstractSingleColumnStandardBasicType<Calendar> {

    private static final long serialVersionUID = 1L;

    public CalendarClobType() {
        super(ClobTypeDescriptor.DEFAULT, CosmoCalendarTypeDescriptor.INSTANCE);
    }

    @Override
    public String getName() {
        return "calendar_clob";
    }

}
