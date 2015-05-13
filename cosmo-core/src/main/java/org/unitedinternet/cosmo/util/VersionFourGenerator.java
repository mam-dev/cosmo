package org.unitedinternet.cosmo.util;

import java.util.UUID;

/**
 * Class used to generate pseudo randomly generated UUID
 * @author iulia
 *
 */
public class VersionFourGenerator {

    public String nextStringIdentifier() {
        return UUID.randomUUID().toString();
    }
}
