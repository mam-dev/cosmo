package org.unitedinternet.cosmo.dao.external;

import org.unitedinternet.cosmo.dao.UuidGenerator;

/**
 * UUID generator to be used for calendars that manage external content.
 * 
 * @author daniel grigore
 *
 */
public final class UuidExternalGenerator extends UuidGenerator {

    public static final String EXPRESSION_PREFIX = "EXT-";
    public static final String EXPRESSION_SUFFIX = "-EXT";    
    public static final UuidExternalGenerator INSTANCE = new UuidExternalGenerator();
    
    private UuidExternalGenerator() {
        super(EXPRESSION_PREFIX, EXPRESSION_SUFFIX);
    }
    
    public static UuidExternalGenerator get() {
        return INSTANCE;
    }      
}
