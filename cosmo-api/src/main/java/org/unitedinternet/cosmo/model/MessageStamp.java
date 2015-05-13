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

import java.io.Reader;

/**
 * Stamp that associates message-specific attributes to an item.
 */
public interface MessageStamp extends Stamp{

    public static final String FORMAT_DATE_SENT = "EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z";
    
    // Property accessors
    public String getMessageId();

    public void setMessageId(String id);

    public String getHeaders();

    public void setHeaders(String headers);

    public void setHeaders(Reader headers);

    public String getFrom();

    public void setFrom(String from);

    public String getTo();

    public void setTo(String to);

    public String getBcc();

    public void setBcc(String bcc);

    public String getCc();

    public void setCc(String cc);

    public String getOriginators();

    public void setOriginators(String originators);

    public String getDateSent();

    public void setDateSent(String dateSent);

    public String getInReplyTo();

    public void setInReplyTo(String inReplyTo);

    public String getReferences();

    public void setReferences(String references);

    public void setReferences(Reader references);

}