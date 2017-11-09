package org.unitedinternet.cosmo.dao.subscription;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.unitedinternet.cosmo.api.ExternalComponentInstanceProvider;
import org.unitedinternet.cosmo.metadata.Callback;

/**
 * 
 * @author daniel grigore
 *
 */
public class FreeBusyObfuscaterFactoryBean implements FactoryBean<FreeBusyObfuscater> {

    private static final Logger LOG = LoggerFactory.getLogger(FreeBusyObfuscaterFactoryBean.class);

    private final ExternalComponentInstanceProvider instanceProvider;

    public FreeBusyObfuscaterFactoryBean(ExternalComponentInstanceProvider instanceProvider) {
        super();
        this.instanceProvider = instanceProvider;
    }

    @Override
    public FreeBusyObfuscater getObject() throws Exception {
        Set<? extends FreeBusyObfuscater> callbacks = (Set<? extends FreeBusyObfuscater>) this.instanceProvider
                .getImplInstancesAnnotatedWith(Callback.class, FreeBusyObfuscater.class);
        FreeBusyObfuscater freeBusyObfuscater = null;
        if (callbacks != null && !callbacks.isEmpty()) {
            if (callbacks.size() > 1) {
                throw new RuntimeException("Moree FreeBusyObfuscater implementations found: " + callbacks);
            }
            freeBusyObfuscater = callbacks.iterator().next();
        }
        if (freeBusyObfuscater == null) {
            freeBusyObfuscater = new FreeBusyObfuscaterDefault();
        }
        LOG.info("[Free-Busy-Obfuscater] using instance: {}", freeBusyObfuscater);
        return freeBusyObfuscater;
    }

    @Override
    public Class<?> getObjectType() {
        return FreeBusyObfuscater.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
