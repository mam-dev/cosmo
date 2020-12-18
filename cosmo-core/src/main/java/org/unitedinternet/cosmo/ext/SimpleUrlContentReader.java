package org.unitedinternet.cosmo.ext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.Stamp;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;

/**
 * Default implementation of {@link UrlContentReader} that reads and validates content.
 * 
 * @author daniel grigore
 *
 */
@Component
public class SimpleUrlContentReader implements UrlContentReader {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleUrlContentReader.class);

    private static final int MAX_LINE_LENGTH = 4096;
    private static final int MAX_HEADER_COUNT = 30;
    private static final int MAX_REDIRECTS = 10;

    private static final String Q_MARK = "?";
    private static final String AND = "&";

    private final ContentConverter converter;

    private final Validator validator;

    @Value("${external.content.size}")
    private int allowedContentSizeInBytes;

    @Autowired
    private ContentSourceProcessor processor;

    @Autowired
    private ProxyFactory proxyFactory;
    
    public SimpleUrlContentReader(ContentConverter converter, Validator validator) {
        this.converter = converter;
        this.validator = validator;
    }

    @Override
    public Set<NoteItem> getContent(String url, int timeoutInMillis) {
        return this.getContent(url, timeoutInMillis, RequestOptions.builder().build());
    }

    /**
     * Gets and validates the content from the specified <code>url</code>.
     * 
     * @param url
     *            <code>URL</code> where to get the content from.
     * @param timeout
     *            maximum time to wait before abandoning the connection in milliseconds
     * @param headersMap
     *            headers to be sent when making the request to the specified URL
     * @return content read from the specified <code>url</code>
     */
    public Set<NoteItem> getContent(String url, int timeoutInMillis, RequestOptions options) {
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        try {
            URL source = build(url, options);
            HttpGet request = new HttpGet(source.toURI());
            for (Entry<String, String> entry : options.headers().entrySet()) {
                request.addHeader(entry.getKey(), entry.getValue());
            }

            client = buildClient(timeoutInMillis, source);
            response = client.execute(request);

            InputStream contentStream = null;
            ByteArrayOutputStream baos = null;
            try {
                contentStream = response.getEntity().getContent();
                baos = new ByteArrayOutputStream();
                AtomicInteger counter = new AtomicInteger();
                byte[] buffer = new byte[1024];
                int offset = 0;
                long startTime = System.currentTimeMillis();
                while ((offset = contentStream.read(buffer)) != -1) {
                    counter.addAndGet(offset);
                    if (counter.get() > allowedContentSizeInBytes) {
                        throw new ExternalContentTooLargeException(
                                "Content from url " + url + " is larger then " + this.allowedContentSizeInBytes);
                    }
                    long now = System.currentTimeMillis();
                    if (startTime + timeoutInMillis < now) {
                        throw new IOException("Too much time spent reading url: " + url);
                    }
                    baos.write(buffer, 0, offset);
                }
                Calendar calendar = new CalendarBuilder().build(new ByteArrayInputStream(baos.toByteArray()));
                this.postProcess(calendar);

                Set<NoteItem> externalItems = converter.asItems(calendar);

                validate(externalItems);

                return externalItems;
            } finally {
                close(contentStream);
                close(baos);
            }
        } catch (IOException | URISyntaxException | ParserException e) {
            throw new ExternalContentInvalidException(e);
        } finally {
            close(response);
            close(client);
        }
    }

    private CloseableHttpClient buildClient(int timeoutInMillis, URL url) {
        RequestConfig config = RequestConfig.custom().setConnectionRequestTimeout(timeoutInMillis)
                .setConnectTimeout(timeoutInMillis).setRedirectsEnabled(true).setMaxRedirects(MAX_REDIRECTS)
                .setProxy(this.proxyFactory.getProxy(url)).build();
        return HttpClientBuilder.create().setDefaultRequestConfig(config)
                .setDefaultConnectionConfig(
                        ConnectionConfig
                                .custom().setMessageConstraints(MessageConstraints.custom()
                                        .setMaxHeaderCount(MAX_HEADER_COUNT).setMaxLineLength(MAX_LINE_LENGTH).build())
                                .build())
                .build();
    }

    private void postProcess(Calendar calendar) {
       this.processor.postProcess(calendar);
    }

    private static URL build(String url, RequestOptions options) throws IOException {
        StringBuilder builder = new StringBuilder(url);
        if (!url.contains(Q_MARK)) {
            builder.append(Q_MARK);
        }
        for (Entry<String, String> entry : options.queryParams().entrySet()) {
            if (!builder.toString().endsWith(AND)) {
                builder.append(AND);
            }
            builder.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return new URL(builder.toString());
    }

    private void validate(Set<NoteItem> items) {
        for (Item item : items) {
            for (Stamp stamp : item.getStamps()) {
                Set<ConstraintViolation<Stamp>> validationResult = validator.validate(stamp);
                if (validationResult != null && !validationResult.isEmpty()) {
                    throw new ExternalContentInvalidException();
                }
            }
        }
    }

    private static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ioe) {
                LOG.error("Error occured while closing stream ", ioe);
            }
        }
    }
}