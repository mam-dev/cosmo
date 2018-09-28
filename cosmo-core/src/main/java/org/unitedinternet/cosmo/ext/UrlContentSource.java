package org.unitedinternet.cosmo.ext;

import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.unitedinternet.cosmo.model.NoteItem;

/**
 * <code>ContentSource</code> component that can read ICS content from public URL-s like HTTP URL-s or local file URL-s.
 * 
 * @author daniel grigore
 * @author corneliu dobrota
 *
 */
@Component
public class UrlContentSource implements ContentSource {

    private final UrlContentReader contentReader;

    @Value("${external.content.connection.timeout}")
    private int timeoutInMillis;

    public UrlContentSource(UrlContentReader contentReader) {
        super();
        this.contentReader = contentReader;
    }

    @Override
    public Set<NoteItem> getContent(String url) {
        return this.contentReader.getContent(url, this.timeoutInMillis);
    }
}