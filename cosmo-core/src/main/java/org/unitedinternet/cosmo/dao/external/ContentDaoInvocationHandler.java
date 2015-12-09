package org.unitedinternet.cosmo.dao.external;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

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

    private final ContentDao dbContentDao;

    public ContentDaoInvocationHandler(ContentDao dbContentDao) {
        super();
        this.dbContentDao = dbContentDao;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        LOG.info("DAO Proxy method: " + method.getName() + " with " + args.length + " parameters.");
        return method.invoke(this.dbContentDao, args);
    }
}
