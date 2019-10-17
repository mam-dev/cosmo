package org.unitedinternet.cosmo.dav.mkcol;

import org.unitedinternet.cosmo.dav.mkcol.CreateCollectionResponse;

public interface CreateCollectionResponseFactory {
    CreateCollectionResponse get(String responseDescription);
}
