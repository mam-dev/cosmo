package org.unitedinternet.cosmo.ext;

import static org.junit.Assert.*;

import java.io.File;
import java.net.Proxy;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.hibernate.EntityConverter;
import org.unitedinternet.cosmo.model.hibernate.HibEntityFactory;

/**
 * 
 * @author daniel grigore
 *
 */
public class URIContentResourceTest {

    private URIContentSource source;

    @Before
    public void setUp() {
        this.source = new URIContentSource(new EntityConverter(new HibEntityFactory()), Proxy.NO_PROXY);
    }

    @Test
    public void shouldReadLocalCalendar() {
        File file = new File("src/test/unit/resources/icalendar/chandler-plain-event.ics");
        Set<NoteItem> items = this.source.getContent("file:///" + file.getAbsolutePath());
        assertNotNull(items);
        assertEquals(1, items.size());
    }

    @Test
    @Ignore("Need only for testing purposes.")
    public void shouldReadExternalCalendar() {
        Set<NoteItem> items = this.source.getContent("https://calendar.google.com/calendar/ical/8ojgn92qi1921h78j3n4p7va4s%40group.calendar.google.com/public/basic.ics");
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(9, items.size());
    }
}
