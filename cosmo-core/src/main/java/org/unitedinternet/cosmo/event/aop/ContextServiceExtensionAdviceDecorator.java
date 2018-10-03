package org.unitedinternet.cosmo.event.aop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.unitedinternet.cosmo.service.interceptors.CalendarGetHandler;
import org.unitedinternet.cosmo.service.interceptors.CollectionCreateHandler;
import org.unitedinternet.cosmo.service.interceptors.CollectionDeleteHandler;
import org.unitedinternet.cosmo.service.interceptors.CollectionUpdateHandler;
import org.unitedinternet.cosmo.service.interceptors.EventAddHandler;
import org.unitedinternet.cosmo.service.interceptors.EventMoveHandler;
import org.unitedinternet.cosmo.service.interceptors.EventRemoveHandler;
import org.unitedinternet.cosmo.service.interceptors.EventUpdateHandler;

/**
 * 
 * TODO See if this is needed in anyway.
 * 
 * Decorator for {@link ContextServiceExtensionsAdvice} that adds the defined event handlers.
 * 
 * @author daniel grigore
 *
 */
public class ContextServiceExtensionAdviceDecorator {

    private final ContextServiceExtensionsAdvice advice;

    /**
     * Constructs an instance taking as argument the <code>ExternalComponentInstanceProvider</code>.
     * 
     * @param provider
     */
    public ContextServiceExtensionAdviceDecorator(
            ContextServiceExtensionsAdvice advice) {
        
        this.advice = advice;
        this.setInterceptors();
    }

    /**
     * Gets a list of instances of specified type <code>clazz</code> annotated with {@link Interceptor}.
     * 
     * @param clazz
     *            type of beans to be queried for
     * @return
     */
    private <T> List<T> getInterceptorsList(Class<T> clazz) {
        Collection<? extends T> handlers = Collections.emptyList();
        List<T> list = new ArrayList<>();
        for (T handler : handlers) {
            list.add(handler);
        }
        return list;
    }

    private final void setInterceptors() {
        this.advice.setAddHandlers(this.getInterceptorsList(EventAddHandler.class));
        this.advice.setUpdateHandlers(this.getInterceptorsList(EventUpdateHandler.class));
        this.advice.setRemoveHandlers(this.getInterceptorsList(EventRemoveHandler.class));
        this.advice.setMoveHandlers(this.getInterceptorsList(EventMoveHandler.class));
        this.advice.setCreateHandlers(this.getInterceptorsList(CollectionCreateHandler.class));
        this.advice.setUpdateCollectionHandlers(this.getInterceptorsList(CollectionUpdateHandler.class));
        this.advice.setDeleteHandlers(this.getInterceptorsList(CollectionDeleteHandler.class));
        this.advice.setCalendarGetHandlers(this.getInterceptorsList(CalendarGetHandler.class));
    }
}
