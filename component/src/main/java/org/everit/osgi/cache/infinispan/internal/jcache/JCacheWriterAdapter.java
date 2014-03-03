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

import org.infinispan.persistence.spi.InitializationContext;
import org.infinispan.marshall.core.MarshalledEntry;

import javax.cache.integration.CacheWriter;

public class JCacheWriterAdapter<K, V> implements org.infinispan.persistence.spi.CacheWriter {

   private CacheWriter<? super K, ? super V> delegate;

   public JCacheWriterAdapter() {
      // Empty constructor required so that it can be instantiated with
      // reflection. This is a limitation of the way the current cache
      // loader configuration works.
   }

   public void setCacheWriter(CacheWriter<? super K, ? super V> delegate) {
      this.delegate = delegate;
   }

   @Override
   public void init(InitializationContext ctx) {
   }

   @Override
   public void write(MarshalledEntry entry) {
      try {
         delegate.write(new JCacheEntry(entry.getKey(), entry.getValue()));
      } catch (Exception e) {
         throw Exceptions.launderCacheWriterException(e);
      }
   }

   @Override
   public boolean delete(Object key) {
      try {
         delegate.delete(key);
      } catch (Exception e) {
         throw Exceptions.launderCacheWriterException(e);
      }
      return false;
   }

   @Override
   public void start() {
   }

   @Override
   public void stop() {
   }

}
