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

public class StateTransfer {

    private Boolean awaitInitialTransfer = null;

    private Integer chunkSize = null;

    private Boolean fetchInMemoryState = null;

    private Long timeout = null;

    public Boolean getAwaitInitialTransfer() {
        return awaitInitialTransfer;
    }

    public Integer getChunkSize() {
        return chunkSize;
    }

    public Boolean getFetchInMemoryState() {
        return fetchInMemoryState;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setAwaitInitialTransfer(final Boolean awaitInitialTransfer) {
        this.awaitInitialTransfer = awaitInitialTransfer;
    }

    public void setChunkSize(final Integer chunkSize) {
        this.chunkSize = chunkSize;
    }

    public void setFetchInMemoryState(final Boolean fetchInMemoryState) {
        this.fetchInMemoryState = fetchInMemoryState;
    }

    public void setTimeout(final Long timeout) {
        this.timeout = timeout;
    }

}
