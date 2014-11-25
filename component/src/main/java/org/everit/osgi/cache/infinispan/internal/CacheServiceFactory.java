package org.everit.osgi.cache.infinispan.internal;

import org.infinispan.AdvancedCache;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

public class CacheServiceFactory<K, V> implements ServiceFactory<AdvancedCache<K, V>> {

    private Component

    @Override
    public AdvancedCache<K, V> getService(final Bundle bundle,
            final ServiceRegistration<AdvancedCache<K, V>> registration) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void ungetService(final Bundle bundle, final ServiceRegistration<AdvancedCache<K, V>> registration,
            final AdvancedCache<K, V> service) {
        // TODO Auto-generated method stub

    }

}
