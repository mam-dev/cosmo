package org.unitedinternet.cosmo.dav.carddav;

import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.xml.Namespace;

/**
 * Provides constants for media types, XML namespaces, names and
 * values, DAV properties and resource types defined by the CardDAV
 * spec https://tools.ietf.org/html/rfc6352 sections 6, 7, 8
 */
public interface CarddavConstants {
    /* The CardDAV XML namespace */
    public static final String PRE_CARDDAV ="C";
    public static final String NS_CARDDAV = "urn:ietf:params:xml:ns:carddav";
    public static final Namespace NAMESPACE_CARDDAV =
            Namespace.getNamespace(PRE_CARDDAV, NS_CARDDAV);


    public static final String ELEMENT_ADDRESSBOOK = "addressbook";


    public static final DavPropertyName ADDRESSBOOK =
            DavPropertyName.create(ELEMENT_ADDRESSBOOK, NAMESPACE_CARDDAV);

        /* The CardDAV property name CARDDAV:addressbook-description */
    public static final String PROPERTY_CARDDAV_ADDRESSBOOK_DESCRIPTION =
            "addressbook-description";

    /* The CardDAV property name CARDDAV:supported-address-data */
    public static final String PROPERTY_CARDDAV_SUPPORTED_ADDRESS_DATA =
            "supported-address-data";

    /** The CardDAV property name CARDDAV:max-resource-size */
    public static final String PROPERTY_CARDDAV_MAX_RESOURCE_SIZE =
        "max-resource-size";


    /* The carddav property name CARDDAV:addressbook-home-set */
    public static final String PROPERTY_CARDDAV_ADDRESSBOOK_HOME_SET =
            "addressbook-home-set";

    /** The CardDAV property name CARDDAV:principal-address */
    public static final String PROPERTY_CARDDAV_PRINCIPAL_ADDRESS =
        "principal-address";
    /** The CardDAV property name CARDDAV:supported-collation-set */
    public static final String PROPERTY_CARDDAV_SUPPORTED_COLLATION_SET =
        "supported-collation-set";


        /* The CardDAV property CARDDAV:addressbook-description */
    public static final DavPropertyName ADDRESSBOOKDESCRIPTION =
            DavPropertyName.create(PROPERTY_CARDDAV_ADDRESSBOOK_DESCRIPTION,
                    NAMESPACE_CARDDAV);

    /* The CardDAV property CARDDAV:supported-address-data */
    public static final DavPropertyName SUPPORTEDADDRESSDATA =
            DavPropertyName.create(PROPERTY_CARDDAV_SUPPORTED_ADDRESS_DATA,
                    NAMESPACE_CARDDAV);

    /** The CardDAV property CARDDAV:max-resource-size */
    public static final DavPropertyName MAXRESOURCESIZE =
        DavPropertyName.create(PROPERTY_CARDDAV_MAX_RESOURCE_SIZE,
                NAMESPACE_CARDDAV);


    /* The carddav property CARDDAV:addressbook-home-set */
    public static final DavPropertyName ADDRESSBOOKHOMESET =
            DavPropertyName.create(PROPERTY_CARDDAV_ADDRESSBOOK_HOME_SET,
                    NAMESPACE_CARDDAV);

    /** The CardDAV property name CARDDAV:principal-address */
    public static final DavPropertyName PRINCIPALADDRESS =
            DavPropertyName.create(PROPERTY_CARDDAV_PRINCIPAL_ADDRESS,
                    NAMESPACE_CARDDAV);

    /** The CardDAV property name CARDDAV:supported-collation-set */
    public static final DavPropertyName SUPPORTEDCOLLATIONSET =
            DavPropertyName.create(PROPERTY_CARDDAV_SUPPORTED_COLLATION_SET,
                    NAMESPACE_CARDDAV);
}
