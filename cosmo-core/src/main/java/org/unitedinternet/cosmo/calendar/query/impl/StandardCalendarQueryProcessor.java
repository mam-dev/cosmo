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
package org.unitedinternet.cosmo.calendar.query.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.unitedinternet.cosmo.calendar.Instance;
import org.unitedinternet.cosmo.calendar.InstanceList;
import org.unitedinternet.cosmo.calendar.query.CalendarFilter;
import org.unitedinternet.cosmo.calendar.query.CalendarFilterEvaluater;
import org.unitedinternet.cosmo.calendar.query.CalendarQueryProcessor;
import org.unitedinternet.cosmo.calendar.query.ComponentFilter;
import org.unitedinternet.cosmo.calendar.query.TimeRangeFilter;
import org.unitedinternet.cosmo.dao.CalendarDao;
import org.unitedinternet.cosmo.dao.ContentDao;
import org.unitedinternet.cosmo.model.CalendarCollectionStamp;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.HomeCollectionItem;
import org.unitedinternet.cosmo.model.ICalendarItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.StampUtils;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.hibernate.EntityConverter;
import org.unitedinternet.cosmo.util.VersionFourGenerator;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VFreeBusy;
import net.fortuna.ical4j.model.parameter.FbType;
import net.fortuna.ical4j.model.property.FreeBusy;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Transp;
import net.fortuna.ical4j.model.property.Uid;

/**
 * CalendarQueryProcessor implementation that uses CalendarDao.
 */
@org.springframework.stereotype.Component
@Transactional(readOnly = true)
public class StandardCalendarQueryProcessor implements CalendarQueryProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(StandardCalendarQueryProcessor.class);

    protected static final VersionFourGenerator UUID_GENERATOR = new VersionFourGenerator();
    
    private CalendarDao calendarDao = null;
    
    private ContentDao contentDao = null;
    
    private EntityConverter entityConverter;
    
    public StandardCalendarQueryProcessor(EntityConverter entityConverter, ContentDao contentDao, CalendarDao calendarDao) {
    	this.calendarDao = calendarDao;
    	this.contentDao = contentDao;
    	this.entityConverter = entityConverter;
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.calendar.query.CalendarQueryProcessor#filterQuery
     * (org.unitedinternet.cosmo.model.CollectionItem, org.unitedinternet.cosmo.calendar.query.CalendarFilter)
     */
    /**
     * Filter query.
     * @param collection The collection item.
     * @param filter The calendar filter.
     * @return The calendar items.
     */
    public Set<ICalendarItem> filterQuery(CollectionItem collection, CalendarFilter filter) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("finding events in collection " + collection.getUid()
                    + " by filter " + filter);
        }

        return new HashSet<ICalendarItem>((Set<ICalendarItem>) calendarDao
                .findCalendarItems(collection, filter));
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.calendar.query.CalendarQueryProcessor#filterQuery
     * (org.unitedinternet.cosmo.model.ICalendarItem, org.unitedinternet.cosmo.calendar.query.CalendarFilter)
     */
    /**
     * Filter query.
     * @param item The ICalendar item.
     * @param filter The calendar filter.
     * @return The calendar filter evaluater.
     */
    public boolean filterQuery(ICalendarItem item, CalendarFilter filter) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("matching item " + item.getUid() + " to filter " + filter);
        }
        
        Calendar calendar = entityConverter.convertContent(item);
        if(calendar!=null) {
            return new CalendarFilterEvaluater().evaluate(calendar, filter);
        }
        else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.calendar.query.CalendarQueryProcessor#freeBusyQuery
     * (org.unitedinternet.cosmo.model.User, net.fortuna.ical4j.model.Period)
     */
    /**
     * VFreeBusy query.
     * @param user The user.
     * @param period The period.
     * @return VFreeBusy. 
     */
    public VFreeBusy freeBusyQuery(User user, Period period) {
        PeriodList busyPeriods = new PeriodList();
        PeriodList busyTentativePeriods = new PeriodList();
        PeriodList busyUnavailablePeriods = new PeriodList();
        
        HomeCollectionItem home = contentDao.getRootItem(user);
        for(Item item: home.getChildren()) {
            if(! (item instanceof CollectionItem)) {
                continue;
            }
            
            CollectionItem collection = (CollectionItem) item;
            if(StampUtils.getCalendarCollectionStamp(collection)==null ||
                    collection.isExcludeFreeBusyRollup()) {
                continue;
            }
            
            doFreeBusyQuery(busyPeriods, busyTentativePeriods, busyUnavailablePeriods,
                    collection, period);  
        }

        return createVFreeBusy(busyPeriods, busyTentativePeriods,
                busyUnavailablePeriods, period);
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.calendar.query.CalendarQueryProcessor#freeBusyQuery
     * (org.unitedinternet.cosmo.model.CollectionItem, net.fortuna.ical4j.model.Period)
     */
    /**
     * Creates FreeBusyQuery.
     * @param collection The collection item.
     * @param period The period.
     * @return Created VFreeBusy.
     */
    public VFreeBusy freeBusyQuery(CollectionItem collection, Period period) {
        PeriodList busyPeriods = new PeriodList();
        PeriodList busyTentativePeriods = new PeriodList();
        PeriodList busyUnavailablePeriods = new PeriodList();
        
        doFreeBusyQuery(busyPeriods, busyTentativePeriods, busyUnavailablePeriods,
                collection, period);

        return createVFreeBusy(busyPeriods, busyTentativePeriods,
                busyUnavailablePeriods, period);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.calendar.query.CalendarQueryProcessor#freeBusyQuery
     * (org.unitedinternet.cosmo.model.ICalendarItem, net.fortuna.ical4j.model.Period)
     */
    /**
     * Creates VFreeBusy query.
     * @param item The ICalendar item.
     * @param period The period.
     * @return The created VFreeBusy query.
     */
    public VFreeBusy freeBusyQuery(ICalendarItem item, Period period) {
        PeriodList busyPeriods = new PeriodList();
        PeriodList busyTentativePeriods = new PeriodList();
        PeriodList busyUnavailablePeriods = new PeriodList();
        
        Calendar calendar = entityConverter.convertContent(item);
        
        // Add busy details from the calendar data
        addBusyPeriods(calendar, null, period, busyPeriods,
                busyTentativePeriods, busyUnavailablePeriods);
        
        return createVFreeBusy(busyPeriods, busyTentativePeriods,
                busyUnavailablePeriods, period);
    }
    
    /**
     * Adds The FreeBusy query.
     * @param busyPeriods The period list.
     * @param busyTentativePeriods The period list.
     * @param busyUnavailablePeriods The busy unavailable periods periods.
     * @param collection The collecton item.
     * @param period The period.
     */
    protected void doFreeBusyQuery(PeriodList busyPeriods,
            PeriodList busyTentativePeriods, PeriodList busyUnavailablePeriods,
            CollectionItem collection, Period period) {

        CalendarCollectionStamp ccs = StampUtils.getCalendarCollectionStamp(collection);
        if(ccs==null) {
            return;
        }
        
        HashSet<ContentItem> results = new HashSet<ContentItem>();
        TimeZone tz = ccs.getTimezone();
        
        // For the time being, use CalendarFilters to get relevant
        // items.
        CalendarFilter[] filters = createQueryFilters(collection, period);
        for(CalendarFilter filter: filters) {
            results.addAll(calendarDao.findCalendarItems(collection, filter));
        }
        
        for(ContentItem content: results) {
            Calendar calendar = entityConverter.convertContent(content);
            if(calendar==null) {
                continue;
            }
            // Add busy details from the calendar data
            addBusyPeriods(calendar, tz, period, busyPeriods,
                    busyTentativePeriods, busyUnavailablePeriods);
        }
    }
    
    /**
     * Adds relevant periods.
     * @param calendar The calendar.
     * @param timezone The timezone.
     * @param freeBusyRange 
     * @param busyPeriods
     * @param busyTentativePeriods
     * @param busyUnavailablePeriods
     */
    protected void addBusyPeriods(Calendar calendar, TimeZone timezone,
            Period freeBusyRange, PeriodList busyPeriods,
            PeriodList busyTentativePeriods, PeriodList busyUnavailablePeriods) {
        
        // Create list of instances within the specified time-range
        InstanceList instances = new InstanceList();
        instances.setUTC(true);
        instances.setTimezone(timezone);

        // Look at each VEVENT/VFREEBUSY component only
        ComponentList<Component> overrides = new ComponentList<>();
        for (Object comp: calendar.getComponents()) {
            if (comp instanceof VEvent) {
                VEvent vcomp = (VEvent) comp;
                // See if this is the master instance
                if (vcomp.getRecurrenceId() == null) {
                    instances.addComponent(vcomp, freeBusyRange.getStart(),
                            freeBusyRange.getEnd());
                } else {
                    overrides.add(vcomp);
                }
            } else if (comp instanceof VFreeBusy) {
                // Add all FREEBUSY BUSY/BUSY-TENTATIVE/BUSY-UNAVAILABLE to the periods
                List<FreeBusy> fbs = ((Component)comp).getProperties().getProperties(Property.FREEBUSY);
                for (FreeBusy fb : fbs) {                    
                    FbType fbt = (FbType) fb.getParameters().getParameter(
                            Parameter.FBTYPE);
                    if (fbt == null || FbType.BUSY.equals(fbt)) {
                        addRelevantPeriods(busyPeriods, fb.getPeriods(),
                                freeBusyRange);
                    } else if (FbType.BUSY_TENTATIVE.equals(fbt)) {
                        addRelevantPeriods(busyTentativePeriods, fb
                                .getPeriods(), freeBusyRange);
                    } else if (FbType.BUSY_UNAVAILABLE.equals(fbt)) {
                        addRelevantPeriods(busyUnavailablePeriods, fb
                                .getPeriods(), freeBusyRange);
                    }
                }
            }
        }

        for (Iterator<Component> i = overrides.iterator(); i.hasNext();) {
            Component comp = i.next();
            instances.addComponent(comp, freeBusyRange.getStart(),
                    freeBusyRange.getEnd());
        }

        // See if there is nothing to do (should not really happen)
        if (instances.size() == 0) {
            return;
        }

        // Add start/end period for each instance
        for (Iterator<Entry<String, Instance>> i = instances.entrySet().iterator(); i.hasNext();) {
            Map.Entry<?, ?> entry = i.next();
            
            Object instanceObj = entry.getValue();
            
            if(!(instanceObj instanceof Instance)){
                continue;
            }
            Instance instance = (Instance) instanceObj;
            
            // Check that the VEVENT has the proper busy status
            if (Transp.TRANSPARENT.equals(instance.getComp().getProperties()
                    .getProperty(Property.TRANSP))) {
                continue;
            }
            if (Status.VEVENT_CANCELLED.equals(instance.getComp()
                    .getProperties().getProperty(Property.STATUS))) {
                continue;
            }

            // Can only have DATE-TIME values in PERIODs
            Date start = null;
            Date end = null;
            
            start =  instance.getStart();
            end =  instance.getEnd();
           
            if (start.compareTo(freeBusyRange.getStart()) < 0) {
                start = (DateTime) org.unitedinternet.cosmo.calendar.util.Dates.getInstance(freeBusyRange
                        .getStart(), start);
            }
            if (end.compareTo(freeBusyRange.getEnd()) > 0) {
                end = (DateTime) org.unitedinternet.cosmo.calendar.util.Dates.getInstance(freeBusyRange.getEnd(),
                        end);
            }
            DateTime dtStart = new DateTime(start);
            DateTime dtEnd = new DateTime(end);
            if (Status.VEVENT_TENTATIVE.equals(instance.getComp()
                    .getProperties().getProperty(Property.STATUS))) {
                busyTentativePeriods.add(new Period(dtStart, dtEnd));
            } else {
                busyPeriods.add(new Period(dtStart, dtEnd));
            }
            
        }
    }
    
    /**
     * Add all periods that intersect a given period to the result PeriodList.
     */
    private void addRelevantPeriods(PeriodList results, PeriodList periods,
            Period range) {

        for (Iterator<Period> it = periods.iterator(); it.hasNext();) {
            Period p = it.next();
            if (p.intersects(range))
                results.add(p);
        }
    }
    
    private CalendarFilter[] createQueryFilters(CollectionItem collection, Period period) {
        DateTime start = period.getStart();
        DateTime end = period.getEnd();
        CalendarFilter[] filters = new CalendarFilter[2];
        TimeZone tz = null;

        // Create calendar-filter elements designed to match
        // VEVENTs/VFREEBUSYs within the specified time range.
        //
        // <C:filter>
        // <C:comp-filter name="VCALENDAR">
        // <C:comp-filter name="VEVENT">
        // <C:time-range start="20051124T000000Z"
        // end="20051125T000000Z"/>
        // </C:comp-filter>
        // <C:comp-filter name="VFREEBUSY">
        // <C:time-range start="20051124T000000Z"
        // end="20051125T000000Z"/>
        // </C:comp-filter>
        // </C:comp-filter>
        // </C:filter>

        // If the calendar collection has a timezone attribute,
        // then use that to convert floating date/times to UTC
        CalendarCollectionStamp ccs = StampUtils.getCalendarCollectionStamp(collection);
        if (ccs!=null) {
            tz = ccs.getTimezone();
        }

        ComponentFilter eventFilter = new ComponentFilter(Component.VEVENT);
        eventFilter.setTimeRangeFilter(new TimeRangeFilter(start, end));
        if(tz!=null) {
            eventFilter.getTimeRangeFilter().setTimezone(tz.getVTimeZone());
        }

        ComponentFilter calFilter = new ComponentFilter(Calendar.VCALENDAR);
        calFilter.getComponentFilters().add(eventFilter);

        CalendarFilter filter = new CalendarFilter();
        filter.setFilter(calFilter);

        filters[0] = filter;

        ComponentFilter freebusyFilter = new ComponentFilter(
                Component.VFREEBUSY);
        freebusyFilter.setTimeRangeFilter(new TimeRangeFilter(start, end));
        if(tz!=null) {
            freebusyFilter.getTimeRangeFilter().setTimezone(tz.getVTimeZone());
        }

        calFilter = new ComponentFilter(Calendar.VCALENDAR);
        calFilter.getComponentFilters().add(freebusyFilter);

        filter = new CalendarFilter();
        filter.setFilter(calFilter);

        filters[1] = filter;

        return filters;
    }
    
    /**
     * Creates VFreeBusy.
     * @param busyPeriods
     * @param busyTentativePeriods
     * @param busyUnavailablePeriods
     * @param period
     * @return
     */
    protected VFreeBusy createVFreeBusy(PeriodList busyPeriods,
            PeriodList busyTentativePeriods, PeriodList busyUnavailablePeriods,
            Period period) {
        // Merge periods
        busyPeriods = busyPeriods.normalise();
        busyTentativePeriods = busyTentativePeriods.normalise();
        busyUnavailablePeriods = busyUnavailablePeriods.normalise();

        // Now create a VFREEBUSY
        VFreeBusy vfb = new VFreeBusy(period.getStart(), period.getEnd());
        String uid = UUID_GENERATOR.nextStringIdentifier();
        vfb.getProperties().add(new Uid(uid));

        // Add all periods to the VFREEBUSY
        if (busyPeriods.size() != 0) {
            FreeBusy fb = new FreeBusy(busyPeriods);
            fb.getParameters().add(FbType.BUSY);
            vfb.getProperties().add(fb);
        }
        if (busyTentativePeriods.size() != 0) {
            FreeBusy fb = new FreeBusy(busyTentativePeriods);
            fb.getParameters().add(FbType.BUSY_TENTATIVE);
            vfb.getProperties().add(fb);
        }
        if (busyUnavailablePeriods.size() != 0) {
            FreeBusy fb = new FreeBusy(busyUnavailablePeriods);
            fb.getParameters().add(FbType.BUSY_UNAVAILABLE);
            vfb.getProperties().add(fb);
        }

        return vfb;
    }

    public void setCalendarDao(CalendarDao calendarDao) {
        this.calendarDao = calendarDao;
    }

    public void setContentDao(ContentDao contentDao) {
        this.contentDao = contentDao;
    }

}
