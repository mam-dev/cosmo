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

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.util.Dates;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitedinternet.cosmo.dao.ContentDao;
import org.unitedinternet.cosmo.model.CalendarCollectionStamp;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.EventStamp;
import org.unitedinternet.cosmo.model.MessageStamp;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.TaskStamp;
import org.unitedinternet.cosmo.model.TriageStatus;
import org.unitedinternet.cosmo.model.TriageStatusUtil;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.util.DateUtil;

/**
 * A helper class that creates out of the box collections and items
 * for a new user account.
 */
public class OutOfTheBoxHelper {
    @SuppressWarnings("unused")
    private static final Log LOG = LogFactory.getLog(OutOfTheBoxHelper.class);
    private static final TimeZoneRegistry TIMEZONE_REGISTRY =
        TimeZoneRegistryFactory.getInstance().createRegistry();

    private ContentDao contentDao;
    private EntityFactory entityFactory;

    /**
     * <p>
     * Creates a collection named like the user's full name. Inside
     * the collection, places a variety of welcome items.
     * @return The collection item created.
     * </p>
     */
    public CollectionItem createOotbCollection(OutOfTheBoxContext context) {
        CollectionItem initial =
            contentDao.createCollection(context.getHomeCollection(),
                                        makeCollection(context));

        contentDao.createContent(initial, makeWelcomeItem(context));

        NoteItem tryOut = (NoteItem)
            contentDao.createContent(initial, makeTryOutItem(context));
        // tryOut note should sort below welcome note
        BigDecimal tryOutRank = tryOut.getTriageStatus().getRank().
            subtract(BigDecimal.valueOf(1, 2));
        tryOut.getTriageStatus().setRank(tryOutRank);

        contentDao.createContent(initial, makeSignUpItem(context));

        return initial;
    }

    private CollectionItem makeCollection(OutOfTheBoxContext context) {
        CollectionItem collection = entityFactory.createCollection();
        User user = context.getUser();

        String name = "";
        String displayName = "";

        collection.setName(name);
        collection.setDisplayName(displayName);
        collection.setOwner(user);

        CalendarCollectionStamp ccs = entityFactory.createCalendarCollectionStamp(collection);
        collection.addStamp(ccs);

        return collection;
    }

    private NoteItem makeWelcomeItem(OutOfTheBoxContext context) {
        NoteItem item = entityFactory.createNote();
        TimeZone tz= context.getTimezone();
        Locale locale = context.getLocale();
        User user = context.getUser();

        String name = "Welcome to Chandler Server";
        String body = "1. Get a tour of Chandler @ chandlerproject.org/getstarted\n\n2. Consult our FAQ @ chandlerproject.org/faq\n\n3. Read about any known issues with the Preview release @ chandlerproject.org/knownissues\n\n4. Ask questions and give us feedback by joining the Chandler-Users mailing list @ chandlerproject.org/mailinglists. Find out how to get involved @ chandlerproject.org/getinvolved\n\n5. Learn more about the project and access a wide range of design, planning and developer documentation @ chandlerproject.org/wikihome";
        String from = "OSAF";
        String sentBy = "OSAF";
        String to = "";

        item.setUid(contentDao.generateUid());
        item.setDisplayName(name);
        item.setOwner(user);
        item.setClientCreationDate(Calendar.getInstance(tz, locale).getTime());
        item.setClientModifiedDate(item.getClientCreationDate());
        TriageStatus triage = entityFactory.createTriageStatus();
        TriageStatusUtil.initialize(triage);
        item.setTriageStatus(triage);
        item.setLastModifiedBy(user.getUsername());
        item.setLastModification(ContentItem.Action.CREATED);
        item.setSent(Boolean.FALSE);
        item.setNeedsReply(Boolean.FALSE);
        item.setIcalUid(item.getUid());
        item.setBody(body);

        Calendar start = Calendar.getInstance(tz, locale);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        DateTime startDate = (DateTime)
            Dates.getInstance(start.getTime(), Value.DATE_TIME);
        startDate.setTimeZone(vtz(tz.getID()));

        EventStamp es = entityFactory.createEventStamp(item);
        item.addStamp(es);
        es.createCalendar();
        es.setStartDate(startDate);
        es.setDuration(new Dur(0, 1, 0, 0));

        TaskStamp ts = entityFactory.createTaskStamp();
        item.addStamp(ts);

        MessageStamp ms = entityFactory.createMessageStamp();
        item.addStamp(ms);
        ms.setFrom(from);
        ms.setTo(to);
        ms.setOriginators(sentBy);
        ms.setDateSent(DateUtil.formatDate(MessageStamp.FORMAT_DATE_SENT,
                                           item.getClientCreationDate()));

        return item;
    }

    private NoteItem makeTryOutItem(OutOfTheBoxContext context) {
        NoteItem item = entityFactory.createNote();
        TimeZone tz= context.getTimezone();
        Locale locale = context.getLocale();
        User user = context.getUser();

        String name = "Download Chandler Desktop";
        String body = "Learn more @ chandlerproject.org\n\nDownload @ chandlerproject.org/download";

        TriageStatus triage = entityFactory.createTriageStatus();
        TriageStatusUtil.initialize(triage);
        triage.setCode(TriageStatus.CODE_LATER);

        item.setUid(contentDao.generateUid());
        item.setDisplayName(name);
        item.setOwner(user);
        item.setClientCreationDate(Calendar.getInstance(tz, locale).getTime());
        item.setClientModifiedDate(item.getClientCreationDate());
        item.setTriageStatus(triage);
        item.setLastModifiedBy(user.getUsername());
        item.setLastModification(ContentItem.Action.CREATED);
        item.setSent(Boolean.FALSE);
        item.setNeedsReply(Boolean.FALSE);
        item.setIcalUid(item.getUid());
        item.setBody(body);

        Calendar start = Calendar.getInstance(tz, locale);
        start.add(Calendar.DAY_OF_MONTH, 1);
        start.set(Calendar.HOUR_OF_DAY, 10);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        DateTime startDate = (DateTime)
            Dates.getInstance(start.getTime(), Value.DATE_TIME);
        startDate.setTimeZone(vtz(tz.getID()));

        EventStamp es = entityFactory.createEventStamp(item);
        item.addStamp(es);
        es.createCalendar();
        es.setStartDate(startDate);
        es.setDuration(new Dur(0, 1, 0, 0));

        TaskStamp ts = entityFactory.createTaskStamp();
        item.addStamp(ts);

        return item;
    }

    private NoteItem makeSignUpItem(OutOfTheBoxContext context) {
        NoteItem item = entityFactory.createNote();
        TimeZone tz= context.getTimezone();
        Locale locale = context.getLocale();
        User user = context.getUser();

        String name = "Sign up for a Chandler Server Account";

        TriageStatus triage = entityFactory.createTriageStatus();
        TriageStatusUtil.initialize(triage);
        triage.setCode(TriageStatus.CODE_DONE);

        item.setUid(contentDao.generateUid());
        item.setDisplayName(name);
        item.setOwner(user);
        item.setClientCreationDate(Calendar.getInstance(tz, locale).getTime());
        item.setClientModifiedDate(item.getClientCreationDate());
        item.setTriageStatus(triage);
        item.setLastModifiedBy(user.getUsername());
        item.setLastModification(ContentItem.Action.CREATED);
        item.setSent(Boolean.FALSE);
        item.setNeedsReply(Boolean.FALSE);
        item.setIcalUid(item.getUid());

        TaskStamp ts = entityFactory.createTaskStamp();
        item.addStamp(ts);

        return item;
    }

    public void init() {
        if (contentDao == null) {
            throw new IllegalStateException("contentDao is required");
        }
        if (entityFactory == null) {
            throw new IllegalStateException("entityFactory is required");
        }
    }
    
    

    public EntityFactory getEntityFactory() {
        return entityFactory;
    }

    public void setEntityFactory(EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }

    public ContentDao getContentDao() {
        return contentDao;
    }

    public void setContentDao(ContentDao contentDao) {
        this.contentDao = contentDao;
    }

    private net.fortuna.ical4j.model.TimeZone vtz(String tzid) {
        return TIMEZONE_REGISTRY.getTimeZone(tzid);
    }
}