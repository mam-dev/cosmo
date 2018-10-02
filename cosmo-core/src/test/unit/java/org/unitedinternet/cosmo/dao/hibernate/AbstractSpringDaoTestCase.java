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

import javax.transaction.Transactional;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.unitedinternet.cosmo.app.CalendarApplication;

import ch.vorburger.mariadb4j.springframework.MariaDB4jSpringService;

/**
 * Abstract Spring Dao test case.
 *
 */
// @RunWith(SpringJUnit4ClassRunner.class)
// @ContextConfiguration(locations={ //TODO - remove this
// "classpath:applicationContext-test.xml",
// "classpath:applicationContext-services.xml",
// "classpath:applicationContext-security-dav.xml",
// "classpath:applicationContext-dao.xml"})
@Rollback
@Transactional

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = CalendarApplication.class)
public abstract class AbstractSpringDaoTestCase {

    private static Logger LOG = LoggerFactory.getLogger(AbstractSpringDaoTestCase.class);

    private static volatile MariaDB4jSpringService mariaDB = new MariaDB4jSpringService();

    @BeforeClass
    public static void startMariaDB() {
        if (mariaDB.isRunning()) {
            return;
        }
        
        LOG.info("\n\n [DB] starting \n\n");
        mariaDB.setDefaultBaseDir("target/maridb/base");
        mariaDB.setDefaultDataDir("target/maridb/data");
        mariaDB.setDefaultPort(33060);
        mariaDB.start();

        LOG.info("[DB] - Started MariaDB test instance.");
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
