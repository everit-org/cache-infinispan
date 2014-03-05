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

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.PropertyOption;
import org.everit.osgi.cache.api.CacheConfiguration;
import org.everit.osgi.cache.infinispan.config.CacheProps;
import org.everit.osgi.cache.infinispan.config.ISPNCacheConfiguration;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.VersioningScheme;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.eviction.EvictionThreadPolicy;
import org.infinispan.util.concurrent.IsolationLevel;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentException;

@Component(name = "org.everit.osgi.cache.infinispan.ISPNCacheConfiguration", configurationFactory = true,
        immediate = true, policy = ConfigurationPolicy.REQUIRE, metatype = true)
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
        @Property(name = CacheProps.CLUSTERING__L1__ON_REHASH, boolValue = true),
        @Property(name = CacheProps.CLUSTERING__L1__CLEANUP_TASK_FREQUENCY, longValue = 600000),
        @Property(name = CacheProps.CLUSTERING__STATE_TRANSFER__FETCH_IN_MEMORY_STATE,
                value = CacheProps.COMMON__BOOLEAN_OPT_DEFAULT,
                options = { @PropertyOption(name = CacheProps.COMMON__BOOLEAN_OPT_DEFAULT,
                        value = CacheProps.COMMON__BOOLEAN_OPT_DEFAULT),
                        @PropertyOption(name = CacheProps.COMMON__BOOLEAN_OPT_FALSE,
                                value = CacheProps.COMMON__BOOLEAN_OPT_FALSE),
                        @PropertyOption(name = CacheProps.COMMON__BOOLEAN_OPT_TRUE,
                                value = CacheProps.COMMON__BOOLEAN_OPT_TRUE) }),
        @Property(name = CacheProps.CLUSTERING__STATE_TRANSFER__AWAIT_INITIAL_TRANSFER,
                value = CacheProps.COMMON__BOOLEAN_OPT_DEFAULT,
                options = { @PropertyOption(name = CacheProps.COMMON__BOOLEAN_OPT_DEFAULT,
                        value = CacheProps.COMMON__BOOLEAN_OPT_DEFAULT),
                        @PropertyOption(name = CacheProps.COMMON__BOOLEAN_OPT_FALSE,
                                value = CacheProps.COMMON__BOOLEAN_OPT_FALSE),
                        @PropertyOption(name = CacheProps.COMMON__BOOLEAN_OPT_TRUE,
                                value = CacheProps.COMMON__BOOLEAN_OPT_TRUE) }),
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
        @Property(name = CacheProps.VERSIONING__ENABLED, boolValue = false),
        @Property(name = CacheProps.VERSIONING__SCHEME, value = CacheProps.VERSIONING__SCHEME_OPT_NONE,
                options = { @PropertyOption(name = CacheProps.VERSIONING__SCHEME_OPT_NONE,
                        value = CacheProps.VERSIONING__SCHEME_OPT_NONE),
                        @PropertyOption(name = CacheProps.VERSIONING__SCHEME_OPT_SIMPLE,
                                value = CacheProps.VERSIONING__SCHEME_OPT_SIMPLE) }),
        // TODO support sites configuration
        // TODO support compatibility mode configuration
        @Property(name = CacheProps.JMX_STATISTICS__ENABLED, boolValue = false) })
public class ISPNCacheConfigurationComponent<K, V> implements ISPNCacheConfiguration<K, V> {

    private Map<String, Object> configuration = new HashMap<String, Object>();

    private ServiceRegistration<?> serviceRegistration = null;

    @Activate
    public void activate(final BundleContext context, final Map<String, Object> componentConfiguration) {
        System.out.println("ACTIVATE CALLLEDDD");
        ReflectiveComponentConfigTransferHelper transferHelper = new ReflectiveComponentConfigTransferHelper(
                componentConfiguration, configuration);

        System.out.println("CHECKING CACHE NAME");
        transferHelper.transferEntry(CacheProps.CACHE_NAME, String.class, true);
        transferHelper.transferEntry(CacheProps.EVICTION__MAX_ENTRIES, Integer.class, false);
        transferHelper.transferEntry(CacheProps.EVICTION__STRATEGY, EvictionStrategy.class, false);
        transferHelper.transferEntry(CacheProps.EVICTION__THREAD_POLICY, EvictionThreadPolicy.class, false);
        transferHelper.transferEntry(CacheProps.EXPIRATION__LIFESPAN, Long.class, false);
        transferHelper.transferEntry(CacheProps.EXPIRATION__MAX_IDLE, Long.class, false);
        transferHelper.transferEntry(CacheProps.EXPIRATION__REAPER_ENABLED, Boolean.class, false);
        transferHelper.transferEntry(CacheProps.EXPIRATION__WAKE_UP_INTERVAL, Long.class, false);
        transferHelper.transferEntry(CacheProps.INVOCATION_BATCHING__ENABLED, Boolean.class, false);
        CacheMode cacheMode = transferHelper.transferEntry(CacheProps.CLUSTERING__CACHE_MODE, CacheMode.class, false);
        if (cacheMode != null && !CacheMode.LOCAL.equals(cacheMode)) {
            transferHelper.transferEntry(CacheProps.CLUSTERING__ASYNC__ASYNC_MARSHALLING, Boolean.class, false);
            transferHelper.transferEntry(CacheProps.CLUSTERING__ASYNC__USE_REPL_QUEUE, Boolean.class, false);
            transferHelper.transferEntry(CacheProps.CLUSTERING__ASYNC__REPL_QUEUE_INTERVAL, Long.class, false);
            transferHelper.transferEntry(CacheProps.CLUSTERING__ASYNC__REPL_QUEUE_MAX_ELEMENTS, Integer.class, false);
            transferHelper.transferEntry(CacheProps.CLUSTERING__HASH__NUM_OWNERS, Integer.class, false);
            transferHelper.transferEntry(CacheProps.CLUSTERING__HASH__NUM_SEGMENTS, Integer.class, false);
            transferHelper.transferEntry(CacheProps.CLUSTERING__HASH__CAPACITY_FACTOR, Float.class, false);
            Boolean l1Enabled = transferHelper.transferEntry(CacheProps.CLUSTERING__L1__ENABLED, Boolean.class, false);

            if (l1Enabled != null && l1Enabled) {
                transferHelper.transferEntry(CacheProps.CLUSTERING__L1__INVALIDATION_TRESHOLD, Integer.class, false);
                transferHelper.transferEntry(CacheProps.CLUSTERING__L1__LIFESPAN, Long.class, false);
                transferHelper.transferEntry(CacheProps.CLUSTERING__L1__ON_REHASH, Boolean.class, false);
                transferHelper.transferEntry(CacheProps.CLUSTERING__L1__CLEANUP_TASK_FREQUENCY, Long.class, false);
            }

            ReflectiveComponentConfigurationHelper configHelper = transferHelper.getConfigHelper();
            transferNullableBoolean(CacheProps.CLUSTERING__STATE_TRANSFER__FETCH_IN_MEMORY_STATE, configHelper);
            transferNullableBoolean(CacheProps.CLUSTERING__STATE_TRANSFER__AWAIT_INITIAL_TRANSFER, configHelper);

            transferHelper.transferEntry(CacheProps.CLUSTERING__STATE_TRANSFER__CHUNK_SIZE, Integer.class, false);
            transferHelper.transferEntry(CacheProps.CLUSTERING__STATE_TRANSFER__TIMEOUT, Long.class, false);
            transferHelper.transferEntry(CacheProps.CLUSTERING__SYNC__REPL_TIMEOUT, Long.class, false);
        }
        transferHelper.transferEntry(CacheProps.LOCKING__CONCURRENCY_LEVEL, Integer.class, false);
        transferHelper.transferEntry(CacheProps.LOCKING__ISOLATION_LEVEL, IsolationLevel.class, false);
        transferHelper.transferEntry(CacheProps.LOCKING__LOCK_ACQUISITION_TIMEOUT, Long.class, false);
        transferHelper.transferEntry(CacheProps.LOCKING__USE_LOCK_STRIPING, Boolean.class, false);
        transferHelper.transferEntry(CacheProps.LOCKING_WRITE_SKEW_CHECK, Boolean.class, false);
        Boolean deadlockDetectionEnabled = transferHelper.transferEntry(CacheProps.DEADLOCK_DETECTION__ENABLED,
                Boolean.class, false);
        if (deadlockDetectionEnabled != null && deadlockDetectionEnabled) {
            transferHelper.transferEntry(CacheProps.DEADLOCKDETECTION__SPIN_DURATION, Long.class, false);
        }

        Boolean versioning = transferHelper.transferEntry(CacheProps.VERSIONING__ENABLED, Boolean.class, false);
        if (versioning != null && versioning) {
            transferHelper.transferEntry(CacheProps.VERSIONING__SCHEME, VersioningScheme.class, false);
        }
        transferHelper.transferEntry(CacheProps.JMX_STATISTICS__ENABLED, Boolean.class, false);

        serviceRegistration = context.registerService(
                new String[] { CacheConfiguration.class.getName(), ISPNCacheConfiguration.class.getName() }, this,
                new Hashtable<String, Object>(configuration));
    }

    private void transferNullableBoolean(String key, ReflectiveComponentConfigurationHelper helper) {
        String propValue = helper.getPropValue(key, String.class, false);
        if (propValue == null || CacheProps.COMMON__BOOLEAN_OPT_DEFAULT.equals(propValue)) {
            return;
        }
        if (CacheProps.COMMON__BOOLEAN_OPT_TRUE.equals(propValue)) {
            configuration.put(key, true);
        } else if (CacheProps.COMMON__BOOLEAN_OPT_FALSE.equals(propValue)) {
            configuration.put(key, false);
        } else {
            throw new ComponentException("The value '" + propValue + "' is not allowed for configuration property "
                    + key);
        }
    }

    @Override
    public void applyValuesOnBuilder(ConfigurationBuilder builder) {

    }

    @Deactivate
    public void deactivate() {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
    }

}
