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

import java.io.File;
import java.text.ParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Assert;
import org.junit.Test;
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
    protected String baseDir = "src/test/unit/resources/testdata/queries";
    
    /**
     * Tests component filter basic.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testComponentFilterBasic() throws Exception {
        Element element = parseFile(new File(baseDir + "/test1.xml"));
        CalendarFilter filter = new CalendarFilter(element);
        ComponentFilter compFilter = filter.getFilter();
        
        Assert.assertNotNull(compFilter);
        Assert.assertEquals("VCALENDAR", compFilter.getName());
        Assert.assertEquals(1, compFilter.getComponentFilters().size());
        
        compFilter = (ComponentFilter) compFilter.getComponentFilters().get(0);
        
        Assert.assertEquals("VEVENT", compFilter.getName());
        Assert.assertNotNull(compFilter.getTimeRangeFilter());
        
        TimeRangeFilter timeRange = compFilter.getTimeRangeFilter();
        Assert.assertEquals("20040902T000000Z", timeRange.getUTCStart());
        Assert.assertEquals("20040903T000000Z", timeRange.getUTCEnd());
    }
    
    
    /**
     * Tests component filter basic.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testIphoneios7bugNoEndDate() throws Exception {
        Element element = parseFile(new File(baseDir + "/testIphoneios7bugNoEndDate.xml"));
        CalendarFilter filter = new CalendarFilter(element);
        ComponentFilter compFilter = filter.getFilter();
        
        Assert.assertNotNull(compFilter);
        Assert.assertEquals("VCALENDAR", compFilter.getName());
        Assert.assertEquals(1, compFilter.getComponentFilters().size());
        
        compFilter = (ComponentFilter) compFilter.getComponentFilters().get(0);
        
        Assert.assertEquals("VEVENT", compFilter.getName());
        Assert.assertNotNull(compFilter.getTimeRangeFilter());
        
        TimeRangeFilter timeRange = compFilter.getTimeRangeFilter();
        Assert.assertEquals("20040902T000000Z", timeRange.getUTCStart());
        Assert.assertEquals("20060902T000000Z", timeRange.getUTCEnd());
    }
    /**
     * Tests component filter is not defined.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testComponentFilterIsNotDefined() throws Exception {
        Element element = parseFile(new File(baseDir + "/test4.xml"));
        CalendarFilter filter = new CalendarFilter(element);
        ComponentFilter compFilter = filter.getFilter();
        
        Assert.assertNotNull(compFilter);
        Assert.assertEquals("VCALENDAR", compFilter.getName());
        Assert.assertEquals(1, compFilter.getComponentFilters().size());
        
        compFilter = (ComponentFilter) compFilter.getComponentFilters().get(0);
        
        Assert.assertEquals("VEVENT", compFilter.getName());
        Assert.assertNotNull(compFilter.getIsNotDefinedFilter());
    }
    
    /**
     * Tests property filter basic.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testPropertyFilterBasic() throws Exception {
        Element element = parseFile(new File(baseDir + "/test2.xml"));
        CalendarFilter filter = new CalendarFilter(element);
        ComponentFilter compFilter = filter.getFilter();
        
        Assert.assertNotNull(compFilter);
        Assert.assertEquals("VCALENDAR", compFilter.getName());
        Assert.assertEquals(1, compFilter.getComponentFilters().size());
        
        compFilter = (ComponentFilter) compFilter.getComponentFilters().get(0);
        
        Assert.assertEquals("VEVENT", compFilter.getName());
        Assert.assertNotNull(compFilter.getTimeRangeFilter());
        
        TimeRangeFilter timeRange = compFilter.getTimeRangeFilter();
        Assert.assertEquals("20040902T000000Z", timeRange.getUTCStart());
        Assert.assertEquals("20040903T000000Z", timeRange.getUTCEnd());
        
        Assert.assertEquals(1, compFilter.getPropFilters().size());
        PropertyFilter propFilter = (PropertyFilter) compFilter.getPropFilters().get(0);
        
        Assert.assertEquals("SUMMARY", propFilter.getName());
        TextMatchFilter textMatch = propFilter.getTextMatchFilter();
        Assert.assertNotNull(textMatch);
        Assert.assertEquals("ABC",textMatch.getValue());
    }
    
    /**
     * Tests property filter is not defined.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testPropertyFilterIsNotDefined() throws Exception {
        Element element = parseFile(new File(baseDir + "/test5.xml"));
        CalendarFilter filter = new CalendarFilter(element);
        ComponentFilter compFilter = filter.getFilter();
        
        Assert.assertNotNull(compFilter);
        Assert.assertEquals("VCALENDAR", compFilter.getName());
        Assert.assertEquals(1, compFilter.getComponentFilters().size());
        
        compFilter = (ComponentFilter) compFilter.getComponentFilters().get(0);
        
        Assert.assertEquals("VEVENT", compFilter.getName());
        Assert.assertNotNull(compFilter.getTimeRangeFilter());
        
        TimeRangeFilter timeRange = compFilter.getTimeRangeFilter();
        Assert.assertEquals("20040902T000000Z", timeRange.getUTCStart());
        Assert.assertEquals("20040903T000000Z", timeRange.getUTCEnd());
        
        Assert.assertEquals(1, compFilter.getPropFilters().size());
        PropertyFilter propFilter = (PropertyFilter) compFilter.getPropFilters().get(0);
        
        Assert.assertEquals("SUMMARY", propFilter.getName());
        Assert.assertNotNull(propFilter.getIsNotDefinedFilter());
    }
    
    /**
     * Tests param filter basic.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testParamFilterBasic() throws Exception {
        Element element = parseFile(new File(baseDir + "/test3.xml"));
        CalendarFilter filter = new CalendarFilter(element);
        ComponentFilter compFilter = filter.getFilter();
        
        Assert.assertNotNull(compFilter);
        Assert.assertEquals("VCALENDAR", compFilter.getName());
        Assert.assertEquals(1, compFilter.getComponentFilters().size());
        
        compFilter = (ComponentFilter) compFilter.getComponentFilters().get(0);
        
        Assert.assertEquals("VEVENT", compFilter.getName());
        Assert.assertNotNull(compFilter.getTimeRangeFilter());
        
        TimeRangeFilter timeRange = compFilter.getTimeRangeFilter();
        Assert.assertEquals("20040902T000000Z", timeRange.getUTCStart());
        Assert.assertEquals("20040903T000000Z", timeRange.getUTCEnd());
        
        Assert.assertEquals(1, compFilter.getPropFilters().size());
        PropertyFilter propFilter = (PropertyFilter) compFilter.getPropFilters().get(0);
        
        Assert.assertEquals("SUMMARY", propFilter.getName());
        TextMatchFilter textMatch = propFilter.getTextMatchFilter();
        Assert.assertNotNull(textMatch);
        Assert.assertEquals("ABC",textMatch.getValue());
        
        Assert.assertEquals(1, propFilter.getParamFilters().size());
        ParamFilter paramFilter = (ParamFilter) propFilter.getParamFilters().get(0);
        Assert.assertEquals("PARAM1", paramFilter.getName());
        
        textMatch = paramFilter.getTextMatchFilter();
        Assert.assertNotNull(textMatch);
        Assert.assertEquals("DEF", textMatch.getValue());
        Assert.assertTrue(textMatch.isCaseless());
    }
    
    /**
     * Tests param filter is not defined.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testParamFilterIsNotDefined() throws Exception {
        Element element = parseFile(new File(baseDir + "/test6.xml"));
        CalendarFilter filter = new CalendarFilter(element);
        ComponentFilter compFilter = filter.getFilter();
        
        Assert.assertNotNull(compFilter);
        Assert.assertEquals("VCALENDAR", compFilter.getName());
        Assert.assertEquals(1, compFilter.getComponentFilters().size());
        
        compFilter = (ComponentFilter) compFilter.getComponentFilters().get(0);
        
        Assert.assertEquals("VEVENT", compFilter.getName());
        Assert.assertNotNull(compFilter.getTimeRangeFilter());
        
        TimeRangeFilter timeRange = compFilter.getTimeRangeFilter();
        Assert.assertEquals("20040902T000000Z", timeRange.getUTCStart());
        Assert.assertEquals("20040903T000000Z", timeRange.getUTCEnd());
        
        Assert.assertEquals(1, compFilter.getPropFilters().size());
        PropertyFilter propFilter = (PropertyFilter) compFilter.getPropFilters().get(0);
        
        Assert.assertEquals("SUMMARY", propFilter.getName());
        TextMatchFilter textMatch = propFilter.getTextMatchFilter();
        Assert.assertNotNull(textMatch);
        Assert.assertEquals("ABC",textMatch.getValue());
        
        Assert.assertEquals(1, propFilter.getParamFilters().size());
        ParamFilter paramFilter = (ParamFilter) propFilter.getParamFilters().get(0);
        Assert.assertEquals("PARAM1", paramFilter.getName());
        
       
        Assert.assertNotNull(paramFilter.getIsNotDefinedFilter());
    }
    
    /**
     * Tets multiple prop filters.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testMultiplePropFilters() throws Exception {
        Element element = parseFile(new File(baseDir + "/test7.xml"));
        CalendarFilter filter = new CalendarFilter(element);
        ComponentFilter compFilter = filter.getFilter();
        
        Assert.assertNotNull(compFilter);
        Assert.assertEquals("VCALENDAR", compFilter.getName());
        Assert.assertEquals(1, compFilter.getComponentFilters().size());
        
        compFilter = (ComponentFilter) compFilter.getComponentFilters().get(0);
        
        Assert.assertEquals("VEVENT", compFilter.getName());
        Assert.assertNotNull(compFilter.getTimeRangeFilter());
        
        TimeRangeFilter timeRange = compFilter.getTimeRangeFilter();
        Assert.assertEquals("20040902T000000Z", timeRange.getUTCStart());
        Assert.assertEquals("20040903T000000Z", timeRange.getUTCEnd());
        
        Assert.assertEquals(2, compFilter.getPropFilters().size());
        PropertyFilter propFilter = (PropertyFilter) compFilter.getPropFilters().get(0);
        
        Assert.assertEquals("SUMMARY", propFilter.getName());
        TextMatchFilter textMatch = propFilter.getTextMatchFilter();
        Assert.assertNotNull(textMatch);
        Assert.assertEquals("ABC",textMatch.getValue());
        
        propFilter = (PropertyFilter) compFilter.getPropFilters().get(1);
        Assert.assertEquals("DESCRIPTION", propFilter.getName());
        Assert.assertNotNull(propFilter.getIsNotDefinedFilter());
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
            Element element = parseFile(new File(baseDir + "/error-test4.xml"));
             filter = new CalendarFilter(element);
            Assert.fail("able to create invalid filter");
        }
        catch(ParseException e) {}
        
        try
        {
            Element element = parseFile(new File(baseDir + "/error-test5.xml"));
            filter = new CalendarFilter(element);
            Assert.fail("able to create invalid filter");
        }
        catch(ParseException e) {}
        
        try
        {
            Element element = parseFile(new File(baseDir + "/error-test6.xml"));
            filter = new CalendarFilter(element);
            Assert.fail("able to create invalid filter");
        }
        catch(ParseException e) {}
        
        try
        {
            Element element = parseFile(new File(baseDir + "/error-test7.xml"));
            filter = new CalendarFilter(element);
            Assert.fail("able to create invalid filter");
        }
        catch(ParseException e) {}
        
        try
        {
            Element element = parseFile(new File(baseDir + "/error-test8.xml"));
            filter = new CalendarFilter(element);
            Assert.fail("able to create invalid filter");
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
            Element element = parseFile(new File(baseDir + "/error-test9.xml"));
            new CalendarFilter(element);
            Assert.fail("able to create invalid filter");
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
            Element element = parseFile(new File(baseDir + "/error-test10.xml"));
            new CalendarFilter(element);
            Assert.fail("able to create invalid filter");
        }
        catch(ParseException e) {}
    }
    
    /**
     * Parse file.
     * @param file The file.
     * @return The element.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    protected Element parseFile(File file) throws Exception{
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document dom = db.parse(file);
        return (Element) dom.getFirstChild();
    }
    
}
