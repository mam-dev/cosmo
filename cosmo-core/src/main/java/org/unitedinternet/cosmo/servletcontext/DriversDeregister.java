package org.unitedinternet.cosmo.servletcontext;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

/**
 * Listener that de-register the drivers to avoid memory leaks.
 * 
 * @author daniel grigore
 */
@Component
public class DriversDeregister implements ApplicationListener<ContextClosedEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(DriversDeregister.class);

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {

        LOG.info("[Drivers] About to de-register drivers...");
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            LOG.info("[Drivers] De-registering {}", driver);
            try {
                DriverManager.deregisterDriver(driver);
            } catch (SQLException e) {
                LOG.error("Failed to deregister driver: {}", driver.getClass());
            }
        }
        LOG.info("[Drivers] De-registered drivers.");
    }
}
