package org.unitedinternet.cosmo.util;

import java.util.UUID;

import org.springframework.stereotype.Component;

/**
 * Class used to generate pseudo randomly generated UUID
 * @author iulia
 *
 */
@Component
public class VersionFourGenerator {

    public String nextStringIdentifier() {
        return UUID.randomUUID().toString();
    }
}
