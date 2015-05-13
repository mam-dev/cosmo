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

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Test Hibernate server property dao.
 */
public class HibernateServerPropertyDaoTest extends AbstractHibernateDaoTestCase {

    @Autowired
    private ServerPropertyDaoImpl serverPropertyDao;
    
    /**
     * The constructor.
     */
    public HibernateServerPropertyDaoTest() {
        super();
    }
    
    /**
     * Tests server properties.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testServerProperties() throws Exception {
        serverPropertyDao.setServerProperty("testprop1", "testvalue1");
        clearSession();
        String propValue = serverPropertyDao.getServerProperty("testprop1");
        Assert.assertEquals("testvalue1", propValue);
        clearSession();
        serverPropertyDao.setServerProperty("testprop1", "testvalue2");
        clearSession();
        propValue = serverPropertyDao.getServerProperty("testprop1");
        Assert.assertEquals("testvalue2", propValue);
    }
}
