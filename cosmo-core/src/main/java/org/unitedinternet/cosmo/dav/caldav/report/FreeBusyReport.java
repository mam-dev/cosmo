/*
 * Copyright 2006-2007 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.dav.caldav.report;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.unitedinternet.cosmo.calendar.FreeBusyUtils;
import org.unitedinternet.cosmo.calendar.ICalendarUtils;
import org.unitedinternet.cosmo.dav.BadRequestException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.ForbiddenException;
import org.unitedinternet.cosmo.dav.UnprocessableEntityException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.impl.DavCalendarCollection;
import org.unitedinternet.cosmo.dav.impl.DavCalendarResource;
import org.unitedinternet.cosmo.dav.impl.DavItemCollection;
import org.unitedinternet.cosmo.dav.report.SimpleReport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.component.VFreeBusy;
import net.fortuna.ical4j.validate.ValidationException;

/**
 * <p>
 * Represents the <code>CALDAV:free-busy-query</code> report that
 * provides a mechanism for finding free-busy information.
 * </p>
 */
public class FreeBusyReport extends SimpleReport implements CaldavConstants {
    
    private Period freeBusyRange;
    private ArrayList<VFreeBusy> freeBusyResults;

    public static final ReportType REPORT_TYPE_CALDAV_FREEBUSY =
            ReportType.register(ELEMENT_CALDAV_CALENDAR_FREEBUSY,
                    NAMESPACE_CALDAV, FreeBusyReport.class);

    // Report methods

    public ReportType getType() {
        return REPORT_TYPE_CALDAV_FREEBUSY;
    }

    // ReportBase methods

    /**
     * Parses the report info, extracting the time range.
     *
     * @param info The report info.
     */
    protected void parseReport(ReportInfo info)
            throws CosmoDavException {
        if (!getType().isRequestedReportType(info)) {
            throw new CosmoDavException("Report not of type " + getType());
        }

        freeBusyRange = findFreeBusyRange(info);
    }

    /**
     * <p>
     * Runs the report query and generates a resulting <code>VFREEBUSY</code>.
     * </p>
     * <p>
     * The set of <code>VFREEBUSY</code> components that are returned by the
     * query are merged into a single component. It is wrapped in a calendar
     * object and converted using UTF-8 to bytes which are set as the report
     * stream. The report's content type and encoding are also set.
     * </p>
     *
     * @throws CosmoDavException - If something is wrong this excepton is thrown.
     */
    protected void runQuery()
            throws CosmoDavException {
        if (!(getResource().isCollection() || getResource() instanceof DavCalendarResource)) {
            throw new UnprocessableEntityException(getType() + " report not supported for non-calendar resources");
        }

        // if the resource or any of its ancestors is excluded from
        // free busy rollups, deny the query
        DavItemCollection dc = getResource().isCollection() ?
                (DavItemCollection) getResource() :
                (DavItemCollection) getResource().getParent();
        while (dc != null) {
            if (dc.isExcludedFromFreeBusyRollups()) {
                throw new ForbiddenException("Targeted resource or ancestor collection does not participate in freebusy rollups");
            }
            dc = (DavItemCollection) dc.getParent();
            if (!dc.exists()) {
                dc = null;
            }
        }

        freeBusyResults = new ArrayList<VFreeBusy>();

        super.runQuery();

        VFreeBusy vfb =
                FreeBusyUtils.mergeComponents(freeBusyResults, freeBusyRange);
        Calendar calendar = ICalendarUtils.createBaseCalendar(vfb);
        String output = writeCalendar(calendar);

        setContentType("text/calendar");
        setEncoding("UTF-8");
        try {
            setStream(new ByteArrayInputStream(output.getBytes("UTF-8")));
        } catch (Exception e) {
            throw new CosmoDavException(e);
        }
    }

    protected void doQuerySelf(WebDavResource resource)
            throws CosmoDavException {
        if (!resource.isCollection()) {
            if (isExcluded(resource.getParent())) {
                return;
            }
            // runQuery() has already determined that this is a calendar
            // resource
            DavCalendarResource dcr = (DavCalendarResource) resource;
            VFreeBusy vfb = dcr.generateFreeBusy(freeBusyRange);
            if (vfb != null) {
                freeBusyResults.add(vfb);
            }
            return;
        }
        // collections themselves never match - only calendar resources
    }

    protected void doQueryChildren(DavCollection collection)
            throws CosmoDavException {
        if (isExcluded(collection)) {
            return;
        }
        if (collection instanceof DavCalendarCollection) {
            DavCalendarCollection dcc = (DavCalendarCollection) collection;
            VFreeBusy vfb = dcc.generateFreeBusy(freeBusyRange);
            if (vfb != null) {
                freeBusyResults.add(vfb);
            }
            return;
        }
        // if it's a regular collection, there won't be any calendar resources
        // within it to match the query
    }

    protected void doQueryDescendents(DavCollection collection)
            throws CosmoDavException {
        if (isExcluded(collection)) {
            return;
        }
        super.doQueryDescendents(collection);
    }

    // our methods

    private static Period findFreeBusyRange(ReportInfo info)
            throws CosmoDavException {
        Element tre =
                info.getContentElement(ELEMENT_CALDAV_TIME_RANGE,
                        NAMESPACE_CALDAV);
        if (tre == null) {
            throw new BadRequestException("Expected " + QN_CALDAV_TIME_RANGE);
        }

        DateTime sdt = null;
        try {
            String start = DomUtil.getAttribute(tre, ATTR_CALDAV_START, null);
            sdt = new DateTime(start);
        } catch (ParseException e) {
            throw new BadRequestException("Attribute " + ATTR_CALDAV_START + " not parseable: " + e.getMessage());
        }

        DateTime edt = null;
        try {
            String end = DomUtil.getAttribute(tre, ATTR_CALDAV_END, null);
            edt = new DateTime(end);
        } catch (ParseException e) {
            throw new BadRequestException("Attribute " + ATTR_CALDAV_END + " not parseable: " + e.getMessage());
        }

        return new Period(sdt, edt);
    }

    @Override
    public Element toXml(Document document) {
        try {
            if (getStream() == null) {
                runQuery();
            }

            Document doc = DomUtil.parseDocument(getStream());
            document.adoptNode(doc);

            return doc.getDocumentElement();
        } catch (IOException | CosmoDavException | ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isExcluded(DavCollection collection) {
        DavItemCollection dic = (DavItemCollection) collection;
        return dic.isExcludedFromFreeBusyRollups();
    }

    private static String writeCalendar(Calendar calendar)
            throws CosmoDavException {
        try {
            StringWriter out = new StringWriter();
            CalendarOutputter outputter = new CalendarOutputter();
            outputter.output(calendar, out);
            String output = out.toString();
            out.close();

            // NB ical4j's outputter generates \r\n line ends but we
            // need only \n, so remove all \r's from the string
            output = output.replaceAll("\r", "");

            return output;
        } catch (IOException | ValidationException e) {
            throw new CosmoDavException(e);
        }
    }

    @Override
    public boolean isMultiStatusReport() {
        return false;
    }
}