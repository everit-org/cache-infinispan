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
package org.everit.osgi.cache.infinispan.config;

/**
 * Defines standard names for infinispan cache factory component configuration
 */
public final class CacheConstants {

    /**
     * Option for "PARAM_CACHEMODE". Sets Asynchronous distributed cache mode.
     */
    public static final String CACHEMODE_DIST_ASYNC = "DIST_ASYNC";

    /**
     * Option for "PARAM_CACHEMODE". Sets Synchronous distributed cache mode.
     */
    public static final String CACHEMODE_DIST_SYNC = "DIST_SYNC";
    /**
     * Option for "PARAM_CACHEMODE". Sets Invalidated asynchronous cache mode.
     */
    public static final String CACHEMODE_INVALIDATION_ASYNC = "INV_ASYNC";
    /**
     * Option for "PARAM_CACHEMODE". Sets Invalidated synchronous cache mode.
     */
    public static final String CACHEMODE_INVALIDATION_SYNC = "INV_SYNC";
    /**
     * Option for "PARAM_CACHEMODE". Sets Local cache mode.
     */
    public static final String CACHEMODE_LOCAL = "LOCAL";
    /**
     * Option for "PARAM_CACHEMODE". Sets Asynchronous Data replicated cache mode.
     */
    public static final String CACHEMODE_REPL_ASYNC = "REPL_ASYNC";
    /**
     * Option for "PARAM_CACHEMODE". Sets Synchronous Data replicated cache mode.
     */
    public static final String CACHEMODE_REPL_SYNC = "REPL_SYNC";
    /**
     * Specifies the mode of the Cache to be created.
     */
    public static final String PARAM_CACHEMODE = "cacheMode";
    /**
     * Maximum idle time a cache entry will be maintained in the cache, in milliseconds. If the idle time is exceeded,
     * the entry will be expired cluster-wide. -1 means the entries never expire. Note that this can be overridden on a
     * per-entry basis by using the Cache API. The only acceptable type for this setting is Long.
     */
    public static final String PARAM_MAXIDLE = "maxIdle";

    /**
     * Number of cluster-wide replicas for each cache entry. The only acceptable type for this setting is Integer.
     */
    public static final String PARAM_NUMOWNERS = "numOwners";
    /**
     * Interval (in milliseconds) between subsequent runs to purge expired entries from memory and any cache stores. If
     * you wish to disable the periodic eviction process altogether, set wakeupInterval to -1. The only acceptable type
     * for this setting is Long.
     */
    public static final String PARAM_WAKEUPINTERVAL = "wakeUpInterval";

    public static final String PROP_CC_CACHE_NAME = "cacheName";

    public static final String PROP_CC_MAX_ENTRIES = "maxEntries";

    public static final String PROP_CF_CLUSTERED = "clustered";

    public static final String PROP_CF_TRANSPORT_CLUSTER_NAME = "clusterName";

    public static final String PROP_CF_TRANSPORT_CONFIGURATION_XML = "transport.configurationXML";

    public static final String PROP_CF_TRANSPORT_DISTRIBUTED_SYNC_TIMEOUT = "transport.distributedSyncTimeout";

    public static final String PROP_CF_TRANSPORT_MACHINE_ID = "transport.machineId";

    public static final String PROP_CF_TRANSPORT_NODE_NAME = "transport.nodeName";

    public static final String PROP_CF_TRANSPORT_RACK_ID = "transport.rackId";

    public static final String PROP_CF_TRANSPORT_SITE_ID = "transpotr.siteId";

    private CacheConstants() {
    }
}
