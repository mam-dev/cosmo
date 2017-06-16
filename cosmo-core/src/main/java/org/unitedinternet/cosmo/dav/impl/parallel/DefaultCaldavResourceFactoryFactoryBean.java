package org.unitedinternet.cosmo.dav.impl.parallel;

import java.util.Set;

import org.springframework.beans.factory.FactoryBean;
import org.unitedinternet.cosmo.api.ExternalComponentInstanceProvider;
import org.unitedinternet.cosmo.calendar.query.CalendarQueryProcessor;
import org.unitedinternet.cosmo.dav.parallel.CalDavResourceFactory;
import org.unitedinternet.cosmo.icalendar.ICalendarClientFilterManager;
import org.unitedinternet.cosmo.metadata.Supplier;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.UserIdentity;
import org.unitedinternet.cosmo.model.UserIdentitySupplier;
import org.unitedinternet.cosmo.security.CosmoSecurityManager;
import org.unitedinternet.cosmo.service.ContentService;
import org.unitedinternet.cosmo.service.UserService;

import com.google.common.collect.Sets;

public class DefaultCaldavResourceFactoryFactoryBean implements FactoryBean<CalDavResourceFactory>{
	
	private static final UserIdentitySupplier DEFAULT_USER_IDENTITY_SUPPLIER = new UserIdentitySupplier() {
		@Override
		public UserIdentity forUser(User user) {
			return UserIdentity.of(Sets.newHashSet(user.getEmail()), user.getFirstName(), user.getLastName());
		}
	};
	
	private final ContentService contentService;
    private final UserService userService;
    private final CosmoSecurityManager securityManager;
    private final EntityFactory entityFactory;
    private final CalendarQueryProcessor calendarQueryProcessor;
    private final ICalendarClientFilterManager clientFilterManager;
    private final ExternalComponentInstanceProvider componentProvider;
    private final boolean schedulingEnabled;
    
    public DefaultCaldavResourceFactoryFactoryBean(ContentService contentService,
    												UserService userService,
    												CosmoSecurityManager securityManager,
    												EntityFactory entityFactory,
    												CalendarQueryProcessor calendarQueryProcessor,
    												ICalendarClientFilterManager clientFilterManager,
    												ExternalComponentInstanceProvider componentProvider,
    												boolean schedulingEnabled){
    	this.contentService=contentService;
    	this.userService=userService;
    	this.securityManager=securityManager;
    	this.entityFactory=entityFactory;
    	this.calendarQueryProcessor=calendarQueryProcessor;
    	this.clientFilterManager=clientFilterManager;
    	this.componentProvider=componentProvider;
    	this.schedulingEnabled=schedulingEnabled;
    }
	@Override
	public CalDavResourceFactory getObject() throws Exception {
		Set<? extends UserIdentitySupplier> identitySuppliers = componentProvider.getImplInstancesAnnotatedWith(Supplier.class, UserIdentitySupplier.class);
		UserIdentitySupplier identitySupplier = identitySuppliers.isEmpty() ? DEFAULT_USER_IDENTITY_SUPPLIER : identitySuppliers.iterator().next();
		
		return new DefaultCalDavResourceFactory(contentService, 
												userService, 
												securityManager, 
												entityFactory, 
												calendarQueryProcessor, 
												clientFilterManager, 
												identitySupplier, 
												schedulingEnabled);
	}

	@Override
	public Class<CalDavResourceFactory> getObjectType() {
		return CalDavResourceFactory.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}
}