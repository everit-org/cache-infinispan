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
@Component(name = "org.everit.osgi.cache.infinispan.CacheFactory", metatype = true, configurationFactory = true,
        policy = ConfigurationPolicy.REQUIRE, immediate = true)
@org.apache.felix.scr.annotations.Properties({
        @Property(name = CacheFactoryProps.CLUSTERED, boolValue = false),
        @Property(name = CacheFactoryProps.TRANSPORT__CLUSTER_NAME),
        @Property(name = CacheFactoryProps.TRANSPORT__MACHINE_ID),
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

    @Reference
    private LogService logService;

    private EmbeddedCacheManager manager;

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

        clustered = getPropValue(componentConfiguration, CacheFactoryProps.CLUSTERED, boolean.class, true);
        serviceProperties.put(CacheFactoryProps.CLUSTERED, clustered);
        if (clustered) {
            serviceProperties.put(CacheFactoryProps.CLUSTERED, Boolean.TRUE);
            builder.clusteredDefault();
            applyConfigOnBuilderValue(componentConfiguration, CacheFactoryProps.TRANSPORT__CLUSTER_NAME, builder,
                    String.class, false);

            applyConfigOnBuilderValue(componentConfiguration, CacheFactoryProps.TRANSPORT__DISTRIBUTED_SYNC_TIMEOUT,
                    builder, long.class, false);

            applyConfigOnBuilderValue(componentConfiguration, CacheFactoryProps.TRANSPORT__MACHINE_ID, builder,
                    String.class, false);

            applyConfigOnBuilderValue(componentConfiguration, CacheFactoryProps.TRANSPORT__NODE_NAME, builder,
                    String.class, false);

            applyConfigOnBuilderValue(componentConfiguration, CacheFactoryProps.TRANSPORT__RACK_ID, builder,
                    String.class, false);

            applyConfigOnBuilderValue(componentConfiguration, CacheFactoryProps.TRANSPORT__SITE_ID, builder,
                    String.class, false);

            String jgroupsXML = getPropValue(componentConfiguration, CacheFactoryProps.TRANSPORT__CONFIGURATION_XML,
                    String.class, false);
            if (jgroupsXML != null) {
                builder.transport().addProperty("configurationXml", jgroupsXML);
            }
        } else {
            builder.nonClusteredDefault();
        }

        Boolean jmxStatisticsEnabled = getPropValue(componentConfiguration,
                CacheFactoryProps.GLOBAL_JMX_STATISTICS__ENABLED, Boolean.class, true);

        if (jmxStatisticsEnabled) {
            applyValue(CacheFactoryProps.GLOBAL_JMX_STATISTICS__ENABLED, jmxStatisticsEnabled, builder, boolean.class);

            applyConfigOnBuilderValue(componentConfiguration,
                    CacheFactoryProps.GLOBAL_JMX_STATISTICS__CACHE_MANAGER_NAME, builder, String.class, false);

            applyConfigOnBuilderValue(componentConfiguration,
                    CacheFactoryProps.GLOBAL_JMX_STATISTICS__JMX_DOMAIN, builder, String.class, false);

            applyConfigOnBuilderValue(componentConfiguration,
                    CacheFactoryProps.GLOBAL_JMX_STATISTICS__ALLOW_DUPLICATE_DOMAINS, builder, Boolean.class, false);
        }

        applyConfigOnBuilderValue(componentConfiguration, CacheFactoryProps.SITE__LOCAL_SITE, builder, String.class,
                false);

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

    private void transferValueToServiceProperties(String key, Object configuration,
            Dictionary<String, ? super Object> serviceProperties) {
        String[] keyParts = key.split("\\.");
        Object currentConfigObject = configuration;
        try {
            for (int i = 0, n = keyParts.length; i < n; i++) {
                if (i < n - 1) {
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
            throw new ComponentException("Could not set configuration with key " + key, e);
        } catch (SecurityException e) {
            throw new ComponentException("Could not set configuration with key " + key, e);
        } catch (IllegalAccessException e) {
            throw new ComponentException("Could not set configuration with key " + key, e);
        } catch (IllegalArgumentException e) {
            throw new ComponentException("Could not set configuration with key " + key, e);
        } catch (InvocationTargetException e) {
            throw new ComponentException("Could not set configuration with key " + key, e);
        }
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
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
        if (manager != null) {
            manager.stop();
        }
    }

    private <V> V getPropValue(Map<String, ?> configuration, final String key, final Class<V> valueType,
            final boolean mandatory) {
        Object value = getObjectValue(configuration, key, mandatory);
        if (value == null || (String.class.isInstance(value) && ((String) value).trim().equals(""))) {
            return null;
        }
        if (!classify(valueType).isInstance(value)) {
            throw new ComponentException("Type of configuration property " + key + " must be " + valueType.toString()
                    + ". Current type is " + value.getClass().toString());
        }
        return (V) value;
    }

    private Class<?> classify(Class<?> potentiallyTypeOfPrimitive) {
        if (potentiallyTypeOfPrimitive.isAssignableFrom(boolean.class)) {
            return Boolean.class;
        }
        if (potentiallyTypeOfPrimitive.isAssignableFrom(int.class)) {
            return Integer.class;
        }
        if (potentiallyTypeOfPrimitive.isAssignableFrom(long.class)) {
            return Long.class;
        }
        return potentiallyTypeOfPrimitive;
    }

    private Object getObjectValue(Map<String, ?> configuration, final String key, final boolean mandatory) {
        Object value = configuration.get(key);
        if (value == null && mandatory) {
            throw new ComponentException("The value of the mandatory configuration property '" + key
                    + "' is not defined.");
        }
        return value;
    }

    private void applyConfigOnBuilderValue(final Map<String, ?> componentConfig, final String key,
            Object builder,
            Class<?> valueType, boolean mandatory) {
        Object propValue = getPropValue(componentConfig, key, valueType, mandatory);
        if (propValue == null) {
            return;
        }
        applyValue(key, propValue, builder, valueType);
    }

    private void applyValue(String key, Object value, Object builder, Class<?> valueType) {
        String[] keyParts = key.split("\\.");
        Object currentConfigObject = builder;
        try {
            for (int i = 0, n = keyParts.length; i < n; i++) {
                if (i < n - 1) {
                    Method method = currentConfigObject.getClass().getMethod(keyParts[i]);
                    currentConfigObject = method.invoke(currentConfigObject);

                } else {
                    Method method = currentConfigObject.getClass().getMethod(keyParts[i], valueType);
                    method.invoke(currentConfigObject, value);
                }
            }
        } catch (NoSuchMethodException e) {
            throw new ComponentException("Could not set configuration with key " + key, e);
        } catch (SecurityException e) {
            throw new ComponentException("Could not set configuration with key " + key, e);
        } catch (IllegalAccessException e) {
            throw new ComponentException("Could not set configuration with key " + key, e);
        } catch (IllegalArgumentException e) {
            throw new ComponentException("Could not set configuration with key " + key, e);
        } catch (InvocationTargetException e) {
            throw new ComponentException("Could not set configuration with key " + key, e);
        }
    }
}
