package org.unitedinternet.cosmo.ext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ValidationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.Stamp;

/**
 * <code>ContentSource</code> component that can read ICS content from public URL-s like HTTP URL-s or local file URL-s.
 * 
 * @author daniel grigore
 *
 */
public class UrlContentSource implements ContentSource {

    private static final Log LOG = LogFactory.getLog(UrlContentSource.class);

    private final ContentConverter converter;

    private final Proxy proxy;

    private final Validator validator;

    private final int allowedContentSizeInBytes;

    private final int timeoutInMillis;

    public UrlContentSource(ContentConverter converter, Proxy proxy, Validator validator, int allowedContentSizeInBytes,
            int timeoutInMillis) {
        super();

        this.converter = converter;
        this.proxy = proxy;
        this.validator = validator;
        this.allowedContentSizeInBytes = allowedContentSizeInBytes;
        this.timeoutInMillis = timeoutInMillis;
    }

    @Override
    public Set<NoteItem> getContent(String uri) {
        return getContent(uri, timeoutInMillis);
    }

    public Set<NoteItem> getContent(String uri, int timeout) {
        try {
            URL url = new URL(uri);

            URLConnection connection = url.openConnection(this.proxy);

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
                while ((offset = contentStream.read(buffer)) != -1) {
                    counter.addAndGet(offset);
                    if (counter.get() > allowedContentSizeInBytes) {
                        throw new ExternalContentTooLargeException(
                                "Content from uri " + uri + " is larger then " + this.allowedContentSizeInBytes);
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
            throw wrap(e);
        }
    }

    private static RuntimeException wrap(Exception e) {
        if (e instanceof IOException) {
            return new RuntimeException(e);
        }
        return new InvalidExternalContentException(e);
    }

    private void validate(Set<NoteItem> items) {
        for (Item item : items) {
            for (Stamp stamp : item.getStamps()) {
                Set<ConstraintViolation<Stamp>> validationResult = validator.validate(stamp);
                if (validationResult != null && !validationResult.isEmpty()) {
                    throw new InvalidExternalContentException();
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