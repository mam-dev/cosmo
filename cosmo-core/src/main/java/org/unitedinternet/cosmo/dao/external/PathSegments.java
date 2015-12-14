package org.unitedinternet.cosmo.dao.external;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author daniel grigore
 * @author corneliu dobrota
 *
 */
public class PathSegments {

    private static final String QUERY_REGEX = "[\\-a-zA-Z0-9@:%\\._\\+~#=]*";

    private static final String ALL_REGEX = String.format(
            "/?(?<homeCollectionUid>%s)?/?(?<collectionUid>%s)?/?(?<eventUid>%s)?", QUERY_REGEX, QUERY_REGEX,
            QUERY_REGEX);

    private final Pattern PATTERN = Pattern.compile(ALL_REGEX);

    private final String homeUid;
    private final String collectionUid;
    private final String eventUid;

    /**
     * 
     * @param path
     */
    public PathSegments(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        Matcher matcher = PATTERN.matcher(path);
        matcher.find();
        this.homeUid = matcher.group("homeCollectionUid");
        this.collectionUid = matcher.group("collectionUid");
        this.eventUid = matcher.group("eventUid");
    }

    public String getHomeUid() {
        return homeUid;
    }

    public String getCollectionUid() {
        return collectionUid;
    }

    public String getEventUid() {
        return eventUid;
    }

}
