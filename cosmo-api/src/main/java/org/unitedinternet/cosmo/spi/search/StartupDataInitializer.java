package org.unitedinternet.cosmo.spi.search;

public interface StartupDataInitializer {

	/**
	 * Initialize database startup data.
	 * Add server properties, version, defaults etc.
	 * Implement this method in order to add other initialization business. 
	 */
    public abstract void initializeStartupData();

}