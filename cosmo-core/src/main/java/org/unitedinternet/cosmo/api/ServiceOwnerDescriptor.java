package org.unitedinternet.cosmo.api;

public class ServiceOwnerDescriptor {
	private final Class<?> ownerType;
	private final Class<?> serviceType;
	
	protected ServiceOwnerDescriptor(Class<?> ownerType, Class<?> serviceType){
		this.ownerType = ownerType;
		this.serviceType = serviceType;
	}
	
	public Class<?> getOwnerType() {
		return ownerType;
	}
	
	public Class<?> getServiceType() {
		return serviceType;
	}
}

