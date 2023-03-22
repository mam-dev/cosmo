package org.unitedinternet.cosmo.dao.external;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.unitedinternet.cosmo.dao.PathSegments;

/**
 * 
 * @author daniel grigore
 *
 */
public class PathSegmentsTest {

    @Test()
    public void shouldThrowExceptionWhenPassingNullPath() {
        assertThrows(IllegalArgumentException.class, () -> {
            new PathSegments(null);
        });
    }

    @Test
    public void shouldGetEmptyStringsForEmptyPath() {
        PathSegments path = new PathSegments("");
        assertNotNull(path);
        assertTrue(path.getHomeCollectionUid().isEmpty());
        assertTrue(path.getCollectionUid().isEmpty());
        assertTrue(path.getEventUid().isEmpty());
    }

    @Test
    public void shouldSuccessfullyCreatePathWithOnlyHome() {
        String home = UUID.randomUUID().toString();
        PathSegments path = new PathSegments("/" + home);
        assertEquals(home, path.getHomeCollectionUid());
        assertTrue(path.getCollectionUid().isEmpty());
        assertTrue(path.getEventUid().isEmpty());
    }

    @Test
    public void shouldSuccessfullyCreatePathWithOnlyHomeAndCollection() {
        String home = UUID.randomUUID().toString();
        String collection = UUID.randomUUID().toString();
        PathSegments path = new PathSegments("/" + home + "/" + collection);
        assertEquals(home, path.getHomeCollectionUid());
        assertEquals(collection, path.getCollectionUid());
        assertTrue(path.getEventUid().isEmpty());
    }

    @Test
    public void shouldSuccessfullyCreatePathWithAllElements() {
        String home = UUID.randomUUID().toString();
        String collection = UUID.randomUUID().toString();
        String event = UUID.randomUUID().toString();
        PathSegments path = new PathSegments("/" + home + "/" + collection + "/" + event);
        assertEquals(home, path.getHomeCollectionUid());
        assertEquals(collection, path.getCollectionUid());
        assertEquals(event, path.getEventUid());
    }
}
