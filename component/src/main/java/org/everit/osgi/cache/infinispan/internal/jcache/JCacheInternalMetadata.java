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

import org.infinispan.container.versioning.EntryVersion;
import org.infinispan.metadata.InternalMetadata;

/**
 * Metadata for entries stored via JCache API
 *
 * @author Galder ZamarreÃ±o
 * @since 6.0
 */
public class JCacheInternalMetadata implements InternalMetadata {

   private final long created; // absolute time of creation
   private final long expiry; // absolute time when entry should expire

   public JCacheInternalMetadata(long created, long expiry) {
      this.created = created;
      this.expiry = expiry;
   }

   @Override
   public long created() {
      return created;
   }

   @Override
   public long lastUsed() {
      return 0;
   }

   @Override
   public boolean isExpired(long now) {
      return expiry > -1 && expiry <= now;
   }

   @Override
   public long expiryTime() {
      return expiry;
   }

   @Override
   public long lifespan() {
      return expiry - created;
   }

   @Override
   public long maxIdle() {
      return -1;
   }

   @Override
   public EntryVersion version() {
      return null;
   }

   @Override
   public Builder builder() {
      return null;
   }

}
