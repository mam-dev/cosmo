package org.unitedinternet.cosmo.model.hibernate;

import org.unitedinternet.cosmo.model.EntityTagCalculableObject;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class EntityTagCalculableModelObject extends BaseModelObject implements EntityTagCalculableObject {

}
