package org.unitedinternet.cosmo.dao;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Represents the path segments as they might appear in the CALDAV requests. e.g. /homeUid/collectionUid/eventUid.
 * 
 * @author daniel grigore
 * @author corneliu dobrota
 *
 */
public class PathSegments {

    private static final String PATH_SEGMENT_REGEX = "[\\-a-zA-Z0-9@:%\\._\\+~#=]*";
    private static final String URI_REGEX = "/?(?<homeCollectionUid>%s)?/?(?<collectionUid>%s)?/?(?<eventUid>%s)?";
    private static final String ALL_REGEX = String.format(URI_REGEX, PATH_SEGMENT_REGEX, PATH_SEGMENT_REGEX, PATH_SEGMENT_REGEX);

    private static final Pattern PATTERN = Pattern.compile(ALL_REGEX);

    private final String homeCollectionUid;
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
        
        this.homeCollectionUid = matcher.group("homeCollectionUid");
        this.collectionUid = matcher.group("collectionUid");
        this.eventUid = matcher.group("eventUid");
    }

    public String getHomeCollectionUid() {
        return decode(homeCollectionUid);
    }

    public String getCollectionUid() {
        return decode(collectionUid);
    }

    public String getEventUid() {
        return decode(eventUid);
    }
    
    private static String decode(String path){
        if (path == null || path.trim().isEmpty()) {
            return path;
        }
        try {
            return URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
