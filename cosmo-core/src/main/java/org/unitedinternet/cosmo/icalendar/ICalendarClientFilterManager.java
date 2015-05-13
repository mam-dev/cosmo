/*
 * Copyright 2008 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.icalendar;

import java.util.Map;

import net.fortuna.ical4j.model.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Manages a set of filters to be applied on a Calendar
 * object.  The manager maintains a threadlocal variable
 * containing a client identifier.  The client identifier
 * should be initialized at the start of a request (most
 * likley using a servlet filter) and maps to a 
 * ICalendarClientFilter instance.
 * 
 * The idea is that there are clients that don't strictly
 * adhere to the icalendar spec (RFC-2445) and by applying
 * filters to a Calendar object, the server can play 
 * nicely with these clients.
 *
 */
public class ICalendarClientFilterManager {
    private Map<String, ICalendarClientFilter> clientFilters;
    private ThreadLocal<String> clientLocal = new ThreadLocal<String>();
    
    private static final Log LOG = LogFactory.getLog(ICalendarClientFilterManager.class);

    
    /**
     * Initialize the client identifier for the current thread
     * @param id client identifier
     */
    public void setClient(String id) {
        clientLocal.set(id);
    }
    
    /**
     * Filter Calendar instance based on the current
     * client identifier.  If a ICalendarClientFilter
     * instance is found for the current client identifier,
     * then apply that filter to the calendar instance, 
     * otherwise do nothing.
     * @param calendar Calendar instance to filter
     */
    public void filterCalendar(Calendar calendar) {
        String clientId = clientLocal.get();
        if(clientId==null || clientFilters==null) {
            return;
        }
        
        ICalendarClientFilter filter = clientFilters.get(clientId);
        if(filter!=null) {
            if(LOG.isDebugEnabled()) {
                //Fix Log Forging - fortify
                //Writing unvalidated user input to log files can allow an attacker to forge log entries
                //or inject malicious content into the logs.
                LOG.debug("applying icalendar filter for client: " + clientId);
            }
            filter.filterCalendar(calendar);
        }
        
    }

    public void setClientFilters(Map<String, ICalendarClientFilter> clientFilters) {
        this.clientFilters = clientFilters;
    }
}
