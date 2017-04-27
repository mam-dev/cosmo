package org.unitedinternet.cosmo.dav.impl.parallel;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.property.DavPropertyName;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.springframework.util.FileCopyUtils;
import org.unitedinternet.cosmo.dav.BadRequestException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.ForbiddenException;
import org.unitedinternet.cosmo.dav.parallel.CalDavResource;
import org.unitedinternet.cosmo.dav.property.ContentLanguage;
import org.unitedinternet.cosmo.dav.property.ContentLength;
import org.unitedinternet.cosmo.dav.property.ContentType;
import org.unitedinternet.cosmo.dav.property.WebDavProperty;
import org.unitedinternet.cosmo.model.DataSizeException;
import org.unitedinternet.cosmo.model.FileItem;
import org.unitedinternet.cosmo.util.ContentTypeUtil;

import net.fortuna.ical4j.model.Calendar;

public class CustomFile extends CalDavFileBase{
	 public void writeTo(OutputContext outputContext)
		        throws CosmoDavException, IOException {
		        if (! exists()) {
		            throw new IllegalStateException("cannot spool a nonexistent resource");
		        }

		        FileItem content = (FileItem) getItem();

		        String contentType =
		            ContentTypeUtil.buildContentType(content.getContentType(),
		                                    content.getContentEncoding());
		        outputContext.setContentType(contentType);

		        if (content.getContentLanguage() != null) {
		            outputContext.setContentLanguage(content.getContentLanguage());
		        }

		        long len = content.getContentLength() != null ?
		            content.getContentLength().longValue() : 0;
		        outputContext.setContentLength(len);
		        outputContext.setModificationTime(getModificationTime());
		        outputContext.setETag(getETag());

		        if (! outputContext.hasStream()) {
		            return;
		        }
		        if (content.getContentInputStream() == null) {
		            return;
		        }

		        FileCopyUtils.copy(content.getContentInputStream(),
		                     outputContext.getOutputStream());
		    }

		    
		    /** */
		    protected void populateItem(InputContext inputContext)
		        throws CosmoDavException {
		        super.populateItem(inputContext);

		        FileItem file = (FileItem) getItem();

		        try {
		            InputStream content = inputContext.getInputStream();
		            if (content != null) {
		                file.setContent(content);
		            }

		            if (inputContext.getContentLanguage() != null) {
		                file.setContentLanguage(inputContext.getContentLanguage());
		            }

		            String contentType = inputContext.getContentType();
		            if (contentType != null) {
		                file.setContentType(ContentTypeUtil.getMimeType(contentType));
		            }
		            else {
		                file.setContentType(ContentTypeUtil.getMimeType(file.getName()));
		            }
		            String contentEncoding = ContentTypeUtil.getEncoding(contentType);
		            if (contentEncoding != null) {
		                file.setContentEncoding(contentEncoding);
		            }
		        } catch (IOException e) {
		            throw new CosmoDavException(e);
		        } catch (DataSizeException e) {
		            throw new ForbiddenException(e.getMessage());
		        }
		    }

		    /** */
		    protected void loadLiveProperties(DavPropertySet properties) {
		        super.loadLiveProperties(properties);

		        FileItem content = (FileItem) getItem();
		        if (content == null) {
		            return;
		        }

		        if (content.getContentLanguage() != null) {
		            properties.add(new ContentLanguage(content.getContentLanguage()));
		        }
		        properties.add(new ContentLength(content.getContentLength()));
		        properties.add(new ContentType(content.getContentType(),
		                                       content.getContentEncoding()));
		    }

		    /** */
		    protected void setLiveProperty(WebDavProperty property, boolean create)
		        throws CosmoDavException {
		        super.setLiveProperty(property, create);

		        FileItem content = (FileItem) getItem();
		        if (content == null) {
		            return;
		        }

		        DavPropertyName name = property.getName();
		        String text = property.getValueText();

		        if (name.equals(DavPropertyName.GETCONTENTLANGUAGE)) {
		            content.setContentLanguage(text);
		            return;
		        }

		        if (name.equals(DavPropertyName.GETCONTENTTYPE)) {
		            String type = ContentTypeUtil.getMimeType(text);
		            if (StringUtils.isBlank(type)) {
		                throw new BadRequestException("Property " + name + " requires a valid media type");
		            }
		            content.setContentType(type);
		            content.setContentEncoding(ContentTypeUtil.getEncoding(text));
		        }
		    }

		    /** */
		    protected void removeLiveProperty(DavPropertyName name)
		        throws CosmoDavException {
		        super.removeLiveProperty(name);

		        FileItem content = (FileItem) getItem();
		        if (content == null) {
		            return;
		        }

		        if (name.equals(DavPropertyName.GETCONTENTLANGUAGE)) {
		            content.setContentLanguage(null);
		            return;
		        }
		    }

		    @Override
		    public boolean isCollection() {
		        return false;
		    }


			@Override
			public CalDavResource getParent() {
				// TODO Auto-generated method stub
				return null;
			}


			@Override
			public Calendar getCalendar() {
				// TODO Auto-generated method stub
				return null;
			}


			@Override
			protected void setCalendar(Calendar calendar) throws CosmoDavException {
				// TODO Auto-generated method stub
				
			}
}
