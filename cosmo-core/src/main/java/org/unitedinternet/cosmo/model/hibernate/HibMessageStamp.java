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
package org.unitedinternet.cosmo.model.hibernate;

import java.io.Reader;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.MessageStamp;
import org.unitedinternet.cosmo.model.QName;
import org.unitedinternet.cosmo.model.Stamp;


/**
 * Hibernate persistent MessageStamp.
 */
@Entity
@DiscriminatorValue("message")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class HibMessageStamp extends HibStamp implements MessageStamp {

    private static final long serialVersionUID = -6100568628972081120L;
    
    public static final QName ATTR_MESSAGE_ID = new HibQName(
            MessageStamp.class, "messageId");
    
    public static final QName ATTR_MESSAGE_HEADERS = new HibQName(
            MessageStamp.class, "headers");
    
    public static final QName ATTR_MESSAGE_FROM = new HibQName(
            MessageStamp.class, "from");
    
    public static final QName ATTR_MESSAGE_TO = new HibQName(
            MessageStamp.class, "to");
    
    public static final QName ATTR_MESSAGE_CC = new HibQName(
            MessageStamp.class, "cc");
    
    public static final QName ATTR_MESSAGE_BCC = new HibQName(
            MessageStamp.class, "bcc");
    
    public static final QName ATTR_MESSAGE_ORIGINATORS = new HibQName(
            MessageStamp.class, "originators");
    
    public static final QName ATTR_MESSAGE_DATE_SENT = new HibQName(
            MessageStamp.class, "dateSent");
    
    public static final QName ATTR_MESSAGE_IN_REPLY_TO = new HibQName(
            MessageStamp.class, "inReplyTo");
    
    public static final QName ATTR_MESSAGE_REFERENCES = new HibQName(
            MessageStamp.class, "references");
    
    /** default constructor */
    public HibMessageStamp() {
    }
    
    public HibMessageStamp(Item item) {
        setItem(item);
    }
   
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.Stamp#getType()
     */
    public String getType() {
        return "message";
    }
    
    // Property accessors
   
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.MessageStamp#getMessageId()
     */
    public String getMessageId() {
        // id stored as StringAttribute on Item
        return HibStringAttribute.getValue(getItem(), ATTR_MESSAGE_ID);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.MessageStamp#setMessageId(java.lang.String)
     */
    public void setMessageId(String id) {
        // id stored as StringAttribute on Item
        HibStringAttribute.setValue(getItem(), ATTR_MESSAGE_ID, id);
        updateTimestamp();
    }
   
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.MessageStamp#getHeaders()
     */
    public String getHeaders() {
        // headers stored as TextAttribute on Item
        return HibTextAttribute.getValue(getItem(), ATTR_MESSAGE_HEADERS);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.MessageStamp#setHeaders(java.lang.String)
     */
    public void setHeaders(String headers) {
        // headers stored as TextAttribute on Item
        HibTextAttribute.setValue(getItem(), ATTR_MESSAGE_HEADERS, headers);
        updateTimestamp();
    }
   
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.MessageStamp#setHeaders(java.io.Reader)
     */
    public void setHeaders(Reader headers) {
        // headers stored as TextAttribute on Item
        HibTextAttribute.setValue(getItem(), ATTR_MESSAGE_HEADERS, headers);
        updateTimestamp();
    }
   
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.MessageStamp#getFrom()
     */
    public String getFrom() {
        // from stored as StringAttribute on Item
        return HibStringAttribute.getValue(getItem(), ATTR_MESSAGE_FROM);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.MessageStamp#setFrom(java.lang.String)
     */
    public void setFrom(String from) {
        // from stored as StringAttribute on Item
        HibStringAttribute.setValue(getItem(), ATTR_MESSAGE_FROM, from);
        updateTimestamp();
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.MessageStamp#getTo()
     */
    public String getTo() {
        // to stored as StringAttribute on Item
        return HibStringAttribute.getValue(getItem(), ATTR_MESSAGE_TO);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.MessageStamp#setTo(java.lang.String)
     */
    public void setTo(String to) {
        // to stored as StringAttribute on Item
        HibStringAttribute.setValue(getItem(), ATTR_MESSAGE_TO, to);
        updateTimestamp();
    }
   
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.MessageStamp#getBcc()
     */
    public String getBcc() {
        // bcc stored as StringAttribute on Item
        return HibStringAttribute.getValue(getItem(), ATTR_MESSAGE_BCC);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.MessageStamp#setBcc(java.lang.String)
     */
    public void setBcc(String bcc) {
        //bcc stored as StringAttribute on Item
        HibStringAttribute.setValue(getItem(), ATTR_MESSAGE_BCC, bcc);
        updateTimestamp();
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.MessageStamp#getCc()
     */
    public String getCc() {
        // cc stored as StringAttribute on Item
        return HibStringAttribute.getValue(getItem(), ATTR_MESSAGE_CC);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.MessageStamp#setCc(java.lang.String)
     */
    public void setCc(String cc) {
        // cc stored as StringAttribute on Item
        HibStringAttribute.setValue(getItem(), ATTR_MESSAGE_CC, cc);
        updateTimestamp();
    }
 
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.MessageStamp#getOriginators()
     */
    public String getOriginators() {
        // originators stored as StringAttribute on Item
        return HibStringAttribute.getValue(getItem(), ATTR_MESSAGE_ORIGINATORS);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.MessageStamp#setOriginators(java.lang.String)
     */
    public void setOriginators(String originators) {
        // originators stored as StringAttribute on Item
        HibStringAttribute.setValue(getItem(), ATTR_MESSAGE_ORIGINATORS, originators);
        updateTimestamp();
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.MessageStamp#getDateSent()
     */
    public String getDateSent() {
        // inReployTo stored as StringAttribute on Item
        return HibStringAttribute.getValue(getItem(), ATTR_MESSAGE_DATE_SENT);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.MessageStamp#setDateSent(java.lang.String)
     */
    public void setDateSent(String dateSent) {
        // inReployTo stored as TextAttribute on Item
        HibStringAttribute.setValue(getItem(), ATTR_MESSAGE_DATE_SENT, dateSent);
        updateTimestamp();
    }
  
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.MessageStamp#getInReplyTo()
     */
    public String getInReplyTo() {
        // inReployTo stored as StringAttribute on Item
        return HibStringAttribute.getValue(getItem(), ATTR_MESSAGE_IN_REPLY_TO);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.MessageStamp#setInReplyTo(java.lang.String)
     */
    public void setInReplyTo(String inReplyTo) {
        // inReployTo stored as TextAttribute on Item
        HibStringAttribute.setValue(getItem(), ATTR_MESSAGE_IN_REPLY_TO, inReplyTo);
        updateTimestamp();
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.MessageStamp#getReferences()
     */
    public String getReferences() {
        // references stored as TextAttribute on Item
        return HibTextAttribute.getValue(getItem(), ATTR_MESSAGE_REFERENCES);
    }

    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.MessageStamp#setReferences(java.lang.String)
     */
    public void setReferences(String references) {
        // references stored as TextAttribute on Item
        HibTextAttribute.setValue(getItem(), ATTR_MESSAGE_REFERENCES, references);
        updateTimestamp();
    }
    
    /* (non-Javadoc)
     * @see org.unitedinternet.cosmo.model.MessageStamp#setReferences(java.io.Reader)
     */
    public void setReferences(Reader references) {
        // references stored as TextAttribute on Item
        HibTextAttribute.setValue(getItem(), ATTR_MESSAGE_REFERENCES, references);
        updateTimestamp();
    }

    /**
     * Return MessageStamp from Item
     * @param item
     * @return MessageStamp from Item
     */
    public static MessageStamp getStamp(Item item) {
        return (MessageStamp) item.getStamp(MessageStamp.class);
    }
    
    public Stamp copy() {
        MessageStamp stamp = new HibMessageStamp();
        return stamp;
    }

    @Override
    public String calculateEntityTag() {
        return "";
    }
}
