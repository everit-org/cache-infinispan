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

import org.everit.osgi.cache.api.CacheConfiguration;

public class ISPNCacheConfiguration<K, V> implements CacheConfiguration<K, V> {

    private String cacheName = null;

    private Clustering clustering = null;

    private DeadlockDetection deadlockDetection = null;

    public String getCacheName() {
        return cacheName;
    }

    public Clustering getClustering() {
        return clustering;
    }

    public DeadlockDetection getDeadlockDetection() {
        return deadlockDetection;
    }

    public void setCacheName(final String cacheName) {
        this.cacheName = cacheName;
    }

    public void setClustering(final Clustering clustering) {
        this.clustering = clustering;
    }

    public void setDeadlockDetection(final DeadlockDetection deadlockDetection) {
        this.deadlockDetection = deadlockDetection;
    }

}
