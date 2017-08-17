package org.unitedinternet.cosmo.dav.impl.parallel;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.security.report.PrincipalMatchReport;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.ExtendedDavConstants;
import org.unitedinternet.cosmo.dav.ForbiddenException;
import org.unitedinternet.cosmo.dav.ProtectedPropertyModificationException;
import org.unitedinternet.cosmo.dav.acl.DavAce;
import org.unitedinternet.cosmo.dav.acl.DavAcl;
import org.unitedinternet.cosmo.dav.acl.DavPrivilege;
import org.unitedinternet.cosmo.dav.acl.report.PrincipalPropertySearchReport;
import org.unitedinternet.cosmo.dav.acl.report.PrincipalSearchPropertySetReport;
import org.unitedinternet.cosmo.dav.parallel.CalDavCollection;
import org.unitedinternet.cosmo.dav.parallel.CalDavContentResource;
import org.unitedinternet.cosmo.dav.parallel.CalDavResource;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceFactory;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceLocator;
import org.unitedinternet.cosmo.dav.property.CurrentUserPrincipal;
import org.unitedinternet.cosmo.dav.property.DisplayName;
import org.unitedinternet.cosmo.dav.property.IsCollection;
import org.unitedinternet.cosmo.dav.property.ResourceType;
import org.unitedinternet.cosmo.dav.property.WebDavProperty;
import org.unitedinternet.cosmo.model.User;

public class CalDavUserPrincipalCollection extends CalDavCollectionBase {

	private static final Set<ReportType> REPORT_TYPES = new HashSet<ReportType>();

	private DavAcl acl;

	static {
		registerLiveProperty(DavPropertyName.DISPLAYNAME);
		registerLiveProperty(DavPropertyName.ISCOLLECTION);
		registerLiveProperty(DavPropertyName.RESOURCETYPE);
		registerLiveProperty(ExtendedDavConstants.CURRENTUSERPRINCIPAL);

		REPORT_TYPES.add(PrincipalMatchReport.REPORT_TYPE);
		REPORT_TYPES.add(PrincipalPropertySearchReport.REPORT_TYPE_PRINCIPAL_PROPERTY_SEARCH);
		REPORT_TYPES.add(PrincipalSearchPropertySetReport.REPORT_TYPE_PRINCIPAL_SEARCH_PROPERTY_SET);
	}

	public CalDavUserPrincipalCollection(CalDavResourceLocator calDavResourceLocator,
			CalDavResourceFactory calDavResourceFactory) {
		super(null, calDavResourceLocator, calDavResourceFactory, null);
		acl = makeAcl();
	}

	private DavAcl makeAcl() {
		DavAcl acl = new DavAcl();

		DavAce unauthenticated = new DavAce.UnauthenticatedAce();
		unauthenticated.setDenied(true);
		unauthenticated.getPrivileges().add(DavPrivilege.ALL);
		unauthenticated.setProtected(true);
		acl.getAces().add(unauthenticated);

		DavAce allAllow = new DavAce.AllAce();
		allAllow.getPrivileges().add(DavPrivilege.READ);
		allAllow.getPrivileges().add(DavPrivilege.READ_CURRENT_USER_PRIVILEGE_SET);
		allAllow.setProtected(true);
		acl.getAces().add(allAllow);

		DavAce allDeny = new DavAce.AllAce();
		allDeny.setDenied(true);
		allDeny.getPrivileges().add(DavPrivilege.ALL);
		allDeny.setProtected(true);
		acl.getAces().add(allDeny);

		return acl;
	}

	public String getSupportedMethods() {
		return "OPTIONS, GET, HEAD, TRACE, PROPFIND, PROPPATCH, REPORT";
	}

	public boolean isCollection() {
		return true;
	}

	public long getModificationTime() {
		return -1;
	}

	public boolean exists() {
		return true;
	}

	public String getDisplayName() {
		return "User Principals";
	}

	public String getETag() {
		return null;
	}

	@Override
	public void addMember(DavResource member, InputContext inputContext) throws DavException {
		throw new UnsupportedOperationException();
	}

	@Override
	public DavResourceIterator getMembers() {
		throw new UnsupportedOperationException();
	}

	@Override
	public DavResourceIterator getCollectionMembers() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeMember(DavResource member) throws DavException {
		throw new UnsupportedOperationException();
	}

	@Override
	public DavResource getCollection() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void move(DavResource destination) throws DavException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void copy(DavResource destination, boolean shallow) throws DavException {
		throw new UnsupportedOperationException();
	}

	// WebDavResource

	@Override
	public CalDavCollection getParent() {
		return null;
	}

	@Override
	public void addContent(CalDavContentResource content, InputContext context) throws CosmoDavException {
		throw new UnsupportedOperationException();
	}

	@Override
	public MultiStatusResponse addCollection(CalDavCollection collection, DavPropertySet properties)
			throws CosmoDavException {
		throw new UnsupportedOperationException();
	}

	@Override
	public CalDavResource findMember(String href) throws CosmoDavException  {
		CalDavResourceLocator locator = calDavLocatorFactory
				.createResourceLocatorByUri(calDavResourceLocator.getContext(), href);
		return calDavResourceFactory.resolve(locator);
	}

	protected Set<QName> getResourceTypes() {
		HashSet<QName> rt = new HashSet<QName>(1);
		rt.add(RESOURCE_TYPE_COLLECTION);
		return rt;
	}
	
	public Set<ReportType> getReportTypes() {
        return REPORT_TYPES;
    }
	
	/**
     * Returns the resource's access control list. The list contains the following ACEs:
     * 
     * <ol>
     * <li> <code>DAV:unauthenticated</code>: deny <code>DAV:all</code></li>
     * <li> <code>DAV:all</code>: allow <code>DAV:read, DAV:read-current-user-privilege-set</code></li>
     * <li> <code>DAV:all</code>: deny <code>DAV:all</code></li>
     * </ol>
     */
    protected DavAcl getAcl() {
        return acl;
    }
    
    /**
     * <p>
     * Extends the superclass method to return {@link DavPrivilege#READ} if the the current principal is a non-admin
     * user.
     * </p>
     */
    protected Set<DavPrivilege> getCurrentPrincipalPrivileges() {
        Set<DavPrivilege> privileges = super.getCurrentPrincipalPrivileges();
        if (!privileges.isEmpty()) {
            return privileges;
        }

        User user = getSecurityManager().getSecurityContext().getUser();
        if (user != null) {
            privileges.add(DavPrivilege.READ);
        }

        return privileges;
    }
    
    protected void loadLiveProperties(DavPropertySet properties) {
        properties.add(new DisplayName(getDisplayName()));
        properties.add(new ResourceType(getResourceTypes()));
        properties.add(new IsCollection(isCollection()));
        properties.add(new CurrentUserPrincipal(calDavResourceLocator,getSecurityManager().getSecurityContext().getUser()));
    }

    //XXX-Review this make it protected again
    public void setLiveProperty(WebDavProperty property, boolean create) throws CosmoDavException {
        throw new ProtectedPropertyModificationException(property.getName());
    }
    
    //XXX-Review this make it protected again
    public void removeLiveProperty(DavPropertyName name) throws CosmoDavException {
        throw new ProtectedPropertyModificationException(name);
    }

    protected void loadDeadProperties(DavPropertySet properties) {
    }

    protected void setDeadProperty(WebDavProperty property) throws CosmoDavException {
        throw new ForbiddenException("Dead properties are not supported on this collection");
    }

    protected void removeDeadProperty(DavPropertyName name) throws CosmoDavException {
        throw new ForbiddenException("Dead properties are not supported on this collection");
    }
}