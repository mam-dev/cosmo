package org.unitedinternet.cosmo.dav.acl.resource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitedinternet.cosmo.dav.acl.AcePrincipal;
import org.unitedinternet.cosmo.dav.acl.AcePrincipalType;
import org.unitedinternet.cosmo.dav.acl.AnyAce;
import org.unitedinternet.cosmo.dav.acl.DavPrivilege;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class AnyAceTest {

    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    @Before
    public void setUp() {
        factory.setNamespaceAware(true);
    }



    private Element getRootElementFromFile(String fileName) {
        final String baseDir = "src/test/unit/resources/testdata/testace/";
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.parse(new File(baseDir + fileName));
            return doc.getDocumentElement();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            Assert.fail("Exception: " + e);
            return null;
        }
    }

    @Test
    public void testLoadValidHrefXML () {
        Element el = getRootElementFromFile("validhref.xml");
        AnyAce ace = AnyAce.fromXml(el);

        Assert.assertFalse(ace.isDenied());
        Assert.assertFalse(ace.isInverted());
        Assert.assertFalse(ace.isProtected());
        Assert.assertEquals(2, ace.getPrivileges().size());
        Assert.assertTrue(ace.getPrivileges().contains(DavPrivilege.READ));
        Assert.assertTrue(ace.getPrivileges().contains(DavPrivilege.WRITE));
        AcePrincipal principal = ace.getAcePrincipal();
        Assert.assertEquals(AcePrincipalType.HREF, principal.getType());
        Assert.assertEquals("http://www.example.com/users/esedlar", principal.getValue());

    }
}
