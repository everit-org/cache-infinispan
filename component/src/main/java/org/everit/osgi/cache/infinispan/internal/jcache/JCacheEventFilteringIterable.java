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

import java.util.Iterator;

import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryEventFilter;

/**
 * An adapter to provide {@link Iterable}s over Cache Entries, those of which
 * are filtered using a {@link CacheEntryEventFilter}.
 * 
 * @author Galder ZamarreÃ±o
 * @param <K> the type of keys
 * @param <V> the type of values
 * @see Class based on the JSR-107 reference implementation (RI) of
 * {@link Iterable<CacheEntryEvent<? extends K, ? extends V>>}
 */
public class JCacheEventFilteringIterable<K, V>
      implements Iterable<CacheEntryEvent<? extends K, ? extends V>> {

    /**
     * The underlying {@link Iterable} to filter.
     */
    private Iterable<CacheEntryEvent<? extends K, ? extends V>> iterable;
    
    /**
     * The filter to apply to entries in the produced {@link Iterator}s.
     */
    private CacheEntryEventFilter<? super K, ? super V> filter;
    
    /**
     * Constructs an {@link JCacheEventFilteringIterable}.
     * 
     * @param iterable the underlying iterable to filter
     * @param filter   the filter to apply to entries in the iterable
     */
    public JCacheEventFilteringIterable(
          Iterable<CacheEntryEvent<? extends K, ? extends V>> iterable,
          CacheEntryEventFilter<? super K, ? super V> filter) {
        this.iterable = iterable;
        this.filter = filter;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<CacheEntryEvent<? extends K, ? extends V>> iterator() {
        return new JCacheEventFilteringIterator<K, V>(
              iterable.iterator(), filter);
    }

}
