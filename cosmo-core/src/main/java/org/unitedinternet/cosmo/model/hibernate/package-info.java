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


/*
 * Type Definitions for custom hibernate data types.
 */
@TypeDefs({
    @TypeDef(
            name="bufferedcontent_blob",
            typeClass = org.unitedinternet.cosmo.hibernate.BufferedContentBlob.class
        ),

    @TypeDef(
            name="calendar_clob",
            typeClass = org.unitedinternet.cosmo.hibernate.CalendarClobType.class
    ),

    @TypeDef(
            name="xml_clob",
            typeClass = org.unitedinternet.cosmo.hibernate.XmlClobType.class
    ),
    
    @TypeDef(
            name="composite_calendar",
            typeClass = org.unitedinternet.cosmo.hibernate.CalendarType.class
    ),
    
    @TypeDef(
            name="long_timestamp",
            typeClass = org.unitedinternet.cosmo.hibernate.LongTimestampType.class
    ),
    
    @TypeDef(
            name="boolean_integer",
            typeClass = org.unitedinternet.cosmo.hibernate.BooleanIntegerType.class
    )
})

/*
 * Named Queries
 */
@NamedQueries({
    // Item Queries
        @NamedQuery(name = "homeCollection.by.ownerId", query = "from HibHomeCollectionItem where owner.id=:ownerid"),
        @NamedQuery(name = "item.by.ownerId.parentId.name", query = "select item from HibItem item join"
                + " item.parentDetails pd where item.owner.id=:ownerid and "
                + "pd.primaryKey.collection.id=:parentid and item.name=:name"),
        @NamedQuery(name = "collections.children.by.parent", query = "select item from HibCollectionItem item join"
                + " item.parentDetails pd where pd.primaryKey.collection=:parent and item.class=HibCollectionItem"),
        @NamedQuery(name = "item.by.ownerId.nullParent.name", query = "select item from HibItem item"
                + " where item.owner.id=:ownerid and size(item.parentDetails)=0 and item.name=:name"),
        @NamedQuery(name = "item.by.ownerId.nullParent.name.minusItem", query = "select item from"
                + " HibItem item where item.id!=:itemid and item.owner.id=:ownerid and "
                + "size(item.parentDetails)=0 and item.name=:name"),
        @NamedQuery(name = "item.by.ownerId.parentId.name.minusItem", query = "select item from HibItem "
                + "item join item.parentDetails pd where item.id!=:itemid and item.owner.id=:ownerid and"
                + " pd.primaryKey.collection.id=:parentid and item.name=:name"),
        @NamedQuery(name = "itemId.by.parentId.name", query = "select item.id from HibItem item join"
                + " item.parentDetails pd where pd.primaryKey.collection.id=:parentid and item.name=:name"),
        @NamedQuery(name = "item.by.uid", query = "from HibItem i where i.uid=:uid"),
        @NamedQuery(name = "item.stamps.by.uid", query = "select i.stamps from HibItem i where i.uid=:uid"),
        @NamedQuery(name = "itemid.by.uid", query = "select i.id from HibItem i where i.uid=:uid"),
        @NamedQuery(name = "collectionItem.by.uid", query = "from HibCollectionItem i where i.uid=:uid"),
        @NamedQuery(name = "contentItem.by.uid", query = "from HibContentItem i where i.uid=:uid"),
        @NamedQuery(name = "item.by.parent.name", query = "select item from HibItem item join"
                + " item.parentDetails pd where pd.primaryKey.collection=:parent and item.name=:name"),
        // FIXME stfl .and.nullparent is not the correct name anymore!
        // FIXME check on class == HibCollectionItem  or select from HibCollectionItem
        @NamedQuery(name = "item.by.ownerName.name.nullParent", query = "select i from "
                + "HibCollectionItem i, HibUser u where i.owner=u and u.username=:username and" + " i.name=:name"),
        @NamedQuery(name = "contentItem.by.parent.timestamp", query = "select item from HibContentItem "
                + "item left join fetch item.stamps left join fetch item.attributes left join fetch"
                + " item.tombstones join item.parentDetails pd where pd.primaryKey.collection=:parent and "
                + "item.modifiedDate>:timestamp"),
        @NamedQuery(name = "contentItem.by.parent", query = "select item from HibContentItem item left "
                + "join fetch item.stamps left join fetch item.attributes left join fetch item.tombstones"
                + " join item.parentDetails pd where pd.primaryKey.collection=:parent"),
        @NamedQuery(name = "noteItemId.by.parent.icaluid", query = "select item.id from HibNoteItem item"
                + " join item.parentDetails pd where pd.primaryKey.collection.id=:parentid and "
                + "item.icalUid=:icaluid and item.modifies is null"),
        @NamedQuery(name = "icalendarItem.by.parent.icaluid", query = "select item.id from "
                + "HibICalendarItem item join item.parentDetails pd where"
                + " pd.primaryKey.collection.id=:parentid and item.icalUid=:icaluid"),
        @NamedQuery(name = "contentItem.by.owner", query = "from HibContentItem i where i.owner=:owner"),

    // Ticket Queries
    @NamedQuery(name="ticket.by.key", query="from HibTicket t where t.key=:key"),
    
    
    // User Queries
    @NamedQuery(name="user.byUsername", query="from HibUser where username=:username"),
    @NamedQuery(name="user.byEmail", query="from HibUser where email=:email"),
        @NamedQuery(name = "user.byUsernameOrEmail", query = "from HibUser where"
                + " id!=:userid and (username=:username or email=:email)"),
        @NamedQuery(name = "user.byUid", query = "from HibUser where uid=:uid"),
        @NamedQuery(name = "user.byActivationId", query = "from HibUser where activationid=:activationId"),
        @NamedQuery(name = "user.all", query = "from HibUser"),
        @NamedQuery(name = "user.count", query = "select count(id) from HibUser"),
        @NamedQuery(name = "users.byPreference", query = "select u from HibUser u join u.preferences"
                + " preference where preference.key=:key and preference.value=:value"),

        // Scheduler Queries
        @NamedQuery(name = "users.withSchedules", query = "select u from HibUser u join u.preferences "
                + "preference where preference.key like 'cosmo.scheduler.job.%.enabled' and " + "preference.value='true'"),

        // Password Recovery entity query
        @NamedQuery(name = "passwordRecovery.byKey", query = "from HibPasswordRecovery where key=:key"),
        @NamedQuery(name = "passwordRecovery.delete.byUser", query = "delete from HibPasswordRecovery where user=:user"),

        // Event Queries
        @NamedQuery(name = "event.by.calendar.icaluid", query = "select i from HibNoteItem i join "
                + "i.parentDetails pd join i.stamps stamp where pd.primaryKey.collection=:calendar and "
                + "stamp.class=HibEventStamp and i.icalUid=:uid"),

        // Event Log Queries
        @NamedQuery(name = "logEntry.by.collection.date", query = "from HibEventLogEntry e where id1=:parentId "
                + "and entryDate between :startDate and :endDate")
    
})
package org.unitedinternet.cosmo.model.hibernate;

import org.hibernate.annotations.NamedQueries;
import org.hibernate.annotations.NamedQuery;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

