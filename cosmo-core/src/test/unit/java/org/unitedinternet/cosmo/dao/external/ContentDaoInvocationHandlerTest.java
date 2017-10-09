package org.unitedinternet.cosmo.dao.external;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.unitedinternet.cosmo.dao.ContentDao;
import org.unitedinternet.cosmo.dao.ContentDaoInvocationHandler;
import org.unitedinternet.cosmo.dao.ContentDaoProxyFactory;
import org.unitedinternet.cosmo.dao.hibernate.ContentDaoImpl;
import org.unitedinternet.cosmo.dao.subscription.ContentDaoSubscriptionImpl;
import org.unitedinternet.cosmo.dav.caldav.CaldavExceptionForbidden;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.ContentItem;
import org.unitedinternet.cosmo.model.Item;
import org.unitedinternet.cosmo.model.NoteItem;
import org.unitedinternet.cosmo.model.filter.ItemFilter;
import org.unitedinternet.cosmo.model.filter.NoteItemFilter;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;
import org.unitedinternet.cosmo.model.hibernate.HibNoteItem;

/**
 * 
 * @author daniel grigore
 *
 */
public class ContentDaoInvocationHandlerTest {

    @Mock
    private ContentDaoImpl contentDaoInternal;
    @Mock
    private ContentDaoExternal contentDaoExternal;
    @Mock
    private ContentDaoSubscriptionImpl contentDaoSub;
    @Mock
    private ApplicationContext applicatioContext;
    
    private ContentDao contentDaoProxy;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        ContentDaoInvocationHandler invocationHandler = new ContentDaoInvocationHandler();
        invocationHandler.setApplicationContext(this.applicatioContext);
        when(applicatioContext.getBean(ContentDaoImpl.class)).thenReturn(this.contentDaoInternal);
        when(applicatioContext.getBean(ContentDaoExternal.class)).thenReturn(this.contentDaoExternal);
        when(applicatioContext.getBean(ContentDaoSubscriptionImpl.class)).thenReturn(this.contentDaoSub);
        this.contentDaoProxy = new ContentDaoProxyFactory(invocationHandler).getObject();

    }

    @Test
    public void shouldSuccessfullyDelegateCallToInternalDaoForNonExternalizableMethod() {
        String uuid = UuidExternalGenerator.get().getNext();
        this.contentDaoProxy.findItemParentByPath(uuid);
        verify(contentDaoInternal, times(1)).findItemParentByPath(uuid);
    }

    @Test
    public void shouldSuccessfullyDelegateMethodCallToInternalDao()
            throws NoSuchMethodException, SecurityException, Throwable {
        String uuid = UUID.randomUUID().toString();
        this.contentDaoProxy.findItemByPath(uuid);
        verify(contentDaoInternal, times(1)).findItemByPath(uuid);
        verify(contentDaoExternal, times(0)).findItemByPath(uuid);
    }

    @Test
    public void shouldSuccessfullyDelegateMethodToExternalDao()
            throws NoSuchMethodException, SecurityException, Throwable {
        String uuid = UuidExternalGenerator.get().getNext();
        this.contentDaoProxy.findItemByPath(uuid);
        verify(contentDaoInternal, times(0)).findItemByPath(uuid);
        verify(contentDaoExternal, times(1)).findItemByPath(uuid);
    }

    @Test
    public void shouldSuccessfullyDelegateToInternalDaoWhenCallingFindItems() {
        ItemFilter filter = new ItemFilter();
        this.contentDaoProxy.findItems(filter);
        verify(contentDaoInternal, times(1)).findItems(filter);
    }

    @Test
    public void shouldSuccessfullyDelegateToExternalDaoWhenCallingFindItems() {
        NoteItemFilter filter = new NoteItemFilter();
        CollectionItem parent = new HibCollectionItem();
        parent.setUid(UuidExternalGenerator.get().getNext());
        filter.setParent(parent);
        this.contentDaoProxy.findItems(filter);
        verify(contentDaoExternal, times(1)).findItems(filter);
    }

    @Test(expected = CaldavExceptionForbidden.class)
    public void shouldThrowExceptionWhenCreatingEventInExternalCalendar() {
        HibCollectionItem delegate = new HibCollectionItem();
        delegate.setUid(UuidExternalGenerator.get().getNext());
        NoteItem child = new HibNoteItem();
        CollectionItem item = new ExternalCollectionItem(delegate, new HashSet<Item>());
        when(contentDaoExternal.createContent(item, child)).thenThrow(new CaldavExceptionForbidden(""));
        this.contentDaoProxy.createContent(item, child);
    }

    @Test(expected = CaldavExceptionForbidden.class)
    public void shouldThrowExceptionWhenUpdatingExternalCalendar() {
        HibCollectionItem delegate = new HibCollectionItem();
        delegate.setUid(UuidExternalGenerator.get().getNext());
        NoteItem child = new HibNoteItem();
        Set<ContentItem> children = new HashSet<>();
        children.add(child);
        CollectionItem item = new ExternalCollectionItem(delegate, new HashSet<Item>());
        when(contentDaoExternal.updateCollection(item, children)).thenThrow(new CaldavExceptionForbidden(""));
        this.contentDaoProxy.updateCollection(item, children);
    }
}
