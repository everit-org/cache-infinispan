/**
 * This file is part of Everit - Infinispan Cache.
 *
 * Everit - Infinispan Cache is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Infinispan Cache is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Infinispan Cache.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.osgi.cache.infinispan.internal;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.cache.Cache;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.everit.osgi.cache.api.CacheConfiguration;
import org.everit.osgi.cache.api.CacheFactory;
import org.everit.osgi.cache.infinispan.config.CacheFactoryProps;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.configuration.global.TransportConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.jgroups.JGroupsTransport;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.component.ComponentException;
import org.osgi.service.log.LogService;

/**
 * A component that can customize and create Cache instances.
 */
@Component(ds = true, metatype = true, configurationFactory = true, policy = ConfigurationPolicy.REQUIRE)
@org.apache.felix.scr.annotations.Properties({
        @Property(name = CacheFactoryProps.CLUSTERED, boolValue = false),
        @Property(name = CacheFactoryProps.TRANSPORT_CLUSTER_NAME),
        @Property(name = CacheFactoryProps.TRANSPORT_MACHINE_ID),
        @Property(name = CacheFactoryProps.TRANSPORT_DISTRIBUTED_SYNC_TIMEOUT, intValue = 4 * 60 * 1000),
        @Property(name = CacheFactoryProps.TRANSPORT_CONFIGURATION_XML),
        @Property(name = "logService.target")
})
public class CacheFactoryComponent implements CacheFactory {


    private Map<String, ?> componentConfiguration;
    @Reference
    private LogService logService;

    private EmbeddedCacheManager manager;

    private Properties properties = new Properties();

    private boolean clustered;

    private ServiceRegistration<CacheFactory> serviceRegistration = null;
    

    @Activate
    public void activate(final BundleContext context, final Map<String, ?> config) {
        this.componentConfiguration = config;
        BundleWiring bundleWiring = context.getBundle().adapt(BundleWiring.class);
        ClassLoader componentClassLoader = bundleWiring.getClassLoader();

        GlobalConfigurationBuilder builder = new GlobalConfigurationBuilder();
        builder.classLoader(new MergedClassLoader(new ClassLoader[] { componentClassLoader,
                JGroupsTransport.class.getClassLoader() }));
        
        Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>();

        clustered = getBooleanConfigValue(CacheFactoryProps.CLUSTERED);
        serviceProperties.put(CacheFactoryProps.CLUSTERED, clustered);
        if (clustered) {
            builder.clusteredDefault();
            TransportConfigurationBuilder transport = builder.transport();
            String clusterName = getStringConfigValue(CacheFactoryProps.TRANSPORT_CLUSTER_NAME, false);
            if (clusterName != null) {
                transport.clusterName(clusterName);
            }
            Integer distributedSyncTimeout = getIntegerConfigValue(
                    CacheFactoryProps.TRANSPORT_DISTRIBUTED_SYNC_TIMEOUT, false);
            if (distributedSyncTimeout != null) {
                transport.distributedSyncTimeout(distributedSyncTimeout);
            }

            String machineId = getStringConfigValue(CacheFactoryProps.TRANSPORT_MACHINE_ID, false);
            if (machineId != null) {
                transport.machineId(machineId);
            }

            String nodeName = getStringConfigValue(CacheFactoryProps.TRANSPORT_NODE_NAME, false);
            if (nodeName != null) {
                transport.nodeName(nodeName);
            }

            String rackId = getStringConfigValue(CacheFactoryProps.TRANSPORT_RACK_ID, false);
            if (rackId != null) {
                transport.rackId(rackId);
            }

            String siteId = getStringConfigValue(CacheFactoryProps.TRANSPORT_RACK_ID, false);
            if (siteId != null) {
                transport.siteId(siteId);
            }
            String configurationXml = getStringConfigValue(CacheFactoryProps.TRANSPORT_CONFIGURATION_XML, false);
            if (configurationXml != null) {
                transport.addProperty("configurationXml", configurationXml);
            }
        } else {
            builder.nonClusteredDefault();
        }

        GlobalConfiguration globalConfig = builder.build();
        manager = new DefaultCacheManager(globalConfig);
        manager.start();

        context.registerService(CacheFactory.class, this, serviceProperties);
    }

    @Override
    public <K, V> Cache<K, V> createCache(final CacheConfiguration<K, V> cacheConfiguration,
            final ClassLoader classLoader) {

        Configuration dcc = manager.getDefaultCacheConfiguration();
        ConfigurationBuilder cb = new ConfigurationBuilder().read(dcc);
        if (classLoader != null) {
            cb.classLoader(classLoader);
        }

        // if (params != null) {
        // if (params.containsKey(CacheConstants.PARAM_MAXIDLE)
        // && (params.get(CacheConstants.PARAM_MAXIDLE) instanceof Long)) {
        // cb.expiration().maxIdle((Long) params.get(CacheConstants.PARAM_MAXIDLE));
        // }
        // if (params.containsKey(CacheConstants.PARAM_WAKEUPINTERVAL)
        // && (params.get(CacheConstants.PARAM_WAKEUPINTERVAL) instanceof Long)) {
        // cb.expiration().wakeUpInterval((Long) params.get(CacheConstants.PARAM_WAKEUPINTERVAL));
        // }
        // if (params.containsKey(CacheConstants.PARAM_CACHEMODE)
        // && (params.get(CacheConstants.PARAM_CACHEMODE) instanceof String)) {
        // String cacheMode = (String) params.get(CacheConstants.PARAM_CACHEMODE);
        // CacheMode mode = null;
        // if (CacheConstants.CACHEMODE_DIST_SYNC.equals(cacheMode)) {
        // mode = CacheMode.DIST_SYNC;
        // if (params.containsKey(CacheConstants.PARAM_NUMOWNERS)
        // && (params.get(CacheConstants.PARAM_NUMOWNERS) instanceof Integer)) {
        // cb.clustering().hash().numOwners((Integer) params.get(CacheConstants.PARAM_NUMOWNERS));
        // }
        // }
        // else if (CacheConstants.CACHEMODE_DIST_ASYNC.equals(cacheMode)) {
        // mode = CacheMode.DIST_ASYNC;
        // }
        // else if (CacheConstants.CACHEMODE_LOCAL.equals(cacheMode)) {
        // mode = CacheMode.LOCAL;
        // }
        // else if (CacheConstants.CACHEMODE_REPL_ASYNC.equals(cacheMode)) {
        // mode = CacheMode.REPL_ASYNC;
        // }
        // else if (CacheConstants.CACHEMODE_REPL_SYNC.equals(cacheMode)) {
        // mode = CacheMode.REPL_SYNC;
        // }
        // else if (CacheConstants.CACHEMODE_INVALIDATION_ASYNC.equals(cacheMode)) {
        // mode = CacheMode.INVALIDATION_ASYNC;
        // }
        // else if (CacheConstants.CACHEMODE_INVALIDATION_SYNC.equals(cacheMode)) {
        // mode = CacheMode.INVALIDATION_SYNC;
        // }
        //
        // if (mode != null) {
        // cb.clustering().cacheMode(mode);
        // }
        // }
        // }
        //
        // Configuration conf = cb.build();
        // manager.defineConfiguration(cacheName, conf);
        // jCacheManager.configureCache(cacheName, (AdvancedCache<Object, Object>) manager.getCache(cacheName));
        //
        // Cache<String, Object> cache = jCacheManager.getCache(cacheName);
        //
        // return cache;
        return null;
    }

    /**
     * Method responsible for closing the CacheManager.
     */
    @Deactivate
    public void deactivate() {
        if (manager != null) {
            manager.stop();
        }
    }

    private boolean getBooleanConfigValue(final String key) {
        Object value = componentConfiguration.get(key);
        if (value == null) {
            throw new ComponentException("The value of the boolean configuration property '" + key
                    + "' is not defined.");
        }
        if (!(value instanceof Boolean)) {
            throw new ComponentException("Type of configuration property must be Boolean. Current type is "
                    + value.getClass().toString());
        }
        return (Boolean) value;
    }

    private Integer getIntegerConfigValue(final String key, final boolean mandatory) {
        Object value = getObjectValue(key, mandatory);
        if (value == null) {
            return null;
        }
        if (!(value instanceof Integer)) {
            throw new ComponentException("Type of configuration property must be String. Current type is "
                    + value.getClass().toString());
        }
        return (Integer) value;
    }

    private Object getObjectValue(final String key, final boolean mandatory) {
        Object value = componentConfiguration.get(key);
        if (value == null && mandatory) {
            throw new ComponentException("The value of the mandatory configuration property '" + key
                    + "' is not defined.");
        }
        return value;
    }

    private String getStringConfigValue(final String key, final boolean mandatory) {
        Object value = getObjectValue(key, mandatory);
        if (value == null) {
            return null;
        }
        if (!(value instanceof String)) {
            throw new ComponentException("Type of configuration property must be String. Current type is "
                    + value.getClass().toString());
        }
        return (String) value;
    }
}
