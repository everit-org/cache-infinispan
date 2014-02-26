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

public class Async {

    private Boolean asyncMarshalling = null;

    private Long replicationQueueInterval = null;

    private Integer replicationQueueMaxElements = null;

    private Boolean useReplicationQueue = null;

    public Boolean getAsyncMarshalling() {
        return asyncMarshalling;
    }

    public void setAsyncMarshalling(Boolean asyncMarshalling) {
        this.asyncMarshalling = asyncMarshalling;
    }

    public Long getReplicationQueueInterval() {
        return replicationQueueInterval;
    }

    public void setReplicationQueueInterval(Long replicationQueueInterval) {
        this.replicationQueueInterval = replicationQueueInterval;
    }

    public Integer getReplicationQueueMaxElements() {
        return replicationQueueMaxElements;
    }

    public void setReplicationQueueMaxElements(Integer replicationQueueMaxElements) {
        this.replicationQueueMaxElements = replicationQueueMaxElements;
    }

    public Boolean getUseReplicationQueue() {
        return useReplicationQueue;
    }

    public void setUseReplicationQueue(Boolean useReplicationQueue) {
        this.useReplicationQueue = useReplicationQueue;
    }

}
