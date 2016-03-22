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

public class DbInitializerFactoryBean implements FactoryBean<DbInitializer> {

    private HibernateSessionFactoryBeanDelegate localSessionFactory;
    private DataSource datasource;
    private ExternalComponentInstanceProvider externalComponentInstanceProvider;

    public DbInitializerFactoryBean(HibernateSessionFactoryBeanDelegate localSessionFactory, DataSource datasource,
            ExternalComponentInstanceProvider externalComponentInstanceProvider) {
        this.localSessionFactory = localSessionFactory;
        this.datasource = datasource;
        this.externalComponentInstanceProvider = externalComponentInstanceProvider;
    }

    @Override
    public DbInitializer getObject() throws Exception {

        Set<? extends DatabaseInitializationCallback> callbacks = externalComponentInstanceProvider
                .getImplInstancesAnnotatedWith(Callback.class, DatabaseInitializationCallback.class);
        List<? extends DatabaseInitializationCallback> callbacksList = new ArrayList<>(callbacks);
        Collections.sort(callbacksList, new Comparator<DatabaseInitializationCallback>() {
            @Override
            public int compare(DatabaseInitializationCallback o1, DatabaseInitializationCallback o2) {

                return -1 * Integer.compare(o1.getClass().getAnnotation(Callback.class).order(),
                        o2.getClass().getAnnotation(Callback.class).order());
            }
        });

        DbInitializer dbInitializer = new DbInitializer();
        dbInitializer.setDataSource(datasource);
        dbInitializer.setLocalSessionFactory(localSessionFactory);
        dbInitializer.setCallbacks(callbacksList);
        return dbInitializer;
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
