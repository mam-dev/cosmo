package test.integrational.api.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.unitedinternet.cosmo.acegisecurity.userdetails.CosmoUserDetails;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.Group;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.service.UserService;

/**
 * Demo <code>AuthenticationProvider</code> that allows all requests that have a username and a password and performs
 * provisioning in case it is needed. This is for testing purposes only.
 * 
 * @author daniel grigore
 *
 */
@Primary
@Component
@Transactional
public class AllowAllAuthenticationProvider implements AuthenticationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(AllowAllAuthenticationProvider.class);

    private final UserService userService;
    private final EntityFactory entityFactory;

    private final String DEFAULT_GROUP_NAME = "all";

    public AllowAllAuthenticationProvider(UserService userService, EntityFactory entityFactory) {
        super();
        this.userService = userService;
        this.entityFactory = entityFactory;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String userName = authentication.getName();
        LOGGER.info("[AUTH] About to authenticate user: {}", userName);
        User user = this.createUserIfNotPresent(authentication);
        return new UsernamePasswordAuthenticationToken(new CosmoUserDetails(user), authentication.getCredentials(),
                authentication.getAuthorities());
    }

    private User createUserIfNotPresent(Authentication authentication) {
        String userName = authentication.getName();
        User user = this.userService.getUser(userName);
        if (user != null) {
            LOGGER.info("[AUTH] Found user with email address: {}", user.getEmail());
        } else {
            LOGGER.info("[AUTH] No user found for email address: {}. Creating one...", userName);
            user = this.entityFactory.createUser();
            user.setUsername(userName);
            user.setEmail(userName);
            user.setFirstName(userName);
            user.setLastName(userName);
            user.setPassword("NOT_NULL");
            user = this.userService.createUser(user);
        }
        // Add every user onto group named "all"

        Group group = this.userService.getGroup(DEFAULT_GROUP_NAME);
        if (group != null) {
            LOGGER.info("[AUTH] Found group " + group.getUsername() + ": " + group.getDisplayName());
        } else {
            LOGGER.info("[AUTH] No group found with name '{}'. Creating a new one...", DEFAULT_GROUP_NAME);
            group = this.entityFactory.createGroup();
            group.setUsername(DEFAULT_GROUP_NAME);
            group.setDisplayName("Group For Everyone");
            group = this.userService.createGroup(group);
        }
        group.addUser(user);




        return user;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

}