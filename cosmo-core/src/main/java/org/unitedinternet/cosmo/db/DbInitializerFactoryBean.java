package org.unitedinternet.cosmo.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.beans.factory.FactoryBean;
import org.unitedinternet.cosmo.api.ExternalComponentInstanceProvider;
import org.unitedinternet.cosmo.datasource.HibernateSessionFactoryBeanDelegate;
import org.unitedinternet.cosmo.metadata.Callback;
import org.unitedinternet.cosmo.service.ServerPropertyService;
import org.unitedinternet.cosmo.service.impl.CosmoStartupDataInitializer;


public class DbInitializerFactoryBean implements FactoryBean<DbInitializer>{
	
	private ServerPropertyService serverPropertyService;
	private HibernateSessionFactoryBeanDelegate localSessionFactory;
	private DataSource datasource;
	private Boolean validateSchema;
	private CosmoStartupDataInitializer cosmoStartupDataInitializer;
	private ExternalComponentInstanceProvider externalComponentInstanceProvider;
	
	public DbInitializerFactoryBean(ServerPropertyService serverPropertyService,
									HibernateSessionFactoryBeanDelegate localSessionFactory,
									DataSource datasource,
									Boolean validateSchema,
									CosmoStartupDataInitializer cosmoStartupDataInitializer,
									ExternalComponentInstanceProvider externalComponentInstanceProvider) {
		this.serverPropertyService = serverPropertyService;
		this.localSessionFactory = localSessionFactory;
		this.datasource = datasource;
		this.validateSchema = validateSchema;
		this.cosmoStartupDataInitializer = cosmoStartupDataInitializer;
		this.externalComponentInstanceProvider = externalComponentInstanceProvider;
	}
	@Override
	public DbInitializer getObject() throws Exception {
		
		Set<? extends DatabaseInitializationCallback> callbacks = externalComponentInstanceProvider.getImplInstancesAnnotatedWith(Callback.class, DatabaseInitializationCallback.class);
		List<? extends DatabaseInitializationCallback> callbacksList = new ArrayList<>(callbacks);
		Collections.sort(callbacksList, new Comparator<DatabaseInitializationCallback>(){
			@Override
			public int compare(DatabaseInitializationCallback o1,
					DatabaseInitializationCallback o2) {
				
				return -1 * Integer.compare(o1.getClass().getAnnotation(Callback.class).order(), 
							o2.getClass().getAnnotation(Callback.class).order());
			}});
		DbInitializer result = new DbInitializer();
		
		result.setCosmoStartupDataInitializer(cosmoStartupDataInitializer);
		result.setDataSource(datasource);
		result.setLocalSessionFactory(localSessionFactory);
		result.setServerPropertyService(serverPropertyService);
		result.setValidateSchema(validateSchema);
		
		
		return result;
	}

	@Override
	public Class<?> getObjectType() {
		return DbInitializer.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
