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
public final class CalendarUuidGenerator {

    public static final String EXPRESSION_PREFIX = "EXT-";
    public static final String EXPRESSION_SUFFIX = "-EXT";
    private static final Pattern PATTERN = Pattern.compile(EXPRESSION_PREFIX + "([[a-z][0-9]-])*" + EXPRESSION_SUFFIX);

    private CalendarUuidGenerator() {
        super();
    }

    public static String genererate() {
        return new StringBuilder(EXPRESSION_PREFIX).append(UUID.randomUUID().toString()).append(EXPRESSION_SUFFIX)
                .toString();
    }

    public static boolean containsExternalUid(String path) {
        if (path != null) {
            Matcher matcher = PATTERN.matcher(path);
            return matcher.find();
        }
        return false;
    }

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
