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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.unitedinternet.cosmo.boot.CalendarTestApplication;

import ch.vorburger.mariadb4j.springframework.MariaDB4jSpringService;

/**
 * Abstract Spring DAO test case.
 *
 */
@Rollback
@Transactional
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = CalendarTestApplication.class)
@ActiveProfiles(value = { "test" })
public abstract class AbstractSpringDaoTestCase {

    private static Logger LOG = LoggerFactory.getLogger(AbstractSpringDaoTestCase.class);

    private static volatile MariaDB4jSpringService mariaDB = new MariaDB4jSpringService();
    
    @PersistenceContext
    private EntityManager entityManager;

    protected HibernateTestHelper helper;

    @Before
    public void setUp() {
        this.helper = new HibernateTestHelper();
    }

    public void clearSession() {
        this.entityManager.flush();
        this.entityManager.clear();
        this.entityManager.close();
    }
    
    @BeforeClass
    public static void startMariaDB() {
        if (mariaDB.isRunning()) {
            return;
        }

        LOG.info("\n\n[DB] Embedded MariaDB test instance about to start... \n\n");
        mariaDB.setDefaultBaseDir("target/maridb/base");
        mariaDB.setDefaultDataDir("target/maridb/data");
        mariaDB.setDefaultPort(33060);
        mariaDB.start();

        LOG.info("\n\n[DB] - Started MariaDB test instance.\n\n");

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                LOG.info("[DB] - About to shutdown MariaDB test instance.");
                mariaDB.stop();
                LOG.info("[DB] - Shutdown MariaDB test instance.");
            }
        }));
    }
    

   
}
