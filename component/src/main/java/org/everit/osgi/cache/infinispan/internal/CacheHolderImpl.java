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

import java.util.concurrent.ConcurrentMap;

import org.everit.osgi.cache.api.CacheHolder;
import org.infinispan.AdvancedCache;

public class CacheHolderImpl<K, V> implements CacheHolder<K, V> {

    private final AdvancedCache<K, V> embeddedCache;

    private CacheFactoryComponent cacheFactory;

    public CacheHolderImpl(final AdvancedCache<K, V> embeddedCache, final CacheFactoryComponent cacheFactory) {
        this.embeddedCache = embeddedCache;
        this.cacheFactory = cacheFactory;
    }

    @Override
    public void close() {
        embeddedCache.stop();
        cacheFactory.cacheClosed(embeddedCache.getName());
    }

    @Override
    public ConcurrentMap<K, V> getCache() {
        return embeddedCache;
    }

}
