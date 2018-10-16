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
@Order(1)
@Component
public class FirstLoggingEventAddHandler implements EventAddHandler {

    private static final Logger LOG = LoggerFactory.getLogger(FirstLoggingEventAddHandler.class);

    /**
     * Default constructor.
     */
    public FirstLoggingEventAddHandler() {

    }

    @Override
    public void beforeAdd(CollectionItem parent, Set<ContentItem> contentItems) {
        LOG.info("[FirstHandler] About to add {} items  to parent {}", contentItems.size(), parent.getDisplayName());
    }

    @Override
    public void afterAdd(CollectionItem parent, Set<ContentItem> contentItems) {
        LOG.info("[FirstHandler] Added {} items  to parent {}", contentItems.size(), parent.getDisplayName());
    }
}
