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
package org.everit.osgi.cache.infinispan;

import org.infinispan.configuration.cache.CacheMode;

public final class ISPNCacheFactoryConstants {

    public static final String SERVICE_FACTORYPID_CACHE_FACTORY =
            "org.everit.osgi.cache.infinispan.CacheFactory";

    /**
     * Whether or not this cache factory accepts clustered cache configurations. In case this property is false, cache
     * configurations with {@link CacheMode#LOCAL} are accepted only. In case of clustered caches, both local and
     * clustered cache configurations are accepted.
     */
    public static final String CLUSTERED = "clustered";

    /**
     * Defines the name of the cluster. Nodes only connect to clusters sharing the same name.
     */
    public static final String TRANSPORT__CLUSTER_NAME = "transport.clusterName";

    /**
     * The full JGroups configuration XML. Should be pasted in one row.
     */
    public static final String TRANSPORT__CONFIGURATION_XML = "transport.configurationXML";

    /**
     * Timeout for coordinating cluster formation when nodes join or leave the cluster.
     */
    public static final String TRANSPORT__DISTRIBUTED_SYNC_TIMEOUT = "transport.distributedSyncTimeout";

    /**
     * The id of the machine where this node runs. Used for <a
     * href="http://community.jboss.org/wiki/DesigningServerHinting">server hinting</a> .
     */
    public static final String TRANSPORT__MACHINE_ID = "transport.machineId";

    /**
     * Name of the current node. This is a friendly name to make logs, etc. make more sense. Defaults to a combination
     * of host name and a random number (to differentiate multiple nodes on the same host)
     */
    public static final String TRANSPORT__NODE_NAME = "transport.nodeName";

    /**
     * The id of the rack where this node runs. Used for <a
     * href="http://community.jboss.org/wiki/DesigningServerHinting">server hinting</a> .
     */
    public static final String TRANSPORT__RACK_ID = "transport.rackId";

    /**
     * The id of the site where this node runs. Used for <a
     * href="http://community.jboss.org/wiki/DesigningServerHinting">server hinting</a> .
     */
    public static final String TRANSPORT__SITE_ID = "transport.siteId";

    /**
     * Configures whether global statistics are gathered and reported via JMX for all caches under this cache manager.
     */
    public static final String GLOBAL_JMX_STATISTICS__ENABLED = "globalJmxStatistics.enabled";

    /**
     * If JMX statistics are enabled then all 'published' JMX objects will appear under this name. This is optional, if
     * not specified an object name will be created for you by default.
     */
    public static final String GLOBAL_JMX_STATISTICS__JMX_DOMAIN = "globalJmxStatistics.jmxDomain";

    /**
     * If true, multiple cache manager instances could be configured under the same configured JMX domain. Each cache
     * manager will in practice use a different JMX domain that has been calculated based on the configured one by
     * adding an incrementing index to it.
     */
    public static final String GLOBAL_JMX_STATISTICS__ALLOW_DUPLICATE_DOMAINS =
            "globalJmxStatistics.allowDuplicateDomains";

    /**
     * If JMX statistics are enabled, this property represents the name of this cache manager. It offers the possibility
     * for clients to provide a user-defined name to the cache manager which later can be used to identify the cache
     * manager within a JMX based management tool amongst other cache managers that might be running under the same JVM.
     */
    public static final String GLOBAL_JMX_STATISTICS__CACHE_MANAGER_NAME = "globalJmxStatistics.cacheManagerName";

    /**
     * Sets the name of the local site. Must be a valid name from the list of sites defined.
     */
    public static final String SITE__LOCAL_SITE = "site.localSite";

    private ISPNCacheFactoryConstants() {
    }

}
