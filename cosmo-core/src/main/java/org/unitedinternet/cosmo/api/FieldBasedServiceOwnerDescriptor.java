package org.unitedinternet.cosmo.api;

import java.lang.reflect.Field;

public class FieldBasedServiceOwnerDescriptor extends ServiceOwnerDescriptor{
	private final Field field; 
	public FieldBasedServiceOwnerDescriptor(Class<?> ownerType, Class<?> serviceType, Field field) {
		super(ownerType, serviceType);
		this.field = field;
	}
	public Field getField() {
		return field;
	}

}
