/*
 * Copyright 2007 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unitedinternet.cosmo.dav;

import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.dav.impl.StandardDavRequest;
import org.unitedinternet.cosmo.dav.impl.StandardDavResponse;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.hibernate.HibEntityFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * A helper bean that provides access to low- and high-level request
 * and response objects useful for testing providers, request handlers
 * and other protocol components.
 */
public class DavTestContext {
    private MockHttpServletRequest httpRequest;
    private MockHttpServletResponse httpResponse;
    private StandardDavRequest davRequest;
    private StandardDavResponse davResponse;
    private EntityFactory entityFactory; 

    /**
     * Constructor.
     * @param locatorFactory Dav resource locator factory.
     */
    public DavTestContext(DavResourceLocatorFactory locatorFactory) {
        httpRequest = new MockHttpServletRequest();
        httpResponse = new MockHttpServletResponse();
        entityFactory = new HibEntityFactory();
        davRequest = new StandardDavRequest(httpRequest, locatorFactory, entityFactory);
        davResponse = new StandardDavResponse(httpResponse);
    }

    /**
     * Gets http request.
     * @return The request.
     */
    public MockHttpServletRequest getHttpRequest() {
        return httpRequest;
    }

    /**
     * Gets http response.
     * @return The mock http servlet response.
     */
    public MockHttpServletResponse getHttpResponse() {
        return httpResponse;
    }

    /**
     * Gets dav request.
     * @return The dav request.
     */
    public DavRequest getDavRequest() {
        return davRequest;
    }

    /**
     * Gets dav response.
     * @return The dav response.
     */
    public DavResponse getDavResponse() {
        return davResponse;
    }

    /**
     * Sets text request body.
     * @param text The text.
     */
    public void setTextRequestBody(String text) {
        try {
            httpRequest.setContent(text.getBytes("UTF-8"));
            httpRequest.setContentType("text/plain; charset=UTF-8");
            httpRequest.addHeader("Content-Type",
                                  httpRequest.getContentType());
        } catch (Exception e) {
            throw new CosmoException(e);
        }
    }
}
