package org.unitedinternet.cosmo.datasource;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.unitedinternet.cosmo.api.ExternalComponentInstanceProvider;
import org.unitedinternet.cosmo.hibernate.CompoundInterceptor;

//https://www.baeldung.com/the-persistence-layer-with-spring-and-jpa
@Configuration
public class HibernateSessionFactoryBeanDelegateConfiguration {

    @Autowired
    private ExternalComponentInstanceProvider externalComponentInstanceProvider;

    @Autowired
    private CompoundInterceptor cosmoHibernateInterceptor;

    @Value("${cosmo.hibernate.hibernatePackagesToScan}")
    private String hibernatePackagesToScan;

    @Autowired
    private DataSource dataSource;

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan(new String[] { "org.unitedinternet.cosmo.model.hibernate" });

        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        Map<String, Object> jpaPropertyMap = additionalProperties();
        jpaPropertyMap.put("hibernate.ejb.interceptor", this.cosmoHibernateInterceptor);
        em.setJpaPropertyMap(jpaPropertyMap);

        return em;
    }

    // TODO - not here. Use yaml file for defining string properties and HibernatePropertiesCustomizer for interceptors.
    private Map<String, Object> additionalProperties() {
        Map<String, Object> hibernateProperties = new HashMap<>();
        hibernateProperties.put("hibernate.cache.use_query_cache", "false");

        hibernateProperties.put("hibernate.cache.use_second_level_cache", "false");
        hibernateProperties.put("hibernate.show_sql", "false");
        hibernateProperties.put("hibernate.format_sql", "false");
        hibernateProperties.put("hibernate.use_sql_comments", "false");
        hibernateProperties.put("hibernate.generate_statistics", "false");
        hibernateProperties.put("hibernate.auto_close_session", "false");
        hibernateProperties.put("hibernate.hbm2ddl.auto", "update");
        hibernateProperties.put("hibernate.dialect", "org.unitedinternet.cosmo.hibernate.CosmoMySQL5InnoDBDialect");
        hibernateProperties.put("hibernate.id.new_generator_mappings", "false");

        return hibernateProperties;
    }
}
