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
package org.unitedinternet.cosmo.dav.provider;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitedinternet.cosmo.calendar.ICalendarUtils;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavRequest;
import org.unitedinternet.cosmo.dav.DavResourceFactory;
import org.unitedinternet.cosmo.dav.DavResponse;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.caldav.ScheduleMultiResponse;
import org.unitedinternet.cosmo.dav.caldav.ScheduleResponse;
import org.unitedinternet.cosmo.dav.io.DavInputContext;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.User;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VFreeBusy;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.RequestStatus;

/**
 * <p>
 * An implementation of <code>DavProvider</code> that implements access to <code>DavOutboxCollection</code> resources.
 * </p>
 * 
 * @see DavProvider
 * @see CollectionProvider
 * @see BaseProvider
 */
public class OutboxCollectionProvider extends CollectionProvider {
    
    private static final Logger LOG = LoggerFactory.getLogger(OutboxCollectionProvider.class);

    public OutboxCollectionProvider(DavResourceFactory resourceFactory, EntityFactory entityFactory) {
        super(resourceFactory, entityFactory);
    }

    // DavProvider methods

    
    @Override
    public void post(DavRequest request, DavResponse response, WebDavResource resource) throws CosmoDavException, IOException {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Handling POST method for Outbox");
        }

        int status = DavResponse.SC_OK;
        ScheduleMultiResponse ms = new ScheduleMultiResponse();
        try {
            // according to http://tools.ietf.org/html/draft-desruisseaux-caldav-sched-05
            // only REQUEST and REFRESH are available for post
            DavInputContext ctx = (DavInputContext) createInputContext(request);
            Calendar calendar = ctx.getCalendar(true);

            processPostRequest(calendar, ms);
        } catch (RuntimeException exc) {
            status = DavResponse.SC_INTERNAL_SERVER_ERROR;
        }

        response.sendXmlResponse(ms, status);
    }

    private void processPostRequest(Calendar calendar, ScheduleMultiResponse ms) {
        if (!Method.REQUEST.equals(calendar.getMethod())) {
            return;
        }

        processPostFreeBusyRequest(calendar, ms);
    }

    private void processPostFreeBusyRequest(Calendar calendar, ScheduleMultiResponse ms) {
        ComponentList<VFreeBusy> freeBusyList = calendar.getComponents(VFreeBusy.VFREEBUSY);
        for (VFreeBusy vFreeBusy : freeBusyList) {
            Date periodStrart =  vFreeBusy.getStartDate().getDate();
            Date periodEnd =  vFreeBusy.getEndDate().getDate();
            Period period = new Period(new DateTime(periodStrart), new DateTime(periodEnd));

            User user = null;
            PropertyList<Attendee> freeBusyProperties = vFreeBusy.getProperties(Property.ATTENDEE);
            for (Attendee attendee : freeBusyProperties) {
                // since we might have multiple responses for one user lets create a flag here
                try {
                    String email = attendee.getCalAddress().getSchemeSpecificPart();
                    user = getResourceFactory().getUserService().getUserByEmail(email);
                } catch (Exception e) {
                    ScheduleResponse resp = new ScheduleResponse(attendee.getCalAddress().toString());
                    resp.setStatus(RequestStatus.CLIENT_ERROR);
                    ms.addResponse(resp);
                    continue;
                }

                // Handle case where user doesn't exist.
                // Not sure what to return, it seems CalendarServer returns the following
                if (user == null) {
                    ScheduleResponse resp = new ScheduleResponse(attendee.getCalAddress().toString());
                    resp.setStatus("3.7;Invalid Calendar User");
                    ms.addResponse(resp);
                    continue;
                }

                /*
                 * TODO Apple iCal send this property, need to be taken into action
                 * 
                 * 
                 * 
                 * https://trac.calendarserver.org/browser/CalendarServer/trunk/doc/Extensions/icalendar-maskuids-02.txt?
                 * rev=1510 120 Property Name: X-CALENDARSERVER-MASK-UID 121 122 Purpose: This property indicates the
                 * unique identifier for a calendar 123 component that is to be ignored when calculating free-busy time.
                 * 124
                 */
                VFreeBusy vfb = getResourceFactory().getCalendarQueryProcessor().freeBusyQuery(user, period);
                vfb.getProperties().add(attendee);
                vfb.getProperties().add(vFreeBusy.getOrganizer());
                Calendar cal = ICalendarUtils.createBaseCalendar(vfb);
                cal.getProperties().add(Method.REPLY);
                ScheduleResponse resp = new ScheduleResponse(attendee.getCalAddress().toString());
                resp.setCalendarData(cal.toString());
                ms.addResponse(resp);
            }
        }
    }
}
