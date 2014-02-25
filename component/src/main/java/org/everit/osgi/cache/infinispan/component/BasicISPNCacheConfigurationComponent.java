package org.everit.osgi.cache.infinispan.component;

import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.cache.api.CacheConfiguration;

@Component(configurationFactory = true, immediate = false, policy = ConfigurationPolicy.REQUIRE, metatype = true)
@Properties({ @Property(name = CacheConstants.PROP_CC_CACHE_NAME), @Property(name = CacheConstants.PROP_CC_MAX_ENTRIES) })
@Service
public class BasicISPNCacheConfigurationComponent<K, V> implements CacheConfiguration<K, V> {

    @Activate
    public void activate(final Map<String, Object> configuration) {

    }

}
