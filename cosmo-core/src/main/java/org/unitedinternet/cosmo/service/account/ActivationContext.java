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
package org.unitedinternet.cosmo.service.account;

import java.util.Locale;

import org.unitedinternet.cosmo.model.User;

public class ActivationContext {
    public static final String LINK_TEMPLATE_VAR_ACTIVATION_ID = 
        "activation-id";

    private Locale locale;
    private boolean activationRequired = true;
    private User sender;
    private String activationLinkTemplate;
    private String hostname;
    
    /**
     * Creates new <code>ActivationContext</code>.
     *
     * @param locale
     */
    public ActivationContext(){
    }

    /**
     * Creates new <code>ActivationContext</code> with
     * specified <code>locale</code> and
     * <code>activationRequired = true</code>.
     *
     * @param locale
     */
    public ActivationContext(Locale locale){
        this();
        setLocale(locale);
    }

    /**
     * Creates new <code>ActivationContext</code> with
     * specified <code>locale</code> and
     * <code>activationRequired</code>.
     *
     * @param locale
     * @param activationRequired
     */
    public ActivationContext(Locale locale,
            boolean activationRequired){
        this(locale);
        setActivationRequired(activationRequired);
    }

    public boolean isActivationRequired() {
        return activationRequired;
    }
    public void setActivationRequired(boolean activationRequired) {
        this.activationRequired = activationRequired;
    }
    public Locale getLocale() {
        return locale;
    }
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public String getActivationLinkTemplate() {
        return activationLinkTemplate;
    }

    public void setActivationLinkTemplate(String activationLinkTemplate) {
        this.activationLinkTemplate = activationLinkTemplate;
    }
    
    public String getActivationLink(String activationId){
        return this.activationLinkTemplate.replace(
                '{' + ActivationContext.LINK_TEMPLATE_VAR_ACTIVATION_ID + "}",
                activationId);
    }

    public String getHostname() {
        return this.hostname;
    }

    public void setHostname(String serverName) {
        this.hostname = serverName;
    }
}
