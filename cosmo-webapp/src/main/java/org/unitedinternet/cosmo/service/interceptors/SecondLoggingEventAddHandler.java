package org.unitedinternet.cosmo.service.interceptors;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;

/**
 * Simple <code>EventAddHandler</code> that only logs messages when events are added.
 * 
 * @author daniel grigore
 *
 */
@Order(2)
@Component
public class SecondLoggingEventAddHandler implements EventAddHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SecondLoggingEventAddHandler.class);

    /**
     * Default constructor.
     */
    public SecondLoggingEventAddHandler() {

    }

    @Override
    public void beforeAdd(CollectionItem parent, Set<ContentItem> contentItems) {
        LOG.info("[SecondHandler] About to add {} items  to parent {}", contentItems.size(), parent.getDisplayName());

    }

    @Override
    public void afterAdd(CollectionItem parent, Set<ContentItem> contentItems) {
        LOG.info("[SecondHandler] Added {} items  to parent {}", contentItems.size(), parent.getDisplayName());
    }
}
