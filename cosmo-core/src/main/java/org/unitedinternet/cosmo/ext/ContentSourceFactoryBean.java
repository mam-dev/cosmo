package org.unitedinternet.cosmo.ext;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.FactoryBean;
import org.unitedinternet.cosmo.api.ExternalComponentInstanceProvider;
import org.unitedinternet.cosmo.metadata.Callback;

/**
 * <code>ContentSource</code> factory bean that looks in sub-application contenxt for a <code>ContetSource</code>
 * implementation and if it does not find one then it returns the default <code>ContentSource</code> configured within
 * COSMO context.
 * 
 * @author daniel grigore
 *
 */
public class ContentSourceFactoryBean implements FactoryBean<ContentSource> {

    private static final Log LOG = LogFactory.getLog(ContentSourceFactoryBean.class);
    private final ContentSource uriContentSource;
    private final ExternalComponentInstanceProvider componentProvider;

    public ContentSourceFactoryBean(ContentSource uriContentSource,
            ExternalComponentInstanceProvider componentProvider) {
        super();
        this.uriContentSource = uriContentSource;
        this.componentProvider = componentProvider;
    }

    @Override
    public ContentSource getObject() throws Exception {
        Set<? extends ContentSource> contentSourceSet = this.componentProvider
                .getImplInstancesAnnotatedWith(Callback.class, ContentSource.class);
        if (!contentSourceSet.isEmpty()) {
            if (contentSourceSet.size() != 1) {
                throw new IllegalArgumentException("Found more than one implementaion of ContentSource.");
            }
            ContentSource contentSource = contentSourceSet.iterator().next();
            LOG.info("EXTERNAL - using @Provided-ContentSource implementation class: " + contentSource);
            return contentSource;
        }
        LOG.info("EXTERNAL - using default ContentSource implementation class: " + uriContentSource);
        return this.uriContentSource;
    }

    @Override
    public Class<?> getObjectType() {
        return ContentSource.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
