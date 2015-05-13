/*
 * EventStampUtil.java Jun 24, 2014
 * 
 * Copyright (c) 2014 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.util;

import net.fortuna.ical4j.model.component.VEvent;

import org.unitedinternet.cosmo.model.BaseEventStamp;
import org.unitedinternet.cosmo.model.EventExceptionStamp;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.Item;

/**
 * Utility class for working with event stamps
 * @author izidaru
 *
 */
public class EventStampUtil {

    /**
     * Extracts the Stamp containing the ics
     * @param item ContentItem
     * @return the Stamp containing the ics
     */
    public static BaseEventStamp extractEventStampFromItem(Item item){
         EventStamp eventStamp = (EventStamp)item.getStamp(EventStamp.class);
         if (eventStamp == null) {
             // if no event stamp, check for exception stamp
             EventExceptionStamp eventExceptionStamp = (EventExceptionStamp)item.getStamp(EventExceptionStamp.class);
             if (eventExceptionStamp == null) {
                 return null;//an item must have an event stam por an exception stamp
             } else {
                 return eventExceptionStamp;
             }
         } else {
             return eventStamp;
         }
     }
    
    
    /**
     *  @param item ContentItem containing a VEvent
     * @return  VEvent event from ContentItem
     */
     public static VEvent extractVEventFromItem(Item item){
         BaseEventStamp eventStamp = extractEventStampFromItem(item);

         if(eventStamp != null){
             return eventStamp.getEvent();
         }

         return  null;
     }
}
