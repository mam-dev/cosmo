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
package org.unitedinternet.cosmo.calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.text.ParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;
import org.unitedinternet.cosmo.calendar.query.CalendarFilter;
import org.unitedinternet.cosmo.calendar.query.ComponentFilter;
import org.unitedinternet.cosmo.calendar.query.ParamFilter;
import org.unitedinternet.cosmo.calendar.query.PropertyFilter;
import org.unitedinternet.cosmo.calendar.query.TextMatchFilter;
import org.unitedinternet.cosmo.calendar.query.TimeRangeFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test CalendarQueryFilter
 */
public class CalendarQueryFilterTest {
    
    /**
     * Tests component filter basic.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testComponentFilterBasic() throws Exception {
        Element element = parseFile("test1.xml");
        CalendarFilter filter = new CalendarFilter(element);
        ComponentFilter compFilter = filter.getFilter();
        
        assertNotNull(compFilter);
        assertEquals("VCALENDAR", compFilter.getName());
        assertEquals(1, compFilter.getComponentFilters().size());
        
        compFilter = (ComponentFilter) compFilter.getComponentFilters().get(0);
        
        assertEquals("VEVENT", compFilter.getName());
        assertNotNull(compFilter.getTimeRangeFilter());
        
        TimeRangeFilter timeRange = compFilter.getTimeRangeFilter();
        assertEquals("20040902T000000Z", timeRange.getUTCStart());
        assertEquals("20040903T000000Z", timeRange.getUTCEnd());
    }
    
    
    /**
     * Tests component filter basic.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testIphoneios7bugNoEndDate() throws Exception {
        Element element = parseFile("testIphoneios7bugNoEndDate.xml");
        CalendarFilter filter = new CalendarFilter(element);
        ComponentFilter compFilter = filter.getFilter();
        
        assertNotNull(compFilter);
        assertEquals("VCALENDAR", compFilter.getName());
        assertEquals(1, compFilter.getComponentFilters().size());
        
        compFilter = (ComponentFilter) compFilter.getComponentFilters().get(0);
        
        assertEquals("VEVENT", compFilter.getName());
        assertNotNull(compFilter.getTimeRangeFilter());
        
        TimeRangeFilter timeRange = compFilter.getTimeRangeFilter();
        assertEquals("20040902T000000Z", timeRange.getUTCStart());
        assertEquals("20060902T000000Z", timeRange.getUTCEnd());
    }
    /**
     * Tests component filter is not defined.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testComponentFilterIsNotDefined() throws Exception {
        Element element = parseFile("/test4.xml");
        CalendarFilter filter = new CalendarFilter(element);
        ComponentFilter compFilter = filter.getFilter();
        
        assertNotNull(compFilter);
        assertEquals("VCALENDAR", compFilter.getName());
        assertEquals(1, compFilter.getComponentFilters().size());
        
        compFilter = (ComponentFilter) compFilter.getComponentFilters().get(0);
        
        assertEquals("VEVENT", compFilter.getName());
        assertNotNull(compFilter.getIsNotDefinedFilter());
    }
    
    /**
     * Tests property filter basic.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testPropertyFilterBasic() throws Exception {
        Element element = parseFile("test2.xml");
        CalendarFilter filter = new CalendarFilter(element);
        ComponentFilter compFilter = filter.getFilter();
        
        assertNotNull(compFilter);
        assertEquals("VCALENDAR", compFilter.getName());
        assertEquals(1, compFilter.getComponentFilters().size());
        
        compFilter = (ComponentFilter) compFilter.getComponentFilters().get(0);
        
        assertEquals("VEVENT", compFilter.getName());
        assertNotNull(compFilter.getTimeRangeFilter());
        
        TimeRangeFilter timeRange = compFilter.getTimeRangeFilter();
        assertEquals("20040902T000000Z", timeRange.getUTCStart());
        assertEquals("20040903T000000Z", timeRange.getUTCEnd());
        
        assertEquals(1, compFilter.getPropFilters().size());
        PropertyFilter propFilter = (PropertyFilter) compFilter.getPropFilters().get(0);
        
        assertEquals("SUMMARY", propFilter.getName());
        TextMatchFilter textMatch = propFilter.getTextMatchFilter();
        assertNotNull(textMatch);
        assertEquals("ABC",textMatch.getValue());
    }
    
    /**
     * Tests property filter is not defined.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testPropertyFilterIsNotDefined() throws Exception {
        Element element = parseFile("test5.xml");
        CalendarFilter filter = new CalendarFilter(element);
        ComponentFilter compFilter = filter.getFilter();
        
        assertNotNull(compFilter);
        assertEquals("VCALENDAR", compFilter.getName());
        assertEquals(1, compFilter.getComponentFilters().size());
        
        compFilter = (ComponentFilter) compFilter.getComponentFilters().get(0);
        
        assertEquals("VEVENT", compFilter.getName());
        assertNotNull(compFilter.getTimeRangeFilter());
        
        TimeRangeFilter timeRange = compFilter.getTimeRangeFilter();
        assertEquals("20040902T000000Z", timeRange.getUTCStart());
        assertEquals("20040903T000000Z", timeRange.getUTCEnd());
        
        assertEquals(1, compFilter.getPropFilters().size());
        PropertyFilter propFilter = (PropertyFilter) compFilter.getPropFilters().get(0);
        
        assertEquals("SUMMARY", propFilter.getName());
        assertNotNull(propFilter.getIsNotDefinedFilter());
    }
    
    /**
     * Tests param filter basic.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testParamFilterBasic() throws Exception {
        Element element = parseFile("test3.xml");
        CalendarFilter filter = new CalendarFilter(element);
        ComponentFilter compFilter = filter.getFilter();
        
        assertNotNull(compFilter);
        assertEquals("VCALENDAR", compFilter.getName());
        assertEquals(1, compFilter.getComponentFilters().size());
        
        compFilter = (ComponentFilter) compFilter.getComponentFilters().get(0);
        
        assertEquals("VEVENT", compFilter.getName());
        assertNotNull(compFilter.getTimeRangeFilter());
        
        TimeRangeFilter timeRange = compFilter.getTimeRangeFilter();
        assertEquals("20040902T000000Z", timeRange.getUTCStart());
        assertEquals("20040903T000000Z", timeRange.getUTCEnd());
        
        assertEquals(1, compFilter.getPropFilters().size());
        PropertyFilter propFilter = (PropertyFilter) compFilter.getPropFilters().get(0);
        
        assertEquals("SUMMARY", propFilter.getName());
        TextMatchFilter textMatch = propFilter.getTextMatchFilter();
        assertNotNull(textMatch);
        assertEquals("ABC",textMatch.getValue());
        
        assertEquals(1, propFilter.getParamFilters().size());
        ParamFilter paramFilter = (ParamFilter) propFilter.getParamFilters().get(0);
        assertEquals("PARAM1", paramFilter.getName());
        
        textMatch = paramFilter.getTextMatchFilter();
        assertNotNull(textMatch);
        assertEquals("DEF", textMatch.getValue());
        assertTrue(textMatch.isCaseless());
    }
    
    /**
     * Tests param filter is not defined.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testParamFilterIsNotDefined() throws Exception {
        Element element = parseFile("/test6.xml");
        CalendarFilter filter = new CalendarFilter(element);
        ComponentFilter compFilter = filter.getFilter();
        
        assertNotNull(compFilter);
        assertEquals("VCALENDAR", compFilter.getName());
        assertEquals(1, compFilter.getComponentFilters().size());
        
        compFilter = (ComponentFilter) compFilter.getComponentFilters().get(0);
        
        assertEquals("VEVENT", compFilter.getName());
        assertNotNull(compFilter.getTimeRangeFilter());
        
        TimeRangeFilter timeRange = compFilter.getTimeRangeFilter();
        assertEquals("20040902T000000Z", timeRange.getUTCStart());
        assertEquals("20040903T000000Z", timeRange.getUTCEnd());
        
        assertEquals(1, compFilter.getPropFilters().size());
        PropertyFilter propFilter = (PropertyFilter) compFilter.getPropFilters().get(0);
        
        assertEquals("SUMMARY", propFilter.getName());
        TextMatchFilter textMatch = propFilter.getTextMatchFilter();
        assertNotNull(textMatch);
        assertEquals("ABC",textMatch.getValue());
        
        assertEquals(1, propFilter.getParamFilters().size());
        ParamFilter paramFilter = (ParamFilter) propFilter.getParamFilters().get(0);
        assertEquals("PARAM1", paramFilter.getName());
        
       
        assertNotNull(paramFilter.getIsNotDefinedFilter());
    }
    
    /**
     * Tets multiple prop filters.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testMultiplePropFilters() throws Exception {
        Element element = parseFile("test7.xml");
        CalendarFilter filter = new CalendarFilter(element);
        ComponentFilter compFilter = filter.getFilter();
        
        assertNotNull(compFilter);
        assertEquals("VCALENDAR", compFilter.getName());
        assertEquals(1, compFilter.getComponentFilters().size());
        
        compFilter = (ComponentFilter) compFilter.getComponentFilters().get(0);
        
        assertEquals("VEVENT", compFilter.getName());
        assertNotNull(compFilter.getTimeRangeFilter());
        
        TimeRangeFilter timeRange = compFilter.getTimeRangeFilter();
        assertEquals("20040902T000000Z", timeRange.getUTCStart());
        assertEquals("20040903T000000Z", timeRange.getUTCEnd());
        
        assertEquals(2, compFilter.getPropFilters().size());
        PropertyFilter propFilter = (PropertyFilter) compFilter.getPropFilters().get(0);
        
        assertEquals("SUMMARY", propFilter.getName());
        TextMatchFilter textMatch = propFilter.getTextMatchFilter();
        assertNotNull(textMatch);
        assertEquals("ABC",textMatch.getValue());
        
        propFilter = (PropertyFilter) compFilter.getPropFilters().get(1);
        assertEquals("DESCRIPTION", propFilter.getName());
        assertNotNull(propFilter.getIsNotDefinedFilter());
    }
    
    /**
     * Tests component filter error.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testComponentFilterError() throws Exception {
        @SuppressWarnings("unused")
        CalendarFilter filter;
        try
        {
            Element element = parseFile("error-test4.xml");
             filter = new CalendarFilter(element);
            fail("able to create invalid filter");
        }
        catch(ParseException e) {}
        
        try
        {
            Element element = parseFile("error-test5.xml");
            filter = new CalendarFilter(element);
            fail("able to create invalid filter");
        }
        catch(ParseException e) {}
        
        try
        {
            Element element = parseFile("error-test6.xml");
            filter = new CalendarFilter(element);
            fail("able to create invalid filter");
        }
        catch(ParseException e) {}
        
        try
        {
            Element element = parseFile("error-test7.xml");
            filter = new CalendarFilter(element);
            fail("able to create invalid filter");
        }
        catch(ParseException e) {}
        
        try
        {
            Element element = parseFile("error-test8.xml");
            filter = new CalendarFilter(element);
            fail("able to create invalid filter");
        }
        catch(ParseException e) {}
        
    }
    
    /**
     * Tests property filter error.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testPropertyFilterError() throws Exception {
        
        try
        {
            Element element = parseFile("error-test9.xml");
            new CalendarFilter(element);
            fail("able to create invalid filter");
        }
        catch(ParseException e) {}
    }
    
    /**
     * Tests param filter error.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testParamFilterError() throws Exception {
        
        try
        {
            Element element = parseFile("error-test10.xml");
            new CalendarFilter(element);
            fail("able to create invalid filter");
        }
        catch(ParseException e) {}
    }
    
    /**
     * Parse file.
     * @param file The file.
     * @return The element.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    protected Element parseFile(String resourceName) throws Exception{
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document dom = db.parse(this.getClass().getResourceAsStream("queries/".concat(resourceName)));
        return (Element) dom.getFirstChild();
    }
    
}
