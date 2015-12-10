package org.unitedinternet.cosmo.dao.external;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitedinternet.cosmo.dao.ContentDao;

/**
 * <code>InvocationHandler</code> that delegates the method calls to the internal DAO object or to an external DAO
 * provider.
 * 
 * @author daniel grigore
 *
 */
public class ContentDaoInvocationHandler implements InvocationHandler {

    private static final Log LOG = LogFactory.getLog(ContentDaoInvocationHandler.class);
    
    /**
     * Database DAO.
     */
    private final ContentDao contentDaoInternal;

    private final ContentDao contentDaoExternal;

    public ContentDaoInvocationHandler(ContentDao contentDaoInternal, ContentDao contentDaoExternal) {
        super();
        this.contentDaoInternal = contentDaoInternal;
        this.contentDaoExternal = contentDaoExternal;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.isAnnotationPresent(Externalizable.class) && isExternalUid(args)) {
            LOG.info("EXTERNAL calling method " + method.getName() + " with args: " + Arrays.toString(args));
            return method.invoke(this.contentDaoExternal, args);
        }
        return method.invoke(this.contentDaoInternal, args);
    }

    private static boolean isExternalUid(Object[] args) {
        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof String && CalendarUuidGenerator.containsExternalUid((String) arg)) {
                    return true;
                }
            }
        }
        return false;
    }
}
