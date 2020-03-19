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
package org.unitedinternet.cosmo.dav.servlet;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ValidationException;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.unitedinternet.cosmo.dav.BadRequestException;
import org.unitedinternet.cosmo.dav.BaseDavTestCase;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.DavRequest;
import org.unitedinternet.cosmo.dav.DavResponse;
import org.unitedinternet.cosmo.dav.DavTestContext;
import org.unitedinternet.cosmo.dav.ForbiddenException;
import org.unitedinternet.cosmo.dav.NotModifiedException;
import org.unitedinternet.cosmo.dav.PreconditionFailedException;
import org.unitedinternet.cosmo.dav.WebDavResource;
import org.unitedinternet.cosmo.dav.acl.DavPrivilege;
import org.unitedinternet.cosmo.dav.acl.NeedsPrivilegesException;
import org.unitedinternet.cosmo.dav.caldav.CaldavExceptionExtMkCalendarForbidden;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.security.ItemSecurityException;
import org.unitedinternet.cosmo.security.Permission;
import org.unitedinternet.cosmo.security.PermissionDeniedException;
import org.unitedinternet.cosmo.server.ServerConstants;

/**
 * Test class for {@link StandardRequestHandler}.
 */
public class StandardRequestHandlerTest extends BaseDavTestCase {

    /**
     * Tests if match all.
     * 
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testIfMatchAll() throws Exception {
        WebDavResource home = testHelper.initializeHomeResource();

        DavTestContext ctx = testHelper.createTestContext();
        ctx.getHttpRequest().addHeader("If-Match", "*");

        try {
            new StandardRequestHandler(null, null, null).preconditions(ctx.getDavRequest(), ctx.getDavResponse(), home);
        } catch (PreconditionFailedException e) {
            Assert.fail("If-Match all failed");
        }
    }

    /**
     * Tests if match ok.
     * 
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testIfMatchOk() throws Exception {
        WebDavResource home = testHelper.initializeHomeResource();

        DavTestContext ctx = testHelper.createTestContext();
        ctx.getHttpRequest().addHeader("If-Match", home.getETag());

        try {
            new StandardRequestHandler(null, null, null).preconditions(ctx.getDavRequest(), ctx.getDavResponse(), home);
        } catch (PreconditionFailedException e) {
            Assert.fail("If-Match specific etag failed");
        }
    }

    /**
     * Tests if match not ok.
     * 
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testIfMatchNotOk() throws Exception {
        WebDavResource home = testHelper.initializeHomeResource();

        DavTestContext ctx = testHelper.createTestContext();
        String requestEtag = "\"aeiou\"";
        ctx.getHttpRequest().addHeader("If-Match", requestEtag);

        try {
            new StandardRequestHandler(null, null, null).preconditions(ctx.getDavRequest(), ctx.getDavResponse(), home);
            Assert.fail("If-Match bogus etag succeeded");
        } catch (PreconditionFailedException e) {
        }

        String responseEtag = (String) ctx.getHttpResponse().getHeader("ETag");
        Assert.assertNotNull("Null ETag header", responseEtag);
        Assert.assertEquals("Incorrect ETag header value", responseEtag, home.getETag());
    }

    /**
     * Tests if none match all.
     * 
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testIfNoneMatchAll() throws Exception {
        WebDavResource home = testHelper.initializeHomeResource();

        DavTestContext ctx = testHelper.createTestContext();
        ctx.getHttpRequest().setMethod("GET");
        ctx.getHttpRequest().addHeader("If-None-Match", "*");

        try {
            new StandardRequestHandler(null, null, null).preconditions(ctx.getDavRequest(), ctx.getDavResponse(), home);
            Assert.fail("If-None-Match all succeeded");
        } catch (NotModifiedException e) {
            // expected
        } catch (PreconditionFailedException e) {
            Assert.fail("If-None-Match all (method GET) failed with 412");
        }

        String responseEtag = (String) ctx.getHttpResponse().getHeader("ETag");
        Assert.assertNotNull("Null ETag header", responseEtag);
        Assert.assertEquals("Incorrect ETag header value", responseEtag, home.getETag());
    }

    /**
     * Tests if none match ok.
     * 
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testIfNoneMatchOk() throws Exception {
        WebDavResource home = testHelper.initializeHomeResource();

        DavTestContext ctx = testHelper.createTestContext();
        String requestEtag = "\"aeiou\"";
        ctx.getHttpRequest().addHeader("If-None-Match", requestEtag);

        try {
            new StandardRequestHandler(null, null, null).preconditions(ctx.getDavRequest(), ctx.getDavResponse(), home);
        } catch (PreconditionFailedException e) {
            Assert.fail("If-None-Match bogus etag failed");
        }
    }

    /**
     * Tests if none match non ok.
     * 
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testIfNoneMatchNotOk() throws Exception {
        WebDavResource home = testHelper.initializeHomeResource();

        DavTestContext ctx = testHelper.createTestContext();
        ctx.getHttpRequest().setMethod("GET");
        ctx.getHttpRequest().addHeader("If-None-Match", home.getETag());

        try {
            new StandardRequestHandler(null, null, null).preconditions(ctx.getDavRequest(), ctx.getDavResponse(), home);
            Assert.fail("If-None-Match specific etag succeeded");
        } catch (NotModifiedException e) {
            // expected
        } catch (PreconditionFailedException e) {
            Assert.fail("If-None-Match specific etag (method GET) failed with 412");
        }

        String responseEtag = (String) ctx.getHttpResponse().getHeader("ETag");
        Assert.assertNotNull("Null ETag header", responseEtag);
        Assert.assertEquals("Incorrect ETag header value", responseEtag, home.getETag());
    }

    /**
     * Tests if modified since unmodified.
     * 
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testIfModifiedSinceUnmodified() throws Exception {
        WebDavResource home = testHelper.initializeHomeResource();

        DavTestContext ctx = testHelper.createTestContext();
        Date requestDate = since(home, 1000000);
        ctx.getHttpRequest().addHeader("If-Modified-Since", requestDate);

        try {
            new StandardRequestHandler(null, null, null).preconditions(ctx.getDavRequest(), ctx.getDavResponse(), home);
            Assert.fail("If-Modified-Since succeeded for unmodified resource");
        } catch (NotModifiedException e) {
            // expected
        } catch (PreconditionFailedException e) {
            Assert.fail("If-Modified-Since failed with 412 for unmodified resource");
        }
    }

    /**
     * Tests if modified since modified.
     * 
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testIfModifiedSinceModified() throws Exception {
        WebDavResource home = testHelper.initializeHomeResource();

        DavTestContext ctx = testHelper.createTestContext();
        Date requestDate = since(home, -1000000);
        ctx.getHttpRequest().addHeader("If-Modified-Since", requestDate);

        try {
            new StandardRequestHandler(null, null, null).preconditions(ctx.getDavRequest(), ctx.getDavResponse(), home);
        } catch (PreconditionFailedException e) {
            Assert.fail("If-Modified-Since failed for mmodified resource");
        }
    }

    /**
     * Tests if unmodified since unmodified.
     * 
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testIfUnmodifiedSinceUnmodified() throws Exception {
        WebDavResource home = testHelper.initializeHomeResource();

        DavTestContext ctx = testHelper.createTestContext();
        Date requestDate = since(home, 1000000);
        ctx.getHttpRequest().addHeader("If-Unmodified-Since", requestDate);

        try {
            new StandardRequestHandler(null, null, null).preconditions(ctx.getDavRequest(), ctx.getDavResponse(), home);
        } catch (PreconditionFailedException e) {
            Assert.fail("If-Unmodified-Since failed for unmodified resource");
        }
    }

    // if unmodified since earlier date, pass
    /**
     * Tests if unmodified since modified.
     * 
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testIfUnmodifiedSinceModified() throws Exception {
        WebDavResource home = testHelper.initializeHomeResource();

        DavTestContext ctx = testHelper.createTestContext();
        Date requestDate = since(home, -1000000);
        ctx.getHttpRequest().addHeader("If-Unmodified-Since", requestDate);

        try {
            new StandardRequestHandler(null, null, null).preconditions(ctx.getDavRequest(), ctx.getDavResponse(), home);
            Assert.fail("If-Unmodified-Since succeeded for modified resource");
        } catch (PreconditionFailedException e) {
        }
    }

    @Test
    public void shouldUseNeedsPrivilegesExceptionForPermissionDeniedExceptionInErrorResponse() throws Exception {
        PermissionDeniedException pde = new PermissionDeniedException("some error message");
        CosmoDavException actual = captureExceptionForErrorResponseCausedBy(pde);

        Assert.assertTrue(actual instanceof NeedsPrivilegesException);
    }

    @Test
    public void shouldUseNeedsPrivilegesExceptionForItemSecurityExceptionInErrorResponseWithSpecificAtts()
            throws Exception {
        Item item = testHelper.getHomeCollection();
        ItemSecurityException pde = new ItemSecurityException(item, "some security message", Permission.READ);
        CosmoDavException actual = captureExceptionForErrorResponseCausedBy(pde);

        Assert.assertTrue(actual instanceof NeedsPrivilegesException);

        // specific atts
        Assert.assertEquals(DavPrivilege.READ, ((NeedsPrivilegesException) actual).getPrivilege());
        Assert.assertEquals("test/Request/Uri", ((NeedsPrivilegesException) actual).getHref());

    }

    @Test
    public void shouldUseTheSameExceptionForCosmoDavExceptionInstances() throws Exception {
        BadRequestException bre = new BadRequestException("an error message");

        CosmoDavException actual = captureExceptionForErrorResponseCausedBy(bre);

        Assert.assertEquals(bre, actual);
    }

    @Test
    public void shouldMapValidationExceptionToForbiddenException() throws Exception {
        ValidationException ve = new ValidationException();

        CosmoDavException actual = captureExceptionForErrorResponseCausedBy(ve);

        Assert.assertTrue(actual instanceof ForbiddenException);
    }

    @Test
    public void shouldMapUnallowedCalendarCreationExceptionToForbiddenException() throws Exception {
        CaldavExceptionExtMkCalendarForbidden ceemcf = new CaldavExceptionExtMkCalendarForbidden("an error message");

        CosmoDavException actual = captureExceptionForErrorResponseCausedBy(ceemcf);

        Assert.assertTrue(actual instanceof ForbiddenException);
    }

    @Test
    public void shouldUseCosmoDavExceptionForUnspecificExceptionsAndSetItAsReqAtt() throws Exception {
        NullPointerException npe = new NullPointerException();

        StandardRequestHandler classUnderTest = new StandardRequestHandler(null, null, null);

        StandardRequestHandler classUnderTestSpied = Mockito.spy(classUnderTest);

        DavTestContext ctx = testHelper.createTestContext();
        HttpServletRequest req = ctx.getDavRequest();
        HttpServletResponse res = ctx.getDavResponse();

        DavRequest mockDavRequest = Mockito.mock(DavRequest.class);
        Mockito.when(mockDavRequest.getRequestURI()).thenReturn("test/Request/Uri");
        Mockito.when(classUnderTestSpied.createDavRequest(req)).thenReturn(mockDavRequest);

        DavResponse mockDavResponse = Mockito.mock(DavResponse.class);
        Mockito.when(classUnderTestSpied.createDavResponse(res)).thenReturn(mockDavResponse);

        Mockito.doThrow(npe).when(classUnderTestSpied).resolveTarget(Mockito.any(DavRequest.class));

        classUnderTestSpied.handleRequest(ctx.getDavRequest(), ctx.getDavResponse());

        ArgumentCaptor<CosmoDavException> exceptionCaptor = ArgumentCaptor.forClass(CosmoDavException.class);
        Mockito.verify(mockDavResponse).sendDavError(exceptionCaptor.capture());

        CosmoDavException actual = exceptionCaptor.getValue();

        Assert.assertTrue(actual instanceof CosmoDavException);
        Assert.assertEquals(npe, req.getAttribute(ServerConstants.ATTR_SERVICE_EXCEPTION));
    }

    private CosmoDavException captureExceptionForErrorResponseCausedBy(Throwable t) throws Exception {
        StandardRequestHandler classUnderTest = new StandardRequestHandler(null, null, null);

        StandardRequestHandler classUnderTestSpied = Mockito.spy(classUnderTest);

        DavTestContext ctx = testHelper.createTestContext();
        HttpServletRequest req = ctx.getDavRequest();
        HttpServletResponse res = ctx.getDavResponse();

        HttpServletRequest spiedReq = Mockito.spy(req);
        Mockito.when(spiedReq.getRequestURI()).thenReturn("test/Request/Uri");

        Mockito.when(classUnderTestSpied.createDavRequest(req)).thenReturn(ctx.getDavRequest());

        DavResponse mockDavResponse = Mockito.mock(DavResponse.class);
        Mockito.when(classUnderTestSpied.createDavResponse(res)).thenReturn(mockDavResponse);

        // throw the exception here
        Mockito.doThrow(t).when(classUnderTestSpied).resolveTarget(Mockito.any(DavRequest.class));

        classUnderTestSpied.handleRequest(spiedReq, res);

        ArgumentCaptor<CosmoDavException> exceptionCaptor = ArgumentCaptor.forClass(CosmoDavException.class);
        Mockito.verify(mockDavResponse).sendDavError(exceptionCaptor.capture());

        CosmoDavException ex = exceptionCaptor.getValue();

        return ex;
    }

    /**
     * Since.
     * 
     * @param resource Dav resource.
     * @param delta    delta.
     * @return The date.
     */
    private static Date since(WebDavResource resource, long delta) {
        return new Date((resource.getModificationTime() / 1000 * 1000) + delta);
    }
}