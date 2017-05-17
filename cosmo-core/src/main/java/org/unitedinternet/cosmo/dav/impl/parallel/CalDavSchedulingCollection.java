package org.unitedinternet.cosmo.dav.impl.parallel;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceIterator;
import org.apache.jackrabbit.webdav.DavResourceIteratorImpl;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.property.DavProperty;
import org.apache.jackrabbit.webdav.property.DavPropertyIterator;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.version.OptionsInfo;
import org.apache.jackrabbit.webdav.version.OptionsResponse;
import org.apache.jackrabbit.webdav.version.report.ReportType;
import org.apache.log4j.Logger;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.ExtendedDavConstants;
import org.unitedinternet.cosmo.dav.ForbiddenException;
import org.unitedinternet.cosmo.dav.ProtectedPropertyModificationException;
import org.unitedinternet.cosmo.dav.acl.DavAce;
import org.unitedinternet.cosmo.dav.acl.DavAcl;
import org.unitedinternet.cosmo.dav.acl.DavPrivilege;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.parallel.CalDavCollection;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceFactory;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceLocator;
import org.unitedinternet.cosmo.dav.property.DisplayName;
import org.unitedinternet.cosmo.dav.property.Etag;
import org.unitedinternet.cosmo.dav.property.IsCollection;
import org.unitedinternet.cosmo.dav.property.ResourceType;
import org.unitedinternet.cosmo.dav.property.WebDavProperty;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.util.ContentTypeUtil;
import org.unitedinternet.cosmo.util.DomWriter;
import org.w3c.dom.Element;

public abstract class CalDavSchedulingCollection extends CalDavResourceBase implements ExtendedDavConstants, CaldavConstants{
	static {
		registerLiveProperty(DavPropertyName.DISPLAYNAME);
		registerLiveProperty(DavPropertyName.ISCOLLECTION);
		registerLiveProperty(DavPropertyName.RESOURCETYPE);
		registerLiveProperty(DavPropertyName.GETETAG);
	}


	private static final Logger LOG = Logger.getLogger(CalDavOutboxCollection.class);

	private static final Set<ReportType> REPORT_TYPES = new HashSet<ReportType>();

	private DavAcl acl;
	private HomeCollection parent;
	
	public CalDavSchedulingCollection(CalDavResourceLocator calDavResourceLocator,
										CalDavResourceFactory calDavResourceFactory) {
		super(calDavResourceLocator, calDavResourceFactory);
		acl = makeAcl();
	}

	@Override
	public abstract String getSupportedMethods();

	@Override
	public long getModificationTime() {
		return -1;
	}

	public abstract String getDisplayName();

	public String getETag() {
		return "";
	}

	@Override
	public void addMember(DavResource member, InputContext inputContext) throws DavException {
		throw new UnsupportedOperationException();
	}

	@Override
	public DavResourceIterator getMembers() {
		return DavResourceIteratorImpl.EMPTY;
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

	@Override
	public CalDavCollection getParent() throws CosmoDavException {
		if (parent == null) {
			CalDavResourceLocator parentLocator = calDavResourceLocator.getParentLocator();
			parent = (HomeCollection) calDavResourceFactory.resolve(parentLocator);
		}
		return parent;
	}


	@Override
	protected abstract Set<QName> getResourceTypes();

	@Override
	protected Set<ReportType> getReportTypes() {
		return REPORT_TYPES;
	}

	/**
	 * Returns the resource's access control list. The list contains the
	 * following ACEs:
	 *
	 * <ol>
	 * <li><code>DAV:unauthenticated</code>: deny <code>DAV:all</code></li>
	 * <li><code>DAV:all</code>: allow
	 * <code>DAV:read, DAV:read-current-user-privilege-set</code></li>
	 * <li><code>DAV:all</code>: deny <code>DAV:all</code></li>
	 * </ol>
	 */
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

	/**
	 * <p>
	 * Extends the superclass method to return {@link DavPrivilege#READ} if the
	 * the current principal is a non-admin user.
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
		properties.add(new Etag(getETag()));
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
		throw new ForbiddenException("Dead properties are not supported on this collection");
	}

	protected void removeDeadProperty(DavPropertyName name) throws CosmoDavException {
		throw new ForbiddenException("Dead properties are not supported on this collection");
	}

	@Override
	public void spool(OutputContext outputContext) throws IOException {
		try {
			writeHtmlDirectoryIndex(outputContext);
		} catch (CosmoDavException e) {
			throw new RuntimeException(e);
		}
	}

	private void writeHtmlDirectoryIndex(OutputContext context) throws CosmoDavException, IOException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("writing html directory index for  " + getDisplayName());
		}
		context.setContentType(ContentTypeUtil.buildContentType("text/html", "UTF-8"));
		// no modification time or etag

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

			User user = getSecurityManager().getSecurityContext().getUser();
			if (user != null) {
				writer.write("<p>\n");

				CalDavResourceLocator homeLocator = calDavLocatorFactory
						.createHomeLocator(calDavResourceLocator.getContext(), user);
				writer.write("<a href=\"");
				writer.write(homeLocator.getHref(true));
				writer.write("\">");
				writer.write("Home collection");
				writer.write("</a><br>\n");

				CalDavResourceLocator principalLocator = calDavLocatorFactory
						.createPrincipalLocator(calDavResourceLocator.getContext(), user);
				writer.write("<a href=\"");
				writer.write(principalLocator.getHref(false));
				writer.write("\">");
				writer.write("Principal resource");
				writer.write("</a><br>\n");
			}

			writer.write("</body>");
			writer.write("</html>\n");
		} finally {
			writer.close();
		}
	}



	@Override
	public OptionsResponse getOptionResponse(OptionsInfo optionsInfo) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isCollection() {
		return true;
	}

	@Override
	protected void updateItem() throws CosmoDavException {
		throw new UnsupportedOperationException();
		
	}

	@Override
	protected Set<String> getDeadPropertyFilter() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean exists() {
		// TODO Auto-generated method stub
		return false;
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
