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

public final class CacheFactoryProps {

    public static final String CLUSTERED = "clustered";

    public static final String TRANSPORT_CLUSTER_NAME = "clusterName";

    public static final String TRANSPORT_CONFIGURATION_XML = "transport.configurationXML";

    public static final String TRANSPORT_DISTRIBUTED_SYNC_TIMEOUT = "transport.distributedSyncTimeout";

    public static final String TRANSPORT_MACHINE_ID = "transport.machineId";

    public static final String TRANSPORT_NODE_NAME = "transport.nodeName";

    public static final String TRANSPORT_RACK_ID = "transport.rackId";

    public static final String TRANSPORT_SITE_ID = "transpotr.siteId";

    private CacheFactoryProps() {
    }

}
