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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.unitedinternet.cosmo.model.HomeCollectionItem;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.service.ServiceEvent;
import org.unitedinternet.cosmo.service.ServiceListener;

/**
 * A <code>ServiceListener</code> that is responsible for creating
 * out-of-the-box collections and items when a user account is
 * created.
 */
public class OutOfTheBoxListener implements ServiceListener {
    private static final Log LOG = LogFactory.getLog(ActivationListener.class);

    private OutOfTheBoxHelper helper;
    private OutOfTheBoxContext context;

    public OutOfTheBoxListener(OutOfTheBoxHelper helper,
                               OutOfTheBoxContext context) { 
        this.helper = helper;
        this.context = context;
    }
    
    public void before(ServiceEvent se) {
        // nothing needs doing
    }

    public void after(ServiceEvent se) {
        if (! se.getId().equals("CREATE_USER")) {
            return;
        }

        User user = (User) se.getState()[0];
        if (user.isOverlord()) {
            return;
        }

        HomeCollectionItem home = (HomeCollectionItem) se.getState()[1];

        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating out-of-the-box collection for user " +
                      user.getUsername());
        }
        context.setUser(user);
        context.setHomeCollection(home);
        helper.createOotbCollection(context);
        // XXX document the possible exceptions
    }
}
