package org.unitedinternet.cosmo.ext;

import java.net.URL;

import org.apache.http.HttpHost;

/**
 * Factory class for for obtaining a proxy to be used when accessing a certain external content URL.
 * 
 * @author daniel grigore
 *
 */
public interface ProxyFactory {
    HttpHost getProxy(URL url);
}
