package org.unitedinternet.cosmo.db;

import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * XXX - Remove this.
 * 
 * @author daniel grigore
 *
 */
@Configuration
public class DbInitializerFactoryBean {

    // private HibernateSessionFactoryBeanDelegate localSessionFactory;
    private DataSource datasource;

    public DbInitializerFactoryBean(DataSource datasource) {
        this.datasource = datasource;
    }

    @Bean
    public DbInitializer getDbInitializer() throws Exception {
        
        List<? extends DatabaseInitializationCallback> callbacksList = Collections.emptyList();        

        DbInitializer dbInitializer = new DbInitializer();
        dbInitializer.setDataSource(datasource);
        // dbInitializer.setLocalSessionFactory(localSessionFactory);
        dbInitializer.setCallbacks(callbacksList);
        return dbInitializer;
    }

}
