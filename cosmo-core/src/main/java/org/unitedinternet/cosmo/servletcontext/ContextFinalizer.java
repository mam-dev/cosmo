package org.unitedinternet.cosmo.servletcontext;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * User: izidaru
 * Date: 8/7/13
 * Time: 11:26 AM
 */
public class ContextFinalizer implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        java.util.Enumeration<Driver> drivers = DriverManager.getDrivers();
        Driver d = null;
        while(drivers.hasMoreElements()) {
            try {
                d = drivers.nextElement();
                DriverManager.deregisterDriver(d);
            } catch (SQLException ex) {
            	ex.printStackTrace();
            }
        }       
    }
}
