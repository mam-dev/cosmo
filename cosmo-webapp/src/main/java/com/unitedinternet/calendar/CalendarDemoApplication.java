package com.unitedinternet.calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

/**
 * Spring boot demo application.
 * 
 * @author daniel grigore
 *
 */
@SpringBootApplication
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = SecurityAutoConfiguration.class)
@ComponentScan(basePackages = { "org.unitedinternet.cosmo", "com.unitedinternet.calendar" })
@EntityScan(basePackages = "org.unitedinternet.cosmo.model.hibernate")
public class CalendarDemoApplication extends SpringBootServletInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(CalendarDemoApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(CalendarDemoApplication.class, args);
        LOG.info("[Start] Calendar Demo Application started.");
    }
}
