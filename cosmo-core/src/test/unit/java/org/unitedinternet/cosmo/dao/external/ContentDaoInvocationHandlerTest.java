package org.unitedinternet.cosmo.dao.external;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.unitedinternet.cosmo.dao.ContentDao;

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
        String uuid = CalendarUuidGenerator.genererate();
        this.contentDaoProxy.findItemByPath(uuid);
        verify(contentDaoInternal, times(0)).findItemByPath(uuid);
        verify(contentDaoExternal, times(1)).findItemByPath(uuid);
    }

    @Test
    public void shouldSuccessfullyDelegateCallToInternalDaoForNonExternalizableMethod() {
        String uuid = CalendarUuidGenerator.genererate();
        this.contentDaoProxy.findItemParentByPath(uuid);
        verify(contentDaoInternal, times(1)).findItemParentByPath(uuid);
    }
}
