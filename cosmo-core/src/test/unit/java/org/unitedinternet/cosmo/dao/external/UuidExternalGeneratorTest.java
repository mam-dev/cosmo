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
public class UuidExternalGeneratorTest {

    @Test
    public void shouldSuccessfullyGenerateUUID() {
        String uuid = UuidExternalGenerator.get().getNext();
        assertNotNull(uuid);
        assertTrue(uuid.startsWith(UuidExternalGenerator.EXPRESSION_PREFIX));
        assertTrue(uuid.endsWith(UuidExternalGenerator.EXPRESSION_SUFFIX));
    }

    @Test
    public void shouldFindNullAsNoExternalUUid() {
        assertFalse(UuidExternalGenerator.get().containsUuid(null));
    }

    @Test
    public void shouldFindRandomStringAsNotExternalUUid() {
        assertFalse(UuidExternalGenerator.get().containsUuid(UUID.randomUUID().toString()));
    }

    @Test
    public void shouldSuccessfullyFindUuidAsExternal() {
        String uuid = UuidExternalGenerator.get().getNext();
        assertTrue(UuidExternalGenerator.get().containsUuid(uuid));
    }

    @Test
    public void shouldSuccessfullyFindPathWithUuidAsExternal() {
        String uuid = UuidExternalGenerator.get().getNext();
        StringBuilder sb = new StringBuilder();
        sb.append(UUID.randomUUID().toString()).append("/").append(uuid).append("/")
                .append(UUID.randomUUID().toString());
        String path = sb.toString();
        assertTrue(UuidExternalGenerator.get().containsUuid(path));
    }

    @Test
    public void shouldReturnNullWhenExtractingUuuidFromNull() {
        String uuid = UuidExternalGenerator.get().extractUuid(null);
        assertNull(uuid);
    }

    @Test
    public void shouldReturnNullWhenExtractingUuuidFromRandomString() {
        String uuid = UuidExternalGenerator.get().extractUuid(UUID.randomUUID().toString());
        assertNull(uuid);
    }

    @Test
    public void shouldSuccessfullyExtractUuidFromSelf() {
        String uuid = UuidExternalGenerator.get().getNext();
        String extractedUuid = UuidExternalGenerator.get().extractUuid(uuid);
        assertEquals(uuid, extractedUuid);
    }

    @Test
    public void shouldSuccessfullyExtractUuidFromPath() {
        String uuid = UuidExternalGenerator.get().getNext();
        String path = UUID.randomUUID().toString() + "/" + uuid + "/" + UUID.randomUUID().toString();
        String extractedUuid = UuidExternalGenerator.get().extractUuid(path);
        assertEquals(uuid, extractedUuid);
    }
}
