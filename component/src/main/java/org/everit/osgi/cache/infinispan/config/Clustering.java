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

import org.infinispan.configuration.cache.CacheMode;

public class Clustering {

    private Async async;

    private CacheMode cacheMode;

    private Hash hash;

    private L1 l1;

    private StateTransfer stateTransfer;

    private Sync sync;

    public Async getAsync() {
        return async;
    }

    public CacheMode getCacheMode() {
        return cacheMode;
    }

    public Hash getHash() {
        return hash;
    }

    public L1 getL1() {
        return l1;
    }

    public StateTransfer getStateTransfer() {
        return stateTransfer;
    }

    public Sync getSync() {
        return sync;
    }

    public void setAsync(final Async async) {
        this.async = async;
    }

    public void setCacheMode(final CacheMode cacheMode) {
        this.cacheMode = cacheMode;
    }

    public void setHash(final Hash hash) {
        this.hash = hash;
    }

    public void setL1(final L1 l1) {
        this.l1 = l1;
    }

    public void setStateTransfer(final StateTransfer stateTransfer) {
        this.stateTransfer = stateTransfer;
    }

    public void setSync(final Sync sync) {
        this.sync = sync;
    }

}
