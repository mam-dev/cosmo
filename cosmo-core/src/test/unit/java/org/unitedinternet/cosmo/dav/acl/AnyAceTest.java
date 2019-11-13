package org.unitedinternet.cosmo.dav.acl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitedinternet.cosmo.dav.DavTestHelper;
import org.unitedinternet.cosmo.dav.property.PrincipalUtils;
import org.unitedinternet.cosmo.model.Ace;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.security.Permission;

import java.util.Collections;

public class AnyAceTest {
    protected DavTestHelper helper;
    private  Ace createDummyUserReadAce() {
        //Create Dummy Ace
        Ace ace = helper.makeDummyAce();
        User user = helper.makeDummyUser();
        ace.setUser(user);
        ace.getPermissions().add(Permission.READ);

        return ace;
    }

    private  Ace createDummyAuthenticatedReadAce() {
        Ace ace = helper.makeDummyAce();
        ace.setType(Ace.Type.AUTHENTICATED);
        ace.getPermissions().add(Permission.READ);
        return ace;
    }

    @Before
    public void setUp() throws Exception {
        helper = new DavTestHelper();
        helper.setUp();

    }

    @Test
    public void fromAceTypeUser() {
        Ace ace = createDummyUserReadAce();
        AnyAce actual = AnyAce.fromAce(ace, helper.getHomeLocator());
        Assert.assertNotNull(actual.getPrincipal());
        Assert.assertEquals(AcePrincipalType.HREF, actual.getPrincipal().getType());
        Assert.assertEquals(PrincipalUtils.href(helper.getHomeLocator(), ace.getUser()), actual.getPrincipal().getValue());
        Assert.assertTrue(actual.getPrivileges().containsRecursive(DavPrivilege.READ));
        Assert.assertFalse(actual.getPrivileges().containsRecursive(DavPrivilege.WRITE));
        Assert.assertFalse(actual.getPrivileges().containsRecursive(DavPrivilege.READ_FREE_BUSY));
        Assert.assertFalse(actual.getPrivileges().containsRecursive(DavPrivilege.ALL));

    }

        @Test
        public void fromAceTypeAuthenticated() {
            Ace ace = createDummyAuthenticatedReadAce();
            AnyAce actual = AnyAce.fromAce(ace, helper.getHomeLocator());
            Assert.assertNotNull(actual.getPrincipal());
            Assert.assertEquals(AcePrincipalType.AUTHENTICATED, actual.getPrincipal().getType());
            Assert.assertTrue(actual.getPrivileges().containsRecursive(DavPrivilege.READ));
            Assert.assertFalse(actual.getPrivileges().containsRecursive(DavPrivilege.WRITE));
            Assert.assertFalse(actual.getPrivileges().containsRecursive(DavPrivilege.READ_FREE_BUSY));
            Assert.assertFalse(actual.getPrivileges().containsRecursive(DavPrivilege.ALL));

    }
    @Test
    public void toUserAce() throws NotRecognizedPrincipalException, NotAllowedPrincipalException {

        User user = helper.makeDummyUser("user", "password");
        helper.getUserService().createUser(user);

        AcePrincipal acePrincipal = new AcePrincipal();
        acePrincipal.setHref(PrincipalUtils.href(helper.getHomeLocator(), user));
        AnyAce davAce = new AnyAce(acePrincipal, Collections.singleton(DavPrivilege.READ));




        davAce.setAcePrincipal(acePrincipal);


        Ace actual = helper.getEntityFactory().createAce();
        davAce.toAce(actual, helper.getHomeLocator(), helper.getResourceFactory());

        Assert.assertNotNull(actual);
        Assert.assertEquals(Ace.Type.USER, actual.getType());
        Assert.assertEquals(user, actual.getUser());
        Assert.assertEquals(Permission.READ, actual.getPermissions().toArray()[0]);
        Assert.assertEquals(1, actual.getPermissions().toArray().length);



    }
}