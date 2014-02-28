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
package org.everit.osgi.cache.infinispan.internal;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.PropertyOption;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.cache.api.CacheConfiguration;
import org.everit.osgi.cache.infinispan.config.CacheProps;

@Component(configurationFactory = true, immediate = false, policy = ConfigurationPolicy.REQUIRE, metatype = true)
@Properties({
        @Property(name = CacheProps.CACHE_NAME),
        @Property(name = CacheProps.EVICTION__MAX_ENTRIES, intValue = -1),
        @Property(name = CacheProps.EVICTION__STRATEGY, options = {
                @PropertyOption(name = CacheProps.EVICTION__STRATEGY_OPT_NONE,
                        value = CacheProps.EVICTION__STRATEGY_OPT_NONE),
                @PropertyOption(name = CacheProps.EVICTION__STRATEGY_OPT_UNORDERED,
                        value = CacheProps.EVICTION__STRATEGY_OPT_UNORDERED),
                @PropertyOption(name = CacheProps.EVICTION__STRATEGY_OPT_LRU,
                        value = CacheProps.EVICTION__STRATEGY_OPT_LRU),
                @PropertyOption(name = CacheProps.EVICTION__STRATEGY_OPT_LIRS,
                        value = CacheProps.EVICTION__STRATEGY_OPT_LIRS) }),
        @Property(name = CacheProps.EVICTION__THREAD_POLICY, options = {
                @PropertyOption(name = CacheProps.EVICTION__THREAD_POLICY_OPT_DEFAULT,
                        value = CacheProps.EVICTION__THREAD_POLICY_OPT_DEFAULT),
                @PropertyOption(name = CacheProps.EVICTION__THREAD_POLICY_OPT_PIGGYBACK,
                        value = CacheProps.EVICTION__THREAD_POLICY_OPT_PIGGYBACK) }),
        @Property(name = CacheProps.EXPIRATION__LIFESPAN, longValue = -1),
        @Property(name = CacheProps.EXPIRATION__MAX_IDLE, longValue = -1),
        @Property(name = CacheProps.EXPIRATION__REAPER_ENABLED, boolValue = true),
        @Property(name = CacheProps.EXPIRATION__WAKE_UP_INTERVAL, longValue = 60000),
        // TODO support persistence stores @Property(name = CacheProps.PERSISTENCE__PASSIVATION, boolValue = false),
        @Property(name = CacheProps.INVOCATION_BATCHING__ENABLED, boolValue = false),
        @Property(name = CacheProps.CLUSTERING__CACHE_MODE,
                options = { @PropertyOption(name = CacheProps.CLUSTERING__CACHE_MODE_OPT_LOCAL,
                        value = CacheProps.CLUSTERING__CACHE_MODE_OPT_LOCAL),
                        @PropertyOption(name = CacheProps.CLUSTERING__CACHEMODE_OPT_REPL_SYNC,
                                value = CacheProps.CLUSTERING__CACHEMODE_OPT_REPL_SYNC),
                        @PropertyOption(name = CacheProps.CLUSTERING__CACHE_MODE_OPT_REPL_ASYNC,
                                value = CacheProps.CLUSTERING__CACHE_MODE_OPT_REPL_ASYNC),
                        @PropertyOption(name = CacheProps.CLUSTERING__CACHE_MODE_OPT_INVALIDATION_SYNC,
                                value = CacheProps.CLUSTERING__CACHE_MODE_OPT_INVALIDATION_SYNC),
                        @PropertyOption(name = CacheProps.CLUSTERING__CACHE_MODE_OPT_INVALIDATION_ASYNC,
                                value = CacheProps.CLUSTERING__CACHE_MODE_OPT_INVALIDATION_ASYNC),
                        @PropertyOption(name = CacheProps.CLUSTERING__CACHE_MODE_OPT_DIST_SYNC,
                                value = CacheProps.CLUSTERING__CACHE_MODE_OPT_DIST_SYNC),
                        @PropertyOption(name = CacheProps.CLUSTERING__CACHE_MODE_OPT_DIST_ASYNC,
                                value = CacheProps.CLUSTERING__CACHE_MODE_OPT_DIST_ASYNC) }),
        @Property(name = CacheProps.CLUSTERING__ASYNC__ASYNC_MARSHALLING, boolValue = false),
        @Property(name = CacheProps.CLUSTERING__ASYNC__USE_REPL_QUEUE, boolValue = false),
        @Property(name = CacheProps.CLUSTERING__ASYNC__REPL_QUEUE_INTERVAL, longValue = 5000),
        @Property(name = CacheProps.CLUSTERING__ASYNC__REPL_QUEUE_MAX_ELEMENTS, intValue = 1000),
        @Property(name = CacheProps.CLUSTERING__HASH__NUM_OWNERS, intValue = 2),
        @Property(name = CacheProps.CLUSTERING__HASH__NUM_SEGMENTS, intValue = 60),
        @Property(name = CacheProps.CLUSTERING__HASH__CAPACITY_FACTOR, floatValue = 1),
        @Property(name = CacheProps.CLUSTERING__L1__ENABLED, boolValue = false),
        @Property(name = CacheProps.CLUSTERING__L1__INVALIDATION_TRESHOLD, intValue = 0),
        @Property(name = CacheProps.CLUSTERING__L1__LIFESPAN, longValue = 600000),
        // TODO on rehash must be nullable
        @Property(name = CacheProps.CLUSTERING__L1__ON_REHASH, boolValue = true),
        @Property(name = CacheProps.CLUSTERING__L1__CLEANUP_TASK_FREQUENCY, longValue = 600000),
        // TODO must be nullable
        @Property(name = CacheProps.CLUSTERING__STATE_TRANSFER__FETCH_IN_MEMORY_STATE, boolValue = false),
        // TODO must be nullable
        @Property(name = CacheProps.CLUSTERING__STATE_TRANSFER__AWAIT_INITIAL_TRANSFER,
                boolValue = false),
        @Property(name = CacheProps.CLUSTERING__STATE_TRANSFER__CHUNK_SIZE, intValue = 10000),
        @Property(name = CacheProps.CLUSTERING__STATE_TRANSFER__TIMEOUT, longValue = 4 * 60 * 1000),
        @Property(name = CacheProps.CLUSTERING__SYNC__REPL_TIMEOUT, longValue = 15000),
        @Property(name = CacheProps.LOCKING__CONCURRENCY_LEVEL, intValue = 32),
        @Property(name = CacheProps.LOCKING__ISOLATION_LEVEL, options = {
                @PropertyOption(name = CacheProps.LOCKING__ISOLATION_LEVEL_OPT_READ_COMMITTED,
                        value = CacheProps.LOCKING__ISOLATION_LEVEL_OPT_READ_COMMITTED),
                @PropertyOption(name = CacheProps.LOCKING__ISOLATION_LEVEL_OPT_REPEATABLE_READ,
                        value = CacheProps.LOCKING__ISOLATION_LEVEL_OPT_REPEATABLE_READ) }),
        @Property(name = CacheProps.LOCKING__LOCK_ACQUISITION_TIMEOUT, longValue = 10000),
        @Property(name = CacheProps.LOCKING__USE_LOCK_STRIPING, boolValue = false),
        @Property(name = CacheProps.LOCKING_WRITE_SKEW_CHECK, boolValue = false),
        @Property(name = CacheProps.DEADLOCK_DETECTION__ENABLED, boolValue = false),
        @Property(name = CacheProps.DEADLOCKDETECTION__SPIN_DURATION, longValue = 100),
        @Property(name = CacheProps.TRANSACTION__TRANSACTION_MANAGER__TARGET),
        @Property(name = CacheProps.TRANSACTION__TRANSACTION_SYNCHRONIZATION_REGISTRY__TARGET),
        @Property(name = CacheProps.TRANSACTION__AUTO_COMMIT, boolValue = true),
        @Property(name = CacheProps.TRANSACTION__CACHE_STOP_TIMEOUT, longValue = 30000),
        @Property(name = CacheProps.TRANSACTION__LOCKING_MODE,
                value = CacheProps.TRANSACTION__LOCKING_MODE_OPT_OPTIMISTIC, options = { @PropertyOption(
                        name = CacheProps.TRANSACTION__LOCKING_MODE_OPT_OPTIMISTIC,
                        value = CacheProps.TRANSACTION__LOCKING_MODE_OPT_OPTIMISTIC), @PropertyOption(
                        name = CacheProps.TRANSACTION__LOCKING_MODE_OPT_PESSIMISTIC,
                        value = CacheProps.TRANSACTION__LOCKING_MODE_OPT_PESSIMISTIC) }),
        @Property(name = CacheProps.TRANSACTION__SYNC_COMMIT_PHASE, boolValue = true),
        @Property(name = CacheProps.TRANSACTION__SYNC_ROLLBACK_PHASE, boolValue = false),
        @Property(name = CacheProps.TRANSACTION__USE_SYNCHRONIZATION, boolValue = true),
        @Property(name = CacheProps.TRANSACTION__RECOVERY__ENABLED, boolValue = true),
        // TODO be careful as getting a the recovery config builder automatically enables
        @Property(name = CacheProps.TRANSACTION__RECOVERY__RECOVERY_INFO_CACHE_NAME),
        @Property(name = CacheProps.TRANSACTION__USE_1PC_FOR_AUTO_COMMIT_TRANSACTIONS, boolValue = false),
        @Property(name = CacheProps.TRANSACTION__REAPER_WAKE_UP_INTERVAL, longValue = 1000),
        @Property(name = CacheProps.TRANSACTION__COMPLETED_TX_TIMEOUT, longValue = 15000),
        @Property(name = CacheProps.TRANSACTION__TRANSACTION_PROTOCOL,
                value = CacheProps.TRANSACTION__TRANSACTION_PROTOCOL_OPT_DEFAULT, options = { @PropertyOption(
                        name = CacheProps.TRANSACTION__TRANSACTION_PROTOCOL_OPT_DEFAULT,
                        value = CacheProps.TRANSACTION__TRANSACTION_PROTOCOL_OPT_DEFAULT), @PropertyOption(
                        name = CacheProps.TRANSACTION__TRANSACTION_PROTOCOL_OPT_TOTAL_ORDER,
                        value = CacheProps.TRANSACTION__TRANSACTION_PROTOCOL_OPT_TOTAL_ORDER) }),
        @Property(name = CacheProps.VERSIONING__ENABLED, boolValue = false),
        @Property(name = CacheProps.VERSIONING__SCHEME, value = CacheProps.VERSIONING__SCHEME_OPT_NONE,
                options = { @PropertyOption(name = CacheProps.VERSIONING__SCHEME_OPT_NONE,
                        value = CacheProps.VERSIONING__SCHEME_OPT_NONE),
                        @PropertyOption(name = CacheProps.VERSIONING__SCHEME_OPT_SIMPLE,
                                value = CacheProps.VERSIONING__SCHEME_OPT_SIMPLE) }),
        @Property(name = CacheProps.VERSIONING__SCHEME_OPT_SIMPLE, boolValue = false),
        // TODO support sites configuration
        // TODO support compatibility mode configuration
        @Property(name = CacheProps.JMX_STATISTICS__ENABLED, boolValue = false) })
@Service
public class ISPNTransactionalCacheConfigurationComponent<K, V> implements CacheConfiguration<K, V> {

}
