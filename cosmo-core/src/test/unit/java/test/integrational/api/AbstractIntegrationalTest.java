package test.integrational.api;

import ch.vorburger.mariadb4j.springframework.MariaDB4jSpringService;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import test.integrational.api.app.IntegrationalTestApplication;

import java.net.URI;
import java.net.URISyntaxException;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = IntegrationalTestApplication.class)
@ActiveProfiles(value = { "test" })
public abstract class AbstractIntegrationalTest {
    @Autowired
    protected static volatile MariaDB4jSpringService mariaDB = new MariaDB4jSpringService();

    @LocalServerPort
    protected int port;

    protected RequestBuilder makeRequest(String method, String path) {
        return RequestBuilder.create(method).setUri(getUri(path));
    }

    protected static HttpClientBuilder withUser(String username, String password) {
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY, credentials);
        return HttpClientBuilder.create().setDefaultCredentialsProvider(provider);
    }
    protected URI getUri(String path) {
        try {
            return new URI(String.format("http://localhost:%d/cosmo/dav/%s", port, path));
        } catch (URISyntaxException e) {
            Assert.fail("Wrong URI: " + e);
        }
        return null;
    }


 }

