package org.unitedinternet.cosmo.dao.external;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;

/**
 * 
 * @author daniel grigore
 *
 */
public class CalendarUuidGeneratorTest {

    @Test
    public void shouldSuccessfullyGenerateUUID() {
        String uuid = CalendarUuidGenerator.genererate();
        assertNotNull(uuid);
        assertTrue(uuid.startsWith(CalendarUuidGenerator.EXPRESSION_PREFIX));
        assertTrue(uuid.endsWith(CalendarUuidGenerator.EXPRESSION_SUFFIX));
    }

    @Test
    public void shouldFindNullAsNoExternalUUid() {
        assertFalse(CalendarUuidGenerator.containsExternalUid(null));
    }

    @Test
    public void shouldFindRandomStringAsNotExternalUUid() {
        assertFalse(CalendarUuidGenerator.containsExternalUid(UUID.randomUUID().toString()));
    }

    @Test
    public void shouldSuccessfullyFindUuidAsExternal() {
        String uuid = CalendarUuidGenerator.genererate();
        assertTrue(CalendarUuidGenerator.containsExternalUid(uuid));
    }

    @Test
    public void shouldSuccessfullyFindPathWithUuidAsExternal() {
        String uuid = CalendarUuidGenerator.genererate();
        StringBuilder sb = new StringBuilder();
        sb.append(UUID.randomUUID().toString()).append("/").append(uuid).append("/")
                .append(UUID.randomUUID().toString());
        String path = sb.toString();
        assertTrue(CalendarUuidGenerator.containsExternalUid(path));
    }

    @Test
    public void shouldReturnNullWhenExtractingUuuidFromNull() {
        String uuid = CalendarUuidGenerator.extractUuid(null);
        assertNull(uuid);
    }

    @Test
    public void shouldReturnNullWhenExtractingUuuidFromRandomString() {
        String uuid = CalendarUuidGenerator.extractUuid(UUID.randomUUID().toString());
        assertNull(uuid);
    }

    @Test
    public void shouldSuccessfullyExtractUuidFromSelf() {
        String uuid = CalendarUuidGenerator.genererate();
        String extractedUuid = CalendarUuidGenerator.extractUuid(uuid);
        assertEquals(uuid, extractedUuid);
    }

    @Test
    public void shouldSuccessfullyExtractUuidFromPath() {
        String uuid = CalendarUuidGenerator.genererate();
        String path = UUID.randomUUID().toString() + "/" + uuid + "/" + UUID.randomUUID().toString();
        String extractedUuid = CalendarUuidGenerator.extractUuid(path);
        assertEquals(uuid, extractedUuid);
    }
}
