/*
 * DomWriterTest.java Dec 30, 2013
 * 
 * Copyright (c) 2013 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.util;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DomWriterTest {
    
    @Test
    public void testWriteNode() throws ParserConfigurationException, XMLStreamException, IOException{
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document doc = builder.newDocument();
        
                   
        // create the root element node
        Element element = doc.createElementNS("http://apple.com/ns/ical/", "E:calendar-color");
        element.setAttribute("xmlns:E", "http://apple.com/ns/ical/");
        element.appendChild(doc.createTextNode("#711A76FF"));
        
        //The element above gets translated into an invalid element
        //Apple PROPFIND request is broken
        //<E:calendar-color xmlns:E="http://apple.com/ns/ical/" xmlns:E="http://apple.com/ns/ical/">#711A76FF</E:calendar-color>             
        Assert.assertEquals("<E:calendar-color xmlns:E=\"http://apple.com/ns/ical/\">#711A76FF</E:calendar-color>", 
                DomWriter.write(element));        
    }

}
