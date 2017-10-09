package org.unitedinternet.cosmo.dao.subscription;

import org.unitedinternet.cosmo.dao.UuidGenerator;

/**
 * UUID Generator to be used for calendars that are subscriptions to other collections.
 * 
 * @author daniel grigore
 *
 */
public class UidSubscriptionGenerator extends UuidGenerator {

    private static final String EXPRESSION_PREFIX = "SUB-";
    private static final String EXPRESSION_SUFFIX = "-SUB";

    private static final UidSubscriptionGenerator INSTANCE = new UidSubscriptionGenerator();

    private UidSubscriptionGenerator() {
        super(EXPRESSION_PREFIX, EXPRESSION_SUFFIX);
    }

    public static UidSubscriptionGenerator get() {
        return INSTANCE;
    }
}
