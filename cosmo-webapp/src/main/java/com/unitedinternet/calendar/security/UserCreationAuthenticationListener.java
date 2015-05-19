package com.unitedinternet.calendar.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.unitedinternet.cosmo.metadata.CalendarSecurity;
import org.unitedinternet.cosmo.metadata.Provided;
import org.unitedinternet.cosmo.model.CalendarCollectionStamp;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.security.SuccessfulAuthenticationListener;
import org.unitedinternet.cosmo.service.ContentService;
import org.unitedinternet.cosmo.service.UserService;

@CalendarSecurity
public class UserCreationAuthenticationListener implements SuccessfulAuthenticationListener{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UserCreationAuthenticationListener.class);
    
    @Provided
	private UserService userService;
	private EntityFactory entityFactory;
	private ContentService contentService;
	
	
	@Override
	public void onSuccessfulAuthentication(Authentication authentication) {
		LOGGER.info("=== Succesful authentication occured ===");
		createUserIfNotPresent(authentication);
	}

	private User createUserIfNotPresent(Authentication authentication) {
		String userName = authentication.getName();
		
		User user = userService.getUser(userName);
		
		if(user != null){
			LOGGER.info("=== Found user with email [{}] ===", user.getEmail());
			return user;
		}
		
		LOGGER.info("=== No user found for principal [{}]. Creating one with an empty calendar.===", userName);
		user = entityFactory.createUser();
        user.setUsername(userName);
        user.setEmail(userName);
        user.setFirstName(userName);
        user.setLastName(userName);
        user.setPassword("NOT_NULL");
        
        user = userService.createUser(user);
        
		
		String defaultCalendarName = "calendar";
		String calendarDisplayName = "calendarDisplayName";
		String color = "#f0f0f0";
		
		CollectionItem collection = entityFactory.createCollection();
		collection.setOwner(user);
		collection.setName(defaultCalendarName);
		collection.setDisplayName(calendarDisplayName);
		collection.getStamp(CalendarCollectionStamp.class);
		CalendarCollectionStamp colorCollectionStamp = entityFactory.createCalendarCollectionStamp(collection);
		colorCollectionStamp.setColor(color);
		colorCollectionStamp.setVisibility(true);
		collection.addStamp(colorCollectionStamp);
		
		CollectionItem rootItem = contentService.getRootItem(user);
		
		contentService.createCollection(rootItem, collection);
		
		return user;
	}
	
	@Provided
    public void setEntityFactory(EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }
	@Provided(unwrapIfProxied=true)
    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

}
