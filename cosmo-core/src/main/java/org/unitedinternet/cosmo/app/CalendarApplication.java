package org.unitedinternet.cosmo.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.dao.PersistenceExceptionTranslationAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

/**
 * TODO Add comments.
 * 
 * @author daniel grigore
 *
 */
@SpringBootApplication
@SpringBootConfiguration
@ComponentScan(basePackages = { "org.unitedinternet.cosmo", "org.unitedinternet.cosmo.app" })
@EnableAutoConfiguration
public class CalendarApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(CalendarApplication.class, args);
    }

}
