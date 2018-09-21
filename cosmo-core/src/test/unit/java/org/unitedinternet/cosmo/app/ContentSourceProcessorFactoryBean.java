package org.unitedinternet.cosmo.app;

import org.mockito.Mockito;
import org.springframework.beans.factory.FactoryBean;
import org.unitedinternet.cosmo.ext.ContentSourceProcessor;

/**
 * Test only. To be deleted.
 * 
 * @author daniel grigore
 *
 */
public class ContentSourceProcessorFactoryBean implements FactoryBean<ContentSourceProcessor> {

    @Override
    public ContentSourceProcessor getObject() throws Exception {
        return Mockito.mock(ContentSourceProcessor.class);
    }

    @Override
    public Class<?> getObjectType() {
        return ContentSourceProcessor.class;
    }

}
