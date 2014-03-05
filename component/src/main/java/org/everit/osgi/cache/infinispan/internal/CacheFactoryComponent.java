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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.everit.osgi.cache.api.CacheConfiguration;
import org.everit.osgi.cache.api.CacheFactory;
import org.everit.osgi.cache.api.CacheHolder;
import org.everit.osgi.cache.infinispan.config.CacheFactoryProps;
import org.everit.osgi.cache.infinispan.config.ISPNCacheConfiguration;
import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.jgroups.JGroupsTransport;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.service.component.ComponentException;
import org.osgi.service.log.LogService;

/**
 * A component that can customize and create Cache instances.
 */
@Component(name = CacheFactoryProps.CACHE_FACTORY_COMPONENT_NAME, metatype = true, configurationFactory = true,
        policy = ConfigurationPolicy.REQUIRE, immediate = true)
@org.apache.felix.scr.annotations.Properties({
        @Property(name = CacheFactoryProps.CLUSTERED, boolValue = false),
        @Property(name = CacheFactoryProps.TRANSPORT__CLUSTER_NAME),
        @Property(name = CacheFactoryProps.TRANSPORT__MACHINE_ID),
        @Property(name = CacheFactoryProps.TRANSPORT__NODE_NAME),
        @Property(name = CacheFactoryProps.TRANSPORT__RACK_ID),
        @Property(name = CacheFactoryProps.TRANSPORT__SITE_ID),
        @Property(name = CacheFactoryProps.TRANSPORT__DISTRIBUTED_SYNC_TIMEOUT, longValue = 4 * 60 * 1000),
        @Property(name = CacheFactoryProps.TRANSPORT__CONFIGURATION_XML),
        @Property(name = CacheFactoryProps.GLOBAL_JMX_STATISTICS__ENABLED, boolValue = false),
        @Property(name = CacheFactoryProps.GLOBAL_JMX_STATISTICS__JMX_DOMAIN),
        @Property(name = CacheFactoryProps.GLOBAL_JMX_STATISTICS__ALLOW_DUPLICATE_DOMAINS, boolValue = false),
        @Property(name = CacheFactoryProps.GLOBAL_JMX_STATISTICS__CACHE_MANAGER_NAME),
        @Property(name = "logService.target")
})
public class CacheFactoryComponent implements CacheFactory {

    private Map<String, ?> componentConfiguration;

    private ConcurrentMap<String, Boolean> activeCacheNames = new ConcurrentHashMap<String, Boolean>();

    @Reference
    private LogService logService;

    private EmbeddedCacheManager manager;

    private boolean clustered;

    private ServiceRegistration<CacheFactory> serviceRegistration = null;

    private String servicePID = null;

    @Activate
    public void activate(final BundleContext context, final Map<String, ?> config) {
        componentConfiguration = config;
        BundleWiring bundleWiring = context.getBundle().adapt(BundleWiring.class);
        ClassLoader componentClassLoader = bundleWiring.getClassLoader();

        GlobalConfigurationBuilder builder = new GlobalConfigurationBuilder();
        builder.classLoader(new MergedClassLoader(new ClassLoader[] { componentClassLoader,
                JGroupsTransport.class.getClassLoader() }));

        Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>();
        servicePID = (String) config.get(Constants.SERVICE_PID);
        serviceProperties.put(Constants.SERVICE_PID, servicePID);

        ReflectiveConfigurationBuilderHelper builderHelper = new ReflectiveConfigurationBuilderHelper(
                componentConfiguration, builder);

        ReflectiveComponentConfigurationHelper configHelper = builderHelper.getComponentConfigHelper();

        clustered = configHelper.getPropValue(CacheFactoryProps.CLUSTERED, boolean.class, true);
        serviceProperties.put(CacheFactoryProps.CLUSTERED, clustered);
        if (clustered) {
            serviceProperties.put(CacheFactoryProps.CLUSTERED, Boolean.TRUE);
            builder.clusteredDefault();
            builderHelper.applyConfigOnBuilderValue(CacheFactoryProps.TRANSPORT__CLUSTER_NAME, String.class, false);

            builderHelper.applyConfigOnBuilderValue(CacheFactoryProps.TRANSPORT__DISTRIBUTED_SYNC_TIMEOUT, long.class,
                    false);

            builderHelper.applyConfigOnBuilderValue(CacheFactoryProps.TRANSPORT__MACHINE_ID, String.class, false);

            builderHelper.applyConfigOnBuilderValue(CacheFactoryProps.TRANSPORT__NODE_NAME, String.class, false);

            builderHelper.applyConfigOnBuilderValue(CacheFactoryProps.TRANSPORT__RACK_ID, String.class, false);

            builderHelper.applyConfigOnBuilderValue(CacheFactoryProps.TRANSPORT__SITE_ID, String.class, false);

            String jgroupsXML = configHelper.getPropValue(CacheFactoryProps.TRANSPORT__CONFIGURATION_XML, String.class,
                    false);

            if (jgroupsXML != null) {
                builder.transport().addProperty("configurationXml", jgroupsXML);
            }
        } else {
            builder.nonClusteredDefault();
        }

        Boolean jmxStatisticsEnabled = configHelper.getPropValue(CacheFactoryProps.GLOBAL_JMX_STATISTICS__ENABLED,
                Boolean.class, true);

        if (jmxStatisticsEnabled) {
            builderHelper.applyValue(CacheFactoryProps.GLOBAL_JMX_STATISTICS__ENABLED, jmxStatisticsEnabled,
                    boolean.class);

            builderHelper.applyConfigOnBuilderValue(CacheFactoryProps.GLOBAL_JMX_STATISTICS__CACHE_MANAGER_NAME,
                    String.class,
                    false);

            builderHelper.applyConfigOnBuilderValue(CacheFactoryProps.GLOBAL_JMX_STATISTICS__JMX_DOMAIN, String.class,
                    false);

            builderHelper.applyConfigOnBuilderValue(CacheFactoryProps.GLOBAL_JMX_STATISTICS__ALLOW_DUPLICATE_DOMAINS,
                    Boolean.class, false);
        }

        builderHelper.applyConfigOnBuilderValue(CacheFactoryProps.SITE__LOCAL_SITE, String.class, false);

        GlobalConfiguration globalConfig = builder.build();

        if (clustered) {
            transferValueToServiceProperties(CacheFactoryProps.TRANSPORT__CLUSTER_NAME, globalConfig,
                    serviceProperties);

            transferValueToServiceProperties(CacheFactoryProps.TRANSPORT__DISTRIBUTED_SYNC_TIMEOUT, globalConfig,
                    serviceProperties);

            transferValueToServiceProperties(CacheFactoryProps.TRANSPORT__MACHINE_ID, globalConfig, serviceProperties);
            transferValueToServiceProperties(CacheFactoryProps.TRANSPORT__NODE_NAME, globalConfig, serviceProperties);
            transferValueToServiceProperties(CacheFactoryProps.TRANSPORT__RACK_ID, globalConfig, serviceProperties);
            transferValueToServiceProperties(CacheFactoryProps.TRANSPORT__SITE_ID, globalConfig, serviceProperties);

            String jgroupsXML = globalConfig.transport().properties().getProperty("configurationXml");
            if (jgroupsXML != null) {
                serviceProperties.put(CacheFactoryProps.TRANSPORT__CONFIGURATION_XML, jgroupsXML);
            }
        }

        transferValueToServiceProperties(CacheFactoryProps.GLOBAL_JMX_STATISTICS__ENABLED, globalConfig,
                serviceProperties);

        if (jmxStatisticsEnabled) {
            transferValueToServiceProperties(CacheFactoryProps.GLOBAL_JMX_STATISTICS__CACHE_MANAGER_NAME, globalConfig,
                    serviceProperties);

            transferValueToServiceProperties("globalJmxStatistics.domain", globalConfig, serviceProperties);

            transferValueToServiceProperties(CacheFactoryProps.GLOBAL_JMX_STATISTICS__ALLOW_DUPLICATE_DOMAINS,
                    globalConfig, serviceProperties);
        }

        transferValueToServiceProperties("sites.localSite", globalConfig, serviceProperties);

        manager = new DefaultCacheManager(globalConfig);
        manager.start();

        context.registerService(CacheFactory.class, this, serviceProperties);
    }

    @Override
    public <K, V> CacheHolder<K, V> createCache(final CacheConfiguration<K, V> cacheConfiguration,
            final ClassLoader classLoader) {

        if (!(cacheConfiguration instanceof ISPNCacheConfiguration)) {
            throw new ComponentException("Only configurations with type " + ISPNCacheConfiguration.class.getName()
                    + " are accepted: " + cacheConfiguration.getClass().getName());
        }
        ISPNCacheConfiguration<K, V> ispnCacheConfiguration = (ISPNCacheConfiguration<K, V>) cacheConfiguration;

        Configuration configuration = ispnCacheConfiguration.getConfiguration();
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.read(configuration);
        if (classLoader != null) {
            cb.classLoader(classLoader);
        }

        String cacheName = ispnCacheConfiguration.getCacheName();
        Boolean cacheAlreadyUsed = activeCacheNames.put(cacheName, Boolean.TRUE);
        if (cacheAlreadyUsed != null) {
            throw new ComponentException("Cache with cache name '" + cacheName + "' is already used");
        }
        try {
            logService.log(LogService.LOG_DEBUG, "Creating cache '" + cacheName + "'");
            manager.defineConfiguration(cacheName, cb.build(true));
            Cache<K, V> cache = manager.getCache(cacheName);
            AdvancedCache<K, V> advancedCache = cache.getAdvancedCache();
            advancedCache.with(classLoader);
            advancedCache.start();
            return new CacheHolderImpl<K, V>(advancedCache, this);
        } catch (RuntimeException e) {
            activeCacheNames.remove(cacheName);
            logService.log(LogService.LOG_DEBUG, "Cache '" + cacheName + "' is removed due to exception");
            throw e;
        }
    }

    void cacheClosed(String cacheName) {
        logService.log(LogService.LOG_DEBUG, "Cache '" + cacheName + "' is removed");
        activeCacheNames.remove(cacheName);
    }

    /**
     * Method responsible for closing the CacheManager.
     */
    @Deactivate
    public void deactivate() {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
        if (manager != null) {
            manager.stop();
        }
    }

    private void transferValueToServiceProperties(final String key, final Object configuration,
            final Dictionary<String, ? super Object> serviceProperties) {
        String[] keyParts = key.split("\\.");
        Object currentConfigObject = configuration;
        try {
            for (int i = 0, n = keyParts.length; i < n; i++) {
                if (i < (n - 1)) {
                    Method method = currentConfigObject.getClass().getMethod(keyParts[i]);
                    currentConfigObject = method.invoke(currentConfigObject);
                } else {
                    Method method = currentConfigObject.getClass().getMethod(keyParts[i]);
                    Object returnValue = method.invoke(currentConfigObject);
                    if (returnValue != null) {
                        serviceProperties.put(key, returnValue);
                    }
                }
            }
        } catch (NoSuchMethodException e) {
            throw new ComponentException("Could not set configuration with key '" + key + "' in component instance "
                    + servicePID, e);
        } catch (SecurityException e) {
            throw new ComponentException("Could not set configuration with key '" + key + "' in component instance "
                    + servicePID, e);
        } catch (IllegalAccessException e) {
            throw new ComponentException("Could not set configuration with key '" + key + "' in component instance "
                    + servicePID, e);
        } catch (IllegalArgumentException e) {
            throw new ComponentException("Could not set configuration with key '" + key + "' in component instance "
                    + servicePID, e);
        } catch (InvocationTargetException e) {
            throw new ComponentException("Could not set configuration with key '" + key + "' in component instance "
                    + servicePID, e);
        }
    }
}
