package org.unitedinternet.cosmo.dav.impl.parallel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;

import org.apache.abdera.util.EntityTag;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.apache.jackrabbit.webdav.DavSession;
import org.apache.jackrabbit.webdav.WebdavRequest;
import org.apache.jackrabbit.webdav.bind.BindInfo;
import org.apache.jackrabbit.webdav.bind.RebindInfo;
import org.apache.jackrabbit.webdav.bind.UnbindInfo;
import org.apache.jackrabbit.webdav.lock.LockInfo;
import org.apache.jackrabbit.webdav.observation.SubscriptionInfo;
import org.apache.jackrabbit.webdav.ordering.OrderPatch;
import org.apache.jackrabbit.webdav.ordering.Position;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.property.PropEntry;
import org.apache.jackrabbit.webdav.transaction.TransactionInfo;
import org.apache.jackrabbit.webdav.version.LabelInfo;
import org.apache.jackrabbit.webdav.version.MergeInfo;
import org.apache.jackrabbit.webdav.version.OptionsInfo;
import org.apache.jackrabbit.webdav.version.UpdateInfo;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.ElementIterator;
import org.unitedinternet.cosmo.dav.BadRequestException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.ExtendedDavConstants;
import org.unitedinternet.cosmo.dav.UnsupportedMediaTypeException;
import org.unitedinternet.cosmo.dav.acl.AclConstants;
import org.unitedinternet.cosmo.dav.acl.DavPrivilege;
import org.unitedinternet.cosmo.dav.acl.DavPrivilegeSet;
import org.unitedinternet.cosmo.dav.caldav.CaldavConstants;
import org.unitedinternet.cosmo.dav.parallel.CalDavRequest;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceLocator;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceLocatorFactory;
import org.unitedinternet.cosmo.dav.property.StandardDavProperty;
import org.unitedinternet.cosmo.dav.ticket.TicketConstants;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.Ticket;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DefaultCalDavRequest
		implements CalDavRequest, ExtendedDavConstants, AclConstants, CaldavConstants, TicketConstants {

	private static final Log LOG = LogFactory.getLog(DefaultCalDavRequest.class);

	private static final MimeType APPLICATION_XML = registerMimeType("application/xml");
	private static final MimeType TEXT_XML = registerMimeType("text/xml");

	private static final MimeType registerMimeType(String s) {
		try {
			return new MimeType(s);
		} catch (javax.activation.MimeTypeParseException e) {
			throw new RuntimeException("Can't register MIME type " + s, e);
		}
	}

	private WebdavRequest requestTarget;

	private DavPropertySet proppatchSet;
	private DavPropertyNameSet proppatchRemove;
	private DavPropertySet mkcalendarSet;
	private Ticket ticket;
	private CalDavResourceLocatorFactory locatorFactory;
	private CalDavResourceLocator locator;
	private CalDavResourceLocator destinationLocator;
	private EntityFactory entityFactory;

	public RebindInfo getRebindInfo() throws DavException {
		return requestTarget.getRebindInfo();
	}

	public String getSubscriptionId() {
		return requestTarget.getSubscriptionId();
	}

	public String getLabel() {
		return requestTarget.getLabel();
	}

	public String getOrderingType() {
		return requestTarget.getOrderingType();
	}

	public TransactionInfo getTransactionInfo() throws DavException {
		return requestTarget.getTransactionInfo();
	}

	public LabelInfo getLabelInfo() throws DavException {
		return requestTarget.getLabelInfo();
	}

	public void setDavSession(DavSession session) {
		requestTarget.setDavSession(session);
	}

	public UnbindInfo getUnbindInfo() throws DavException {
		return requestTarget.getUnbindInfo();
	}

	public Position getPosition() {
		return requestTarget.getPosition();
	}

	public long getPollTimeout() {
		return requestTarget.getPollTimeout();
	}

	public DavSession getDavSession() {
		return requestTarget.getDavSession();
	}

	public BindInfo getBindInfo() throws DavException {
		return requestTarget.getBindInfo();
	}

	public DavResourceLocator getRequestLocator() {
		return requestTarget.getRequestLocator();
	}

	public SubscriptionInfo getSubscriptionInfo() throws DavException {
		return requestTarget.getSubscriptionInfo();
	}

	public MergeInfo getMergeInfo() throws DavException {
		return requestTarget.getMergeInfo();
	}

	public String getTransactionId() {
		return requestTarget.getTransactionId();
	}

	public OrderPatch getOrderPatch() throws DavException {
		return requestTarget.getOrderPatch();
	}

	public DavResourceLocator getDestinationLocator() throws DavException {
		return requestTarget.getDestinationLocator();
	}

	public DavResourceLocator getHrefLocator(String href) throws DavException {
		return requestTarget.getHrefLocator(href);
	}

	public UpdateInfo getUpdateInfo() throws DavException {
		return requestTarget.getUpdateInfo();
	}

	public DavResourceLocator getMemberLocator(String segment) {
		return requestTarget.getMemberLocator(segment);
	}

	public boolean isOverwrite() {
		return requestTarget.isOverwrite();
	}

	public ReportInfo getReportInfo() throws DavException {
		return requestTarget.getReportInfo();
	}

	public int getDepth() {
		return requestTarget.getDepth();
	}

	public OptionsInfo getOptionsInfo() throws DavException {
		return requestTarget.getOptionsInfo();
	}

	public int getDepth(int defaultValue) {
		return requestTarget.getDepth(defaultValue);
	}

	public Object getAttribute(String name) {
		return requestTarget.getAttribute(name);
	}

	public String getLockToken() {
		return requestTarget.getLockToken();
	}

	public String getAuthType() {
		return requestTarget.getAuthType();
	}

	public long getTimeout() {
		return requestTarget.getTimeout();
	}

	public Document getRequestDocument() throws DavException {
		return requestTarget.getRequestDocument();
	}

	public Cookie[] getCookies() {
		return requestTarget.getCookies();
	}

	public Enumeration<String> getAttributeNames() {
		return requestTarget.getAttributeNames();
	}

	public int getPropFindType() throws DavException {
		return requestTarget.getPropFindType();
	}

	public long getDateHeader(String name) {
		return requestTarget.getDateHeader(name);
	}

	public String getCharacterEncoding() {
		return requestTarget.getCharacterEncoding();
	}

	public DavPropertyNameSet getPropFindProperties() throws DavException {
		return requestTarget.getPropFindProperties();
	}

	public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
		requestTarget.setCharacterEncoding(env);
	}

	public List<? extends PropEntry> getPropPatchChangeList() throws DavException {
		return requestTarget.getPropPatchChangeList();
	}

	public int getContentLength() {
		return requestTarget.getContentLength();
	}

	public String getHeader(String name) {
		return requestTarget.getHeader(name);
	}

	public LockInfo getLockInfo() throws DavException {
		return requestTarget.getLockInfo();
	}

	public long getContentLengthLong() {
		return requestTarget.getContentLengthLong();
	}

	public Enumeration<String> getHeaders(String name) {
		return requestTarget.getHeaders(name);
	}

	public boolean matchesIfHeader(DavResource resource) {
		return requestTarget.matchesIfHeader(resource);
	}

	public String getContentType() {
		return requestTarget.getContentType();
	}

	public boolean matchesIfHeader(String href, String token, String eTag) {
		return requestTarget.matchesIfHeader(href, token, eTag);
	}

	public ServletInputStream getInputStream() throws IOException {
		return requestTarget.getInputStream();
	}

	public String getParameter(String name) {
		return requestTarget.getParameter(name);
	}

	public Enumeration<String> getHeaderNames() {
		return requestTarget.getHeaderNames();
	}

	public int getIntHeader(String name) {
		return requestTarget.getIntHeader(name);
	}

	public Enumeration<String> getParameterNames() {
		return requestTarget.getParameterNames();
	}

	public String getMethod() {
		return requestTarget.getMethod();
	}

	public String[] getParameterValues(String name) {
		return requestTarget.getParameterValues(name);
	}

	public String getPathInfo() {
		return requestTarget.getPathInfo();
	}

	public Map<String, String[]> getParameterMap() {
		return requestTarget.getParameterMap();
	}

	public String getPathTranslated() {
		return requestTarget.getPathTranslated();
	}

	public String getProtocol() {
		return requestTarget.getProtocol();
	}

	public String getScheme() {
		return requestTarget.getScheme();
	}

	public String getContextPath() {
		return requestTarget.getContextPath();
	}

	public String getServerName() {
		return requestTarget.getServerName();
	}

	public int getServerPort() {
		return requestTarget.getServerPort();
	}

	public BufferedReader getReader() throws IOException {
		return requestTarget.getReader();
	}

	public String getQueryString() {
		return requestTarget.getQueryString();
	}

	public String getRemoteUser() {
		return requestTarget.getRemoteUser();
	}

	public String getRemoteAddr() {
		return requestTarget.getRemoteAddr();
	}

	public String getRemoteHost() {
		return requestTarget.getRemoteHost();
	}

	public boolean isUserInRole(String role) {
		return requestTarget.isUserInRole(role);
	}

	public void setAttribute(String name, Object o) {
		requestTarget.setAttribute(name, o);
	}

	public Principal getUserPrincipal() {
		return requestTarget.getUserPrincipal();
	}

	public void removeAttribute(String name) {
		requestTarget.removeAttribute(name);
	}

	public String getRequestedSessionId() {
		return requestTarget.getRequestedSessionId();
	}

	public Locale getLocale() {
		return requestTarget.getLocale();
	}

	public String getRequestURI() {
		return requestTarget.getRequestURI();
	}

	public Enumeration<Locale> getLocales() {
		return requestTarget.getLocales();
	}

	public boolean isSecure() {
		return requestTarget.isSecure();
	}

	public StringBuffer getRequestURL() {
		return requestTarget.getRequestURL();
	}

	public RequestDispatcher getRequestDispatcher(String path) {
		return requestTarget.getRequestDispatcher(path);
	}

	public String getServletPath() {
		return requestTarget.getServletPath();
	}

	@SuppressWarnings("deprecation")
	public String getRealPath(String path) {
		return requestTarget.getRealPath(path);
	}

	public HttpSession getSession(boolean create) {
		return requestTarget.getSession(create);
	}

	public int getRemotePort() {
		return requestTarget.getRemotePort();
	}

	public String getLocalName() {
		return requestTarget.getLocalName();
	}

	public String getLocalAddr() {
		return requestTarget.getLocalAddr();
	}

	public int getLocalPort() {
		return requestTarget.getLocalPort();
	}

	public ServletContext getServletContext() {
		return requestTarget.getServletContext();
	}

	public HttpSession getSession() {
		return requestTarget.getSession();
	}

	public AsyncContext startAsync() throws IllegalStateException {
		return requestTarget.startAsync();
	}

	public String changeSessionId() {
		return requestTarget.changeSessionId();
	}

	public boolean isRequestedSessionIdValid() {
		return requestTarget.isRequestedSessionIdValid();
	}

	public boolean isRequestedSessionIdFromCookie() {
		return requestTarget.isRequestedSessionIdFromCookie();
	}

	public boolean isRequestedSessionIdFromURL() {
		return requestTarget.isRequestedSessionIdFromURL();
	}

	@SuppressWarnings("deprecation")
	public boolean isRequestedSessionIdFromUrl() {
		return requestTarget.isRequestedSessionIdFromUrl();
	}

	public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
		return requestTarget.authenticate(response);
	}

	public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
			throws IllegalStateException {
		return requestTarget.startAsync(servletRequest, servletResponse);
	}

	public void login(String username, String password) throws ServletException {
		requestTarget.login(username, password);
	}

	public void logout() throws ServletException {
		requestTarget.logout();
	}

	public Collection<Part> getParts() throws IOException, ServletException {
		return requestTarget.getParts();
	}

	public boolean isAsyncStarted() {
		return requestTarget.isAsyncStarted();
	}

	public boolean isAsyncSupported() {
		return requestTarget.isAsyncSupported();
	}

	public Part getPart(String name) throws IOException, ServletException {
		return requestTarget.getPart(name);
	}

	public AsyncContext getAsyncContext() {
		return requestTarget.getAsyncContext();
	}

	public DispatcherType getDispatcherType() {
		return requestTarget.getDispatcherType();
	}

	public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
		return requestTarget.upgrade(handlerClass);
	}

	@Override
	public EntityTag[] getIfMatch() {
		return EntityTag.parseTags(getHeader("If-Match"));
	}

	@Override
	public Date getIfModifiedSince() {
		long value = getDateHeader("If-Modified-Since");
        return value != -1 ? new Date(value) : null;
	}

	@Override
	public EntityTag[] getIfNoneMatch() {
		return EntityTag.parseTags(getHeader("If-None-Match"));
	}

	@Override
	public Date getIfUnmodifiedSince() {
		long value = getDateHeader("If-Unmodified-Since");
		return value != -1 ? new Date(value) : null;
	}

	@Override
	public DavPropertySet getProppatchSetProperties() throws CosmoDavException {
		if (proppatchSet == null) {
			parsePropPatchRequest();
		}
		return proppatchSet;
	}

	@Override
	public DavPropertyNameSet getProppatchRemoveProperties() throws CosmoDavException {
		if (proppatchRemove == null) {
			parsePropPatchRequest();
		}
		return proppatchRemove;
	}

	private void parsePropPatchRequest() throws CosmoDavException {
		Document requestDocument = getSafeRequestDocument();
		if (requestDocument == null) {
			throw new BadRequestException("PROPPATCH requires entity body");
		}

		Element root = requestDocument.getDocumentElement();
		if (!DomUtil.matches(root, XML_PROPERTYUPDATE, NAMESPACE)) {
			throw new BadRequestException("Expected " + QN_PROPERTYUPDATE + " root element");
		}

		ElementIterator sets = DomUtil.getChildren(root, XML_SET, NAMESPACE);
		ElementIterator removes = DomUtil.getChildren(root, XML_REMOVE, NAMESPACE);
		if (!(sets.hasNext() || removes.hasNext())) {
			throw new BadRequestException(
					"Expected at least one of " + QN_REMOVE + " and " + QN_SET + " as a child of " + QN_PROPERTYUPDATE);
		}

		Element prop = null;
		ElementIterator i = null;

		proppatchSet = new DavPropertySet();
		while (sets.hasNext()) {
			Element set = sets.nextElement();
			prop = DomUtil.getChildElement(set, XML_PROP, NAMESPACE);
			if (prop == null) {
				throw new BadRequestException("Expected " + QN_PROP + " child of " + QN_SET);
			}
			i = DomUtil.getChildren(prop);
			while (i.hasNext()) {
				StandardDavProperty p = StandardDavProperty.createFromXml(i.nextElement());
				proppatchSet.add(p);
			}
		}

		proppatchRemove = new DavPropertyNameSet();
		while (removes.hasNext()) {
			Element remove = removes.nextElement();
			prop = DomUtil.getChildElement(remove, XML_PROP, NAMESPACE);
			if (prop == null) {
				throw new BadRequestException("Expected " + QN_PROP + " child of " + QN_REMOVE);
			}
			i = DomUtil.getChildren(prop);
			while (i.hasNext()) {
				proppatchRemove.add(DavPropertyName.createFromXml(i.nextElement()));
			}
		}
	}

	@Override
	public CalDavResourceLocator getResourceLocator() {
		if (locator == null) {
			URL context = null;
			try {
				String basePath = getContextPath() + getServletPath();
				context = new URL(getScheme(), getServerName(), getServerPort(), basePath);

				locator = (CalDavResourceLocator) locatorFactory.createResourceLocatorByUri(context, getRequestURI());
			} catch (CosmoDavException e) {
				throw new RuntimeException(e);
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}

		return locator;
	}

	@Override
	public CalDavResourceLocator getDestinationResourceLocator() throws CosmoDavException {
		if (destinationLocator != null) {
			return destinationLocator;
		}

		String destination = getHeader(HEADER_DESTINATION);
		if (destination == null) {
			return null;
		}

		URL context = ((CalDavResourceLocator) getResourceLocator()).getContext();

		destinationLocator = (CalDavResourceLocator) locatorFactory.createResourceLocatorByUri(context, destination);

		return destinationLocator;
	}

	@Override
	public DavPropertySet getMkCalendarSetProperties() throws CosmoDavException {
		if (mkcalendarSet == null) {
			mkcalendarSet = parseMkCalendarRequest();
		}
		return mkcalendarSet;
	}

	private DavPropertySet parseMkCalendarRequest() throws CosmoDavException {
		DavPropertySet propertySet = new DavPropertySet();

		Document requestDocument = getSafeRequestDocument(false);
		if (requestDocument == null) {
			return propertySet;
		}

		Element root = requestDocument.getDocumentElement();
		if (!DomUtil.matches(root, ELEMENT_CALDAV_MKCALENDAR, NAMESPACE_CALDAV)) {
			throw new BadRequestException("Expected " + QN_MKCALENDAR + " root element");
		}
		Element set = DomUtil.getChildElement(root, XML_SET, NAMESPACE);
		if (set == null) {
			throw new BadRequestException("Expected " + QN_SET + " child of " + QN_MKCALENDAR);
		}
		Element prop = DomUtil.getChildElement(set, XML_PROP, NAMESPACE);
		if (prop == null) {
			throw new BadRequestException("Expected " + QN_PROP + " child of " + QN_SET);
		}
		ElementIterator i = DomUtil.getChildren(prop);
		while (i.hasNext()) {
			propertySet.add(StandardDavProperty.createFromXml(i.nextElement()));
		}

		return propertySet;
	}

	@Override
	public Ticket getTicketInfo() throws CosmoDavException {
		if (ticket == null) {
			ticket = parseTicketRequest();
		}
		return ticket;
	}

	private Ticket parseTicketRequest() throws CosmoDavException {
		Document requestDocument = getSafeRequestDocument();
		if (requestDocument == null) {
			throw new BadRequestException("MKTICKET requires entity body");
		}

		Element root = requestDocument.getDocumentElement();
		if (!DomUtil.matches(root, ELEMENT_TICKET_TICKETINFO, NAMESPACE_TICKET)) {
			throw new BadRequestException("Expected " + QN_TICKET_TICKETINFO + " root element");
		}
		if (DomUtil.hasChildElement(root, ELEMENT_TICKET_ID, NAMESPACE_TICKET)) {
			throw new BadRequestException(QN_TICKET_TICKETINFO + " may not contain child " + QN_TICKET_ID);
		}
		if (DomUtil.hasChildElement(root, XML_OWNER, NAMESPACE)) {
			throw new BadRequestException(QN_TICKET_TICKETINFO + " may not contain child " + QN_OWNER);
		}

		String timeout = DomUtil.getChildTextTrim(root, ELEMENT_TICKET_TIMEOUT, NAMESPACE_TICKET);
		if (timeout != null && !timeout.equals(TIMEOUT_INFINITE)) {
			try {
				int seconds = Integer.parseInt(timeout.substring(7));
				LOG.trace("Timeout seconds: " + seconds);
			} catch (NumberFormatException e) {
				throw new BadRequestException("Malformed " + QN_TICKET_TIMEOUT + " value " + timeout);
			}
		} else {
			timeout = TIMEOUT_INFINITE;
		}

		// visit limits are not supported

		Element pe = DomUtil.getChildElement(root, XML_PRIVILEGE, NAMESPACE);
		if (pe == null) {
			throw new BadRequestException("Expected " + QN_PRIVILEGE + " child of " + QN_TICKET_TICKETINFO);
		}

		DavPrivilegeSet privileges = DavPrivilegeSet.createFromXml(pe);
		if (!privileges.containsAny(DavPrivilege.READ, DavPrivilege.WRITE, DavPrivilege.READ_FREE_BUSY)) {
			throw new BadRequestException("Empty or invalid " + QN_PRIVILEGE);
		}

		Ticket ticket = entityFactory.creatTicket();
		ticket.setTimeout(timeout);
		privileges.setTicketPrivileges(ticket);

		return ticket;
	}

	private Document getSafeRequestDocument() throws CosmoDavException {
		return getSafeRequestDocument(true);
	}

	/**
	 * 
	 * @param requireDocument
	 *            boolean
	 * @return Document
	 * @throws CosmoDavException
	 */
	private Document getSafeRequestDocument(boolean requireDocument) throws CosmoDavException {
		try {
			if (StringUtils.isBlank(getContentType()) && requireDocument) {
				throw new BadRequestException("No Content-Type specified");
			}
			MimeType mimeType = new MimeType(getContentType());
			if (!(mimeType.match(APPLICATION_XML) || mimeType.match(TEXT_XML))) {
				throw new UnsupportedMediaTypeException("Expected Content-Type " + APPLICATION_XML + " or " + TEXT_XML);
			}

			return getRequestDocument();

		} catch (MimeTypeParseException e) {
			throw new UnsupportedMediaTypeException(e.getMessage());
		} catch (IllegalArgumentException e) {
			throwBadRequestExceptionFrom(e);
		} catch (DavException e) {
			throwBadRequestExceptionFrom(e);
		}

		return null;
	}

	private void throwBadRequestExceptionFrom(Exception e) throws BadRequestException {
		Throwable cause = e.getCause();
		String msg = cause != null ? cause.getMessage() : "Unknown error parsing request document";
		throw new BadRequestException(msg);
	}

	@Override
	public String getTicketKey() throws CosmoDavException {
		String key = getParameter(PARAM_TICKET);
		if (key == null) {
			key = getHeader(HEADER_TICKET);
		}
		if (key != null) {
			return key;
		}
		throw new BadRequestException("Request does not contain a ticket key");
	}
}