package org.unitedinternet.cosmo.dav.impl.parallel;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.DavResourceIteratorImpl;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyIterator;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.security.report.PrincipalMatchReport;
import org.apache.jackrabbit.webdav.version.OptionsInfo;
import org.apache.jackrabbit.webdav.version.OptionsResponse;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.ForbiddenException;
import org.unitedinternet.cosmo.dav.ProtectedPropertyModificationException;
import org.unitedinternet.cosmo.dav.acl.AclConstants;
import org.unitedinternet.cosmo.dav.acl.DavAce;
import org.unitedinternet.cosmo.dav.acl.DavAcl;
import org.unitedinternet.cosmo.dav.acl.DavPrivilege;
import org.unitedinternet.cosmo.dav.acl.property.AlternateUriSet;
import org.unitedinternet.cosmo.dav.acl.property.GroupMembership;
import org.unitedinternet.cosmo.dav.acl.property.PrincipalUrl;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.caldav.property.CalendarHomeSet;
import org.unitedinternet.cosmo.dav.caldav.property.CalendarUserAddressSet;
import org.unitedinternet.cosmo.dav.caldav.property.ScheduleInboxURL;
import org.unitedinternet.cosmo.dav.caldav.property.ScheduleOutboxURL;
import org.unitedinternet.cosmo.dav.parallel.CalDavCollection;
import org.unitedinternet.cosmo.dav.parallel.CalDavResource;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceFactory;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceLocator;
import org.unitedinternet.cosmo.dav.property.CreationDate;
import org.unitedinternet.cosmo.dav.property.DisplayName;
import org.unitedinternet.cosmo.dav.property.Etag;
import org.unitedinternet.cosmo.dav.property.IsCollection;
import org.unitedinternet.cosmo.dav.property.LastModified;
import org.unitedinternet.cosmo.dav.property.ResourceType;
import org.unitedinternet.cosmo.dav.property.WebDavProperty;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.UserIdentity;
import org.unitedinternet.cosmo.model.UserIdentitySupplier;
import org.unitedinternet.cosmo.util.ContentTypeUtil;
import org.unitedinternet.cosmo.util.DomWriter;
import org.w3c.dom.Element;

/**
 * Models a WebDAV principal resource (as described in RFC 3744) that represents
 * a user account.
 * 
 * @author cdobrota
 *
 */
public class CalDavUserPrincipal extends CalDavResourceBase implements CaldavConstants, AclConstants {

	private static final Log LOG = LogFactory.getLog(CalDavUserPrincipal.class);

	private static final Set<ReportType> REPORT_TYPES = new HashSet<ReportType>();

	static {
		registerLiveProperty(DavPropertyName.CREATIONDATE);
		registerLiveProperty(DavPropertyName.GETLASTMODIFIED);
		registerLiveProperty(DavPropertyName.DISPLAYNAME);
		registerLiveProperty(DavPropertyName.ISCOLLECTION);
		registerLiveProperty(DavPropertyName.RESOURCETYPE);
		registerLiveProperty(DavPropertyName.GETETAG);
		registerLiveProperty(CALENDARHOMESET);
		registerLiveProperty(CALENDARUSERADDRESSSET);
		registerLiveProperty(SCHEDULEINBOXURL);
		registerLiveProperty(SCHEDULEOUTBOXURL);
		registerLiveProperty(ALTERNATEURISET);
		registerLiveProperty(PRINCIPALURL);
		registerLiveProperty(GROUPMEMBERSHIP);

		REPORT_TYPES.add(PrincipalMatchReport.REPORT_TYPE);
	}

	private User user;
	private UserIdentitySupplier userIdentitySupplier;
	private CalDavUserPrincipalCollection parent;
	private DavAcl acl;



	public CalDavUserPrincipal(User user, 
								CalDavResourceLocator locator,
								CalDavResourceFactory calDavResourceFactory, 
								UserIdentitySupplier userIdentitySupplier) {
		super(locator, calDavResourceFactory);
		
		this.user = user;
		this.userIdentitySupplier = userIdentitySupplier;
		this.acl = makeAcl();
	}

	@Override
	public String getSupportedMethods() {
		return "OPTIONS, GET, HEAD, TRACE, PROPFIND, PROPPATCH, REPORT";
	}

	@Override
	public boolean isCollection() {
		return false;
	}

	public long getModificationTime() {
		return user.getModifiedDate().getTime();
	}

	public boolean exists() {
		return true;
	}

	public String getDisplayName() {
		UserIdentity userIdentity = userIdentitySupplier.forUser(user);
		String firstName = userIdentity.getFirstName();
		String lastName = userIdentity.getLastName();
		String email = userIdentity.getEmails().isEmpty() ? "" : userIdentity.getEmails().iterator().next();

		String toReturn = null;

		if (firstName == null && lastName == null) {
			toReturn = email;
		} else if (firstName == null) {
			toReturn = lastName;
		} else if (lastName == null) {
			toReturn = firstName;
		} else {
			toReturn = firstName + " " + lastName;
		}
		return toReturn;
	}

	public String getETag() {
		return "\"" + user.getEntityTag() + "\"";
	}

	@Override
	public void addMember(DavResource member, InputContext inputContext) throws DavException {
		throw new UnsupportedOperationException();
	}

	public DavResourceIterator getMembers() {
		// while it would be ideal to throw an UnsupportedOperationException,
		// MultiStatus tries to add a MultiStatusResponse for every member
		// of a WebDavResource regardless of whether or not it's a collection,
		// so we need to return an empty iterator.
		return new DavResourceIteratorImpl(new ArrayList<DavResource>());
	}

	public void removeMember(DavResource member) {
		throw new UnsupportedOperationException();
	}

	public CalDavResource getCollection() {
		if (parent == null) {
			CalDavResourceLocator parentLocator = calDavResourceLocator.getParentLocator();
			try {
				parent = (CalDavUserPrincipalCollection) calDavResourceFactory.resolve(parentLocator);
			} catch (CosmoDavException e) {
				throw new RuntimeException(e);
			}
		}

		return parent;
	}

	public void move(DavResource destination) throws DavException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void updateItem() throws CosmoDavException {
		throw new UnsupportedOperationException();
	}

	public void copy(DavResource destination, boolean shallow) throws DavException {
		throw new UnsupportedOperationException();
	}

	public User getUser() {
		return user;
	}

	@Override
	protected Set<String> getDeadPropertyFilter() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Set<QName> getResourceTypes() {
		HashSet<QName> rt = new HashSet<QName>(1);
		rt.add(RESOURCE_TYPE_PRINCIPAL);
		return rt;
	}

	public Set<ReportType> getReportTypes() {
		return REPORT_TYPES;
	}

	protected DavAcl getAcl() {
		return acl;
	}

	private DavAcl makeAcl() {
		DavAcl acl = new DavAcl();

		DavAce unauthenticated = new DavAce.UnauthenticatedAce();
		unauthenticated.setDenied(true);
		unauthenticated.getPrivileges().add(DavPrivilege.ALL);
		unauthenticated.setProtected(true);
		acl.getAces().add(unauthenticated);

		DavAce owner = new DavAce.SelfAce();
		owner.getPrivileges().add(DavPrivilege.ALL);
		owner.setProtected(true);
		acl.getAces().add(owner);

		DavAce allAllow = new DavAce.AllAce();
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

	/**
	 * <p>
	 * Extends the superclass method to return {@link DavPrivilege#READ} if the
	 * the current principal is the non-admin user represented by this resource.
	 * </p>
	 */
	protected Set<DavPrivilege> getCurrentPrincipalPrivileges() {
		Set<DavPrivilege> privileges = super.getCurrentPrincipalPrivileges();
		if (!privileges.isEmpty()) {
			return privileges;
		}

		User user = getSecurityManager().getSecurityContext().getUser();
		if (user != null && user.equals(this.user)) {
			privileges.add(DavPrivilege.READ);
		}

		return privileges;
	}

	protected void loadLiveProperties(DavPropertySet properties) {
		properties.add(new CreationDate(user.getCreationDate()));
		properties.add(new DisplayName(getDisplayName()));
		properties.add(new ResourceType(getResourceTypes()));
		properties.add(new IsCollection(isCollection()));
		properties.add(new Etag(user.getEntityTag()));
		properties.add(new LastModified(user.getModifiedDate()));
		properties.add(new CalendarHomeSet(calDavResourceLocator, user));

		properties.add(new CalendarUserAddressSet(user, userIdentitySupplier));
		properties.add(new ScheduleInboxURL(calDavResourceLocator, user));
		properties.add(new ScheduleOutboxURL(calDavResourceLocator, user));

		properties.add(new AlternateUriSet());
		properties.add(new PrincipalUrl(calDavResourceLocator, user));
		properties.add(new GroupMembership());
	}

	protected void setLiveProperty(WebDavProperty property, boolean create) throws CosmoDavException {
		throw new ProtectedPropertyModificationException(property.getName());
	}

	protected void removeLiveProperty(DavPropertyName name) throws CosmoDavException {
		throw new ProtectedPropertyModificationException(name);
	}

	protected void loadDeadProperties(DavPropertySet properties) {
	}

	protected void setDeadProperty(WebDavProperty property) throws CosmoDavException {
		throw new ForbiddenException("Dead properties are not supported on this resource");
	}

	protected void removeDeadProperty(DavPropertyName name) throws CosmoDavException {
		throw new ForbiddenException("Dead properties are not supported on this resource");
	}

	private void writeHtmlRepresentation(OutputContext context) throws CosmoDavException, IOException {
		context.setContentType(ContentTypeUtil.buildContentType("text/html", "UTF-8"));
		context.setModificationTime(getModificationTime());
		context.setETag(getETag());

		if (!context.hasStream()) {
			return;
		}

		PrintWriter writer = new PrintWriter(new OutputStreamWriter(context.getOutputStream(), "utf8"));
		try {
			writer.write("<html>\n<head><title>");
			writer.write(StringEscapeUtils.escapeHtml(getDisplayName()));
			writer.write("</title></head>\n");
			writer.write("<body>\n");
			writer.write("<h1>");
			writer.write(StringEscapeUtils.escapeHtml(getDisplayName()));
			writer.write("</h1>\n");

			writer.write("<h2>Properties</h2>\n");
			writer.write("<dl>\n");
			for (DavPropertyIterator i = getProperties().iterator(); i.hasNext();) {
				WebDavProperty prop = (WebDavProperty) i.nextProperty();
				Object value = prop.getValue();
				String text = null;
				if (value instanceof Element) {
					try {
						text = DomWriter.write((Element) value);
					} catch (XMLStreamException e) {
						LOG.warn("Error serializing value for property " + prop.getName());
					}
				}
				if (text == null) {
					text = prop.getValueText();
				}
				writer.write("<dt>");
				writer.write(StringEscapeUtils.escapeHtml(prop.getName().toString()));
				writer.write("</dt><dd>");
				writer.write(StringEscapeUtils.escapeHtml(text));
				writer.write("</dd>\n");
			}
			writer.write("</dl>\n");

			CalDavResource parent = getParent();
			writer.write("<a href=\"");
			writer.write(parent.getLocator().getHref(true));
			writer.write("\">");
			writer.write(StringEscapeUtils.escapeHtml(parent.getDisplayName()));
			writer.write("</a></li>\n");

			User user = getSecurityManager().getSecurityContext().getUser();
			if (user != null) {
				writer.write("<p>\n");
				DavResourceLocator homeLocator = calDavLocatorFactory
						.createHomeLocator(calDavResourceLocator.getContext(), user);
				writer.write("<a href=\"");
				writer.write(homeLocator.getHref(true));
				writer.write("\">");
				writer.write("Home collection");
				writer.write("</a><br>\n");
			}

			writer.write("</body>");
			writer.write("</html>\n");
		} finally {
			writer.close();
		}
	}

	@Override
	public void spool(OutputContext outputContext) throws IOException {
		try {
			writeHtmlRepresentation(outputContext);
		} catch (CosmoDavException e) {
			e.printStackTrace();
		}
	}

	@Override
	public CalDavCollection getParent() throws CosmoDavException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OptionsResponse getOptionResponse(OptionsInfo optionsInfo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DavPropertyName[] getPropertyNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DavProperty<?> getProperty(DavPropertyName name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DavPropertySet getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setProperty(DavProperty<?> property) throws DavException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeProperty(DavPropertyName propertyName) throws DavException {
		// TODO Auto-generated method stub
		
	}

}