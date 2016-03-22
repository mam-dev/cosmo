package org.unitedinternet.cosmo.dao.external;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * UUID generator to be used when working with calendars that manage external content.
 * 
 * @author daniel grigore
 *
 */
public final class UuidExternalGenerator {

    public static final String EXPRESSION_PREFIX = "EXT-";
    public static final String EXPRESSION_SUFFIX = "-EXT";
    private static final Pattern PATTERN = Pattern.compile(EXPRESSION_PREFIX + "([[a-z][0-9]-])*" + EXPRESSION_SUFFIX);

    private UuidExternalGenerator() {
        super();
    }

    /**
     * Generates an external UUID value.
     * 
     * @return an external UUID value
     */
    public static String getNext() {
        return new StringBuilder(EXPRESSION_PREFIX).append(UUID.randomUUID().toString()).append(EXPRESSION_SUFFIX)
                .toString();
    }

    /**
     * Tells whether the specified <code>path</code> contains an external UUID value.
     * 
     * @param path
     *            path to be verified
     * @return <code>true</code> if the specified <code>path</code> contains an external UUID value, <code>false</code>
     *         otherwise
     */
    public static boolean containsExternalUid(String path) {
        if (path != null) {
            Matcher matcher = PATTERN.matcher(path);
            return matcher.find();
        }
        return false;
    }

    /**
     * Extracts the external UUID value from specified <code>path</code>.
     * 
     * @param path
     *            the path to be queried
     * @return the external UUID value from specified <code>path</code> if it exists, <code>null</code> otherwise
     */
    public static String extractUuid(String path) {
        if (path != null) {
            Matcher matcher = PATTERN.matcher(path);
            if (matcher.find()) {
                return matcher.group();
            }
        }
        return null;
    }
}
