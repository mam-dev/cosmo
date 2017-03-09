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
package org.unitedinternet.cosmo.calendar;

import java.util.Iterator;
import java.util.List;

import org.unitedinternet.cosmo.util.VersionFourGenerator;



import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VFreeBusy;
import net.fortuna.ical4j.model.parameter.FbType;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.FreeBusy;
import net.fortuna.ical4j.model.property.Uid;

/**
 * 
 * FreeBusyUtils class.
 */
public class FreeBusyUtils {
    
    private static final VersionFourGenerator UUID_GENERATOR = new VersionFourGenerator();
    
    /**
        A VFREEBUSY component overlaps a given time range if the condition
        for the corresponding component state specified in the table below
        is satisfied.  The conditions depend on the presence in the
        VFREEBUSY component of the DTSTART and DTEND properties, and any
        FREEBUSY properties in the absence of DTSTART and DTEND.  Any
        DURATION property is ignored, as it has a special meaning when
        used in a VFREEBUSY component.
    
        When only FREEBUSY properties are used, each period in each
        FREEBUSY property is compared against the time range, irrespective
        of the type of free busy information (free, busy, busy-tentative,
        busy-unavailable) represented by the property.
    
    
        +------------------------------------------------------+
        | VFREEBUSY has both the DTSTART and DTEND properties? |
        |   +--------------------------------------------------+
        |   | VFREEBUSY has the FREEBUSY property?             |
        |   |   +----------------------------------------------+
        |   |   | Condition to evaluate                        |
        +---+---+----------------------------------------------+
        | Y | * | (start <= DTEND) AND (end > DTSTART)         |
        +---+---+----------------------------------------------+
        | N | Y | (start <  freebusy-period-end) AND           |
        |   |   | (end   >  freebusy-period-start)             |
        +---+---+----------------------------------------------+
        | N | N | FALSE                                        |
        +---+---+----------------------------------------------+
     *
     * @param freeBusy comoponent to test
     * @param period period to test against
     * @param tz timezone to use for floating times
     * @return true if component overlaps specified range, false otherwise
     */
    public static boolean overlapsPeriod(VFreeBusy freeBusy, Period period, TimeZone tz){
        
        DtStart start = freeBusy.getStartDate();
        DtEnd end = freeBusy.getEndDate();
         
        if (start != null && end != null) {
            InstanceList instances = new InstanceList();
            instances.setTimezone(tz);
            instances.addComponent(freeBusy, period.getStart(),period.getEnd());
            return instances.size() > 0;
        }
        
        PropertyList<FreeBusy> props = freeBusy.getProperties(Property.FREEBUSY);
        if (props.size()==0) {
            return false;
        }
        
        for (FreeBusy fb: props) {            
            PeriodList periods = fb.getPeriods();
            Iterator<Period> periodIt = periods.iterator();
            while(periodIt.hasNext()) {
                Period fbPeriod = periodIt.next();
                if(fbPeriod.intersects(period)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Merge multiple VFREEBUSY components into a single VFREEBUSY
     * component.
     * @param components components to merge
     * @param range time range of new component (DTSTART/DTEND)
     * @return merged component
     */
    public static VFreeBusy mergeComponents(List<VFreeBusy> components, Period range) {
        // no merging required if there's only one component
        if (components.size() == 1) {
            return components.get(0);
        }

        // merge results into single VFREEBUSY
        PeriodList busyPeriods = new PeriodList();
        PeriodList busyTentativePeriods = new PeriodList();
        PeriodList busyUnavailablePeriods = new PeriodList();
        
        for(VFreeBusy vfb: components) {
            PropertyList<FreeBusy> props = vfb.getProperties(Property.FREEBUSY);
            for(FreeBusy fb : props) {                
                FbType fbt = (FbType)
                    fb.getParameters().getParameter(Parameter.FBTYPE);
                if (fbt == null || FbType.BUSY.equals(fbt)) {
                    busyPeriods.addAll(fb.getPeriods());
                } else if (FbType.BUSY_TENTATIVE.equals(fbt)) {
                    busyTentativePeriods.addAll(fb.getPeriods());
                } else if (FbType.BUSY_UNAVAILABLE.equals(fbt)) {
                    busyUnavailablePeriods.addAll(fb.getPeriods());
                }
            }
        }
        
        // Merge periods
        busyPeriods = busyPeriods.normalise();
        busyTentativePeriods = busyTentativePeriods.normalise();
        busyUnavailablePeriods = busyUnavailablePeriods.normalise();
        
        // Construct new VFREEBUSY
        VFreeBusy vfb =
            new VFreeBusy(range.getStart(), range.getEnd());
        String uid = UUID_GENERATOR.nextStringIdentifier();
        vfb.getProperties().add(new Uid(uid));
       
        // Add all periods to the VFREEBUSY
        if (busyPeriods.size() != 0) {
            FreeBusy fb = new FreeBusy(busyPeriods);
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
    
}
