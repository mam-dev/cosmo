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
package org.unitedinternet.cosmo.dav.provider;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitedinternet.cosmo.dav.BaseDavTestCase;
import org.unitedinternet.cosmo.dav.ConflictException;
import org.unitedinternet.cosmo.dav.DavCollection;
import org.unitedinternet.cosmo.dav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.DavTestContext;
import org.unitedinternet.cosmo.dav.ExistsException;
import org.unitedinternet.cosmo.dav.UnsupportedMediaTypeException;
import org.unitedinternet.cosmo.dav.impl.DavCollectionBase;

/**
 * Test case for <code>MKCOL</code>.
 */
public class CreateCollectionTest extends BaseDavTestCase {

    /**
     * <blockquote>
     * If the Request-URI is already mapped to a resource, then the MKCOL
     * MUST fail.
     * </blockquote>
     */
    public CreateCollectionTest() {
        super();
    }
    
    /**
     * 
     * @throws Exception 
     */
    @Test
    public void testExists() throws Exception { 
        CollectionProvider provider = createCollectionProvider();
        DavTestContext ctx = testHelper.createTestContext();
        DavCollection member = testHelper.initializeHomeResource();

        try {
            provider.mkcol(ctx.getDavRequest(), ctx.getDavResponse(), member);
            Assert.fail("mkcol succeeded when resource already exists");
        } catch (ExistsException e) {}
    }

    /**
     * <blockquote>
     * During MKCOL processing, a server MUST make the Request-URI an internal
     * member of its parent collection, unless the Request-URI is "/".
     * </blockquote>
     * <p>
     * There is no need to test the final clause since root collections by
     * definition always exist in Cosmo and are therefore covered by the test
     * for <code>MKCOL</code> against an existing resource.
     * </p>
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testAddMember() throws Exception {
        CollectionProvider provider = createCollectionProvider();
        DavTestContext ctx = testHelper.createTestContext();
        DavCollection member = createTestMember("add-member");

        provider.mkcol(ctx.getDavRequest(), ctx.getDavResponse(), member);

        Assert.assertEquals("response status not 201", 201,
                     ctx.getHttpResponse().getStatus());

        DavCollection home = testHelper.initializeHomeResource();
        Assert.assertNotNull("member not found in parent collection",
                      testHelper.findMember(home, "add-member"));
    }

    /**
     * <blockquote>
     * If no such ancestor exists, the method MUST fail.  When the MKCOL
     * operation creates a new collection resource, all ancestors MUST
     * already exist, or the method MUST fail with a 409 (Conflict) status
     * code.  For example, if a request to create collection /a/b/c/d/ is
     * made, and /a/b/c/ does not exist, the request must fail.
     * </blockquote>
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testAddMemberAtBogusLocation() throws Exception { 
        CollectionProvider provider = createCollectionProvider();
        DavTestContext ctx = testHelper.createTestContext();
        DavCollection member = createTestMember("/a/b/c/d", "member");

        try {
            provider.mkcol(ctx.getDavRequest(), ctx.getDavResponse(), member);
            Assert.fail("mkcol succeeded when location is bogus");
        } catch (ConflictException e) {}
    }

    /**
     * <blockquote>
     * When MKCOL is invoked without a request body, the newly created
     * collection SHOULD have no members.
     * </blockquote>
     * <p>
     * The server does not any default members to newly created collections.
     * </p>
     * @throws  Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testNoBody() throws Exception {
         CollectionProvider provider = createCollectionProvider();
         DavTestContext ctx = testHelper.createTestContext();
         DavCollection member = createTestMember("no-body");

         provider.mkcol(ctx.getDavRequest(), ctx.getDavResponse(), member);

         Assert.assertEquals("found unexpected members in new collection", 0,
                       member.getMembers().size());
    }

    /**
     * <blockquote>
     * A MKCOL request message may contain a message body.  The precise
     * behavior of a MKCOL request when the body is present is undefined,
     * but limited to creating collections, members of a collection, bodies
     * of members, and properties on the collections or members.  If the
     * server receives a MKCOL request entity type it does not support or
     * understand, it MUST respond with a 415 (Unsupported Media Type)
     * status code.
     * </blockquote>
     * <p>
     * The server does not support bodies for <code>MKCOL</code> requests.
     * </p>
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testWithBody() throws Exception {
        CollectionProvider provider = createCollectionProvider();
        DavTestContext ctx = testHelper.createTestContext();
        ctx.setTextRequestBody("<this-is-the-request-body/>");
        DavCollection member = createTestMember("with-body");

        try {
            provider.mkcol(ctx.getDavRequest(), ctx.getDavResponse(), member);
            Assert.fail("mkcol succeeded even with request body");
        } catch (UnsupportedMediaTypeException e) {}
     }

    // working provider methods require a security context so that owner
    // info can be set on created resources, etc
    /**
     * SetUp.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();
        testHelper.logIn();
    }

    /**
     * Creates collection provider.
     * @return The collection provider.
     */
    private CollectionProvider createCollectionProvider() {
        return new CollectionProvider(testHelper.getResourceFactory(),
                testHelper.getEntityFactory());
    }

    /**
     * Creates test member.
     * @param segment The segment.
     * @return The dav collection.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    private DavCollection createTestMember(String segment)
        throws Exception {
        return createTestMember(testHelper.getHomeLocator(), segment);
    }

    /**
     * Creates test member.
     * @param path The path.
     * @param segment The segment.
     * @return The dav collection.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    private DavCollection createTestMember(String path,
                                           String segment)
        throws Exception {
        return createTestMember(testHelper.createLocator(path), segment);
    }

    /**
     * Creates test member.
     * @param locator The dav resource locator.
     * @param segment The segment.
     * @return The dav collection.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    private DavCollection createTestMember(DavResourceLocator locator,
                                           String segment)
        throws Exception {
        DavResourceLocator memberLocator =
            testHelper.createMemberLocator(locator, segment);
        return new DavCollectionBase(memberLocator,
                                     testHelper.getResourceFactory(),
                                     testHelper.getEntityFactory());
    }
}
