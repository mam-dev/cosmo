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
package org.unitedinternet.cosmo.dao.hibernate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.fortuna.ical4j.model.Calendar;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.unitedinternet.cosmo.calendar.util.CalendarUtils;
import org.unitedinternet.cosmo.dao.ContentDao;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.QName;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.hibernate.HibItem;
import org.unitedinternet.cosmo.model.hibernate.HibUser;

/**
 * Test Hibernate helper.
 *
 */
public class HibernateTestHelper {
    /**
     * Creates dummy user.
     * @param userDao The userDao.
     * @param index The index.
     * @return The user.
     */
    public User createDummyUser(UserDao userDao, int index) {
        User user = new HibUser();
        user.setUsername("user" + index);
        user.setPassword("password" + index);
        user.setEmail("user" + index + "@test" + index);
        user.setAdmin(Boolean.TRUE);
        user.setFirstName("fristname" + index);
        user.setLastName("lastname" + index);
        return userDao.createUser(user);
    }

    /**
     * Verify item.
     * @param item1 Item1.
     * @param item2 Item2.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    public void verifyItem(Item item1, Item item2) throws Exception {
        Assert.assertEquals(item1.getName(), item2.getName());
        Assert.assertEquals(item1.getCreationDate(), item2.getCreationDate());
        Assert.assertEquals(item1.getClientCreationDate(), item2.getClientCreationDate());
        Assert.assertEquals(item1.getClientModifiedDate(), item2.getClientModifiedDate());
        Assert.assertEquals(item1.getModifiedDate(), item2.getModifiedDate());
        Assert.assertEquals(item1.getDisplayName(), item2.getDisplayName());
        Assert.assertEquals(getHibItem(item1).getId(), getHibItem(item2).getId());
        Assert.assertEquals(item1.getUid(), item2.getUid());
        Assert.assertEquals(item1.getAttributes().size(), item2.getAttributes()
                .size());
        for (@SuppressWarnings("rawtypes")
        Iterator it = item1.getAttributes().keySet().iterator(); it
                .hasNext();) {
            QName key = (QName) it.next();
            Object val1 = item1.getAttributeValue(key);
            Object val2 = item2.getAttributeValue(key);
            verifyAttributeValue(val1, val2);
        }

    }

    /**
     * Verify item in collection.
     * @param items The items.
     * @param item The item.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    public void verifyItemInCollection(@SuppressWarnings("rawtypes") Collection items, Item item)
            throws Exception {
        for (@SuppressWarnings("rawtypes")
        Iterator it = items.iterator(); it.hasNext();) {
            Item nextItem = (Item) it.next();
            if (nextItem.getUid().equals(item.getUid())) {
                return;
            }
        }
        Assert.fail("Item not in collection");
    }

    /**
     * Verify map.
     * @param val1 Val1.
     * @param val2 Val2.
     */
    public void verifyMap(@SuppressWarnings("rawtypes") Map val1, @SuppressWarnings("rawtypes") Map val2) {
        Assert.assertEquals(val1.size(), val2.size());
        for (@SuppressWarnings("rawtypes")
        Iterator keys = val1.keySet().iterator(); keys.hasNext();) {
            String key = (String) keys.next();
            Assert.assertEquals(val1.get(key), val2.get(key));
        }
    }

    /**
     * Verify set.
     * @param val1 Val1.
     * @param val2 Val2.
     */
    public void verifySet(@SuppressWarnings("rawtypes") Set val1, @SuppressWarnings("rawtypes") Set val2) {
        Assert.assertEquals(val1.size(), val2.size());
        for (@SuppressWarnings("rawtypes")
        Iterator elems = val1.iterator(); elems.hasNext();) {
            Assert.assertTrue(val2.contains(elems.next()));
        }
    }

    /**
     * verify date.
     * @param val1 Val1.
     * @param val2 Val2.
     */
    public void verifyDate(Date val1, Date val2) {
        Assert.assertEquals(val1.getTime(), val2.getTime());
    }

    /**
     * Verify attribute value.
     * @param val1 Val1.
     * @param val2 Val2.
     * @throws Exception - if something is wrong this exception is thrown. 
     */
    @SuppressWarnings("rawtypes")
    public void verifyAttributeValue(Object val1, Object val2) throws Exception {
        if (val1 instanceof String) {
            Assert.assertEquals(val1, val2);
        }
        else if (val1 instanceof byte[]) {
            verifyBytes((byte[]) val1, (byte[]) val2);
        }
        else if (val1 instanceof Set) {
            verifySet((Set) val1, (Set) val2);
        }
        else if (val1 instanceof Map) {
            verifyMap((Map) val1, (Map) val2);
        }
        else if (val1 instanceof Date) {
            verifyDate((Date) val1, (Date) val2);
        }
        else if (!val1.equals(val2)) {
            Assert.fail("attributes not equal");
        }
    }

    /**
     * Gets bytes.
     * @param name The name.
     * @return byte[].
     * @throws Exception - if something is wrong this exception is thrown.
     */
    public byte[] getBytes(String name) throws Exception {
        InputStream in = getClass().getClassLoader().getResourceAsStream(name);
        if (in == null) {
            throw new IllegalStateException("resource " + name + " not found");
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        IOUtils.copy(in, bos);
        return bos.toByteArray();
    }
    
    /**
     * Gets input stream.
     * @param name The name.
     * @return The input stream.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    public InputStream getInputStream(String name) throws Exception {
        InputStream in = getClass().getClassLoader().getResourceAsStream(name);
        if (in == null) {
            throw new IllegalStateException("resource " + name + " not found");
        }
        return in;
    }
    
    /**
     * Gets calendar.
     * @param name The name.
     * @return The calendar.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    public Calendar getCalendar(String name) throws Exception {
        InputStream in = getClass().getClassLoader().getResourceAsStream(name);
        return CalendarUtils.parseCalendar(in);
    }

    /**
     * Verify input stream.
     * @param is1 The input stream.
     * @param content The content.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    public void verifyInputStream(InputStream is1, byte[] content)
            throws Exception {
        byte[] buf1 = new byte[4096];
        byte[] buf2 = new byte[4096];

        ByteArrayInputStream is2 = new ByteArrayInputStream(content);

        int read1 = is1.read(buf1);
        int read2 = is2.read(buf2);
        while (read1 > 0 || read2 > 0) {
            Assert.assertEquals(read1, read2);

            for (int i = 0; i < read1; i++) {
                Assert.assertEquals(buf1[i], buf2[i]);
            }

            read1 = is1.read(buf1);
            read2 = is2.read(buf2);
        }
    }

    /**
     * Verify bytes.
     * @param content1 Content1.
     * @param content2 Content2
     * @throws Exception - if something is wrong this exception is thrown.
     */
    public void verifyBytes(byte[] content1, byte[] content2) throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(content1);
        verifyInputStream(is, content2);
    }

    /**
     * Gets user.
     * @param userDao UserDao.
     * @param contentDao ContentDao.
     * @param username The username.
     * @return The user.
     */
    public User getUser(UserDao userDao, ContentDao contentDao, String username) {
        User user = userDao.getUser(username);
        if (user == null) {
            user = new HibUser();
            user.setUsername(username);
            user.setPassword(username);
            user.setEmail(username + "@testem");
            user.setAdmin(Boolean.TRUE);
            user.setFirstName("testfn");
            user.setLastName("testln");
            userDao.createUser(user);

            user = userDao.getUser(username);

            // create root item
            contentDao.createRootItem(user);
        }
        return user;
    }
    
    /**
     * Gets Hib Item.
     * @param item The item.
     * @return HibItem.
     */
    private HibItem getHibItem(Item item) {
        return (HibItem) item;
    }
}
