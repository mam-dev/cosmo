/*
 * EventValidatorTest.java Dec 12, 2013
 * 
 * Copyright (c) 2013 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.hibernate.validator;


public class EventValidatorTest {
  /*  EventValidator classUnderTest = new EventValidator();
    
    @Before
    public void init(){
        classUnderTest.initialize(null);
    }
    @Test
    public void shouldInvalidateAnEventWithASecondlyRucurrenceFrequence() throws ParseException{
        
        VEvent evt = new VEvent();
        evt.getProperties().add(new DtStart("20121010T101010Z"));
        RRule rrule= new RRule("FREQ=SECONDLY");
        evt.getProperties().add(rrule);
        Calendar c = new Calendar();
        c.getProperties().add(new ProdId("aProdId"));
        c.getProperties().add(new Version("1", "1"));
        c.getComponents().add(evt);
        
        Assert.assertFalse(classUnderTest.isValid(c, null));
    }
    
    @Test
    public void shouldInvalidateAnEventWithAMinutelyRucurrenceFrequence() throws ParseException{
        
        VEvent evt = new VEvent();
        evt.getProperties().add(new DtStart("20121010T101010Z"));
        RRule rrule= new RRule("FREQ=MINUTELY");
        evt.getProperties().add(rrule);
        Calendar c = new Calendar();
        c.getProperties().add(new ProdId("aProdId"));
        c.getProperties().add(new Version("1", "1"));
        c.getComponents().add(evt);
        
        Assert.assertFalse(classUnderTest.isValid(c, null));
    }
    
    @Test
    public void shouldInvalidateAnEventWithAHourlyRucurrenceFrequence() throws ParseException{
        
        VEvent evt = new VEvent();
        evt.getProperties().add(new DtStart("20121010T101010Z"));
        RRule rrule= new RRule("FREQ=HOURLY");
        evt.getProperties().add(rrule);
        Calendar c = new Calendar();
        c.getProperties().add(new ProdId("aProdId"));
        c.getProperties().add(new Version("1", "1"));
        c.getComponents().add(evt);
        
        Assert.assertFalse(classUnderTest.isValid(c, null));
    }
    
    @Test
    public void shouldInvalidateASummaryGreaterThan64Chars() throws ParseException{
        VEvent evt = new VEvent();
        evt.getProperties().add(new DtStart("20121010T101010Z"));
        RRule rrule= new RRule("FREQ=DAILY");
        evt.getProperties().add(rrule);
        Calendar c = new Calendar();
        c.getProperties().add(new ProdId("aProdId"));
        c.getProperties().add(new Version("1", "1"));
        c.getComponents().add(evt);
        
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <=65;i++){
            sb.append(i);
        }
        
        evt.getProperties().add(new Summary(sb.toString()));
        Assert.assertFalse(classUnderTest.isValid(c, null));
    }
    @Test
    public void shouldInvalidateALocationGreaterThan64Chars() throws ParseException{
        VEvent evt = new VEvent();
        evt.getProperties().add(new DtStart("20121010T101010Z"));
        Calendar c = new Calendar();
        c.getProperties().add(new ProdId("aProdId"));
        c.getProperties().add(new Version("1", "1"));
        c.getComponents().add(evt);
        
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <=65;i++){
            sb.append(i);
        }
        
        evt.getProperties().add(new Location(sb.toString()));
        Assert.assertFalse(classUnderTest.isValid(c, null));
    }
    
    @Test
    public void shouldInvalidateADescriptionGreaterThan1024Chars() throws ParseException{
        VEvent evt = new VEvent();
        evt.getProperties().add(new DtStart("20121010T101010Z"));
        Calendar c = new Calendar();
        c.getProperties().add(new ProdId("aProdId"));
        c.getProperties().add(new Version("1", "1"));
        c.getComponents().add(evt);
        
        StringBuilder sb = new StringBuilder();
        for(int i = 1; i <=1024;i++){
            sb.append(i);
        }
        
        evt.getProperties().add(new Description(sb.toString()));
        Assert.assertFalse(classUnderTest.isValid(c, null));
    }*/
}
