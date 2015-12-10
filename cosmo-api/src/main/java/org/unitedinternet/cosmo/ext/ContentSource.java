/*
 * ContentSource.java Dec 7, 2015
 * 
 * Copyright (c) 2015 1&1 Internet AG. All rights reserved.
 * 
 * $Id$
 */
package org.unitedinternet.cosmo.ext;

import java.util.Set;

import org.unitedinternet.cosmo.model.NoteItem;

public interface ContentSource {

    boolean isContentFrom(String uri);
    
    Set<NoteItem> getContent();
}
