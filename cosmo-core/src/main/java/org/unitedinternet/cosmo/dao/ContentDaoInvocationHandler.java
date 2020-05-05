package org.unitedinternet.cosmo.dao;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.unitedinternet.cosmo.dao.external.ContentDaoExternal;
import org.unitedinternet.cosmo.dao.external.ExternalCollectionItem;
import org.unitedinternet.cosmo.dao.external.UuidExternalGenerator;
import org.unitedinternet.cosmo.dao.hibernate.ContentDaoImpl;
import org.unitedinternet.cosmo.dao.subscription.ContentDaoSubscriptionImpl;
import org.unitedinternet.cosmo.dao.subscription.UuidSubscriptionGenerator;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.filter.NoteItemFilter;
import org.unitedinternet.cosmo.model.hibernate.HibCollectionSubscriptionItem;

/**
 * <code>InvocationHandler</code> that delegates the method calls to appropriate DAO implementation.
 * 
 * @author daniel grigore
 *
 */
@Component
public class ContentDaoInvocationHandler implements InvocationHandler, ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(ContentDaoInvocationHandler.class);

    private static final int WRAPPED_COUNT = 100;

    private ApplicationContext applicationContext;

    public ContentDaoInvocationHandler() {

    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String path = getPath(args);
        ContentDao dao = this.getContentDao(method, args);
        try {
            return method.invoke(dao, args);
        } catch (Exception e) {
            Throwable unwrapped = unwrap(e);
            LOG.error("Exc {} with msg '{}' caught when calling method {} with args length {} at path: {}",
                    unwrapped.getClass().getSimpleName(), unwrapped.getMessage(), method.getName(), args.length, path);
            throw unwrapped;
        }
    }

    private static String getPath(Object[] args) {
        String path = null;
        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof String) {
                    // Method findItemByPath(String) or findItemByPath(String, String)
                    path = (String) arg;
                } else if (arg instanceof NoteItemFilter) {
                    // Method findItems(NoteItemFilter)
                    NoteItemFilter filter = (NoteItemFilter) arg;
                    CollectionItem parent = filter.getParent();
                    if (parent != null) {
                        path = parent.getUid();
                    }
                } else if (arg instanceof ExternalCollectionItem) {
                    ExternalCollectionItem external = (ExternalCollectionItem) arg;
                    if (external.getDelegate() != null) {
                        path = external.getDelegate().getUid();
                    }
                } else if (arg instanceof HibCollectionSubscriptionItem) {
                    HibCollectionSubscriptionItem subscription = (HibCollectionSubscriptionItem) arg;
                    path = subscription.getUid();
                }
            }
        }
        return path;
    }

    private static Throwable unwrap(Throwable t) {
        Throwable unwrapped = t;
        int count = 0;
        while (unwrapped.getCause() != null && !unwrapped.getCause().equals(unwrapped)) {
            unwrapped = t.getCause();
            count++;
            if (count > WRAPPED_COUNT) {
                break;
            }
        }
        return unwrapped;
    }

    private ContentDao getContentDao(Method method, Object[] args) {
        if (method.isAnnotationPresent(ExternalizableContent.class)) {
            String path = getPath(args);
            if (path != null) {
                if (UuidExternalGenerator.get().containsUuid(path)) {
                    return this.applicationContext.getBean(ContentDaoExternal.class);
                } else if (UuidSubscriptionGenerator.get().containsUuid(path)) {
                    return this.applicationContext.getBean(ContentDaoSubscriptionImpl.class);
                }
            }
        }
        return applicationContext.getBean(ContentDaoImpl.class);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
