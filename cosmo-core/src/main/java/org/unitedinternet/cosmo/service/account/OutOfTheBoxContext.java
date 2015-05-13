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
package org.unitedinternet.cosmo.service.account;

import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.unitedinternet.cosmo.model.HomeCollectionItem;
import org.unitedinternet.cosmo.model.User;

/**
 * A bean that stores information needed to construct the
 * out-of-the-box items.
 */
public class OutOfTheBoxContext {
    @SuppressWarnings("unused")
    private static final Log LOG = LogFactory.getLog(OutOfTheBoxContext.class);

    private User user;
    private HomeCollectionItem home;
    private Locale locale;
    private TimeZone timezone;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public HomeCollectionItem getHomeCollection() {
        return home;
    }

    public void setHomeCollection(HomeCollectionItem home) {
        this.home = home;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public TimeZone getTimezone() {
        return timezone;
    }

    public void setTimezone(TimeZone timeZone) {
        this.timezone = timeZone;
    }
}
