package com.unitedinternet.calendar;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import ch.vorburger.mariadb4j.springframework.MariaDB4jSpringService;

/**
 * Data source configuration that starts an embedded Maria DB instance before data source is created.
 *
 * @author daniel grigore
 *
 */
@Configuration
@ConfigurationProperties("spring.datasource")
public class DataSourceConfig extends HikariConfig {

    @Bean(initMethod = "start", destroyMethod = "stop", name = "mariaDB")
    public MariaDB4jSpringService mariaDBService() {
        MariaDB4jSpringService db = new MariaDB4jSpringService();
        db.setDefaultBaseDir("target/maridb/base");
        db.setDefaultDataDir("target/maridb/data");
        db.setDefaultPort(33060);
        return db;
    }

    @Bean
    @DependsOn("mariaDB")
    public DataSource ds() {
        return new HikariDataSource(this);
    }
}
