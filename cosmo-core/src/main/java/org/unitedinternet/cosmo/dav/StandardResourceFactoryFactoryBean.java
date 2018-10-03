package org.unitedinternet.cosmo.dav;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.unitedinternet.cosmo.calendar.query.CalendarQueryProcessor;
import org.unitedinternet.cosmo.icalendar.ICalendarClientFilterManager;
import org.unitedinternet.cosmo.model.EntityFactory;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.UserIdentity;
import org.unitedinternet.cosmo.model.UserIdentitySupplier;
import org.unitedinternet.cosmo.security.CosmoSecurityManager;
import org.unitedinternet.cosmo.service.ContentService;
import org.unitedinternet.cosmo.service.UserService;

import com.google.common.collect.Sets;

@Configuration
public class StandardResourceFactoryFactoryBean {

    private static final UserIdentitySupplier DEFAULT_USER_IDENTITY_SUPPLIER = new UserIdentitySupplier() {

        @Override
        public UserIdentity forUser(User user) {
            return UserIdentity.of(Sets.newHashSet(user.getEmail()), user.getFirstName(), user.getLastName());
        }
    };

    private ContentService contentService;
    private UserService userService;
    private CosmoSecurityManager securityManager;
    private EntityFactory entityFactory;
    private CalendarQueryProcessor calendarQueryProcessor;
    private ICalendarClientFilterManager clientFilterManager;

    @Value("${cosmo.caldav.schedulingEnabled}")
    private boolean schedulingEnabled;

    @Autowired
    public StandardResourceFactoryFactoryBean(ContentService contentService, UserService userService,
            CosmoSecurityManager securityManager, EntityFactory entityFactory,
            CalendarQueryProcessor calendarQueryProcessor, ICalendarClientFilterManager clientFilterManager) {

        this.contentService = contentService;
        this.userService = userService;
        this.securityManager = securityManager;
        this.entityFactory = entityFactory;
        this.calendarQueryProcessor = calendarQueryProcessor;
        this.clientFilterManager = clientFilterManager;

    }

    @Bean
    public DavResourceFactory getStandardResourceFactory() throws Exception {
        UserIdentitySupplier identitySupplier = DEFAULT_USER_IDENTITY_SUPPLIER;
        return new StandardResourceFactory(contentService, userService, securityManager, entityFactory,
                calendarQueryProcessor, clientFilterManager, identitySupplier, schedulingEnabled);
    }

}