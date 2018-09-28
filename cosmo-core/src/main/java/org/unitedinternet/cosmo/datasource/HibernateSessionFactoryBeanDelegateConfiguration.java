package org.unitedinternet.cosmo.datasource;


import java.io.IOException;
import java.util.Properties;

import javax.persistence.PersistenceUnit;
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
	      LocalContainerEntityManagerFactoryBean em 
	        = new LocalContainerEntityManagerFactoryBean();
	      em.setDataSource(dataSource);
	      em.setPackagesToScan(new String[] { "org.unitedinternet.cosmo.model.hibernate" });
	      
	      JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
	      em.setJpaVendorAdapter(vendorAdapter);
	      em.setJpaProperties(additionalProperties());
	 
	      return em;
	   }
	
	Properties 
	additionalProperties() {
		Properties hibernateProperties = new Properties(); //TODO - not here
		hibernateProperties.setProperty("hibernate.cache.use_query_cache", "false");
		
		hibernateProperties.setProperty("hibernate.cache.use_second_level_cache", "false");
		hibernateProperties.setProperty("hibernate.show_sql", "false");
		hibernateProperties.setProperty("hibernate.format_sql", "false");
		hibernateProperties.setProperty("hibernate.use_sql_comments", "false");
		hibernateProperties.setProperty("hibernate.generate_statistics", "false");
		hibernateProperties.setProperty("hibernate.auto_close_session", "false");
		hibernateProperties.setProperty("hibernate.hbm2ddl.auto", "update");
		hibernateProperties.setProperty("hibernate.dialect", "org.unitedinternet.cosmo.hibernate.CosmoMySQL5InnoDBDialect");
		hibernateProperties.setProperty("hibernate.id.new_generator_mappings", "false");
	        
	       return hibernateProperties;
	   }
	
	//@Bean
	//TODO - not used
	public HibernateSessionFactoryBeanDelegate getHibernateSessionFactoryBeanDelegate() throws IOException {
		
		HibernateSessionFactoryBeanDelegate hibernateSessionFactory = new HibernateSessionFactoryBeanDelegate(externalComponentInstanceProvider);
		
//		hibernateSessionFactory.setPackagesToScan(hibernatePackagesToScan);
		hibernateSessionFactory.setPackagesToScan("org.unitedinternet.cosmo.model.hibernate");
//		hibernateSessionFactory.setAnnotatedPackages("org.unitedinternet.cosmo.model", "org.unitedinternet.cosmo.model.hibernate");
		hibernateSessionFactory.setAnnotatedClasses(org.unitedinternet.cosmo.model.hibernate.HibUser.class);
		hibernateSessionFactory.setEntityInterceptor(cosmoHibernateInterceptor);
		
		hibernateSessionFactory.setDataSource(dataSource);
//		hibernateSessionFactory.setDataSource(getDataSource());
		
		Properties hibernateProperties = new Properties(); //TODO - not here
		hibernateProperties.setProperty("hibernate.cache.use_query_cache", "false");
		
		hibernateProperties.setProperty("hibernate.cache.use_second_level_cache", "false");
		hibernateProperties.setProperty("hibernate.show_sql", "false");
		hibernateProperties.setProperty("hibernate.format_sql", "false");
		hibernateProperties.setProperty("hibernate.use_sql_comments", "false");
		hibernateProperties.setProperty("hibernate.generate_statistics", "false");
		hibernateProperties.setProperty("hibernate.hbm2ddl.auto", "update");
		hibernateProperties.setProperty("hibernate.dialect", "org.unitedinternet.cosmo.hibernate.CosmoMySQL5InnoDBDialect");
		hibernateProperties.setProperty("hibernate.id.new_generator_mappings", "false");
		
		
//		hibernateSessionFactory.getConfiguration().registerTypeOverride(new LongTimestampType());
		
		hibernateSessionFactory.afterPropertiesSet();
		
		return hibernateSessionFactory;
	}

//	@Override
//	public SessionFactoryBuilder getSessionFactoryBuilder(MetadataImplementor metadata,
//			SessionFactoryBuilderImplementor defaultBuilder) {
//		
//		 metadata.getTypeResolver().registerTypeOverride(new LongTimestampType()); //TODO - singleton for LongTimestampType
//	        return defaultBuilder;
//	}
	
	//TODO - this is configured for tests - extract the properties into a property file 
	//TODO - possible to not be used this config
//	private DataSource getDataSource() {
//		DataSource dataSource = new DataSource();
//		
//		dataSource.setDriverClassName("org.mariadb.jdbc.Driver");
//		dataSource.setUrl("jdbc:mysql://localhost:33060/test?autoReconnect=true");
//		dataSource.setUsername("root");
//		dataSource.setPassword("");
//		dataSource.setMaxActive(100);
//		dataSource.setMaxIdle(20);
//		dataSource.setMaxWait(10000);
//		dataSource.setTestOnBorrow(true);
//		dataSource.setValidationQuery("SELECT 1;");
//		return dataSource;
//	}
}
