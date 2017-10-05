/*
 * Copyright 2005-2006 Open Source Applications Foundation
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
package org.unitedinternet.cosmo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.security.Principal;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.parameter.XParameter;
import net.fortuna.ical4j.model.property.Action;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.model.property.XProperty;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.CollectionSubscription;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.FileItem;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.Preference;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.mock.MockEntityFactory;
import org.unitedinternet.cosmo.security.mock.MockAnonymousPrincipal;
import org.unitedinternet.cosmo.security.mock.MockUserPrincipal;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 */
@Ignore
public class TestHelper {
    protected static final DocumentBuilderFactory BUILDER_FACTORY =
        DocumentBuilderFactory.newInstance();

    protected static CalendarBuilder calendarBuilder = new CalendarBuilder();

    static int apseq = 0;
    static int cseq = 0;
    static int eseq = 0;
    static int iseq = 0;
    static int lseq = 0;
    static int pseq = 0;
    static int rseq = 0;
    static int sseq = 0;
    static int tseq = 0;
    static int useq = 0;

    private EntityFactory entityFactory = new MockEntityFactory();

    /**
     * Constructor.
     */
    public TestHelper() {
    }

    /**
     * Constructor.
     * @param entityFactory - Entity factory.
     */
    public TestHelper(EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }

    /**
     * Makes dummy calendar.
     * @return The dummy calendar.
     */
    public Calendar makeDummyCalendar() {
        Calendar cal =new Calendar();

        cal.getProperties().add(new ProdId(CosmoConstants.PRODUCT_ID));
        cal.getProperties().add(Version.VERSION_2_0);

        return cal;
    }

    /**
     * Makes dummy calendar with event.
     * @return The calendar.
     */
    public Calendar makeDummyCalendarWithEvent() {
        Calendar cal = makeDummyCalendar();

        VEvent e1 = makeDummyEvent();
        cal.getComponents().add(e1);

        VTimeZone tz1 = TimeZoneRegistryFactory.getInstance().createRegistry().
        getTimeZone("America/Los_Angeles").getVTimeZone();
        cal.getComponents().add(tz1);

        return cal;
    }

    /**
     * Makes dummy event.
     * @return The event.
     */
    public VEvent makeDummyEvent() {
        String serial = Integer.toString(++eseq);
        String summary = "dummy" + serial;

        // tomorrow
        java.util.Calendar start = java.util.Calendar.getInstance();
        start.add(java.util.Calendar.DAY_OF_MONTH, 1);
        start.set(java.util.Calendar.HOUR_OF_DAY, 9);
        start.set(java.util.Calendar.MINUTE, 30);

        // 1 hour duration
        Dur duration = new Dur(0, 1, 0, 0);
 
        VEvent event = new VEvent(new Date(start.getTime()), duration, summary);
        event.getProperties().add(new Uid(serial));
 
        // add timezone information
        VTimeZone tz = TimeZoneRegistryFactory.getInstance().createRegistry().
            getTimeZone("America/Los_Angeles").getVTimeZone();
        String tzValue =
            tz.getProperties().getProperty(Property.TZID).getValue();
        net.fortuna.ical4j.model.parameter.TzId tzParam =
            new net.fortuna.ical4j.model.parameter.TzId(tzValue);
        event.getProperties().getProperty(Property.DTSTART).
            getParameters().add(tzParam);

        // add an alarm for 5 minutes before the event with an xparam
        // on the description
        Dur trigger = new Dur(0, 0, -5, 0);
        VAlarm alarm = new VAlarm(trigger);
        alarm.getProperties().add(Action.DISPLAY);
        Description description = new Description("Meeting at 9:30am");
        XParameter xparam = new XParameter("X-COSMO-TEST-PARAM", "deadbeef");
        description.getParameters().add(xparam);
        alarm.getProperties().add(description);
        alarm.getProperties().add(new Description("Meeting at 9:30am"));
        event.getAlarms().add(alarm);

        // add an x-property with an x-param
        XProperty xprop = new XProperty("X-COSMO-TEST-PROP", "abc123");
        xprop.getParameters().add(xparam);
        event.getProperties().add(xprop);

        return event;
    }

    /**
     * Makes dummy ticket.
     * @param timeout The timout.
     * @return The ticket.
     */
    public Ticket makeDummyTicket(String timeout) {
        Ticket ticket = entityFactory.creatTicket();
        ticket.setTimeout(timeout);
        ticket.setPrivileges(new HashSet<String>());
        ticket.getPrivileges().add(Ticket.PRIVILEGE_READ);
        return ticket;
    }

    /**
     * makes dummy ticket.
     * @param timeout The timeout.
     * @return The ticket.
     */
    public Ticket makeDummyTicket(int timeout) {
        return makeDummyTicket("Second-" + timeout);
    }

    /**
     * makes dummy ticket.
     * @return The ticket.
     */
    public Ticket makeDummyTicket() {
        return makeDummyTicket(Ticket.TIMEOUT_INFINITE);
    }

    /**
     * Makes dummy ticket.
     * @param user The user.
     * @return The ticket.
     */
    public Ticket makeDummyTicket(User user) {
        Ticket ticket = makeDummyTicket();
        ticket.setOwner(user);
        ticket.setKey(Integer.toString(++tseq));
        return ticket;
    }

    /**
     * Makes dummy user.
     * @param username The username.
     * @param password The password.
     * @return The user.
     */
    public User makeDummyUser(String username,
                              String password) {
        if (username == null) {
            throw new IllegalArgumentException("username required");
        }
        
        if (password == null) {
            throw new IllegalArgumentException("password required");
        }

        User user = entityFactory.createUser();
        user.setUsername(username);
        user.setFirstName(username);
        user.setLastName(username);
        user.setEmail(username + "@localhost");
        user.setPassword(password);

        return user;
    }

    /**
     * Makes dummy user.
     * @return The user.
     */
    public User makeDummyUser() {
        String serial = Integer.toString(++useq);;
        String username = "dummy" + serial;
        return makeDummyUser(username, username);
    }

    /**
     * Makes dummy subscription.
     * @param collection The coolection item.
     * @param ticket The ticket.
     * @return The collection subscrition.
     */
    public CollectionSubscription makeDummySubscription(CollectionItem collection, Ticket ticket) {
        if (collection == null) {
            throw new IllegalArgumentException("collection required");
        }
        if (ticket == null) {
            throw new IllegalArgumentException("ticket required");
        }


        CollectionSubscription sub = entityFactory.createCollectionSubscription();
        
        
        return sub;
    }

    /**
     * Makes dummy subscription.
     * @param user The user.
     * @return The collection subscription.
     */
    public CollectionSubscription makeDummySubscription(User user) {
        CollectionItem collection = makeDummyCollection(user);
        Ticket ticket = makeDummyTicket(user);

        CollectionSubscription sub = makeDummySubscription(collection, ticket);
        sub.setOwner(user);

        return sub;
    }

    /**
     * Makes dummy preference.
     * @return The preference.
     */
    public Preference makeDummyPreference() {
        String serial = Integer.toString(++pseq);

        Preference pref = entityFactory.createPreference();
        pref.setKey("dummy pref " + serial);
        pref.setValue(pref.getKey());
       
        return pref;
    }

    /**
     * Makes dummy user principal.
     * @return The principal.
     */
    public Principal makeDummyUserPrincipal() {
        return new MockUserPrincipal(makeDummyUser());
    }

    /**
     * Makes dummy user principal.
     * @param name The name.
     * @param password The password.
     * @return The principal.
     */
    public Principal makeDummyUserPrincipal(String name,
                                            String password) {
        return new MockUserPrincipal(makeDummyUser(name, password));
    }
    
    /**
     * Makes dummy user principal.
     * @param user The user.
     * @return The principal.
     */
    public Principal makeDummyUserPrincipal(User user) {
        return new MockUserPrincipal(user);
    }

    /**
     * Makes dummy anonymous principal.
     * @return The principal.
     */
    public Principal makeDummyAnonymousPrincipal() {
        String serial = Integer.toString(++apseq);
        return new MockAnonymousPrincipal("dummy" + serial);
    }

    /**
     * makes dummy root principal.
     * @return The principal.
     */
    public Principal makeDummyRootPrincipal() {
        User user = makeDummyUser();
        user.setAdmin(Boolean.TRUE);
        return new MockUserPrincipal(user);
    }

    /**
     * Loads xml.
     * @param name The name.
     * @return The document xml loaded.
     * @throws IOException - if something is wrong this exception is thrown.
     * @throws SAXException - if something is wrong this exception is thrown.
     * @throws ParserConfigurationException - if something is wrong this exception is thrown.
     */
    public Document loadXml(String name) throws SAXException,
            ParserConfigurationException, IOException {
        InputStream in = getInputStream(name);
        BUILDER_FACTORY.setNamespaceAware(true);
        DocumentBuilder docBuilder = BUILDER_FACTORY.newDocumentBuilder();
        return docBuilder.parse(in);
    }
    
    /**
     * Loads ics.
     * @param name The name.
     * @return The calendar.
     * @throws IOException - if something is wrong this exception is thrown.
     * @throws ParserException - if something is wrong this exception is thrown.
     */
    public Calendar loadIcs(String name) throws IOException, ParserException{
        InputStream in = getInputStream(name);
        return calendarBuilder.build(in);
    }

    /**
     * Makes dummy content.
     * @param user The user.
     * @return The content item.
     */
    public ContentItem makeDummyContent(User user) {
        String serial = Integer.toString(++cseq);
        String name = "test content " + serial;

        FileItem content = entityFactory.createFileItem();

        content.setUid(name);
        content.setName(name);
        content.setOwner(user);
        content.setContent("test!".getBytes());
        content.setContentEncoding("UTF-8");
        content.setContentLanguage("en_US");
        content.setContentType("text/plain");
        
        return content;
    }

    /**
     * Makes dummy item.
     * @param user The user.
     * @return The note item.
     */
    public NoteItem makeDummyItem(User user) {
        return makeDummyItem(user, null);
    }

    /**
     * Makes dummy item.
     * @param user The user.
     * @param name The name.
     * @return The note item.
     */
    public NoteItem makeDummyItem(User user,
                                  String name) {
        String serial =Integer.toString(++iseq);
        if (name == null) {
            name = "test item " + serial;
        }

        NoteItem note = entityFactory.createNote();

        note.setUid(name);
        note.setName(name);
        note.setOwner(user);
        note.setIcalUid(serial);
        note.setBody("This is a note. I love notes.");
       
        return note;
    }

    /**
     * Makes dummy collection.
     * @param user The user.
     * @return The collection item.
     */
    public CollectionItem makeDummyCollection(User user) {
        String serial = Integer.toString(++lseq);
        String name = "test collection " + serial;

        CollectionItem collection = entityFactory.createCollection();
        collection.setUid(serial);
        collection.setName(name);
        collection.setDisplayName(name);
        collection.setOwner(user);
       
        return collection;
    }
    
    /**
     * Makes dummy calendar collection.
     * @param user The user.
     * @return The collection item.
     */
    public CollectionItem makeDummyCalendarCollection(User user) {
        return makeDummyCalendarCollection(user, null);
    }

    /**
     * Makes dummy calendar collection.
     * @param user The user.
     * @param name The name.
     * @return The collection item.
     */
    public CollectionItem makeDummyCalendarCollection(User user, String name) {
        String serial = Integer.toString(++lseq);
        if (name == null) {
            name = "test calendar collection " + serial;
        }

        CollectionItem collection = entityFactory.createCollection();
        collection.setUid(serial);
        collection.setName(name);
        collection.setDisplayName(name);
        collection.setOwner(user);
       
        collection.addStamp(entityFactory.createCalendarCollectionStamp(collection));

        return collection;
    }

    /**
     * Gets input stream.
     * @param name The name.
     * @return The input stream.
     */
    public InputStream getInputStream(String name){
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
        if (in == null) {
            throw new IllegalStateException("resource " + name + " not found");
        }
        return in;
    }
    
    /**
     * Gets bytes.
     * @param name The name.
     * @return The bytes.
     * @throws IOException - if something is wrong this exception is thrown.
     */
    public byte[] getBytes(String name) throws IOException {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
        if (in == null) {
            throw new IllegalStateException("resource " + name + " not found");
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        IOUtils.copy(in, bos);
        return bos.toByteArray();
    }

    /**
     * Gets reader.
     * @param name The name.
     * @return The reader.
     */
    public Reader getReader(String name) {
        try {
            byte[] buf = IOUtils.toByteArray(getInputStream(name));
            return new StringReader(new String(buf));
        } catch (IOException e) {
            throw new CosmoIOException("error converting input stream to reader", e);
        }
    }

    /**
     * Gets entity factory.
     * @return The entity factory.
     */
    public EntityFactory getEntityFactory() {
        return entityFactory;
    }

    /**
     * Sets entity factory.
     * @param entityFactory Entity factory.
     */
    public void setEntityFactory(EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }
    
    
}
