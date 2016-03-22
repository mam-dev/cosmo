package org.unitedinternet.cosmo.ext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.unitedinternet.cosmo.ext.EnvironmentProxyFactory.PROP_PROXY_HOST;
import static org.unitedinternet.cosmo.ext.EnvironmentProxyFactory.PROP_PROXY_NON_PROXY_HOSTS;
import static org.unitedinternet.cosmo.ext.EnvironmentProxyFactory.PROP_PROXY_PORT;

import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;

/**
 * 
 * @author daniel grigore
 * @see EnvironmentProxyFactory
 */
public class EnvironmentProxyFactoryTest {

    @Test
    public void shouldGetNoProxyWhenNoPropertyIsSet() {
        EnvironmentProxyFactory factory = withProperties(new HashMap<String, Object>());
        factory.setApplicationContext(new StaticApplicationContext());
        Proxy proxy = factory.get(null);
        assertEquals(Proxy.NO_PROXY, proxy);
        proxy = factory.get("localhost");
        assertEquals(Proxy.NO_PROXY, proxy);
    }

    @Test
    public void shouldGetNoProxyWhenProxyPropertiesAreNotSet() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(PROP_PROXY_NON_PROXY_HOSTS, "");
        ProxyFactory factory = withProperties(properties);
        Proxy proxy = factory.get(null);
        assertEquals(Proxy.NO_PROXY, proxy);
        proxy = factory.get("localhost");
        assertEquals(Proxy.NO_PROXY, proxy);
    }

    @Test
    public void voidShouldGetDifferentProxiesWhenPropertiesAreSet() {

        String proxyHost = "proxyHost";
        String proxyPort = "8080";

        Map<String, Object> properties = new HashMap<>();
        properties.put(PROP_PROXY_HOST, proxyHost);
        properties.put(PROP_PROXY_PORT, proxyPort);
        properties.put(PROP_PROXY_NON_PROXY_HOSTS, "host1|host2|host3");
        ProxyFactory factory = withProperties(properties);

        Proxy proxy = factory.get(null);
        assertNotNull(proxy);
        assertEquals(proxyHost + ":" + proxyPort, proxy.address().toString());

        proxy = factory.get("someHost");
        assertNotNull(proxy);
        assertEquals(proxyHost + ":" + proxyPort, proxy.address().toString());

        proxy = factory.get("host1");
        assertEquals(Proxy.NO_PROXY, proxy);
        
        proxy = factory.get("host3");
        assertEquals(Proxy.NO_PROXY, proxy);
    }

    static EnvironmentProxyFactory withProperties(Map<String, Object> properties) {
        StaticApplicationContext context = new StaticApplicationContext();
        StandardEnvironment env = (StandardEnvironment) context.getEnvironment();
        MutablePropertySources sources = env.getPropertySources();
        sources.addFirst(new MapPropertySource("defaultSource", properties));
        EnvironmentProxyFactory factory = new EnvironmentProxyFactory();
        factory.setApplicationContext(context);
        return factory;
    }
}
