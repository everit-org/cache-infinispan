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

import org.everit.osgi.cache.infinispan.internal.jcache.logging.Log;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryRemoved;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryVisited;
import org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryModifiedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryRemovedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryVisitedEvent;
import org.infinispan.util.logging.LogFactory;

/**
 * Adapts Infinispan notification mechanism to JSR 107 requirements.
 *
 * @author Vladimir Blagojevic
 * @author Galder ZamarreÃ±o
 * @since 5.3
 */
@Listener
public class JCacheListenerAdapter<K, V> {

   private static final Log log =
         LogFactory.getLog(JCacheListenerAdapter.class, Log.class);

   private static final boolean isTrace = log.isTraceEnabled();

   private final Cache<K, V> cache;
   private final JCacheNotifier<K, V> notifier;

   public JCacheListenerAdapter(Cache<K, V> cache, JCacheNotifier<K, V> notifier) {
      this.cache = cache;
      this.notifier = notifier;
   }

   @CacheEntryCreated
   @SuppressWarnings("unused")
   public void handleCacheEntryCreatedEvent(CacheEntryCreatedEvent<K, V> e) {
      // JCache listeners notified only once, so do it after the event
      if (!e.isPre())
         notifier.notifyEntryCreated(cache, e.getKey(), e.getValue());
   }

   @CacheEntryModified
   @SuppressWarnings("unused")
   public void handleCacheEntryModifiedEvent(CacheEntryModifiedEvent<K, V> e) {
      // JCache listeners notified only once, so do it after the event
      if (!e.isPre() && !e.isCreated())
         notifier.notifyEntryUpdated(cache, e.getKey(), e.getValue());
   }

   @CacheEntryRemoved
   @SuppressWarnings("unused")
   public void handleCacheEntryRemovedEvent(CacheEntryRemovedEvent<K, V> e) {
      // JCache listeners notified only once, so do it after the event
      if (!e.isPre())
         notifier.notifyEntryRemoved(cache, e.getKey(), e.getOldValue());
   }

}
