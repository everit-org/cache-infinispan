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

import org.infinispan.commons.CacheListenerException;
import org.infinispan.persistence.spi.PersistenceException;

import javax.cache.CacheException;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.integration.CacheLoaderException;
import javax.cache.integration.CacheWriterException;
import javax.cache.processor.EntryProcessorException;

/**
 * Exception laundering utility class.
 *
 * @author Galder ZamarreÃ±o
 * @since 6.0
 */
public class Exceptions {

   // Suppresses default constructor, ensuring non-instantiability.
   private Exceptions(){
   }

   static RuntimeException launderCacheLoaderException(Exception e) {
      if (!(e instanceof CacheLoaderException)) {
         return new CacheLoaderException("Exception in CacheLoader", e);
      } else {
         return new PersistenceException(e);
      }
   }

   static RuntimeException launderCacheWriterException(Exception e) {
      if (!(e instanceof CacheWriterException)) {
         return new CacheWriterException("Exception in CacheWriter", e);
      } else {
         return new CacheException("Error in CacheWriter", e);
      }
   }

   static RuntimeException launderEntryProcessorException(Exception e) {
      if (!(e instanceof EntryProcessorException)) {
         return new EntryProcessorException(e);
      } else {
         return new CacheException(e);
      }
   }

   static RuntimeException launderCacheListenerException(CacheListenerException e) {
      Throwable cause = e.getCause();

      if (cause instanceof CacheEntryListenerException)
         return (CacheEntryListenerException) cause;

      if (cause instanceof Exception)
         return new CacheEntryListenerException(cause);

      if (cause instanceof Error)
         throw (Error) cause;

      return e;
   }

}
