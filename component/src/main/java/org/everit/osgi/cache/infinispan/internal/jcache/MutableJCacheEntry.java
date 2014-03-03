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

import org.infinispan.AdvancedCache;
import org.infinispan.commons.util.ReflectionUtil;

import javax.cache.processor.MutableEntry;

/**
 * Infinispan implementation of {@link MutableEntry} designed to
 * be passed as parameter to {@link javax.cache.processor.EntryProcessor#process(javax.cache.processor.MutableEntry, Object...)}.
 *
 * @param <K> the type of key maintained by this cache entry
 * @param <V> the type of value maintained by this cache entry
 * @author Galder ZamarreÃ±o
 * @since 5.3
 */
public final class MutableJCacheEntry<K, V> implements MutableEntry<K, V> {

   private final AdvancedCache<K, V> cache;

   private final K key;

   private final V oldValue;

   private V value; // mutable

   private Operation operation;

   public MutableJCacheEntry(AdvancedCache<K, V> cache, K key, V value) {
      this.cache = cache;
      this.key = key;
      this.oldValue = value;
      this.operation = Operation.NONE;
   }

   @Override
   public boolean exists() {
      if (value != null)
         return true;
      else if (!operation.isRemoved())
         return cache.containsKey(key);

      return false;
   }

   @Override
   public void remove() {
      operation = value != null ? Operation.NONE : Operation.REMOVE;
      value = null;
   }

   @Override
   public void setValue(V value) {
      this.value = value;
      operation = Operation.UPDATE;
   }

   @Override
   public K getKey() {
      return key;
   }

   @Override
   public V getValue() {
      if (value != null)
         return value;

      if (!operation.isRemoved()) {

         if (oldValue != null) {
            operation = Operation.ACCESS;
            return oldValue;
         } else {
            // If not updated or removed, and old entry is null, do a read-through
            return cache.get(key);
         }
      }

      return null;
   }

   @Override
   public <T> T unwrap(Class<T> clazz) {
      return ReflectionUtil.unwrap(this, clazz);
   }

   V getNewValue() {
      return value;
   }

   Operation getOperation() {
      return operation;
   }

   public enum Operation {
      NONE, ACCESS, REMOVE, UPDATE;

      boolean isRemoved() {
         return this == REMOVE;
      }
   }

}
