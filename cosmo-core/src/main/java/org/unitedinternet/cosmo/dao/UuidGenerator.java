package org.unitedinternet.cosmo.dao;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * UUID generator that uses prefix and suffix before and after the unique value for pattern usage.
 * 
 * @author daniel grigore
 *
 */
public abstract class UuidGenerator {

    private final String prefix;
    private final String suffix;
    private final Pattern pattern;

    public UuidGenerator(String prefix, String suffix) {
        this.check(prefix, suffix);
        this.prefix = prefix;
        this.suffix = suffix;
        this.pattern = Pattern.compile(prefix + "([[a-z][0-9]-])*" + suffix);
    }

    private void check(String... texts) {
        for (String text : texts) {
            if (text == null || text.trim().isEmpty()) {
                throw new IllegalArgumentException();
            }
        }
    }

    /**
     * Generates an UUID value.
     * 
     * @return an UUID value
     */
    public String getNext() {
        return new StringBuilder(this.prefix).append(UUID.randomUUID().toString()).append(this.suffix).toString();
    }

    /**
     * Tells whether the specified <code>path</code> contains an UUID value.
     * 
     * @param path
     *            path to be verified
     * @return <code>true</code> if the specified <code>path</code> contains an UUID value, <code>false</code> otherwise
     */
    public boolean containsUuid(String path) {
        if (path != null) {
            Matcher matcher = this.pattern.matcher(path);
            return matcher.find();
        }
        return false;
    }

    /**
     * Extracts the UUID value from specified <code>path</code>.
     * 
     * @param path
     *            the path to be queried
     * @return the UUID value from specified <code>path</code> if it exists, <code>null</code> otherwise
     */
    public String extractUuid(String path) {
        if (path != null) {
            Matcher matcher = this.pattern.matcher(path);
            if (matcher.find()) {
                return matcher.group();
            }
        }
        return null;
    }
}
