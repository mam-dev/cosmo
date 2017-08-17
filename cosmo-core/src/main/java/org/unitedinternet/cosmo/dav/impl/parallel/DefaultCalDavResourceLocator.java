package org.unitedinternet.cosmo.dav.impl.parallel;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.unitedinternet.cosmo.CosmoException;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceLocator;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceLocatorFactory;
import org.unitedinternet.cosmo.util.PathUtil;

public class DefaultCalDavResourceLocator implements CalDavResourceLocator{
	
	private URL context;
	private String path;
	private CalDavResourceLocatorFactory factory;
	
	public DefaultCalDavResourceLocator(URL context, String path, CalDavResourceLocatorFactory factory){
		this.context = context;
		this.path = path;
		this.factory = factory;
	}
	
	@Override
	public String getPrefix() {
		return context.toString();
	}

	@Override
	public String getResourcePath() {
		return path;
	}

	@Override
	public String getWorkspacePath() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getWorkspaceName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSameWorkspace(DavResourceLocator locator) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSameWorkspace(String workspaceName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getHref(boolean isCollection) {
		 return getHref(false, isCollection);
	}

	@Override
	public boolean isRootLocation() {
		return false;
	}

	@Override
	public CalDavResourceLocatorFactory getFactory() {
		return factory;
	}

	@Override
	public String getRepositoryPath() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getHref(boolean absolute, boolean isCollection) {
		try {
	        return buildHref(context, absolute, isCollection);
	    } catch (Exception e) {
	        throw new CosmoException(e);
	    }
	}

	@Override
	public URL getUrl(boolean absolute, boolean isCollection) {
		try {
			return new URL(getHref(isCollection, isCollection));
		} catch (MalformedURLException e) {
	        throw new CosmoException(e);
	    }
	}

	@Override
	public String getBaseHref() {
		 return getBaseHref(false);
	}

	@Override
	public String getBaseHref(boolean absolute) {
		 try {
	            if (absolute) {
	                return context.toURI().toASCIIString();
	            }
	            return new URI(null, null, context.getPath(), null).toASCIIString();
	        } catch (Exception e) {
	            throw new CosmoException(e);
	        }
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public URL getContext() {
		return context;
	}

	@Override
	public CalDavResourceLocator getParentLocator() {
		return factory.createResourceLocatorByPath(context,
                PathUtil.getParentPath(path));
	}
	
	private String buildHref(URL context, boolean isCollection, boolean absolute) throws URISyntaxException {
        String protocol = context.getProtocol();
        String host = context.getHost();
        String path = isCollection && ! this.path.equals("/") ? this.path  + "/" : this.path;
        path = context.getPath() + path;
        int port = context.getPort();
        StringBuilder sb = new StringBuilder();
        
        if (protocol != null && absolute) {
            sb.append(protocol);
            sb.append(':');
        }
        
        if (host != null && absolute) {
            sb.append("//");
            
            boolean needBrackets = ((host.indexOf(':') >= 0)
                                && !host.startsWith("[")
                                && !host.endsWith("]"));
            if (needBrackets) {
                sb.append('[');
            }
            
            sb.append(host);
            
            if (needBrackets){ 
                sb.append(']');
            }
            
            if (port != -1) {
                sb.append(':');
                sb.append(port);
            }
            
        }
        
        if (path != null){
            sb.append(path);
        }
        
        if(isCollection && sb.charAt(sb.length()-1) != '/'){
            sb.append("/");
        }
        return sb.toString();
    }
}