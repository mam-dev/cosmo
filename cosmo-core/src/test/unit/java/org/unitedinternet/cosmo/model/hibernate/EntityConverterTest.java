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
package org.unitedinternet.cosmo.model.hibernate;


import java.io.FileInputStream;
import java.time.Duration;
import java.util.Iterator;
import java.util.Set;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.unitedinternet.cosmo.calendar.ICalendarUtils;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.EventExceptionStamp;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.ICalendarItem;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.StampUtils;
import org.unitedinternet.cosmo.model.TriageStatus;
import org.unitedinternet.cosmo.model.TriageStatusUtil;
import org.unitedinternet.cosmo.model.mock.MockCalendarCollectionStamp;
import org.unitedinternet.cosmo.model.mock.MockCollectionItem;
import org.unitedinternet.cosmo.model.mock.MockEntityFactory;
import org.unitedinternet.cosmo.model.mock.MockEventExceptionStamp;
import org.unitedinternet.cosmo.model.mock.MockEventStamp;
import org.unitedinternet.cosmo.model.mock.MockNoteItem;
import org.unitedinternet.cosmo.model.mock.MockTaskStamp;
import org.unitedinternet.cosmo.model.mock.MockTriageStatus;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.Completed;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.RDate;
import net.fortuna.ical4j.model.property.RecurrenceId;
import net.fortuna.ical4j.model.property.Status;
import static org.unitedinternet.cosmo.calendar.ICalendarUtils.createBaseCalendar;


/**
 * Test EntityConverter.
 *
 */
public class EntityConverterTest {
    protected String baseDir = "src/test/unit/resources/testdata/entityconverter/";
    private static final TimeZoneRegistry TIMEZONE_REGISTRY =
        TimeZoneRegistryFactory.getInstance().createRegistry();
    
    
    protected EntityFactory entityFactory = new MockEntityFactory();
    protected EntityConverter converter = new EntityConverter(entityFactory);
    
    /**
     * Tests entity convertor task.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testEntityConverterTask() throws Exception {
        Calendar calendar = getCalendar("vtodo.ics");
        
        NoteItem note = converter.convertTaskCalendar(calendar);
        
        assertTrue(TriageStatus.CODE_NOW==note.getTriageStatus().getCode());
        
        // add COMPLETED
        DateTime completeDate = new DateTime("20080122T100000Z");
        
        VToDo vtodo = (VToDo) calendar.getComponents(Component.VTODO).get(0);
        ICalendarUtils.setCompleted(completeDate, vtodo);
        note = converter.convertTaskCalendar(calendar);
        
        TriageStatus ts = note.getTriageStatus();
        assertTrue(TriageStatus.CODE_DONE==ts.getCode());
        assertTrue(TriageStatusUtil.getDateFromRank(ts.getRank()).getTime()==completeDate.getTime());
    
        note.setTriageStatus(null);
        ICalendarUtils.setCompleted(null, vtodo);
        assertNull(vtodo.getDateCompleted());
        ICalendarUtils.setStatus(Status.VTODO_COMPLETED, vtodo);
        
        // verify that TriageStatus.rank is set ot current time when 
        // STATUS:COMPLETED is present and COMPLETED is not present
        long begin = (System.currentTimeMillis() / 1000) * 1000;
        note = converter.convertTaskCalendar(calendar);
        long end = (System.currentTimeMillis() / 1000) * 1000;
        ts = note.getTriageStatus();
        assertTrue(TriageStatus.CODE_DONE==ts.getCode());
        long rankTime = TriageStatusUtil.getDateFromRank(ts.getRank()).getTime();
        assertTrue(rankTime<=end && rankTime>=begin);
    }
    
    /**
     * Tests entity converter event.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testEntityConverterEvent() throws Exception {
        
        Calendar calendar = getCalendar("event_with_exception.ics");
        NoteItem master = entityFactory.createNote();
        Set<NoteItem> items = converter.convertEventCalendar(master, calendar);
        
        // should be master and mod
        assertEquals(2, items.size());
        
        // get master
        Iterator<NoteItem> it = items.iterator();
        master = it.next();
        
        // check ical props
        // DTSTART
//      This fails, which is pretty strange
//        EventStamp masterEvent = StampUtils.getEventStamp(master); 
//        assertNotNull(masterEvent);
//        DateTime testStart = new DateTime("20060102T140000", TimeZoneUtils.getTimeZone("US/Eastern_mod"));
//        assertTrue(masterEvent.getStartDate().equals(testStart));
        // Triage status
        TriageStatus ts = master.getTriageStatus();
        // the event is in the past, it should be DONE
        assertTrue(TriageStatus.CODE_DONE==ts.getCode());
        // DTSTAMP
        assertEquals(master.getClientModifiedDate(), new DateTime("20051222T210507Z").getTime());
        // UID
        assertEquals(master.getIcalUid(), "F5B811E00073B22BA6B87551@ninevah.local");
        // SUMMARY
        assertEquals(master.getDisplayName(), "event 6");
        
        // get mod
        NoteItem mod = it.next();
        
        ModificationUidImpl uid = new ModificationUidImpl(mod.getUid());
        
        assertEquals(master.getUid(), uid.getParentUid());
        assertEquals("20060104T190000Z", uid.getRecurrenceId().toString());
        
        assertTrue(mod.getModifies()==master);
        EventExceptionStamp ees = StampUtils.getEventExceptionStamp(mod);
        assertNotNull(ees);
        
        // mod should include VTIMEZONES
        Calendar eventCal = ees.getEventCalendar();
        ComponentList<VTimeZone> vtimezones = eventCal.getComponents(Component.VTIMEZONE);
        assertEquals(1, vtimezones.size());
        
        
        // update event (change mod and add mod)
        calendar = getCalendar("event_with_exception2.ics");
        items = converter.convertEventCalendar(master, calendar);
        
        // should be master and 2 mods
        assertEquals(3, items.size());
        
        mod = findModByRecurrenceIdForNoteItems(items, "20060104T190000Z");
        assertNotNull(mod);
        assertEquals("event 6 mod 1 changed", mod.getDisplayName());
        
        mod = findModByRecurrenceIdForNoteItems(items, "20060105T190000Z");
        assertNotNull(mod);
        assertEquals("event 6 mod 2", mod.getDisplayName());
        
        // update event again (remove mod)
        calendar = getCalendar("event_with_exception3.ics");
        items = converter.convertEventCalendar(master, calendar);
        
        // should be master and 1 active mod/ 1 deleted mod
        assertEquals(3, items.size());
        
        mod = findModByRecurrenceIdForNoteItems(items, "20060104T190000Z");
        assertNotNull(mod);
        assertFalse(mod.getIsActive().booleanValue());
        
        mod = findModByRecurrenceIdForNoteItems(items, "20060105T190000Z");
        assertNotNull(mod);
        assertEquals("event 6 mod 2 changed", mod.getDisplayName());
        
    }
    
    /**
     * Tests entity converter multi component calendar.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testEntityConverterMultiComponentCalendar() throws Exception {
       
        // test converting calendar with many different components
        // into ICalendarItems
        
        Calendar calendar = getCalendar("bigcalendar.ics");
        @SuppressWarnings("unused")
		NoteItem master = entityFactory.createNote();
        Set<ICalendarItem> items = converter.convertCalendar(calendar);
        
        // should be 8
        assertEquals(8, items.size());
        
        ICalendarItem item = findItemByIcalUid(items, "8qv7nuaq50vk3r98tvj37vjueg@google.com" );
        assertNotNull(item);
        assertTrue(item instanceof NoteItem);
        assertNotNull(StampUtils.getEventStamp(item));
        
        
        item = findItemByIcalUid(items, "e3i849b29kd3fbp48hmkmgjst0@google.com" );
        assertNotNull(item);
        assertTrue(item instanceof NoteItem);
        assertNotNull(StampUtils.getEventStamp(item));
        
        
        item = findItemByIcalUid(items, "4csitoh29h1arc46bnchg19oc8@google.com" );
        assertNotNull(item);
        assertTrue(item instanceof NoteItem);
        assertNotNull(StampUtils.getEventStamp(item));
        
        
        item = findItemByIcalUid(items, "f920n2rdb0qdd6grkjh4m4jrq0@google.com" );
        assertNotNull(item);
        assertTrue(item instanceof NoteItem);
        assertNotNull(StampUtils.getEventStamp(item));
        
        
        item = findItemByIcalUid(items, "jev0phs8mnfkuvoscrra1fh8j0@google.com" );
        assertNotNull(item);
        assertTrue(item instanceof NoteItem);
        assertNotNull(StampUtils.getEventStamp(item));
        
        item = findModByRecurrenceId(items, "20071129T203000Z" );
        assertNotNull(item);
        assertTrue(item instanceof NoteItem);
        assertNotNull(StampUtils.getEventExceptionStamp(item));
        
        item = findItemByIcalUid(items, "19970901T130000Z-123404@host.com" );
        assertNotNull(item);
        assertTrue(item instanceof NoteItem);
       
        item = findItemByIcalUid(items, "19970901T130000Z-123405@host.com" );
        assertNotNull(item);
        assertTrue(item instanceof NoteItem);
        assertEquals(0, item.getStamps().size());
        
    }
    
    /**
     * Tests get calendar from collection.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testGetCalendarFromCollection() throws Exception {
        
        Calendar c1 = getCalendar("eventwithtimezone1.ics");
        Calendar c2 = getCalendar("vtodo.ics");
        NoteItem note1 = converter.convertEventCalendar(c1).iterator().next();
        NoteItem note2 = converter.convertTaskCalendar(c2);
       
        MockCollectionItem collection = new MockCollectionItem();
        collection.addStamp(new MockCalendarCollectionStamp(collection));
        collection.addChild(note1);
        collection.addChild(note2);
        
        Calendar fullCal = converter.convertCollection(collection);
        fullCal.validate();
        assertNotNull(fullCal);
        
        // VTIMEZONE, VTODO, VEVENT
        assertEquals(3,fullCal.getComponents().size());
        assertEquals(1, fullCal.getComponents(Component.VTIMEZONE).size());
        assertEquals(1, fullCal.getComponents(Component.VEVENT).size());
        assertEquals(1, fullCal.getComponents(Component.VTODO).size());
    }
    
    /**
     * Tests convert task.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testConvertTask() throws Exception {
        @SuppressWarnings("unused")
		TimeZoneRegistry registry =
            TimeZoneRegistryFactory.getInstance().createRegistry();
        NoteItem master = new MockNoteItem();
        master.setDisplayName("displayName");
        master.setBody("body");
        master.setIcalUid("icaluid");
        master.setClientModifiedDate(new DateTime("20070101T100000Z").getTime());
        master.setTriageStatus(TriageStatusUtil.initialize(new MockTriageStatus()));
        
        Calendar cal = converter.convertNote(master);
        cal.validate();
        
        assertEquals(1, cal.getComponents().size());
        
        ComponentList<VToDo> comps = cal.getComponents(Component.VTODO);
        assertEquals(1, comps.size());
        VToDo task = comps.get(0);
        
        assertNull(task.getDateCompleted());
        assertNull(ICalendarUtils.getXProperty("X-OSAF-STARRED", task));
        
        DateTime completeDate = new DateTime("20080122T100000Z");
        
        master.getTriageStatus().setCode(TriageStatus.CODE_DONE);
        master.getTriageStatus().setRank(TriageStatusUtil.getRank(completeDate.getTime()));
        master.addStamp(new MockTaskStamp());
        
        cal = converter.convertNote(master);
        task = (VToDo) cal.getComponents().get(0);
        
        Completed completed = task.getDateCompleted();
        assertNotNull(completed);
        assertEquals(completeDate.getTime(), completed.getDate().getTime());
        assertEquals("TRUE", ICalendarUtils.getXProperty("X-OSAF-STARRED", task));
        
    }
    
    /**
     * Tests convert event.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testConvertEvent() throws Exception {
        TimeZoneRegistry registry =
            TimeZoneRegistryFactory.getInstance().createRegistry();
        NoteItem master = new MockNoteItem();
        master.setDisplayName("displayName");
        master.setBody("body");
        master.setIcalUid("icaluid");
        master.setClientModifiedDate(new DateTime("20070101T100000Z").getTime());
        EventStamp eventStamp = new HibEventStamp(master);
        
        VEvent vEvent = new VEvent();
        Calendar calendar = createBaseCalendar(vEvent);
        vEvent.getProperties().add(new DtStart(new DateTime("20070212T074500")));
        eventStamp.setEventCalendar(calendar);
        master.addStamp(eventStamp);
        
        Calendar cal = converter.convertNote(master);
        cal.validate();
        
        // date has no timezone, so there should be no timezones
        assertEquals(0, cal.getComponents(Component.VTIMEZONE).size());
      
        vEvent.getProperties().clear();
        vEvent.getProperties().add(new DtStart(new DateTime("20070212T074500",TIMEZONE_REGISTRY.getTimeZone("America/Chicago"))));
        eventStamp.setEventCalendar(calendar);
        
        cal = converter.convertNote(master);
        cal.validate();
        
        // should be a single VEVENT
        ComponentList<VEvent> comps = cal.getComponents(Component.VEVENT);
        assertEquals(1, comps.size());
        VEvent event = (VEvent) comps.get(0);
        
        // test VALUE=DATE-TIME is not present
        assertNull(event.getStartDate().getParameter(Parameter.VALUE));
        
        // test item properties got merged into calendar
        assertEquals("displayName", event.getSummary().getValue());
        assertEquals("body", event.getDescription().getValue());
        assertEquals("icaluid", event.getUid().getValue());
        assertEquals(master.getClientModifiedDate(), event.getDateStamp().getDate().getTime());
         
        // date has timezone, so there should be a timezone
        assertEquals(1, cal.getComponents(Component.VTIMEZONE).size());
        
        vEvent.getProperties().add(new DtEnd(new DateTime("20070212T074500",TIMEZONE_REGISTRY.getTimeZone("America/Los_Angeles"))));
        eventStamp.setEventCalendar(calendar);
        
        
        cal = converter.convertNote(master);
        cal.validate();
        
        // dates have 2 different timezones, so there should be 2 timezones
        assertEquals(2, cal.getComponents(Component.VTIMEZONE).size());
        
        vEvent.getProperties().clear();
        eventStamp.setEventCalendar(calendar);
        cal = converter.convertNote(master);
        cal.validate();
        //No dates, no timezone
        assertEquals(0, cal.getComponents(Component.VTIMEZONE).size());
        
        // add timezones to master event calendar
        calendar.getComponents().add(registry.getTimeZone("America/Chicago").getVTimeZone());
        calendar.getComponents().add(registry.getTimeZone("America/Los_Angeles").getVTimeZone());
        eventStamp.setEventCalendar(calendar);
        
        cal = converter.convertNote(master);
        cal.validate();
        assertEquals(2, cal.getComponents(Component.VTIMEZONE).size());
    }
    
    /**
     * Tests event event modification.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testEventModificationGetCalendar() throws Exception {
        NoteItem master = new MockNoteItem();
        master.setIcalUid("icaluid");
        master.setDisplayName("master displayName");
        master.setBody("master body");
        EventStamp eventStamp = new MockEventStamp(master);
        
        VEvent vEvent = new VEvent();
        Calendar calendar = createBaseCalendar(vEvent);
        vEvent.getProperties().add(new DtStart(new DateTime("20070212T074500")));
        vEvent.getProperties().add(new net.fortuna.ical4j.model.property.Duration(Duration.ofHours(1)));
        vEvent.getProperties().add(new Location("master location"));
        DateList dates = new DateList();
        dates.add(new Date("20070212T074500"));
        dates.add(new Date("20070213T074500"));
        vEvent.getProperties().add(new RDate(dates));
        eventStamp.setEventCalendar(calendar);
        
        master.addStamp(eventStamp);
        
        eventStamp.getEventCalendar().validate();
       
        NoteItem mod = new MockNoteItem();
        mod.setDisplayName("modDisplayName");
        mod.setBody("modBody");
        mod.setModifies(master);
        master.addModification(mod);
        EventExceptionStamp exceptionStamp = new MockEventExceptionStamp(mod);
        mod.addStamp(exceptionStamp);

        VEvent vEventEx = new VEvent();
        vEventEx.getProperties().add(new Location("ex"));
        Calendar calendarEx = createBaseCalendar(vEventEx);        
        vEventEx.getProperties().add(new RecurrenceId(eventStamp.getStartDate()));
        exceptionStamp.setEventCalendar(calendarEx);        
        mod.addStamp(exceptionStamp);
        
        // test modification VEVENT gets added properly
        Calendar cal = converter.convertNote(master);
        ComponentList<VEvent> comps = cal.getComponents(Component.VEVENT);
        assertEquals(2, comps.size());
        @SuppressWarnings("unused")
		VEvent masterEvent = (VEvent) comps.get(0);
        VEvent modEvent = (VEvent) comps.get(1);
        
        // test merged properties
        assertEquals("modDisplayName", modEvent.getSummary().getValue());
        assertEquals("modBody", modEvent.getDescription().getValue());
        assertEquals("icaluid", modEvent.getUid().getValue());
        
        // test duration got added to modfication
        assertNotNull(modEvent.getDuration());
        assertEquals("PT1H", modEvent.getDuration().getDuration().toString());
        
        // test inherited description/location/body
        mod.setDisplayName(null);
        mod.setBody((String) null);
        Location location = exceptionStamp.getEvent().getProperties().getProperty(Property.LOCATION);
        exceptionStamp.getEvent().getProperties().remove(location);
        
        cal = converter.convertNote(master);
        comps = cal.getComponents(Component.VEVENT);
        assertEquals(2, comps.size());
        masterEvent = (VEvent) comps.get(0);
        modEvent = (VEvent) comps.get(1);
        
        assertEquals("master displayName", modEvent.getSummary().getValue());
        assertEquals("master body", modEvent.getDescription().getValue());
        assertEquals("master location", modEvent.getLocation().getValue());
        
    }
    
    /**
     * Finds item by Ical uid.
     * @param items The items.
     * @param icalUid The uid.
     * @return The calendar item.
     */
    private ICalendarItem findItemByIcalUid(Set<ICalendarItem> items, String icalUid) {
        for(ICalendarItem item: items) {
            if(icalUid.equals(item.getIcalUid())) {
                return item;
            }
        }
        return null;
    }
    
    /**
     * Finds mod by recurrence id.
     * @param items The items.
     * @param rid The id.
     * @return The calendar item.
     */
    private ICalendarItem findModByRecurrenceId(Set<ICalendarItem> items, String rid) {
        for(ICalendarItem item: items) {
            if(item instanceof NoteItem) {
                NoteItem note = (NoteItem) item;
                if(note.getModifies()!=null && note.getUid().contains(rid)) {
                    return note;
                }
            }
        }
        return null;
    }
    
    /**
     * Finds mod by recurrence id.
     * @param items The items.
     * @param rid The id.
     * @return The note item.
     */
    private NoteItem findModByRecurrenceIdForNoteItems(Set<NoteItem> items, String rid) {
        for(NoteItem note: items) {
            if(note.getModifies()!=null && note.getUid().contains(rid)) {
                return note;
            }
        }
        
        return null;
    }
    
    /**
     * Gets calendar.
     * @param name The name.
     * @return The calendar.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    protected Calendar getCalendar(String name) throws Exception {
        CalendarBuilder cb = new CalendarBuilder();
        FileInputStream fis = new FileInputStream(baseDir + name);
        Calendar calendar = cb.build(fis);
        return calendar;
    }
    
}
