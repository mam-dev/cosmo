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
package org.unitedinternet.cosmo.dav.property;

import java.text.DateFormat;
import java.util.Date;

import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.util.HttpDateFormat;

/**
 * Represents the DAV:getlastmodified property.
 */
public class LastModified extends StandardDavProperty {
    //ThreadLocal used as recommended in DateFormat
    private static ThreadLocal<DateFormat> dateFormatLocal = new ThreadLocal<DateFormat>(){

        @Override
        protected DateFormat initialValue() {
            return new HttpDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        }
        
    };
    
    public LastModified(Date date) {
        super(DavPropertyName.GETLASTMODIFIED, dateFormatLocal(date), false);
    }

    private static String dateFormatLocal(Date date) {
        // need one DateFormat instance per thread
        DateFormat df = dateFormatLocal.get();           
        if (date == null) {
            date = new Date();
        }
        
        return df.format(date);
    }
}
