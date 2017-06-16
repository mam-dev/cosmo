package org.unitedinternet.cosmo.dav.impl.parallel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jackrabbit.webdav.DavConstants;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.WebdavResponse;
import org.apache.jackrabbit.webdav.WebdavResponseImpl;
import org.apache.jackrabbit.webdav.lock.ActiveLock;
import org.apache.jackrabbit.webdav.observation.EventDiscovery;
import org.apache.jackrabbit.webdav.observation.Subscription;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.apache.jackrabbit.webdav.xml.XmlSerializable;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.impl.DavItemResource;
import org.unitedinternet.cosmo.dav.parallel.CalDavResponse;
import org.unitedinternet.cosmo.dav.ticket.TicketConstants;
import org.unitedinternet.cosmo.dav.ticket.property.TicketDiscovery;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DefaultCalDavResponse implements CalDavResponse, DavConstants, TicketConstants {
	private static final XMLOutputFactory XML_OUTPUT_FACTORY = XMLOutputFactory.newInstance();

	private static final Log LOG = LogFactory.getLog(DefaultCalDavResponse.class);
	 
	private WebdavResponse webDavResponseTarget;

	public DefaultCalDavResponse(HttpServletResponse servletResponse){
		this.webDavResponseTarget = new WebdavResponseImpl(servletResponse);
	}
	
	public void sendSubscriptionResponse(Subscription subscription) throws IOException {
		webDavResponseTarget.sendSubscriptionResponse(subscription);
	}

	public void sendPollResponse(EventDiscovery eventDiscovery) throws IOException {
		webDavResponseTarget.sendPollResponse(eventDiscovery);
	}

	public void sendError(DavException error) throws IOException {
		webDavResponseTarget.sendError(error);
	}

	public void sendMultiStatus(MultiStatus multistatus) throws IOException {
		webDavResponseTarget.sendMultiStatus(multistatus);
	}

	public void addCookie(Cookie cookie) {
		webDavResponseTarget.addCookie(cookie);
	}

	public boolean containsHeader(String name) {
		return webDavResponseTarget.containsHeader(name);
	}

	public void sendRefreshLockResponse(ActiveLock[] locks) throws IOException {
		webDavResponseTarget.sendRefreshLockResponse(locks);
	}

	public String encodeURL(String url) {
		return webDavResponseTarget.encodeURL(url);
	}

	public void sendXmlResponse(XmlSerializable serializable, int status) throws IOException {
		webDavResponseTarget.sendXmlResponse(serializable, status);
	}

	public String getCharacterEncoding() {
		return webDavResponseTarget.getCharacterEncoding();
	}

	public String encodeRedirectURL(String url) {
		return webDavResponseTarget.encodeRedirectURL(url);
	}

	public String getContentType() {
		return webDavResponseTarget.getContentType();
	}

	@SuppressWarnings("deprecation")
	public String encodeUrl(String url) {
		return webDavResponseTarget.encodeUrl(url);
	}

	public ServletOutputStream getOutputStream() throws IOException {
		return webDavResponseTarget.getOutputStream();
	}

	@SuppressWarnings("deprecation")
	public String encodeRedirectUrl(String url) {
		return webDavResponseTarget.encodeRedirectUrl(url);
	}

	public void sendError(int sc, String msg) throws IOException {
		webDavResponseTarget.sendError(sc, msg);
	}

	public PrintWriter getWriter() throws IOException {
		return webDavResponseTarget.getWriter();
	}

	public void sendError(int sc) throws IOException {
		webDavResponseTarget.sendError(sc);
	}

	public void setCharacterEncoding(String charset) {
		webDavResponseTarget.setCharacterEncoding(charset);
	}

	public void sendRedirect(String location) throws IOException {
		webDavResponseTarget.sendRedirect(location);
	}

	public void setContentLength(int len) {
		webDavResponseTarget.setContentLength(len);
	}

	public void setContentLengthLong(long len) {
		webDavResponseTarget.setContentLengthLong(len);
	}

	public void setDateHeader(String name, long date) {
		webDavResponseTarget.setDateHeader(name, date);
	}

	public void setContentType(String type) {
		webDavResponseTarget.setContentType(type);
	}

	public void addDateHeader(String name, long date) {
		webDavResponseTarget.addDateHeader(name, date);
	}

	public void setHeader(String name, String value) {
		webDavResponseTarget.setHeader(name, value);
	}

	public void setBufferSize(int size) {
		webDavResponseTarget.setBufferSize(size);
	}

	public void addHeader(String name, String value) {
		webDavResponseTarget.addHeader(name, value);
	}

	public void setIntHeader(String name, int value) {
		webDavResponseTarget.setIntHeader(name, value);
	}

	public int getBufferSize() {
		return webDavResponseTarget.getBufferSize();
	}

	public void addIntHeader(String name, int value) {
		webDavResponseTarget.addIntHeader(name, value);
	}

	public void flushBuffer() throws IOException {
		webDavResponseTarget.flushBuffer();
	}

	public void setStatus(int sc) {
		webDavResponseTarget.setStatus(sc);
	}

	public void resetBuffer() {
		webDavResponseTarget.resetBuffer();
	}

	public boolean isCommitted() {
		return webDavResponseTarget.isCommitted();
	}

	@SuppressWarnings("deprecation")
	public void setStatus(int sc, String sm) {
		webDavResponseTarget.setStatus(sc, sm);
	}

	public void reset() {
		webDavResponseTarget.reset();
	}

	public int getStatus() {
		return webDavResponseTarget.getStatus();
	}

	public String getHeader(String name) {
		return webDavResponseTarget.getHeader(name);
	}

	public void setLocale(Locale loc) {
		webDavResponseTarget.setLocale(loc);
	}

	public Collection<String> getHeaders(String name) {
		return webDavResponseTarget.getHeaders(name);
	}

	public Collection<String> getHeaderNames() {
		return webDavResponseTarget.getHeaderNames();
	}

	public Locale getLocale() {
		return webDavResponseTarget.getLocale();
	}

	@Override
	public void sendDavError(CosmoDavException e) throws IOException {
		setStatus(e.getErrorCode());
		if (!e.hasContent()) {
			return;
		}

		XMLStreamWriter writer = null;

		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			writer = XML_OUTPUT_FACTORY.createXMLStreamWriter(out);
			writer.writeStartDocument();
			e.writeTo(writer);
			writer.writeEndDocument();

			setContentType("text/xml; charset=UTF-8");
			byte[] bytes = out.toByteArray();
			setContentLength(bytes.length);
			getOutputStream().write(bytes);
		} catch (Exception e2) {
			LOG.error("Error writing XML", e2);
			LOG.error("Original exception", e);
			setStatus(500);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (XMLStreamException e2) {
					LOG.warn("Unable to close XML writer", e2);
				}
			}
		}

	}

	@Override
	public void sendMkTicketResponse(DavItemResource resource, String ticketId) throws CosmoDavException, IOException {
		setHeader(HEADER_TICKET, ticketId);

		TicketDiscovery ticketdiscovery = (TicketDiscovery) resource.getProperties().get(TICKETDISCOVERY);
		MkTicketInfo info = new MkTicketInfo(ticketdiscovery);

		sendXmlResponse(info, SC_OK);

	}

	private static class MkTicketInfo implements XmlSerializable {
		private TicketDiscovery td;

		public MkTicketInfo(TicketDiscovery td) {
			this.td = td;
		}

		public Element toXml(Document document) {
			Element prop = DomUtil.createElement(document, XML_PROP, NAMESPACE);
			prop.appendChild(td.toXml(document));
			return prop;
		}
	}

}