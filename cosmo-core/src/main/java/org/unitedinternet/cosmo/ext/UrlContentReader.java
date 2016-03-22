package org.unitedinternet.cosmo.ext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.Stamp;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ValidationException;

/**
 * Component able to read calendar content from URL-s in a configurable manner.
 * 
 * @author daniel grigore
 * @author corneliu dobrota
 */
public class UrlContentReader {

    private static final Log LOG = LogFactory.getLog(UrlContentReader.class);

    private final ContentConverter converter;

    private final ProxyFactory proxyFactory;

    private final Validator validator;

    private final int allowedContentSizeInBytes;

    public UrlContentReader(ContentConverter converter, ProxyFactory proxyFactory, Validator validator,
            int allowedContentSizeInBytes) {
        super();
        this.converter = converter;
        this.proxyFactory = proxyFactory;
        this.validator = validator;
        this.allowedContentSizeInBytes = allowedContentSizeInBytes;
    }

    /**
     * Gets and validates the content from the specified <code>url</code>.
     * 
     * @param url
     *            <code>URL</code> where to get the content from.
     * @param timeout
     *            maximum time to wait before abandoning the connection in milliseconds
     * @return content read from the specified <code>url</code>
     */
    public Set<NoteItem> getContent(String url, int timeoutInMillis) {
        try {
            URL source = new URL(url);
            URLConnection connection = source.openConnection(this.proxyFactory.get(source.getHost()));

            connection.setConnectTimeout(timeoutInMillis);
            connection.setReadTimeout(timeoutInMillis);

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
                calendar.validate();

                Set<NoteItem> externalItems = converter.asItems(calendar);

                validate(externalItems);

                return externalItems;
            } finally {
                close(contentStream);
                close(baos);
            }
        } catch (IOException | ValidationException | ParserException e) {
            throw new ExternalContentInvalidException(e);
        }
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
