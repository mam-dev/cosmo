package org.unitedinternet.cosmo.dav.impl.parallel;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.jackrabbit.webdav.DavResourceLocator;
import org.unitedinternet.cosmo.dav.BadRequestException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.ExtendedDavConstants;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceLocator;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceLocatorFactory;
import org.unitedinternet.cosmo.model.User;

public class DefaultCalDavResourceLocatorFactory  implements CalDavResourceLocatorFactory, ExtendedDavConstants{

	@Override
	public DavResourceLocator createResourceLocator(String prefix, String href){
		try {
			return createResourceLocatorByUri(new URL(prefix), href);
		} catch (CosmoDavException | MalformedURLException e) {
			 throw new RuntimeException(e);
		}
	}

	@Override
	public DavResourceLocator createResourceLocator(String prefix, String workspacePath, String resourcePath) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DavResourceLocator createResourceLocator(String prefix, String workspacePath, String path, boolean isResourcePath) {
		throw new UnsupportedOperationException();
	}

	@Override
	public CalDavResourceLocator createResourceLocatorByPath(URL context, String path) {
		 return new DefaultCalDavResourceLocator(context, path, this);
	}

	@Override
	public CalDavResourceLocator createResourceLocatorByUri(URL context, String uriText) throws CosmoDavException {
		 try {
	            URI uri = new URI(uriText);

	            URL url = null;
	            if (uriText.startsWith("/")) {
	                // absolute-path relative URL
	                url = new URL(context, uri.getRawPath());
	            } else {
	                // absolute URL
	                url = new URL(uri.getScheme(), uri.getHost(), uri.getPort(), uri.getRawPath());

	                // make sure that absolute URLs use the same scheme and
	                // authority (host:port)
	                if (url.getProtocol() != null &&
	                    ! url.getProtocol().equals(context.getProtocol())) {
	                    throw new BadRequestException("target " + uri
	                            + " does not specify same scheme " + "as context "
	                            + context.toString());
	                }
	                
	                // look at host
	                if(url.getHost() !=null &&  ! url.getHost().equals(context.getHost())) {
	                    throw new BadRequestException("target " + uri
	                            + " does not specify same host " + "as context "
	                            + context.toString());
	                }
	                
	                // look at ports
	                // take default ports 80 and 443 into account so that
	                // https://server is the same as https://server:443
	                int port1 = translatePort(context.getProtocol(), context.getPort());
	                int port2 = translatePort(url.getProtocol(), url.getPort());
	                
	                if(port1!=port2) {
	                    throw new BadRequestException("target " + uri
	                            + " does not specify same port " + "as context "
	                            + context.toString());
	                }
	            }

	            if (! url.getPath().startsWith(context.getPath())) {
	                throw new BadRequestException(uri + " does not specify correct dav path " + 
	                        context.getPath());
	            }

	            // trim base path
	            String path = url.getPath().substring(context.getPath().length()) + "/";
	            path = path.replaceAll("/{2,}", "/");

	            return new DefaultCalDavResourceLocator(context, path, this);
	        } catch (URISyntaxException | MalformedURLException e) {
	            throw new BadRequestException("Invalid URL: " + e.getMessage());
	        }
	}
	
	private int translatePort(String protocol, int port) {
        if (port == -1 || port == 80 && "http".equals(protocol) 
                || port == 443 && "https".equals(protocol)) {
            return -1;
        }
        else {
            return port;
        }
    }

	@Override
	public CalDavResourceLocator createHomeLocator(URL context, User user) throws CosmoDavException {
		 String path = TEMPLATE_HOME.bind(user.getUsername());
	     return new DefaultCalDavResourceLocator(context, path, this);
	}

	@Override
	public CalDavResourceLocator createPrincipalLocator(URL context, User user) throws CosmoDavException {
		 String path = TEMPLATE_USER.bind(user.getUsername());
	     return new DefaultCalDavResourceLocator(context, path, this);
	}
}