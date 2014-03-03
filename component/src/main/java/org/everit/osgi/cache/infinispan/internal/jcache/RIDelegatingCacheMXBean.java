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

import javax.cache.Cache;
import javax.cache.management.CacheMXBean;

/**
 * Class to help implementers
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values*
 * @author Yannis Cosmadopoulos
 * @since 1.0
 */
public class RIDelegatingCacheMXBean<K, V> implements CacheMXBean {

   private final Cache<K, V> cache;

   /**
    * Constructor
    * @param cache the cache
    */
   public RIDelegatingCacheMXBean(Cache<K, V> cache) {
      this.cache = cache;
   }

   @Override
   public boolean isManagementEnabled() {
      return cache.getConfiguration().isManagementEnabled();
   }

   @Override
   public boolean isReadThrough() {
      return cache.getConfiguration().isReadThrough();
   }

   @Override
   public boolean isStatisticsEnabled() {
      return cache.getConfiguration().isStatisticsEnabled();
   }

   @Override
   public boolean isStoreByValue() {
      return cache.getConfiguration().isStoreByValue();
   }

   @Override
   public boolean isWriteThrough() {
      return cache.getConfiguration().isWriteThrough();
   }

}
