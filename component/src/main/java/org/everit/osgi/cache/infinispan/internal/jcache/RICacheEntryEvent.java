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
package org.everit.osgi.cache.infinispan.internal.jcache;

import org.infinispan.commons.util.ReflectionUtil;

import javax.cache.Cache;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.EventType;

/**
 * The reference implementation of the {@link CacheEntryEvent}.
 * 
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of cached values
 * @author Greg Luck
 * @since 1.0
 */
public class RICacheEntryEvent<K, V> extends CacheEntryEvent<K, V> {

    /** The serialVersionUID */
    private static final long serialVersionUID = 6515030413069752679L;
    
    private K key;
    private V value;
    private V oldValue;
    private boolean oldValueAvailable;

    /**
     * Constructs a cache entry event from a given cache as source
     * (without an old value)
     *
     * @param source the cache that originated the event
     * @param key    the key
     * @param value  the value
     */
    public RICacheEntryEvent(Cache<K, V> source, K key, V value, EventType eventType) {
        super(source, eventType);
        this.key = key;
        this.value = value;
        this.oldValue = null;
        this.oldValueAvailable = false;
    }

    /**
     * Returns the key of the cache entry with the event
     *
     * @return the key
     */
    @Override
    public K getKey() {
        return key;
    }

    /**
     * Returns the value of the cache entry with the event
     *
     * @return the value
     */
    @Override
    public V getValue() {
        return value;
    }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> T unwrap(Class<T> clazz) {
      return ReflectionUtil.unwrap(this, clazz);
   }

   /**
     * Returns the value of the cache entry with the event
     *
     * @return the value
     * @throws UnsupportedOperationException if the old value is not available
     */
    @Override
    public V getOldValue() throws UnsupportedOperationException {
        if (isOldValueAvailable()) {
            return oldValue;
        } else {
            throw new UnsupportedOperationException("Old value is not available for key");
        }
    }

    /**
     * Whether the old value is available
     *
     * @return true if the old value is populated
     */
    @Override
    public boolean isOldValueAvailable() {
        return oldValueAvailable;
    }
}
