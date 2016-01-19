package org.unitedinternet.cosmo.ext;

import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitedinternet.cosmo.model.NoteItem;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ValidationException;

/**
 * <code>ContentSource</code> component that can read ICS content from public URI-s like HTTP URL-s or local file URL-s.
 * 
 * @author daniel grigore
 *
 */
public class UriContentSource implements ContentSource {

    /**
     * Ten seconds timeout for reading content.
     */
    private static final int TIMEOUT = 10 * 1000;
    private static final Log LOG = LogFactory.getLog(UriContentSource.class);

    private final ContentConverter converter;

    private final Proxy proxy;

    public UriContentSource(ContentConverter converter, Proxy proxy) {
        super();
        this.converter = converter;
        this.proxy = proxy;
    }

    @Override
    public Set<NoteItem> getContent(String uri) {
        Calendar calendar = this.readFrom(uri);
        return this.converter.asItems(calendar);
    }

    private Calendar readFrom(String uri) {
        InputStream input = null;
        try {
            URL url = new URL(uri);
            URLConnection connection = url.openConnection(this.proxy);
            connection.setReadTimeout(TIMEOUT);
            connection.setConnectTimeout(TIMEOUT);
            connection.connect();
            input = connection.getInputStream();
            Calendar calendar = new CalendarBuilder().build(input);
            calendar.validate();
            return calendar;
        } catch (IOException | ParserException | ValidationException e) {
            LOG.error("Exception occured when reading content from " + uri, e);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                LOG.error("Exception occured when closing stream for " + uri, e);
            }
        }
        return null;
    }
}
