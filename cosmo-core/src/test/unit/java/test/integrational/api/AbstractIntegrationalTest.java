package test.integrational.api;

import ch.vorburger.mariadb4j.springframework.MariaDB4jSpringService;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import test.integrational.api.app.IntegrationalTestApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = IntegrationalTestApplication.class)
@ActiveProfiles(value = { "test" })
public abstract class AbstractIntegrationalTest {
    @Autowired
    protected static volatile MariaDB4jSpringService mariaDB = new MariaDB4jSpringService();


 }

