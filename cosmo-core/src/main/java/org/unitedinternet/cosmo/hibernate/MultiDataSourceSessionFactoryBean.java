package org.unitedinternet.cosmo.hibernate;

import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;

/**
 * The link between Spring datasource objects and Hibernate is LocalSessionFactoryBean.
 * MultiTenacy classes are instantiated by Hibernate, so, we have to pass the datasource map
 * as property.
 *
 * @author Iulia
 */
public class MultiDataSourceSessionFactoryBean extends LocalSessionFactoryBean {
    private Properties auxiliaryHibernateProperties = new Properties();

    public static final String DATA_SOURCE_MAP = "x-hibernate.connection.datasource";

    private Map<String, DataSource> dataSourceMap;

    @Override
    protected SessionFactory buildSessionFactory(LocalSessionFactoryBuilder sfb) {
        if (dataSourceMap != null) {
            auxiliaryHibernateProperties.put(DATA_SOURCE_MAP, dataSourceMap);
        }
        sfb.addProperties(auxiliaryHibernateProperties);
        return super.buildSessionFactory(sfb);
    }

    public void setDataSourceMap(Map<String, DataSource> dataSourceMap) {
        this.dataSourceMap = dataSourceMap;
    }


}
