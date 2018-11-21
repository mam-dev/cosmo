package org.unitedinternet.cosmo.ext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.hibernate.EntityConverter;
import org.unitedinternet.cosmo.model.hibernate.HibEntityFactory;

/**
 * 
 * @author daniel grigore
 *
 */
@Ignore
public class SimpleUrlContentReaderTest {

    private static final int TIMEOUT = 5 * 1000;

    private ContentConverter converter;

    private LocalValidatorFactoryBean validator;

    private UrlContentReader instanceUnderTest;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        this.validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        EntityFactory entityFactory = new HibEntityFactory();
        EntityConverter entityConverter = new EntityConverter(entityFactory);
        this.converter = new ContentConverter(entityConverter);

        instanceUnderTest = new SimpleUrlContentReader(converter, validator);
    }

    @Test
    public void shouldReadRomanianHolidays() {
        this.instanceUnderTest = new SimpleUrlContentReader(converter, validator);
        Set<NoteItem> items = this.instanceUnderTest.getContent(
                "https://calendar.google.com/calendar/ical/ro.romanian%23holiday%40group.v.calendar.google.com/public/basic.ics",
                TIMEOUT);
        assertNotNull(items);
        assertEquals(80, items.size());
    }

    @Test(expected = ExternalContentInvalidException.class)
    public void shouldFailAnInvalidEvent() {
        instanceUnderTest.getContent("http://google.com", TIMEOUT);
    }

    @Test(expected = ExternalContentTooLargeException.class)
    public void shouldFailTooLargeContent() {
        instanceUnderTest.getContent(
                "https://calendar.google.com/calendar/ical/8ojgn92qi1921h78j3n4p7va4s%40group.calendar.google.com/public/basic.ics",
                TIMEOUT);
    }

    @Test
    public void shouldReadExternalCalendar() {
        this.instanceUnderTest = new SimpleUrlContentReader(converter, validator);
        Set<NoteItem> items = this.instanceUnderTest.getContent(
                "https://calendar.google.com/calendar/ical/8ojgn92qi1921h78j3n4p7va4s%40group.calendar.google.com/public/basic.ics",
                TIMEOUT);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(9, items.size());
    }
}
