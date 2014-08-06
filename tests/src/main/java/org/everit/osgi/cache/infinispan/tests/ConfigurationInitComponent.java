/**
 * This file is part of Everit - Infinispan Cache Tests.
 *
 * Everit - Infinispan Cache Tests is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Infinispan Cache Tests is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Infinispan Cache Tests.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.osgi.cache.infinispan.tests;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.cache.infinispan.config.CacheFactoryProps;
import org.everit.osgi.cache.infinispan.config.CacheConfigurationProps;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

@Component(immediate = true)
@Service(value = ConfigurationInitComponent.class)
public class ConfigurationInitComponent {

    @Reference(bind = "bindConfigAdmin")
    private ConfigurationAdmin configAdmin;

    @Activate
    public void activate(final BundleContext bundleContext) {
        try {
            Dictionary<String, Object> cacheFactoryProps = new Hashtable<String, Object>();
            cacheFactoryProps.put(CacheFactoryProps.CLUSTERED, false);
            cacheFactoryProps.put(CacheFactoryProps.GLOBAL_JMX_STATISTICS__ENABLED, false);
            getOrCreateConfiguration(CacheFactoryProps.SERVICE_FACTORYPID_CACHE_FACTORY, "("
                    + CacheFactoryProps.GLOBAL_JMX_STATISTICS__ENABLED + "=false)",
                    cacheFactoryProps);

            Dictionary<String, Object> cacheFactoryProps2 = new Hashtable<String, Object>();
            cacheFactoryProps2.put(CacheFactoryProps.CLUSTERED, false);
            cacheFactoryProps2.put(CacheFactoryProps.GLOBAL_JMX_STATISTICS__ENABLED, true);
            getOrCreateConfiguration(CacheFactoryProps.SERVICE_FACTORYPID_CACHE_FACTORY, "("
                    + CacheFactoryProps.GLOBAL_JMX_STATISTICS__ENABLED + "=true)",
                    cacheFactoryProps2);

            Dictionary<String, Object> simpleCacheConfigProps = new Hashtable<String, Object>();
            simpleCacheConfigProps.put(CacheConfigurationProps.CACHE_NAME, "simpleCache");
            getOrCreateConfiguration(CacheConfigurationProps.SERVICE_FACTORYPID_CACHE_CONFIGURATION, "(" + CacheConfigurationProps.CACHE_NAME
                    + "=simpleCache)", simpleCacheConfigProps);

            Dictionary<String, Object> transactionalCacheConfigProps = new Hashtable<String, Object>();
            transactionalCacheConfigProps.put(CacheConfigurationProps.CACHE_NAME, "transactionalCache");
            getOrCreateConfiguration(CacheConfigurationProps.SERVICE_FACTORYPID_CACHE_CONFIGURATION, "(" + CacheConfigurationProps.CACHE_NAME
                    + "=transactionalCache)", transactionalCacheConfigProps);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void bindConfigAdmin(final ConfigurationAdmin configAdmin) {
        this.configAdmin = configAdmin;
    }

    private String getOrCreateConfiguration(final String factoryPid, final String filterParam,
            final Dictionary<String, Object> props) throws IOException, InvalidSyntaxException {
        String filter = "(service.factoryPid=" + factoryPid + ")";
        if (filterParam != null) {
            filter = "(&" + filter + filterParam + ")";
        }

        Configuration[] configurations = configAdmin.listConfigurations(filter);
        if (configurations != null && configurations.length > 0) {
            return configurations[0].getFactoryPid();
        }

        Configuration configuration = configAdmin.createFactoryConfiguration(factoryPid, null);
        configuration.update(props);
        return configuration.getPid();
    }
}
