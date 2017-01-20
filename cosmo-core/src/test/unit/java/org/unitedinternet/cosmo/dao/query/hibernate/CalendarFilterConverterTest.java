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
package org.unitedinternet.cosmo.dao.query.hibernate;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;

import org.junit.Assert;
import org.junit.Test;
import org.unitedinternet.cosmo.calendar.query.CalendarFilter;
import org.unitedinternet.cosmo.calendar.query.ComponentFilter;
import org.unitedinternet.cosmo.calendar.query.PropertyFilter;
import org.unitedinternet.cosmo.calendar.query.TextMatchFilter;
import org.unitedinternet.cosmo.calendar.query.TimeRangeFilter;
import org.unitedinternet.cosmo.dao.query.hibernate.CalendarFilterConverter;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.filter.EventStampFilter;
import org.unitedinternet.cosmo.model.filter.FilterCriteria;
import org.unitedinternet.cosmo.model.filter.FilterExpression;
import org.unitedinternet.cosmo.model.filter.ILikeExpression;
import org.unitedinternet.cosmo.model.filter.ItemFilter;
import org.unitedinternet.cosmo.model.filter.LikeExpression;
import org.unitedinternet.cosmo.model.filter.NoteItemFilter;
import org.unitedinternet.cosmo.model.filter.StampFilter;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;


/**
 * Test CalendarFilterConverter.
 */
public class CalendarFilterConverterTest {

    CalendarFilterConverter converter = new CalendarFilterConverter();
    
    /**
     * Constructor.
     */
    public CalendarFilterConverterTest() {
        super();
    }

    /**
     * Tests translate item to filter.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testTranslateItemToFilter() throws Exception {
        CollectionItem calendar = new HibCollectionItem();
        calendar.setUid("calendar");
        CalendarFilter calFilter = new CalendarFilter();
        ComponentFilter rootComp = new ComponentFilter();
        rootComp.setName("VCALENDAR");
        calFilter.setFilter(rootComp);
        ComponentFilter eventComp = new ComponentFilter();
        eventComp.setName("VEVENT");
        rootComp.getComponentFilters().add(eventComp);
        
        Period period = new Period(new DateTime("20070101T100000Z"), new DateTime("20070201T100000Z"));
        TimeRangeFilter timeRangeFilter = new TimeRangeFilter(period);
        eventComp.setTimeRangeFilter(timeRangeFilter);
        
        PropertyFilter uidFilter = new PropertyFilter();
        uidFilter.setName("UID");
        TextMatchFilter uidMatch = new TextMatchFilter();
        uidMatch.setValue("uid");
        uidMatch.setCaseless(false);
        uidFilter.setTextMatchFilter(uidMatch);
        eventComp.getPropFilters().add(uidFilter);
        
        PropertyFilter summaryFilter = new PropertyFilter();
        summaryFilter.setName("SUMMARY");
        TextMatchFilter summaryMatch = new TextMatchFilter();
        summaryMatch.setValue("summary");
        summaryMatch.setCaseless(false);
        summaryFilter.setTextMatchFilter(summaryMatch);
        eventComp.getPropFilters().add(summaryFilter);
        
        PropertyFilter descFilter = new PropertyFilter();
        descFilter.setName("DESCRIPTION");
        TextMatchFilter descMatch = new TextMatchFilter();
        descMatch.setValue("desc");
        descMatch.setCaseless(true);
        descFilter.setTextMatchFilter(descMatch);
        eventComp.getPropFilters().add(descFilter);
        
        ItemFilter itemFilter = converter.translateToItemFilter(calendar, calFilter);
        
        Assert.assertTrue(itemFilter instanceof NoteItemFilter);
        NoteItemFilter noteFilter = (NoteItemFilter) itemFilter;
        Assert.assertEquals(calendar.getUid(), noteFilter.getParent().getUid());
        Assert.assertTrue(noteFilter.getDisplayName() instanceof LikeExpression);
        verifyFilterExpressionValue(noteFilter.getDisplayName(), "summary");
        Assert.assertTrue(noteFilter.getIcalUid() instanceof LikeExpression);
        verifyFilterExpressionValue(noteFilter.getIcalUid(), "uid");
        Assert.assertTrue(noteFilter.getBody() instanceof ILikeExpression);
        verifyFilterExpressionValue(noteFilter.getBody(), "desc");
       
        EventStampFilter sf = (EventStampFilter) noteFilter.getStampFilter(EventStampFilter.class);
        Assert.assertNotNull(sf);
        Assert.assertNotNull(sf.getPeriod());
        Assert.assertEquals(sf.getPeriod().getStart().toString(), "20070101T100000Z");
        Assert.assertEquals(sf.getPeriod().getEnd().toString(), "20070201T100000Z");
    }
    
    /**
     * Tests get first pass filter.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testGetFirstPassFilter() throws Exception {
        CollectionItem calendar = new HibCollectionItem();
        calendar.setUid("calendar");
        CalendarFilter calFilter = new CalendarFilter();
        ComponentFilter rootComp = new ComponentFilter();
        rootComp.setName("VCALENDAR");
        calFilter.setFilter(rootComp);
        ComponentFilter taskComp = new ComponentFilter();
        taskComp.setName("VTODO");
        rootComp.getComponentFilters().add(taskComp);
        
        try {
            converter.translateToItemFilter(calendar, calFilter);
            Assert.fail("shouldn't get here");
        } catch(IllegalArgumentException e) {}
        
        
        ItemFilter itemFilter = converter.getFirstPassFilter(calendar, calFilter);
        Assert.assertNotNull(itemFilter);
        Assert.assertTrue(itemFilter instanceof NoteItemFilter);
        NoteItemFilter noteFilter = (NoteItemFilter) itemFilter;
      
        Assert.assertFalse(noteFilter.getIsModification().booleanValue());
        Assert.assertEquals(1, noteFilter.getStampFilters().size());
        
        StampFilter sf = noteFilter.getStampFilters().get(0);
        Assert.assertEquals(EventStamp.class, sf.getStampClass());
        Assert.assertEquals(true, sf.isMissing());
    }
    
    /**
     * Verify filter expression value.
     * @param fc Filter criteria.
     * @param value The value.
     */
    private void verifyFilterExpressionValue(FilterCriteria fc, Object value) {
        FilterExpression fe = (FilterExpression) fc;
        Assert.assertTrue(fe.getValue().equals(value));
    }

}
