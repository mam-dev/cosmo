package com.unitedinternet.calendar;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.PrintWriter;
import java.util.Properties;

/**
 * Data source configuration that starts an embedded Maria DB instance before data source is created.
 * 
 * @author daniel grigore
 *
 */
@Configuration
@ConfigurationProperties("spring.datasource")
public class DataSourceConfig {
    @Bean
    public DataSource ds() {
        Properties props = new Properties();
        props.setProperty("dataSourceClassName", "org.postgresql.ds.PGSimpleDataSource");
        props.setProperty("dataSource.user", "test");
        props.setProperty("dataSource.password", "test");
        props.setProperty("dataSource.databaseName", "caldav");
        props.put("dataSource.logWriter", new PrintWriter(System.out));

        return new HikariDataSource(new HikariConfig(props));
    }
}
