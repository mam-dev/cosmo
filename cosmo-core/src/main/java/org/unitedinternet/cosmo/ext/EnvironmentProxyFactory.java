package org.unitedinternet.cosmo.ext;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;

/**
 * <code>Factory</code> that creates a <code>java.net.Proxy</code> by reading the environment properties.
 * 
 * @author daniel grigore
 *
 */
public class EnvironmentProxyFactory implements ApplicationContextAware, ProxyFactory {

    static final String PROP_PROXY_HOST = "external.proxy.host";
    static final String PROP_PROXY_PORT = "external.proxy.port";
    static final String PROP_PROXY_NON_PROXY_HOSTS = "external.proxy.nonProxyHosts";
    private static final String SEPARATOR_REGEX = "\\|";

    private static final Log LOG = LogFactory.getLog(EnvironmentProxyFactory.class);

    private String[] nonProxyHosts;
    private Proxy defaultProxy;

    public EnvironmentProxyFactory() {
        super();
    }

    @Override
    public Proxy get(String host) {
        if (this.isNonProxyHost(host)) {
            return Proxy.NO_PROXY;
        }
        return this.defaultProxy;
    }

    private boolean isNonProxyHost(String host) {
        for (String nonProxyHost : this.nonProxyHosts) {
            if (nonProxyHost.equals(host)) {
                return true;
            }
        }
        return false;
    }

    private void setDefaultProxy(Environment env) {
        String proxyHost = env.getProperty(PROP_PROXY_HOST);
        String proxyPort = env.getProperty(PROP_PROXY_PORT);
        if (proxyHost != null && !proxyHost.trim().isEmpty() && proxyPort != null && !proxyPort.trim().isEmpty()) {
            LOG.info("NET Setting proxy host: " + proxyHost + " and port: " + proxyPort
                    + " for getting external content.");
            this.defaultProxy = new Proxy(Type.HTTP, new InetSocketAddress(proxyHost, Integer.valueOf(proxyPort)));
        } else {
            this.defaultProxy = Proxy.NO_PROXY;
        }
    }

    private void setNonProxyHosts(Environment env) {
        if (env.getProperty(PROP_PROXY_NON_PROXY_HOSTS) == null) {
            this.nonProxyHosts = new String[0];
        } else {
            this.nonProxyHosts = env.getProperty(PROP_PROXY_NON_PROXY_HOSTS).split(SEPARATOR_REGEX);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Environment env = applicationContext.getEnvironment();
        this.setNonProxyHosts(env);
        this.setDefaultProxy(env);
    }
}
