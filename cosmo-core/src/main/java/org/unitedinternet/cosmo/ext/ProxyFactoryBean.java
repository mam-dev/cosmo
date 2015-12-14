package org.unitedinternet.cosmo.ext;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;

/**
 * <code>Factory</code> that creates a <code>java.net.Proxy</code> that is used when making requests for getting
 * external content.
 * 
 * @author daniel grigore
 *
 */
public class ProxyFactoryBean implements FactoryBean<Proxy>, ApplicationContextAware {

    private static final Log LOG = LogFactory.getLog(ProxyFactoryBean.class);
    private static final String PROP_PROXY_HOST = "external.proxy.host";
    private static final String PROP_PROXY_PORT = "external.proxy.port";

    private Environment env;

    public ProxyFactoryBean() {
        super();
    }

    @Override
    public Proxy getObject() throws Exception {
        String proxyHost = this.env.getProperty(PROP_PROXY_HOST);
        String proxyPort = this.env.getProperty(PROP_PROXY_PORT);
        if (proxyHost != null && !proxyHost.trim().isEmpty() && proxyPort != null && !proxyPort.trim().isEmpty()) {
            LOG.info("NET Setting proxy host: " + proxyHost + " and port: " + proxyPort
                    + " for getting external content.");
            return new Proxy(Type.HTTP, new InetSocketAddress(proxyHost, Integer.valueOf(proxyPort)));
        }
        return Proxy.NO_PROXY;
    }

    @Override
    public Class<?> getObjectType() {
        return Proxy.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.env = applicationContext.getEnvironment();
    }
}
