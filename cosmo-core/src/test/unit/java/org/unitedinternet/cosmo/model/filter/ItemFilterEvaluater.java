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
package org.unitedinternet.cosmo.model.filter;

import java.util.Date;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;

import org.unitedinternet.cosmo.calendar.query.CalendarFilter;
import org.unitedinternet.cosmo.calendar.query.CalendarFilterEvaluater;
import org.unitedinternet.cosmo.calendar.query.ComponentFilter;
import org.unitedinternet.cosmo.calendar.query.TimeRangeFilter;
import org.unitedinternet.cosmo.model.Attribute;
import org.unitedinternet.cosmo.model.BaseEventStamp;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.Stamp;
import org.unitedinternet.cosmo.model.TextAttribute;
import org.unitedinternet.cosmo.model.hibernate.EntityConverter;

/**
 * Provides api for determining if item matches given filter.
 */
public class ItemFilterEvaluater {
    
    /**
     * Evaluate.
     * @param item The item.
     * @param filter The item filter.
     * @return boolean.
     */
    public boolean evaulate(Item item, ItemFilter filter) {
        
        if(item==null || filter==null) {
            return false;
        }
        
        if(filter.getParent()!=null) {
            if(!item.getParents().contains(filter.getParent())) {
                return false;
            }
        }
        
        if(filter instanceof NoteItemFilter) { 
            if(!handleNoteItemFilter((NoteItemFilter) filter, item)) {
                return false;
            }
        }
        
        if(filter instanceof ContentItemFilter) {
            if(!handleContentItemFilter((ContentItemFilter) filter, item)) {
                return false;
            }
        }
    
        if(filter.getDisplayName()!=null) {
            if(!handleFilterCriteria(item.getDisplayName(), filter.getDisplayName())) {
                return false;
            }
        }
        
        if(filter.getUid()!=null) {
            if(!handleFilterCriteria(item.getUid(), filter.getUid())) {
                return false;
            }
        }
        
        for(AttributeFilter af: filter.getAttributeFilters()) {
            if(!handleAttributeFilter(item, af)) {
                return false;
            }
        }
        
        for(StampFilter sf: filter.getStampFilters()) {
            if(!handleStampFilter(item, sf)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Handle content item filter.
     * @param filter The content item filter.
     * @param item The item.
     * @return boolean.
     */
    private boolean handleContentItemFilter(ContentItemFilter filter, Item item) {
        if (! (item instanceof ContentItem)) {
            return false;
        }
        
        ContentItem content = (ContentItem) item;
        
        if (filter.getTriageStatusCode() != null) {
            if(!handleFilterCriteria(content.getTriageStatus()==null ? 
                    null : content.getTriageStatus().getCode(),  filter.getTriageStatusCode())) {
                return false;
            }
        }
        
        return true;
            
    }
    
    /**
     * Handle note item filter.
     * @param filter The note item filter.
     * @param item The item.
     * @return boolean.
     */
    private boolean handleNoteItemFilter(NoteItemFilter filter, Item item) {
        if (! (item instanceof NoteItem)) {
            return false;
        }
        
        NoteItem note = (NoteItem) item;
        
        if (filter.getMasterNoteItem()!=null) {
            if (note.getModifies()!=null && !note.getModifies().equals(filter.getMasterNoteItem())) {
                return false;
            }
            if (!note.equals(filter.getMasterNoteItem())) {
                return false;
            }
        }
        
        if (filter.getIsModification()!=null) {
            if (filter.getIsModification()==true && note.getModifies()==null) {
                return false;
            }
            else if (note.getModifies()!=null) {
                return false;     
            }
        }
        
        if (filter.getHasModifications()!=null) {
            if (filter.getHasModifications()==true && note.getModifications().size()==0) {
                return false;
            }
            else if (note.getModifications().size()>0) {
                return false;
            }
        }
        
        if (filter.getIcalUid()!=null) {
            if(!handleFilterCriteria(note.getIcalUid(), filter.getIcalUid())) {
                return false;
            }
        }
        
        if (filter.getBody() != null) {
            if (!handleFilterCriteria(note.getBody(), filter.getBody())) {
                return false;
            }
        }
        
        if (filter.getReminderTime() != null) {
            if (!handleFilterCriteria(note.getReminderTime(), filter.getReminderTime())) {
                return false;
            }
        }
        
        return true;
            
    }
    
    /**
     * Tests filter criteria.
     * @param val The value.
     * @param criteria The filter criteria.
     * @return boolean.
     */
    private boolean handleFilterCriteria(Object val, FilterCriteria criteria) {
       
        if (criteria instanceof EqualsExpression) {
            EqualsExpression exp = (EqualsExpression) criteria;
            if (val == null) {
                return false;
            }
            if (exp.isNegated() && val.equals(exp.getValue())) {
                return false;
            }
            else if (!val.equals(exp.getValue())) {
                return false;
            }
        }
        
        if(criteria instanceof BetweenExpression) {
            BetweenExpression exp = (BetweenExpression) criteria;
            if (val == null) {
                return false;
            }
            if (val instanceof Date) {
                Date date = (Date) val;
                boolean between = !(date.before((Date) exp.getValue1()) || date.after((Date) exp.getValue2()));
                if (exp.isNegated() && between) {
                    return false;
                }
                else {
                    return between;
                }
            }
        }
        
        if (criteria instanceof NullExpression) {
            NullExpression exp = (NullExpression) criteria;
            if (exp.isNegated() && val == null) {
                return false;
            }
            else if (val != null) {
                return false;
            }
        }
        
        if(criteria instanceof LikeExpression) {
            LikeExpression exp = (LikeExpression) criteria;
            if (val == null) {
                return false;
            }
            if (exp.isNegated() && ((String) val).contains((String) exp.getValue())) {
                return false;
            }
            else if (!((String) val).contains((String) exp.getValue())) {
                return false;
            }
        }
        
        if (criteria instanceof ILikeExpression) {
            ILikeExpression exp = (ILikeExpression) criteria;
            if (val == null) {
                return false;
            }
            
            String strVal = ((String) val).toLowerCase();
            String compareVal = ((String) exp.getValue()).toLowerCase();
            
            if (exp.isNegated() && strVal.contains(compareVal)) {
                return false;
            }
            else if(!strVal.contains(compareVal)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Handle attribute filter.
     * @param item The item.
     * @param af The attribute filter.
     * @return boolean.
     */
    private boolean handleAttributeFilter(Item item, AttributeFilter af) {
        Attribute a = item.getAttribute(af.getQname());
        if (af.isMissing() && a != null) {
            return false;
        }
        else if(af.isMissing() && a==null) {
            return true;
        }
        else if(a==null ) {
            return false;
        }
        
        if (af instanceof TextAttributeFilter) {
            if (!handleTextAttributeFilter(a, (TextAttributeFilter) af)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Handle text attribute filter.
     * @param a The attribute.
     * @param taf The text attribute filter.
     * @return boolean.
     */
    private boolean handleTextAttributeFilter(Attribute a, TextAttributeFilter taf) {
        if(! (a instanceof TextAttribute)) {
            return false;
        }
        
        TextAttribute ta = (TextAttribute) a;
        
        if (!handleFilterCriteria(ta.getValue(), taf.getValue())) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Handle stamp filter.
     * @param item The item.
     * @param sf The stamp filter.
     * @return boolean.
     */
    private boolean handleStampFilter(Item item, StampFilter sf) {
        Stamp s = item.getStamp(sf.getStampClass());
        if (sf.isMissing() && s!=null) {
            return false;
        }
        else if(sf.isMissing() && s==null) {
            return true;
        }
        else if(s==null ) {
            return false;
        }
        
        if(sf instanceof EventStampFilter) {
            if(!handleEventStampFilter(s, (EventStampFilter) sf)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * handle event stamp filter.
     * @param s The stamp.
     * @param esf The event stamp filter.
     * @return boolean.
     */
    private boolean handleEventStampFilter(Stamp s, EventStampFilter esf) {
        
        BaseEventStamp es = (BaseEventStamp) s;
        
        // check recurring
        if(esf.getIsRecurring()!=null) {
            if(esf.getIsRecurring().booleanValue() && !es.isRecurring()) {
                return false;
            }
            else if(!esf.getIsRecurring().booleanValue() && es.isRecurring()) {
                return false;
            }
        }   
        
        Calendar cal = new EntityConverter(null).convertNote((NoteItem) s.getItem());
        CalendarFilter cf = getCalendarFilter(esf);
        CalendarFilterEvaluater cfe = new CalendarFilterEvaluater();
        
        if(cfe.evaluate(cal, cf)==false) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Gets calendar filter.
     * @param esf event stamp filter.
     * @return calendar filter.
     */
    private CalendarFilter getCalendarFilter(EventStampFilter esf) {
        ComponentFilter eventFilter = new ComponentFilter(Component.VEVENT);
        eventFilter.setTimeRangeFilter(new TimeRangeFilter(esf.getPeriod().getStart(), esf.getPeriod().getEnd()));
        if (esf.getTimezone() != null) {
            eventFilter.getTimeRangeFilter().setTimezone(esf.getTimezone().getVTimeZone());
        }

        ComponentFilter calFilter = new ComponentFilter(
                net.fortuna.ical4j.model.Calendar.VCALENDAR);
        calFilter.getComponentFilters().add(eventFilter);

        CalendarFilter filter = new CalendarFilter();
        filter.setFilter(calFilter);
        
        return filter;
    }
}
