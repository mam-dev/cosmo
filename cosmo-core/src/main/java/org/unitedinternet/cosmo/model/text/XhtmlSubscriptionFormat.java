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
package org.unitedinternet.cosmo.model.text;

import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.CollectionSubscription;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.Ticket;

/**
 * Parses and formats subscriptions in XHTML with a custom microformat (yet to be described.)
 */
public class XhtmlSubscriptionFormat extends BaseXhtmlFormat implements SubscriptionFormat {
    public CollectionSubscription parse(String source, EntityFactory entityFactory) {
        throw new UnsupportedOperationException();
    }

    public String format(CollectionSubscription sub) {
        return format(sub, false, null, false, null);
    }

    public String format(CollectionSubscription sub, CollectionItem collection, Ticket ticket) {
        return format(sub, true, collection, true, ticket);
    }

    private String format(CollectionSubscription sub, boolean isCollectionProvided, CollectionItem collection,
            boolean isTicketProvided, Ticket ticket) {
        throw new UnsupportedOperationException();
    }
}
