package org.unitedinternet.cosmo.dao.subscription;

import org.unitedinternet.cosmo.dao.UuidGenerator;

/**
 * UUID Generator to be used for calendars that are subscriptions to other collections.
 * 
 * @author daniel grigore
 *
 */
public class UuidSubscriptionGenerator extends UuidGenerator {

    private static final String EXPRESSION_PREFIX = "SUB-";
    private static final String EXPRESSION_SUFFIX = "-SUB";

    private static final UuidSubscriptionGenerator INSTANCE = new UuidSubscriptionGenerator();

    private UuidSubscriptionGenerator() {
        super(EXPRESSION_PREFIX, EXPRESSION_SUFFIX);
    }

    public static UuidSubscriptionGenerator get() {
        return INSTANCE;
    }
}
