package org.unitedinternet.cosmo.api;

import java.lang.reflect.Method;

public class SetterBasedServiceOwnerDescriptor extends ServiceOwnerDescriptor{
	private final Method setter;
	
	public SetterBasedServiceOwnerDescriptor(Class<?> ownerType, Class<?> serviceType, Method setter) {
		super(ownerType, serviceType);
		this.setter = setter;
	}
	public Method getSetter() {
		return setter;
	}

}
