package org.unitedinternet.cosmo.ext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitedinternet.cosmo.api.ExternalComponentInstanceProvider;
import org.unitedinternet.cosmo.metadata.Callback;
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
 * @author corneliu dobrota
 */
public class SimpleUrlContentReader implements UrlContentReader {

    private static final Log LOG = LogFactory.getLog(SimpleUrlContentReader.class);

    private static final String Q_MARK = "?";
    private static final String AND = "&";

    private final ContentConverter converter;

    private final ProxyFactory proxyFactory;

    private final Validator validator;

    private final int allowedContentSizeInBytes;

    private final ExternalComponentInstanceProvider instanceProvider;

    public SimpleUrlContentReader(ContentConverter converter, ProxyFactory proxyFactory, Validator validator,
            int allowedContentSizeInBytes, ExternalComponentInstanceProvider instanceProvider) {
        super();
        this.converter = converter;
        this.proxyFactory = proxyFactory;
        this.validator = validator;
        this.allowedContentSizeInBytes = allowedContentSizeInBytes;
        this.instanceProvider = instanceProvider;
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
        try {
            URL source = build(url, options);
            URLConnection connection = source.openConnection(this.proxyFactory.get(source.getHost()));

            connection.setConnectTimeout(timeoutInMillis);
            connection.setReadTimeout(timeoutInMillis);

            for (Entry<String, String> entry : options.headers().entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }

            InputStream contentStream = null;
            ByteArrayOutputStream baos = null;

            try {
                contentStream = connection.getInputStream();
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
        } catch (IOException | ParserException e) {
            throw new ExternalContentInvalidException(e);
        }
    }

    private void postProcess(Calendar calendar) {
        Set<? extends ContentSourceProcessor> processors = this.instanceProvider
                .getImplInstancesAnnotatedWith(Callback.class, ContentSourceProcessor.class);
        for (ContentSourceProcessor processor : processors) {
            processor.postProcess(calendar);
        }
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
