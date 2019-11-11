package test.integrational.api.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = SecurityAutoConfiguration.class)
@ComponentScan(basePackages = { "org.unitedinternet.cosmo", "test.integrational" })
@EntityScan(basePackages = "org.unitedinternet.cosmo.model.hibernate")
public class IntegrationalTestApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(IntegrationalTestApplication.class);
    }
}
