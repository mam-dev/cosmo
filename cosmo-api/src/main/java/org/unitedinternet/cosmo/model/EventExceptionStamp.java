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

import net.fortuna.ical4j.model.component.VEvent;

/**
 * Event stamp that represents an exception to a recurring
 * event.
 */
public interface EventExceptionStamp extends BaseEventStamp{

    /**
     * Returns the exception event extracted from the underlying
     * icalendar object. Changes to the exception event will be persisted
     * when the stamp is saved.
     */
    public VEvent getExceptionEvent();

    /**
     * Sets the exception event.
     * @param event exception event
     */
    public void setExceptionEvent(VEvent event);

    /**
     * Get the EventStamp from the parent NoteItem
     * @return EventStamp of parent NoteItem
     */
    public EventStamp getMasterStamp();

}