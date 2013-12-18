package org.everit.osgi.cache.infinispan.component;

/*
 * Copyright (c) 2011, Everit Kft.
 *
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.cache.Cache;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.cache.api.CacheFactory;
import org.infinispan.AdvancedCache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.jcache.JCacheManager;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.jgroups.JGroupsTransport;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleWiring;

/**
 * A component that can customize and create Cache instances.
 */
@Component(ds = true, metatype = true, configurationFactory = true, policy = ConfigurationPolicy.REQUIRE,
        immediate = true)
@org.apache.felix.scr.annotations.Properties({
        @Property(name = "clusterName"),
        @Property(name = "multicastAddress"),
        @Property(name = "multicastPort") })
@Service
public class CacheFactoryComponent implements CacheFactory {

    private Properties properties = new Properties();
    private EmbeddedCacheManager manager;
    private JCacheManager jCacheManager;

    @Activate
    public void activate(final BundleContext context, final Map<String, Object> config) {
        modified(config);

        properties
                .setProperty(
                        "jgroupsConfString",
                        String.format(
                                "<config xmlns=\"urn:org:jgroups\"\r\n        xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n        xsi:schemaLocation=\"urn:org:jgroups http://www.jgroups.org/schema/JGroups-3.3.xsd\">\r\n    <UDP\r\n %1$s \r\n %2$s />\r\n    <PING/>\r\n    <MERGE2/>\r\n    <FD_SOCK/>\r\n    <FD_ALL/>\r\n    <VERIFY_SUSPECT/>\r\n    <pbcast.NAKACK2/>\r\n    <UNICAST3/>\r\n    <pbcast.STABLE/>\r\n    <pbcast.GMS/>\r\n    <UFC/>\r\n    <MFC/>\r\n    <FRAG2/>\r\n    <pbcast.STATE_TRANSFER />\r\n    <pbcast.FLUSH timeout=\"0\"/>\r\n</config>",
                                properties.getProperty("mcast_addr"), properties.getProperty("mcast_port")));

        BundleWiring bundleWiring = context.getBundle().adapt(BundleWiring.class);
        ClassLoader componentClassLoader = bundleWiring.getClassLoader();

        GlobalConfigurationBuilder builder = new GlobalConfigurationBuilder();
        builder.classLoader(new MergedClassLoader(new ClassLoader[] { componentClassLoader,
                JGroupsTransport.class.getClassLoader() }));
        builder
                .transport().defaultTransport()
                .addProperty("configurationXml", properties.getProperty("jgroupsConfString"))
                .clusterName(properties.getProperty("clusterName"));

        GlobalConfiguration globalConfig = builder.build();
        manager = new DefaultCacheManager(globalConfig);
        jCacheManager = new JCacheManager(null, manager, null);

    }

    @Override
    public Cache<String, Object> createCache(final int maxEntries, final Map<String, Object> params) {

        String uid = UUID.randomUUID().toString();

        Configuration dcc = manager.getDefaultCacheConfiguration();
        ConfigurationBuilder cb = new ConfigurationBuilder().read(dcc);
        cb.eviction().maxEntries(maxEntries);

        if (params != null) {
            if (params.containsKey(Constants.PARAM_MAXIDLE) && (params.get(Constants.PARAM_MAXIDLE) instanceof Long)) {
                cb.expiration().maxIdle((Long) params.get(Constants.PARAM_MAXIDLE));
            }
            if (params.containsKey(Constants.PARAM_WAKEUPINTERVAL)
                    && (params.get(Constants.PARAM_WAKEUPINTERVAL) instanceof Long)) {
                cb.expiration().wakeUpInterval((Long) params.get(Constants.PARAM_WAKEUPINTERVAL));
            }
            if (params.containsKey(Constants.PARAM_CACHEMODE)
                    && (params.get(Constants.PARAM_CACHEMODE) instanceof String)) {
                String cacheMode = (String) params.get(Constants.PARAM_CACHEMODE);
                CacheMode mode = null;
                if (Constants.CACHEMODE_DIST_SYNC.equals(cacheMode)) {
                    mode = CacheMode.DIST_SYNC;
                    if (params.containsKey(Constants.PARAM_NUMOWNERS)
                            && (params.get(Constants.PARAM_NUMOWNERS) instanceof Integer)) {
                        cb.clustering().hash().numOwners((Integer) params.get(Constants.PARAM_NUMOWNERS));
                    }
                }
                else if (Constants.CACHEMODE_DIST_ASYNC.equals(cacheMode)) {
                    mode = CacheMode.DIST_ASYNC;
                }
                else if (Constants.CACHEMODE_LOCAL.equals(cacheMode)) {
                    mode = CacheMode.LOCAL;
                }
                else if (Constants.CACHEMODE_REPL_ASYNC.equals(cacheMode)) {
                    mode = CacheMode.REPL_ASYNC;
                }
                else if (Constants.CACHEMODE_REPL_SYNC.equals(cacheMode)) {
                    mode = CacheMode.REPL_SYNC;
                }
                else if (Constants.CACHEMODE_INVALIDATION_ASYNC.equals(cacheMode)) {
                    mode = CacheMode.INVALIDATION_ASYNC;
                }
                else if (Constants.CACHEMODE_INVALIDATION_SYNC.equals(cacheMode)) {
                    mode = CacheMode.INVALIDATION_SYNC;
                }

                if (mode != null) {
                    cb.clustering().cacheMode(mode);
                }
            }
        }

        Configuration conf = cb.build();
        manager.defineConfiguration(uid, conf);
        jCacheManager.configureCache(uid, (AdvancedCache<Object, Object>) manager.getCache(uid));

        Cache<String, Object> cache = jCacheManager.getCache(uid);

        return cache;
    }

    /**
     * Method responsible for closing the CacheManager.
     */
    @Deactivate
    public void deactivate() {
        jCacheManager.close();
    }

    /**
     * This method checks the validity of the given configuration and sets them to the properties variable.
     * 
     * @param config
     *            The new configuration of the component.
     */
    @Modified
    public void modified(final Map<String, Object> config) {
        Object clusterName = config.get("clusterName");
        if (clusterName != null) {
            if (!(clusterName instanceof String)) {
                throw new RuntimeException("Expected type for clusterName is String but got "
                        + clusterName.getClass());
            }
            properties.setProperty("clusterName", (String) clusterName);
        }

        Object multicastPort = config.get("multicastPort");
        if (multicastPort != null) {
            if (!(multicastPort instanceof String)) {
                throw new RuntimeException("Expected type for multicastPort is String but got "
                        + multicastPort.getClass());
            }

            int i = Integer.parseInt((String) multicastPort);
            if ((i < 0) || (i > 65535)) {
                throw new RuntimeException(
                        "The given port number in not valid. It is not within the 0-65535 range.");
            }
            properties.setProperty("mcast_port", "mcast_port=" + '"' + multicastPort + '"');
        } else {
            properties.setProperty("mcast_port", "");
        }

        Object multicastAddress = config.get("multicastAddress");
        if (multicastAddress != null) {
            if (multicastAddress instanceof String) {
                String ip = (String) multicastAddress;

                String[] parts = ip.split("\\.");
                if (parts.length != 4) {
                    throw new RuntimeException(
                            "The given IP address in not valid. There are not 4 parts of the address.");
                }

                for (String s : parts) {
                    int i = Integer.parseInt(s);
                    if ((i < 0) || (i > 255)) {
                        throw new RuntimeException(
                                "The given IP address in not valid. One of the parts is not within the 0-255 range.");
                    }
                }

            } else {
                throw new RuntimeException("Expected type for multicastAddress is String but got "
                        + multicastAddress.getClass());
            }

            properties.setProperty("mcast_addr", "mcast_addr=" + '"' + multicastAddress + '"');
        } else {
            properties.setProperty("mcast_addr", "");
        }
    }
}
