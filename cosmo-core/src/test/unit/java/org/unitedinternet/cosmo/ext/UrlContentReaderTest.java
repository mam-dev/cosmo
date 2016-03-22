package org.unitedinternet.cosmo.ext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.unitedinternet.cosmo.hibernate.validator.EventValidator;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.hibernate.EntityConverter;
import org.unitedinternet.cosmo.model.hibernate.HibEntityFactory;

/**
 * 
 * @author daniel grigore
 *
 */
public class UrlContentReaderTest {
    
    private static final int TIMEOUT = 5 * 1000;
    
    private static final ProxyFactory NO_PROXY_FACTORY = new NoProxyFactory();

    private ContentConverter converter;

    private LocalValidatorFactoryBean validator;

    private UrlContentReader instanceUnderTest;

    @Before
    public void setUp() {
        EventValidator.ValidationConfig validationConfig = new EventValidator.ValidationConfig();
        validationConfig.setEnvironment(Mockito.mock(Environment.class));
        EventValidator.setValidationConfig(validationConfig);

        this.validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        EntityFactory entityFactory = new HibEntityFactory();
        EntityConverter entityConverter = new EntityConverter(entityFactory);
        this.converter = new ContentConverter(entityConverter);

        instanceUnderTest = new UrlContentReader(converter, NO_PROXY_FACTORY, validator, 700);
    }

    @Test
    public void shouldReadLocalCalendar() {
        Set<NoteItem> items = this.instanceUnderTest.getContent(urlForName("chandler-plain-event.ics"), TIMEOUT);
        assertNotNull(items);
        assertEquals(1, items.size());
    }

    @Test
    public void shouldReadRomanianHolidays() {
        this.instanceUnderTest = new UrlContentReader(converter, NO_PROXY_FACTORY, validator, 1024 * 1024);
        Set<NoteItem> items = this.instanceUnderTest.getContent(urlForName("romanian-holidays.ics"), TIMEOUT);
        assertNotNull(items);
        assertEquals(80, items.size());
    }

    @Test(expected = ExternalContentInvalidException.class)
    public void shouldFailAnInvalidEvent() {
        instanceUnderTest.getContent(urlForName("invalid-event.ics"), TIMEOUT);
    }

    @Test(expected = ExternalContentTooLargeException.class)
    public void shouldFailTooLargeContent() {
        instanceUnderTest.getContent(urlForName("2445.ics"), TIMEOUT);
    }

    @Test
    @Ignore("Need only for testing purposes.")
    public void shouldReadExternalCalendar() {
        this.instanceUnderTest = new UrlContentReader(converter, NO_PROXY_FACTORY, validator, 1024 * 1024);
        Set<NoteItem> items = this.instanceUnderTest.getContent(
                "https://calendar.google.com/calendar/ical/8ojgn92qi1921h78j3n4p7va4s%40group.calendar.google.com/public/basic.ics",
                TIMEOUT);
        assertNotNull(items);
        assertFalse(items.isEmpty());
        assertEquals(9, items.size());
    }

    private static String urlForName(String name) {
        return "file:///" + new File("src/test/unit/resources/icalendar/" + name).getAbsolutePath();
    }
}
