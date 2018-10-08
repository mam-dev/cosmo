package org.unitedinternet.cosmo.app;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.unitedinternet.cosmo.acegisecurity.userdetails.CosmoUserDetails;
import org.unitedinternet.cosmo.model.CalendarCollectionStamp;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.service.ContentService;
import org.unitedinternet.cosmo.service.UserService;

/**
 * TODO
 * 
 * @author daniel grigore
 *
 */
@Primary
@Component
@Transactional
public class DaoAuthProvider implements AuthenticationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(DaoAuthProvider.class);

    private final UserService userService;
    private final EntityFactory entityFactory;
    private final ContentService contentService;

    public DaoAuthProvider(UserService userService, EntityFactory entityFactory, ContentService contentService) {
        super();
        this.userService = userService;
        this.entityFactory = entityFactory;
        this.contentService = contentService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String userName = authentication.getName();
        LOGGER.info("===Authenticating user [{}]===", userName);
        User user = this.createUserIfNotPresent(authentication);
        return new UsernamePasswordAuthenticationToken(new CosmoUserDetails(user), authentication.getCredentials(),
                authentication.getAuthorities());
    }

    private User createUserIfNotPresent(Authentication authentication) {
        String userName = authentication.getName();

        User user = userService.getUser(userName);

        if (user != null) {
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

        // String defaultCalendarName = "calendar";
        // String calendarDisplayName = "calendarDisplayName";
        // String color = "#f0f0f0";
        //
        // CollectionItem collection = entityFactory.createCollection();
        // collection.setOwner(user);
        // collection.setName(defaultCalendarName);
        // collection.setDisplayName(calendarDisplayName);
        // collection.getStamp(CalendarCollectionStamp.class);
        // CalendarCollectionStamp colorCollectionStamp = entityFactory.createCalendarCollectionStamp(collection);
        // colorCollectionStamp.setColor(color);
        // colorCollectionStamp.setVisibility(true);
        // collection.addStamp(colorCollectionStamp);
        //
        // CollectionItem rootItem = contentService.getRootItem(user);
        //
        // contentService.createCollection(rootItem, collection);

        return user;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

}