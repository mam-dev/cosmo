package test.integrational.api;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.unitedinternet.cosmo.dao.hibernate.AbstractSpringDaoTestCase;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.hibernate.HibUser;
import org.apache.commons.io.IOUtils;
import org.unitedinternet.cosmo.service.UserService;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


public class MkcalendarTest extends AbstractIntegrationalTest {

    @LocalServerPort
    private  int port;


    protected RequestBuilder mkcalendar() {
        return RequestBuilder.create("MKCALENDAR");
    }

    protected HttpClientBuilder withUser(String username, String password) {
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
    protected RequestBuilder makeRequest(String method, String path) {
        return RequestBuilder.create(method).setUri(getUri(path));
    }
    protected RequestBuilder makeMkcalendar(String path) {
        return makeRequest("MKCALENDAR", path);
    }
    @Test
    public void testMkcalendar() throws IOException {
        final String creatorEmail = "calendarer@example.org";
        final String creatorPassword = "password";
        HttpClientBuilder builder = withUser(creatorEmail, creatorPassword);
        HttpClient client = builder.build();
        HttpUriRequest request = makeMkcalendar(creatorEmail + "/calendar").build();
        HttpResponse response = client.execute(request);
        //Should return response
        String resp = IOUtils.toString(response.getEntity().getContent());
        System.err.println(resp);
        // should return 201 OK
        Assert.assertEquals(201, response.getStatusLine().getStatusCode());

    }

}
