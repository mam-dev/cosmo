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

import java.text.ParseException;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests ICalValueParser
 *
 */
public class ICalValueParserTest {
    /**
     * Tests parse with params.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testParseWithParams() throws Exception {
        String str =
            ";VALUE=DATE-TIME;TZID=America/Los_Angeles:20021010T120000";

        ICalValueParser parser = new ICalValueParser(str);
        parser.parse();

        assertNotNull("null value", parser.getValue());
        assertEquals("20021010T120000", parser.getValue(), "incorrect value");

        assertEquals(2, parser.getParams().keySet().size(), 
                     "wrong number of params");
        assertEquals("DATE-TIME", parser.getParams().get("VALUE"), 
                     "wrong VALUE value");
        assertEquals("America/Los_Angeles", parser.getParams().get("TZID"),
                     "wrong TZID value");
    }
    
    /**
     * Tests parse without params.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testParseWithoutParams() throws Exception {
        String str = "20021010T120000";

        ICalValueParser parser = new ICalValueParser(str);
        parser.parse();

        assertNotNull(parser.getValue(), "null value");
        assertEquals("20021010T120000", parser.getValue(), "incorrect value");

        assertEquals(0, parser.getParams().keySet().size(), 
                     "wrong number of params");
    }

    /**
     * Tests parse quoted param.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testParseQuotedParam() throws Exception {
        String str =";VALUE=\"DATE-TIME\":20021010T120000";

        ICalValueParser parser = new ICalValueParser(str);
        parser.parse();

        assertNotNull(parser.getValue(), "null value");
        assertEquals("20021010T120000", parser.getValue(), "incorrect value");

        assertEquals(1, parser.getParams().keySet().size(),
                     "wrong number of params");
        assertEquals("DATE-TIME", parser.getParams().get("VALUE"), 
                     "wrong VALUE value");
    }

    /**
     * Tests parse unclosed quotes.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testParseUnclosedQuotes() throws Exception {
        String str = ";VALUE=\"DATE-TIME:20021010T120000";

        ICalValueParser parser = new ICalValueParser(str);
        try {
            parser.parse();
            fail("parsed param value with unclosed quotes");
        } catch (ParseException e) {}
    }
}
