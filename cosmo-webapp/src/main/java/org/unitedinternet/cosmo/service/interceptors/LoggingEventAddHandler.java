package org.unitedinternet.cosmo.service.interceptors;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unitedinternet.cosmo.metadata.Interceptor;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;

/**
 * Simple <code>EventAddHandler</code> that only logs messages when events are added.
 * 
 * @author daniel grigore
 *
 */
@Interceptor
public class LoggingEventAddHandler implements EventAddHandler {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingEventAddHandler.class);

    /**
     * Default constructor.
     */
    public LoggingEventAddHandler() {

    }

    @Override
    public void beforeAdd(CollectionItem parent, Set<ContentItem> contentItems) {
        LOG.info("ABOUT to add " + contentItems.size() + " items to parent " + parent.getDisplayName());

    }

    @Override
    public void afterAdd(CollectionItem parent, Set<ContentItem> contentItems) {
        LOG.info("ADDED " + contentItems.size() + " items to parent " + parent.getDisplayName());
    }

}
