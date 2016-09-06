package org.unitedinternet.cosmo.servlet;

import javax.servlet.ServletContextListener;
/**
 * Provides the same contract as <code>ServletContextListener</code>.
 * Implementations of this class will be called internally by the Calendar server after 
 * some Calendar application context listeners are executed. The reason is to guarantee that 
 * delegates will be called after a certain state has been achieved (i.e. database has been initialized)  
 * 
 * @author cdobrota
 *
 */
public interface ServletContextListenerDelegate extends ServletContextListener{
	
}
