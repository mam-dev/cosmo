package com.unitedinternet.calendar;

import javax.sql.DataSource;

import ch.vorburger.mariadb4j.springframework.MariaDB4jSpringService;
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
        this.setJdbcUrl("jdbc:mysql://localhost:33060/test?autoReconnect=true");
        this.setUsername("root");
        this.setPassword("");
        this.setDriverClassName("org.mariadb.jdbc.Driver");
        return new HikariDataSource(this);
    }
}

