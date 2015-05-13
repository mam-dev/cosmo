package org.unitedinternet.cosmo.hibernate;

import java.util.Map;

import javax.sql.DataSource;

import org.hibernate.HibernateException;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.hibernate.service.spi.ServiceRegistryAwareService;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.service.spi.Stoppable;

/**
 * Resolves the datasource to be used.
 *
 * @author Iulia
 */

public class MultiTenantConnectionProvider extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl
        implements ServiceRegistryAwareService, Stoppable {
    
    private static final long serialVersionUID = 6613844123097178607L;
    private Map<String, DataSource> dataSourceMap;
    public static final String TENANT_IDENTIFIER_TO_USE_FOR_ANY_KEY = "hibernate.multi_tenant.datasource.identifier_for_any";
    private String tenantIdentifierForAny;

    @Override
    public void stop() {

    }

    @SuppressWarnings("unchecked")
    @Override
    public void injectServices(ServiceRegistryImplementor serviceRegistry) {
        final Object dataSourceConfigValue = serviceRegistry.getService(ConfigurationService.class)
                .getSettings()
                .get(MultiDataSourceSessionFactoryBean.DATA_SOURCE_MAP);
        dataSourceMap = (Map<String, DataSource>) dataSourceConfigValue;
        this.tenantIdentifierForAny = (String) serviceRegistry.getService(ConfigurationService.class)
                .getSettings()
                .get(TENANT_IDENTIFIER_TO_USE_FOR_ANY_KEY);
        if (tenantIdentifierForAny == null) {
            throw new HibernateException("JNDI name named a Context, but tenant identifier to use for ANY was not specified");
        }

    }

    @Override
    protected DataSource selectAnyDataSource() {
        return dataSourceMap.get(tenantIdentifierForAny);
    }

    @Override
    protected DataSource selectDataSource(String tenantIdentifier) {
        return dataSourceMap.get(tenantIdentifier);
    }

}
