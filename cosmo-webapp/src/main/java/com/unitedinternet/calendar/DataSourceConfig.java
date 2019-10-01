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
public class DataSourceConfig extends HikariConfig {
    @Bean
    public DataSource ds() {
        this.setJdbcUrl("jdbc:mysql://localhost:3306/caldav");
        this.setUsername("test");
        this.setPassword("test");
        return  new HikariDataSource(this);
    }
}
