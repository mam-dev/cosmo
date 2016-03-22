package org.unitedinternet.cosmo.ext;

import java.util.Set;

import org.unitedinternet.cosmo.model.NoteItem;

/**
 * <code>ContentSource</code> component that can read ICS content from public URL-s like HTTP URL-s or local file URL-s.
 * 
 * @author daniel grigore
 * @author corneliu dobrota
 *
 */
public class UrlContentSource implements ContentSource {

    private final UrlContentReader contentReader;

    private final int timeoutInMillis;

    public UrlContentSource(UrlContentReader contentReader, int timeoutInMillis) {
        super();
        this.contentReader = contentReader;
        this.timeoutInMillis = timeoutInMillis;
    }

    @Override
    public Set<NoteItem> getContent(String url) {
        return this.contentReader.getContent(url, this.timeoutInMillis);
    }
}