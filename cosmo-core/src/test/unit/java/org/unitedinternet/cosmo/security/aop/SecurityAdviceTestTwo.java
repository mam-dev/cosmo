package org.unitedinternet.cosmo.security.aop;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.unitedinternet.cosmo.dao.ContentDao;
import org.unitedinternet.cosmo.dao.UserDao;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.CollectionSubscription;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.Ticket;
import org.unitedinternet.cosmo.model.TicketType;
import org.unitedinternet.cosmo.model.User;
import org.unitedinternet.cosmo.model.hibernate.HibTicket;
import org.unitedinternet.cosmo.security.CosmoSecurityContext;
import org.unitedinternet.cosmo.security.CosmoSecurityManager;
import org.unitedinternet.cosmo.security.ItemSecurityException;
import org.unitedinternet.cosmo.security.impl.CosmoSecurityContextImpl;

/**
 * Test for {@link SecurityAdvice} with only mocks.
 * 
 * @author daniel grigore
 *
 */
public class SecurityAdviceTestTwo {

    private static final String U_SHARER = "sharer-1";
    private static final String U_SHAREE = "sharee-1";

    @Mock
    private ContentDao contentDao;
    @Mock
    private UserDao userDao;
    @Mock
    private CosmoSecurityManager securityManager;

    @Mock
    private ProceedingJoinPoint pjp;
    @Mock
    private ContentItem item;
    @Mock
    private CollectionItem collection;
    @Mock
    private User sharee;
    @Mock
    private User sharer;

    @Mock
    private CollectionSubscription subscription;

    // Use real ticket
    private Ticket ticket;

    private SecurityAdvice advice = null;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        this.advice = new SecurityAdvice(securityManager,contentDao,userDao);        
        Authentication authentication = new PreAuthenticatedAuthenticationToken(U_SHAREE, "passwd");
        Set<Ticket> tickets = Collections.emptySet();
        CosmoSecurityContext context = new CosmoSecurityContextImpl(authentication, tickets, sharee);
        when(securityManager.getSecurityContext()).thenReturn(context);

        when(collection.getOwner()).thenReturn(sharer);
        when(collection.getUid()).thenReturn("collection-uid");
        this.setUpOwner(sharer);
        Set<CollectionItem> parents = new HashSet<>(Arrays.asList(new CollectionItem[] { collection }));
        when(item.getParents()).thenReturn(parents);

        when(sharer.getUsername()).thenReturn(U_SHARER);
        when(sharee.getUsername()).thenReturn(U_SHAREE);
        when(userDao.getUser(U_SHARER)).thenReturn(sharer);
        when(userDao.getUser(U_SHAREE)).thenReturn(sharee);
    }

    // addItemToCollection - Start

    @Test(expected = ItemSecurityException.class)
    public void shouldThrowExceptionWhenAddingItemToCollectionAndShareeHasNoPrivileges() throws Throwable {
        this.advice.checkAddItemToCollection(pjp, item, collection);
    }

    @Test
    public void shouldAddItemToCollectionWhenUserOwnsThem() throws Throwable {
        this.setUpOwner(sharee);
        this.advice.checkAddItemToCollection(pjp, item, collection);
        verify(this.pjp).proceed();
    }

    @Test
    public void shouldAddItemToCollectionWhenShareeHasWritePrivilegeSubscription() throws Throwable {
        this.setUpSubscriptionAndTicket(TicketType.READ_WRITE);
        this.advice.checkAddItemToCollection(pjp, item, collection);
        verify(this.pjp).proceed();
    }

    @Test(expected = ItemSecurityException.class)
    public void shouldThrowExceptionWhenAddItemToCollectionAndTicketIsReadOnly() throws Throwable {
        this.setUpSubscriptionAndTicket(TicketType.READ_ONLY);
        this.advice.checkAddItemToCollection(pjp, item, collection);
    }

    // addItemToCollection - End

    private void setUpOwner(User user) {
        when(collection.getOwner()).thenReturn(user);
        when(item.getOwner()).thenReturn(user);
    }

    private void setUpSubscriptionAndTicket(TicketType ticketType) {
        Set<CollectionSubscription> subscriptions = new HashSet<>(
                Arrays.asList(new CollectionSubscription[] { subscription }));

        when(this.sharee.getSubscriptions()).thenReturn(subscriptions);
        when(subscription.getTargetCollection()).thenReturn(collection);
        ticket = new HibTicket(ticketType);
        ticket.setItem(collection);
        when(subscription.getTicket()).thenReturn(ticket);

        Set<Ticket> tickets = new HashSet<>(Arrays.asList(new Ticket[] { ticket }));
        when(collection.getTickets()).thenReturn(tickets);
    }

    // checkRemoveTicket - Start

    @Test(expected = ItemSecurityException.class)
    public void shouldThrowExceptionWhenDeletingAnItemWithoutPermission() throws Throwable {
        this.advice.checkRemoveItem(pjp, item);
    }

    @Test
    public void shouldDeleteItemWhenOwningit() throws Throwable {
        this.setUpOwner(sharee);
        this.advice.checkRemoveItem(pjp, item);
        verify(pjp).proceed();
    }

    @Test(expected = ItemSecurityException.class)
    public void shouldThrowExceptionWhenRemovingItemWithReadonlyTicket() throws Throwable {
        this.setUpSubscriptionAndTicket(TicketType.READ_ONLY);
        this.advice.checkRemoveItem(pjp, item);
    }

    @Test
    public void shouldRemoveItemWhenHavingWriteTicket() throws Throwable {
        this.setUpSubscriptionAndTicket(TicketType.READ_WRITE);
        this.advice.checkRemoveItem(pjp, item);
        verify(this.pjp, times(1)).proceed();
    }

    // checkRemoveTicket - End

    // checkUpdateContent - Start

    @Test(expected = ItemSecurityException.class)
    public void shouldThrowExceptionWhenModifyingContentWithoutRights() throws Throwable {
        this.advice.checkUpdateContent(pjp, item);
    }

    @Test
    public void shouldUpdateContentWhenOwningIt() throws Throwable {
        this.setUpOwner(sharee);
        this.advice.checkUpdateContent(pjp, item);
        verify(this.pjp).proceed();
    }

    @Test(expected = ItemSecurityException.class)
    public void shouldThrowExceptionWhenUpdatingContentWithReadOnlyTicket() throws Throwable {
        this.setUpSubscriptionAndTicket(TicketType.READ_ONLY);
        this.advice.checkUpdateContent(pjp, item);
    }
    
    @Test
    public void shouldUpdateContentWithWhenHavingWriteTicket() throws Throwable {
        this.setUpSubscriptionAndTicket(TicketType.READ_WRITE);
        this.advice.checkUpdateContent(pjp, item);
        verify(this.pjp).proceed();
    }
    
    // checkUpdateContent - End
}
