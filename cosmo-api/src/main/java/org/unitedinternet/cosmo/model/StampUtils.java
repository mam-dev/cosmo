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
package org.unitedinternet.cosmo.model;

/**
 * Contains static helper methods for dealing with Stamps.
 */
public class StampUtils {
    
   /**
    * Return MessageStamp from Item
    * @param item
    * @return MessageStamp from Item
    */
   public static MessageStamp getMessageStamp(Item item) {
       return (MessageStamp) item.getStamp(MessageStamp.class);
   }
    
    /**
     * Return BaseEventStamp from Item
     * @param item
     * @return BaseEventStamp from Item
     */
    public static BaseEventStamp getBaseEventStamp(Item item) {
        return (BaseEventStamp) item.getStamp(BaseEventStamp.class);
    }
    
    /**
     * Return EventStamp from Item
     * @param item
     * @return EventStamp from Item
     */
    public static EventStamp getEventStamp(Item item) {
        return (EventStamp) item.getStamp(EventStamp.class);
    }
    
    /**
     * Return EventExceptionStamp from Item
     * @param item
     * @return EventExceptionStamp from Item
     */
    public static EventExceptionStamp getEventExceptionStamp(Item item) {
        return (EventExceptionStamp) item.getStamp(EventExceptionStamp.class);
    }
    
    /**
     * Return CalendarCollectionStamp from Item
     * @param item
     * @return CalendarCollectionStamp from Item
     */
    public static CalendarCollectionStamp getCalendarCollectionStamp(Item item) {
        return (CalendarCollectionStamp) item.getStamp(CalendarCollectionStamp.class);
    }
    
    /**
     * Return TaskStamp from Item
     * @param item
     * @return TaskStamp from Item
     */
    public static TaskStamp getTaskStamp(Item item) {
        return (TaskStamp) item.getStamp(TaskStamp.class);
    }
}
