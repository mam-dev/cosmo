package org.unitedinternet.cosmo.dao.external;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitedinternet.cosmo.dao.ContentDao;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.filter.ItemFilter;
import org.unitedinternet.cosmo.model.filter.NoteItemFilter;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionItem;

/**
 * 
 * @author daniel grigore
 *
 */
public class ContentDaoInvocationHandlerTest {

    @Mock
    private ContentDao contentDaoInternal;
    @Mock
    private ContentDao contentDaoExternal;

    private ContentDao contentDaoProxy;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.contentDaoProxy = new ContentDaoProxyFactory(
                new ContentDaoInvocationHandler(contentDaoInternal, contentDaoExternal)).getObject();
    }
       
    @Test
    public void shouldSuccessfullyDelegateCallToInternalDaoForNonExternalizableMethod() {
        String uuid = UuidExternalGenerator.getNext();
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
        String uuid = UuidExternalGenerator.getNext();
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
        parent.setUid(UuidExternalGenerator.getNext());
        filter.setParent(parent);
        this.contentDaoProxy.findItems(filter);
        verify(contentDaoExternal, times(1)).findItems(filter);
    }
}
