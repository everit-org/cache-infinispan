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

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.everit.osgi.cache.infinispan.config.CacheFactoryConstants;
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

/**
 * A component that can customize and create Cache instances.
 */
@Component(name = CacheFactoryConstants.SERVICE_FACTORYPID_CACHE_FACTORY, metatype = true, configurationFactory = true,
        policy = ConfigurationPolicy.REQUIRE, immediate = true)
@org.apache.felix.scr.annotations.Properties({
        @Property(name = CacheFactoryConstants.CLUSTERED, boolValue = false),
        @Property(name = CacheFactoryConstants.TRANSPORT__CLUSTER_NAME),
        @Property(name = CacheFactoryConstants.TRANSPORT__MACHINE_ID),
        @Property(name = CacheFactoryConstants.TRANSPORT__NODE_NAME),
        @Property(name = CacheFactoryConstants.TRANSPORT__RACK_ID),
        @Property(name = CacheFactoryConstants.TRANSPORT__SITE_ID),
        @Property(name = CacheFactoryConstants.TRANSPORT__DISTRIBUTED_SYNC_TIMEOUT, longValue = 4 * 60 * 1000),
        @Property(name = CacheFactoryConstants.TRANSPORT__CONFIGURATION_XML),
        @Property(name = CacheFactoryConstants.GLOBAL_JMX_STATISTICS__ENABLED, boolValue = false),
        @Property(name = CacheFactoryConstants.GLOBAL_JMX_STATISTICS__JMX_DOMAIN),
        @Property(name = CacheFactoryConstants.GLOBAL_JMX_STATISTICS__ALLOW_DUPLICATE_DOMAINS, boolValue = false),
        @Property(name = CacheFactoryConstants.GLOBAL_JMX_STATISTICS__CACHE_MANAGER_NAME),
        @Property(name = "logService.target"),
        @Property(name = Constants.SERVICE_DESCRIPTION, propertyPrivate = false)
})
public class CacheManagerComponent {

    private Map<String, ?> componentConfiguration;

    private EmbeddedCacheManager manager;

    private boolean clustered;

    private ServiceRegistration<?> serviceRegistration = null;

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

        clustered = configHelper.getPropValue(CacheFactoryConstants.CLUSTERED, boolean.class, true);
        serviceProperties.put(CacheFactoryConstants.CLUSTERED, clustered);
        if (clustered) {
            serviceProperties.put(CacheFactoryConstants.CLUSTERED, Boolean.TRUE);
            builder.clusteredDefault();
            builderHelper.applyConfigOnBuilderValue(CacheFactoryConstants.TRANSPORT__CLUSTER_NAME, String.class, false);

            builderHelper.applyConfigOnBuilderValue(CacheFactoryConstants.TRANSPORT__DISTRIBUTED_SYNC_TIMEOUT,
                    long.class,
                    false);

            builderHelper.applyConfigOnBuilderValue(CacheFactoryConstants.TRANSPORT__MACHINE_ID, String.class, false);

            builderHelper.applyConfigOnBuilderValue(CacheFactoryConstants.TRANSPORT__NODE_NAME, String.class, false);

            builderHelper.applyConfigOnBuilderValue(CacheFactoryConstants.TRANSPORT__RACK_ID, String.class, false);

            builderHelper.applyConfigOnBuilderValue(CacheFactoryConstants.TRANSPORT__SITE_ID, String.class, false);

            String jgroupsXML = configHelper.getPropValue(CacheFactoryConstants.TRANSPORT__CONFIGURATION_XML,
                    String.class,
                    false);

            if (jgroupsXML != null) {
                builder.transport().addProperty("configurationXml", jgroupsXML);
            }
        } else {
            builder.nonClusteredDefault();
        }

        String jmxStatisticsCacheManagerName = builderHelper.applyConfigOnBuilderValue(
                CacheFactoryConstants.GLOBAL_JMX_STATISTICS__CACHE_MANAGER_NAME, String.class, false);

        if (jmxStatisticsCacheManagerName == null) {
            // The cache manager name must be unique even if the jmx is not enabled in configuration. Therefore the
            // default is the service PID that is unique for sure.
            jmxStatisticsCacheManagerName = servicePID;
            builderHelper.applyValue(CacheFactoryConstants.GLOBAL_JMX_STATISTICS__CACHE_MANAGER_NAME,
                    jmxStatisticsCacheManagerName, String.class);
        }

        builderHelper.applyConfigOnBuilderValue(CacheFactoryConstants.GLOBAL_JMX_STATISTICS__JMX_DOMAIN, String.class,
                false);

        builderHelper.applyConfigOnBuilderValue(CacheFactoryConstants.GLOBAL_JMX_STATISTICS__ALLOW_DUPLICATE_DOMAINS,
                Boolean.class, false);

        builderHelper.applyConfigOnBuilderValue(CacheFactoryConstants.GLOBAL_JMX_STATISTICS__ENABLED,
                boolean.class, false);

        builderHelper.applyConfigOnBuilderValue(CacheFactoryConstants.SITE__LOCAL_SITE, String.class, false);

        GlobalConfiguration globalConfig = builder.build();

        if (clustered) {
            transferValueToServiceProperties(CacheFactoryConstants.TRANSPORT__CLUSTER_NAME, globalConfig,
                    serviceProperties);

            transferValueToServiceProperties(CacheFactoryConstants.TRANSPORT__DISTRIBUTED_SYNC_TIMEOUT, globalConfig,
                    serviceProperties);

            transferValueToServiceProperties(CacheFactoryConstants.TRANSPORT__MACHINE_ID, globalConfig,
                    serviceProperties);
            transferValueToServiceProperties(CacheFactoryConstants.TRANSPORT__NODE_NAME, globalConfig,
                    serviceProperties);
            transferValueToServiceProperties(CacheFactoryConstants.TRANSPORT__RACK_ID, globalConfig, serviceProperties);
            transferValueToServiceProperties(CacheFactoryConstants.TRANSPORT__SITE_ID, globalConfig, serviceProperties);

            String jgroupsXML = globalConfig.transport().properties().getProperty("configurationXml");
            if (jgroupsXML != null) {
                serviceProperties.put(CacheFactoryConstants.TRANSPORT__CONFIGURATION_XML, jgroupsXML);
            }
        }

        transferValueToServiceProperties(CacheFactoryConstants.GLOBAL_JMX_STATISTICS__ENABLED, globalConfig,
                serviceProperties);

        serviceProperties.put(CacheFactoryConstants.GLOBAL_JMX_STATISTICS__CACHE_MANAGER_NAME,
                jmxStatisticsCacheManagerName);

        transferValueToServiceProperties("globalJmxStatistics.domain", globalConfig, serviceProperties);

        transferValueToServiceProperties(CacheFactoryConstants.GLOBAL_JMX_STATISTICS__ALLOW_DUPLICATE_DOMAINS,
                globalConfig, serviceProperties);

        transferValueToServiceProperties("sites.localSite", globalConfig, serviceProperties);

        Object serviceDescription = config.get(Constants.SERVICE_DESCRIPTION);
        if (serviceDescription != null) {
            serviceProperties.put(Constants.SERVICE_DESCRIPTION, serviceDescription);
        }

        manager = new DefaultCacheManager(globalConfig);
        manager.start();

        serviceRegistration = context.registerService(new String[] {}, manager, serviceProperties);
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
