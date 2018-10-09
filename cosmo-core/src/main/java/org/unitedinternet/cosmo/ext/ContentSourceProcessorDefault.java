package org.unitedinternet.cosmo.ext;

import org.springframework.stereotype.Component;

import net.fortuna.ical4j.model.Calendar;

/**
 * Default implementation that does not alter the content. Other implementations might provide another primary bean for
 * this.
 * 
 * @author daniel grigore
 *
 */
@Component
public class ContentSourceProcessorDefault implements ContentSourceProcessor {

    public ContentSourceProcessorDefault() {

    }

    @Override
    public void postProcess(Calendar calendar) {
        // Nothing to do.
    }
}
