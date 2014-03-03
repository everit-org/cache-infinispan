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
package org.everit.osgi.cache.infinispan.internal.jcache.interceptor;

import org.everit.osgi.cache.infinispan.internal.jcache.JCacheNotifier;
import org.infinispan.commands.read.GetKeyValueCommand;
import org.infinispan.container.DataContainer;
import org.infinispan.container.entries.InternalCacheEntry;
import org.infinispan.context.InvocationContext;
import org.infinispan.interceptors.base.CommandInterceptor;
import org.infinispan.util.TimeService;

import javax.cache.Cache;

/**
 * An interceptor that tracks expiration of entries and notifies JCache
 * {@link javax.cache.event.CacheEntryExpiredListener} instances.
 *
 * This interceptor must be placed before
 * {@link org.infinispan.interceptors.EntryWrappingInterceptor} because this
 * interceptor can result in container entries being removed upon expiration
 * (alongside their metadata).
 *
 * TODO: How to track expired entry in cache stores?
 * TODO: Could this be used as starting point to centrally track expiration?
 * Currently, logic split between data container, cache stores...etc.
 *
 * @author Galder ZamarreÃ±o
 * @since 5.3
 */
public class ExpirationTrackingInterceptor extends CommandInterceptor {

   private final DataContainer container;
   private final Cache<Object, Object> cache;
   private final JCacheNotifier<Object, Object> notifier;
   private final TimeService timeService;

   @SuppressWarnings("unchecked")
   public ExpirationTrackingInterceptor(DataContainer container,
         Cache<?, ?> cache, JCacheNotifier<?, ?> notifier, TimeService timeService) {
      this.container = container;
      this.timeService = timeService;
      this.cache = (Cache<Object, Object>) cache;
      this.notifier = (JCacheNotifier<Object, Object>) notifier;
   }

   @Override
   public Object visitGetKeyValueCommand
         (InvocationContext ctx, GetKeyValueCommand command) throws Throwable {
      Object key = command.getKey();
      InternalCacheEntry entry = container.peek(key);
      if (entry != null && entry.canExpire() && entry.isExpired(timeService.wallClockTime()))
         notifier.notifyEntryExpired(cache, key, entry.getValue());

      return super.visitGetKeyValueCommand(ctx, command);
   }

   // TODO: Implement any other visitX methods?

}