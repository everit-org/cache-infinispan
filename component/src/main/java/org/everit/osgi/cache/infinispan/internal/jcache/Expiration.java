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

import org.everit.osgi.cache.infinispan.internal.jcache.logging.Log;
import org.infinispan.util.logging.LogFactory;

import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;

/**
 * Utility class for expiration calculations.
 *
 * @author Galder ZamarreÃ±o
 * @since 6.0
 */
public class Expiration {

   private static final Log log =
         LogFactory.getLog(Expiration.class, Log.class);

   // Suppresses default constructor, ensuring non-instantiability.
   private Expiration(){
   }

   /**
    * Return expiry for a given cache operation. It returns null when the
    * expiry time cannot be determined, in which case clients should not update
    * expiry settings for the cached entry.
    */
   public static Duration getExpiry(ExpiryPolicy policy, Operation op) {
      switch (op) {
         case CREATION:
            try {
               return policy.getExpiryForCreation();
            } catch (Throwable t) {
               return getDefaultDuration();
            }
         case ACCESS:
            try {
               return policy.getExpiryForAccess();
            } catch (Throwable t) {
               // If an exception is thrown, leave expiration untouched
               return null;
            }
         case UPDATE:
            try {
               return policy.getExpiryForUpdate();
            } catch (Exception e) {
               // If an exception is thrown, leave expiration untouched
               return null;
            }
         default:
            throw log.unknownExpiryOperation(op.toString());
      }
   }

   public static Duration getDefaultDuration() {
      return Duration.ETERNAL;
   }

   public enum Operation {
      CREATION, ACCESS, UPDATE
   }

}
