package org.unitedinternet.cosmo.ext;

import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitedinternet.cosmo.model.ICalendarItem;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.hibernate.EntityConverter;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ValidationException;

/**
 * <code>ContentSource</code> able to fetch content from a <code>URI</code>.
 * 
 * @author daniel grigore
 *
 */
public class URIContentSource implements ContentSource {

    /**
     * Ten seconds timeout for reading content.
     */
    private static final int TIMEOUT = 10 * 1000;
    private static final Log LOG = LogFactory.getLog(URIContentSource.class);

    private final EntityConverter entityConverter;

    private final Proxy proxy;

    public URIContentSource(EntityConverter entityConverter, Proxy proxy) {
        super();
        this.entityConverter = entityConverter;
        this.proxy = proxy;
    }

    @Override
    public boolean isContentFrom(String uri) {
        return uri != null;
    }

    @Override
    public Set<NoteItem> getContent(String uri) {
        Set<NoteItem> items = new HashSet<>();
        Calendar calendar = this.readFrom(uri);
        if (calendar != null) {
            Set<ICalendarItem> calendarItems = this.entityConverter.convertCalendar(calendar);
            
            for (ICalendarItem item : calendarItems) {
                /**
                 * Only VEVENT are supported currently. VTODO or VJOURNAL are not yet supported.
                 */
                if (item instanceof NoteItem) {
                    items.add((NoteItem) item);
                    item.setName(item.getIcalUid() + ".ics");
                }
            }

        }
        return items;
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
