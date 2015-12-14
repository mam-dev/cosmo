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
package org.unitedinternet.cosmo.model.mock;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.Calendar;

import org.unitedinternet.cosmo.model.AvailabilityItem;
import org.unitedinternet.cosmo.model.BinaryAttribute;
import org.unitedinternet.cosmo.model.CalendarAttribute;
import org.unitedinternet.cosmo.model.CalendarCollectionStamp;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.CollectionSubscription;
import org.unitedinternet.cosmo.model.DecimalAttribute;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.EventExceptionStamp;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.FileItem;
import org.unitedinternet.cosmo.model.FreeBusyItem;
import org.unitedinternet.cosmo.model.IntegerAttribute;
import org.unitedinternet.cosmo.model.MessageStamp;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.PasswordRecovery;
import org.unitedinternet.cosmo.model.Preference;
import org.unitedinternet.cosmo.model.QName;
import org.unitedinternet.cosmo.model.StringAttribute;
import org.unitedinternet.cosmo.model.TaskStamp;
import org.unitedinternet.cosmo.model.TextAttribute;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.TicketType;
import org.unitedinternet.cosmo.model.TriageStatus;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.XmlAttribute;
import org.unitedinternet.cosmo.util.VersionFourGenerator;
import org.w3c.dom.Element;

/**
 * EntityFactory implementation that uses mock objects.
 */
public class MockEntityFactory implements EntityFactory {

    private VersionFourGenerator idGenerator = new VersionFourGenerator();
    
    /**
     * Creates collection.
     * {@inheritDoc}
     * @return collection item.
     */
    public CollectionItem createCollection() {
        return new MockCollectionItem();
    }
    
    public CollectionItem createCollection(String targetUri) {
        return new MockCollectionItem();
    }
    
    /**
     * Creates note.
     * {@inheritDoc}
     * @return note item.
     */
    public NoteItem createNote() {
        return new MockNoteItem();
    }

    /**
     * Creates availability.
     * {@inheritDoc}
     * @return availability item.
     */
    public AvailabilityItem createAvailability() {
        return new MockAvailabilityItem();
    }

    /**
     * Creates binary attribute.
     * {@inheritDoc}
     * @param qname The name.
     * @param bytes The bytes.
     * @return binary attribute
     */
    public BinaryAttribute createBinaryAttribute(QName qname, byte[] bytes) {
        return new MockBinaryAttribute(qname, bytes);
    }

    /**
     * Creates binary attribute.
     * {@inheritDoc}
     * @param qname The name.
     * @param is The input stream.
     * @return binary attribute
     */
    public BinaryAttribute createBinaryAttribute(QName qname, InputStream is) {
        return new MockBinaryAttribute(qname, is);
    }

    /**
     * Creates calendar attribute.
     * {@inheritDoc}
     * @param qname The name.
     * @param cal The calendar.
     * @return calendar attribute.
     */
    public CalendarAttribute createCalendarAttribute(QName qname, Calendar cal) {
        return new MockCalendarAttribute(qname, cal);
    }

    /**
     * Creates calendar collection stamp.
     * {@inheritDoc}
     * @param col The collecton item.
     * @return calendar collection stamp.
     */
    public CalendarCollectionStamp createCalendarCollectionStamp(CollectionItem col) {
        return new MockCalendarCollectionStamp(col);
    }
    
    /**
     * Creates collection subscription.
     * @return The collection subscription.
     * {@inheritDoc}
     */
    public CollectionSubscription createCollectionSubscription() {
        return new MockCollectionSubscription();
    }

    /**
     * Creates decimal attribute.
     * {@inheritDoc}
     * @param qname The name.
     * @param bd The big decimal.
     * @return decimal attribute.
     */
    public DecimalAttribute createDecimalAttribute(QName qname, BigDecimal bd) {
        return new MockDecimalAttribute(qname, bd);
    }
    
    /**
     * Creates xml attribute.
     * @param qname The name.
     * @param e The element.
     * @return xml attribute.
     * {@inheritDoc}
     */
    public XmlAttribute createXMLAttribute(QName qname, Element e) {
        return new MockXmlAttribute(qname, e);
    }

    /**
     * Creates event exception stamp.
     * @param note The note item.
     * {@inheritDoc}
     * @return The event exception stamp.
     */
    public EventExceptionStamp createEventExceptionStamp(NoteItem note) {
        return new MockEventExceptionStamp(note);
    }

    /**
     * Creates event stamp.
     * {@inheritDoc}
     * @param note The note item.
     * @return The event stamp.
     */
    public EventStamp createEventStamp(NoteItem note) {
        return new MockEventStamp(note);
    }

    /**
     * Creates file item.
     * {@inheritDoc}
     * @return The file item.
     */
    public FileItem createFileItem() {
        return new MockFileItem();
    }

    /**
     * Creates free busy.
     * {@inheritDoc}
     * @return The free busy item.
     */
    public FreeBusyItem createFreeBusy() {
        return new MockFreeBusyItem();
    }

    /**
     * Creates integer attribute.
     * {@inheritDoc}
     * @param qname The name.
     * @param longVal LongVal.
     * @return Integer attribute.
     */
    public IntegerAttribute createIntegerAttribute(QName qname, Long longVal) {
        return new MockIntegerAttribute(qname, longVal);
    }

    /**
     * Creates message stamp.
     * {@inheritDoc}
     * @return message stamp.
     */
    public MessageStamp createMessageStamp() {
        return new MockMessageStamp();
    }

    /**
     * Creates password recovery.
     * {@inheritDoc}
     * @param user The user.
     * @param key The key.
     * @return password recovery.
     */
    public PasswordRecovery createPasswordRecovery(User user, String key) {
        return new MockPasswordRecovery(user, key);
    }

    /**
     * Creates preference.
     * {@inheritDoc}
     * @return preference.
     */
    public Preference createPreference() {
        return new MockPreference();
    }

    /**
     * Creates preference.
     * {@inheritDoc}
     * @param key The key.
     * @param value The value.
     * @return The preference.
     */
    public Preference createPreference(String key, String value) {
        return new MockPreference(key, value);
    }

    /**
     * Creates QName.
     * @param namespace The namespace.
     * @param localname The local name.
     * @return The QName.
     * {@inheritDoc}
     */
    public QName createQName(String namespace, String localname) {
        return new MockQName(namespace, localname);
    }

    /**
     * Creates string attribute.
     * @param qname The qname.
     * @param str Str.
     * @return The strin attribute.
     * {@inheritDoc}
     */
    public StringAttribute createStringAttribute(QName qname, String str) {
        return new MockStringAttribute(qname, str);
    }

    /**
     * Creates task stamp.
     * {@inheritDoc}
     * @return The task stamp.
     */
    public TaskStamp createTaskStamp() {
        return new MockTaskStamp();
    }

    /**
     * Creates text attribute.
     * {@inheritDoc}
     * @param qname The qname.
     * @param reader The reader.
     * @return The text attribute.
     */
    public TextAttribute createTextAttribute(QName qname, Reader reader) {
        return new MockTextAttribute(qname, reader);
    }

    /**
     * Creates ticket.
     * {@inheritDoc}
     * @param type The type.
     * @return The ticket.
     */
    public Ticket createTicket(TicketType type) {
        return new MockTicket(type);
    }

    /**
     * Creates triage status.
     * {@inheritDoc}
     * @return The triage status.
     */
    public TriageStatus createTriageStatus() {
        return new MockTriageStatus();
    }

    /**
     * Creates user.
     * {@inheritDoc}
     * @return The user.
     */
    public User createUser() {
        return new MockUser();
    }

    /**
     * Creates ticket.
     * {@inheritDoc}
     * @return The ticket.
     */
    public Ticket creatTicket() {
        return new MockTicket();
    }

    /**
     * generates uid.
     * {@inheritDoc}
     * @return The id generated.
     */
    public String generateUid() {
        return idGenerator.nextStringIdentifier();
    }

}
