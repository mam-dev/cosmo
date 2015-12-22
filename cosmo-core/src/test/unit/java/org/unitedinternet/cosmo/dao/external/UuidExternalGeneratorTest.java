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
        String uuid = UuidExternalGenerator.getNext();
        assertNotNull(uuid);
        assertTrue(uuid.startsWith(UuidExternalGenerator.EXPRESSION_PREFIX));
        assertTrue(uuid.endsWith(UuidExternalGenerator.EXPRESSION_SUFFIX));
    }

    @Test
    public void shouldFindNullAsNoExternalUUid() {
        assertFalse(UuidExternalGenerator.containsExternalUid(null));
    }

    @Test
    public void shouldFindRandomStringAsNotExternalUUid() {
        assertFalse(UuidExternalGenerator.containsExternalUid(UUID.randomUUID().toString()));
    }

    @Test
    public void shouldSuccessfullyFindUuidAsExternal() {
        String uuid = UuidExternalGenerator.getNext();
        assertTrue(UuidExternalGenerator.containsExternalUid(uuid));
    }

    @Test
    public void shouldSuccessfullyFindPathWithUuidAsExternal() {
        String uuid = UuidExternalGenerator.getNext();
        StringBuilder sb = new StringBuilder();
        sb.append(UUID.randomUUID().toString()).append("/").append(uuid).append("/")
                .append(UUID.randomUUID().toString());
        String path = sb.toString();
        assertTrue(UuidExternalGenerator.containsExternalUid(path));
    }

    @Test
    public void shouldReturnNullWhenExtractingUuuidFromNull() {
        String uuid = UuidExternalGenerator.extractUuid(null);
        assertNull(uuid);
    }

    @Test
    public void shouldReturnNullWhenExtractingUuuidFromRandomString() {
        String uuid = UuidExternalGenerator.extractUuid(UUID.randomUUID().toString());
        assertNull(uuid);
    }

    @Test
    public void shouldSuccessfullyExtractUuidFromSelf() {
        String uuid = UuidExternalGenerator.getNext();
        String extractedUuid = UuidExternalGenerator.extractUuid(uuid);
        assertEquals(uuid, extractedUuid);
    }

    @Test
    public void shouldSuccessfullyExtractUuidFromPath() {
        String uuid = UuidExternalGenerator.getNext();
        String path = UUID.randomUUID().toString() + "/" + uuid + "/" + UUID.randomUUID().toString();
        String extractedUuid = UuidExternalGenerator.extractUuid(path);
        assertEquals(uuid, extractedUuid);
    }
}
