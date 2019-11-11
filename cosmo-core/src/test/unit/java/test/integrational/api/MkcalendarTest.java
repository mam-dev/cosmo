package test.integrational.api;

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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


public class MkcalendarTest extends AbstractIntegrationalTest {
    

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
