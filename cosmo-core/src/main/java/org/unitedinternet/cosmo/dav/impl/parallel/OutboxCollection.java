package org.unitedinternet.cosmo.dav.impl.parallel;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.property.DavPropertyIterator;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.parallel.CalDavResource;
import org.unitedinternet.cosmo.dav.property.WebDavProperty;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.util.ContentTypeUtil;
import org.unitedinternet.cosmo.util.DomWriter;
import org.w3c.dom.Element;

public class OutboxCollection extends CalDavCollectionBase{

	@Override
	public CalDavResource getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSupportedMethods() {
		// TODO Auto-generated method stub
		return null;
	}
	
	 private void writeHtmlDirectoryIndex(OutputContext context)
		        throws CosmoDavException, IOException {
		        if (LOG.isDebugEnabled()) {
		            LOG.debug("writing html directory index for  " +
		                      getDisplayName());
		        }
		        context.setContentType(ContentTypeUtil.buildContentType("text/html", "UTF-8"));
		        // no modification time or etag

		        if (! context.hasStream()) {
		            return;
		        }

		        PrintWriter writer =
		            new PrintWriter(new OutputStreamWriter(context.getOutputStream(),
		                                                   "utf8"));
		        try{
		            writer.write("<html>\n<head><title>");
		            writer.write(StringEscapeUtils.escapeHtml(getDisplayName()));
		            writer.write("</title></head>\n");
		            writer.write("<body>\n");
		            writer.write("<h1>");
		            writer.write(StringEscapeUtils.escapeHtml(getDisplayName()));
		            writer.write("</h1>\n");
		    
		            writer.write("<h2>Properties</h2>\n");
		            writer.write("<dl>\n");
		            for (DavPropertyIterator i=getProperties().iterator(); i.hasNext();) {
		                WebDavProperty prop = (WebDavProperty) i.nextProperty();
		                Object value = prop.getValue();
		                String text = null;
		                if (value instanceof Element) {
		                    try {
		                        text = DomWriter.write((Element)value);
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
		                DavResourceLocator homeLocator =
		                    getResourceLocator().getFactory().
		                    createHomeLocator(getResourceLocator().getContext(), user);
		                writer.write("<a href=\"");
		                writer.write(homeLocator.getHref(true));
		                writer.write("\">");
		                writer.write("Home collection");
		                writer.write("</a><br>\n");
		    
		                DavResourceLocator principalLocator = 
		                    getResourceLocator().getFactory().
		                    createPrincipalLocator(getResourceLocator().getContext(),
		                                           user);
		                writer.write("<a href=\"");
		                writer.write(principalLocator.getHref(false));
		                writer.write("\">");
		                writer.write("Principal resource");
		                writer.write("</a><br>\n");
		            }
		    
		            writer.write("</body>");
		            writer.write("</html>\n");
		        }finally{
		            writer.close();
		        }
		    }
}
