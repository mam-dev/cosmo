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
package org.unitedinternet.cosmo.model;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.Calendar;

import org.w3c.dom.Element;

/**
 * Factory api for creating model objects.
 */
public interface EntityFactory {
    
    /**
     * Generate a unique identifier that can be used as
     * the uid of an entity.
     * @return unique identifier
     */
    public String generateUid();
    
    
    /**
     * Create new CollectionItem
     * @return new ColletionItem
     */
    public CollectionItem createCollection();

    public CollectionItem createCollection(String targetUri);
    
    /**
     * Create new NoteItem
     * @return new NoteItem
     */
    public NoteItem createNote();
    
    
    /**
     * Create new PasswordRecovery
     * @param user associated user
     * @param key recovery key
     * @return new PasswordRecovery
     */
    public PasswordRecovery createPasswordRecovery(User user, String key);
    
    
    /**
     * Create new User
     * @return new User
     */
    public User createUser();
    
    
    /**
     * Create new CollectionSubscription
     * @return new CollectionSubscription
     */
    public CollectionSubscription createCollectionSubscription();
    
    
    /**
     * Create new AvailabilityItem
     * @return new AvailabilityItem
     */
    public AvailabilityItem createAvailability();
    
    
    /**
     * Create new CalendarCollectionStamp
     * @param col associated CollectionItem
     * @return new CalendarCollectionStamp
     */
    public CalendarCollectionStamp createCalendarCollectionStamp(CollectionItem col);
    
    
    /**
     * Create new TriageStatus
     * @return new TriageStatus
     */
    public TriageStatus createTriageStatus();
    
    
    /**
     * Create new EventStamp
     * @param note associated NoteItem
     * @return new EventStamp
     */
    public EventStamp createEventStamp(NoteItem note);
    
    
    /**
     * Create new EventExceptionStamp
     * @param note associated NoteItem
     * @return new EventExceptionStamp
     */
    public EventExceptionStamp createEventExceptionStamp(NoteItem note);
    
    
    /**
     * Create new FileItem
     * @return new FileItem
     */
    public FileItem createFileItem();
    
    
    /**
     * Create new FreeBusyItem
     * @return new FreeBusyItem
     */
    public FreeBusyItem createFreeBusy();
    
    
    /**
     * Create new QName
     * @param namespace 
     * @param localname
     * @return new QName
     */
    public QName createQName(String namespace, String localname);
    
    
    /**
     * Create new TaskStamp
     * @return new TaskStamp
     */
    public TaskStamp createTaskStamp();
    
    
    /**
     * Create new MessageStamp
     * @return new MessageStamp
     */
    public MessageStamp createMessageStamp();
    
    
    /**
     * Create new Ticket
     * @return new Ticket
     */
    public Ticket creatTicket();
    
    
    /**
     * Create new Ticket with specified type.
     * @param type ticket type
     * @return new Ticket
     */
    public Ticket createTicket(TicketType type);
    
    
    /**
     * Create new Preference
     * @return new Preference
     */
    public Preference createPreference();
    
    
    /**
     * Create new Preference with specified key and value
     * @param key
     * @param value
     * @return new Preference
     */
    public Preference createPreference(String key, String value);
    
    
    /**
     * Create new BinaryAttribute using InpuStream.
     * @param qname QName of attribute
     * @param is data
     * @return new BinaryAttribute
     */
    public BinaryAttribute createBinaryAttribute(QName qname, InputStream is);
    
    /**
     * Create new BinaryAttribute using byte array.
     * @param qname QName of attribute
     * @param is data
     * @return new BinaryAttribute
     */
    public BinaryAttribute createBinaryAttribute(QName qname, byte[] bytes);
    
    /**
     * Create new TextAttribute using Reader.
     * @param qname QName of attribute
     * @param reader text value
     * @return new TextAttribute
     */
    public TextAttribute createTextAttribute(QName qname, Reader reader);
    
    
    /**
     * Create new DecimalAttribute using BigDecimal
     * @param qname QName of attribute
     * @param bd decimal value
     * @return new DecimalAttribute
     */
    public DecimalAttribute createDecimalAttribute(QName qname, BigDecimal bd);
    
    
    /**
     * Create new CalendarAttribute using Calendar
     * @param qname QName of attribute
     * @param cal calendar value
     * @return new CalendarAttribute
     */
    public CalendarAttribute createCalendarAttribute(QName qname, Calendar cal);
    
    /**
     * Create new IntegerAttribute using Long
     * @param qname QName of attribute
     * @param longVal integer value
     * @return new IntegerAttribute
     */
    public IntegerAttribute createIntegerAttribute(QName qname, Long longVal);
    
    /**
     * Create new StringAttribute using string value
     * @param qname QName of attribute
     * @param str string value
     * @return new StringAttribute
     */
    public StringAttribute createStringAttribute(QName qname, String str);
    
    /**
     * Create new XMLAttribute using element value
     * @param qname QName of attribute
     * @param element element value
     * @return new XMLAttribute
     */
    public XmlAttribute createXMLAttribute(QName qname, Element e);

}
