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

import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.PropertyOption;
import org.apache.felix.scr.annotations.Reference;
import org.everit.osgi.cache.CacheConfiguration;
import org.everit.osgi.cache.infinispan.CacheConfigurationProps;
import org.everit.osgi.cache.infinispan.ISPNCacheConfiguration;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.VersioningScheme;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.eviction.EvictionThreadPolicy;
import org.infinispan.transaction.LockingMode;
import org.infinispan.transaction.TransactionMode;
import org.infinispan.transaction.TransactionProtocol;
import org.infinispan.transaction.lookup.TransactionManagerLookup;
import org.infinispan.transaction.lookup.TransactionSynchronizationRegistryLookup;
import org.infinispan.util.concurrent.IsolationLevel;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentException;

@Component(name = CacheConfigurationProps.SERVICE_FACTORYPID_CACHE_CONFIGURATION, configurationFactory = true,
        immediate = true, policy = ConfigurationPolicy.REQUIRE, metatype = true)
@Properties({
        @Property(name = CacheConfigurationProps.CACHE_NAME),
        @Property(name = CacheConfigurationProps.EVICTION__MAX_ENTRIES, intValue = -1),
        @Property(name = CacheConfigurationProps.EVICTION__STRATEGY, options = {
                @PropertyOption(name = CacheConfigurationProps.EVICTION__STRATEGY_OPT_NONE,
                        value = CacheConfigurationProps.EVICTION__STRATEGY_OPT_NONE),
                @PropertyOption(name = CacheConfigurationProps.EVICTION__STRATEGY_OPT_UNORDERED,
                        value = CacheConfigurationProps.EVICTION__STRATEGY_OPT_UNORDERED),
                @PropertyOption(name = CacheConfigurationProps.EVICTION__STRATEGY_OPT_LRU,
                        value = CacheConfigurationProps.EVICTION__STRATEGY_OPT_LRU),
                @PropertyOption(name = CacheConfigurationProps.EVICTION__STRATEGY_OPT_LIRS,
                        value = CacheConfigurationProps.EVICTION__STRATEGY_OPT_LIRS) }),
        @Property(name = CacheConfigurationProps.EVICTION__THREAD_POLICY, options = {
                @PropertyOption(name = CacheConfigurationProps.EVICTION__THREAD_POLICY_OPT_DEFAULT,
                        value = CacheConfigurationProps.EVICTION__THREAD_POLICY_OPT_DEFAULT),
                @PropertyOption(name = CacheConfigurationProps.EVICTION__THREAD_POLICY_OPT_PIGGYBACK,
                        value = CacheConfigurationProps.EVICTION__THREAD_POLICY_OPT_PIGGYBACK) }),
        @Property(name = CacheConfigurationProps.EXPIRATION__LIFESPAN, longValue = -1),
        @Property(name = CacheConfigurationProps.EXPIRATION__MAX_IDLE, longValue = -1),
        @Property(name = CacheConfigurationProps.EXPIRATION__REAPER_ENABLED, boolValue = true),
        @Property(name = CacheConfigurationProps.EXPIRATION__WAKE_UP_INTERVAL, longValue = 60000),
        // TODO support persistence stores @Property(name = CacheProps.PERSISTENCE__PASSIVATION, boolValue = false),
        @Property(name = CacheConfigurationProps.INVOCATION_BATCHING__ENABLE, boolValue = false),
        @Property(name = CacheConfigurationProps.CLUSTERING__CACHE_MODE,
                value = CacheConfigurationProps.CLUSTERING__CACHE_MODE_OPT_LOCAL,
                options = { @PropertyOption(name = CacheConfigurationProps.CLUSTERING__CACHE_MODE_OPT_LOCAL,
                        value = CacheConfigurationProps.CLUSTERING__CACHE_MODE_OPT_LOCAL),
                        @PropertyOption(name = CacheConfigurationProps.CLUSTERING__CACHEMODE_OPT_REPL_SYNC,
                                value = CacheConfigurationProps.CLUSTERING__CACHEMODE_OPT_REPL_SYNC),
                        @PropertyOption(name = CacheConfigurationProps.CLUSTERING__CACHE_MODE_OPT_REPL_ASYNC,
                                value = CacheConfigurationProps.CLUSTERING__CACHE_MODE_OPT_REPL_ASYNC),
                        @PropertyOption(name = CacheConfigurationProps.CLUSTERING__CACHE_MODE_OPT_INVALIDATION_SYNC,
                                value = CacheConfigurationProps.CLUSTERING__CACHE_MODE_OPT_INVALIDATION_SYNC),
                        @PropertyOption(name = CacheConfigurationProps.CLUSTERING__CACHE_MODE_OPT_INVALIDATION_ASYNC,
                                value = CacheConfigurationProps.CLUSTERING__CACHE_MODE_OPT_INVALIDATION_ASYNC),
                        @PropertyOption(name = CacheConfigurationProps.CLUSTERING__CACHE_MODE_OPT_DIST_SYNC,
                                value = CacheConfigurationProps.CLUSTERING__CACHE_MODE_OPT_DIST_SYNC),
                        @PropertyOption(name = CacheConfigurationProps.CLUSTERING__CACHE_MODE_OPT_DIST_ASYNC,
                                value = CacheConfigurationProps.CLUSTERING__CACHE_MODE_OPT_DIST_ASYNC) }),
        @Property(name = CacheConfigurationProps.CLUSTERING__ASYNC__ASYNC_MARSHALLING, boolValue = false),
        @Property(name = CacheConfigurationProps.CLUSTERING__ASYNC__USE_REPL_QUEUE, boolValue = false),
        @Property(name = CacheConfigurationProps.CLUSTERING__ASYNC__REPL_QUEUE_INTERVAL, longValue = 5000),
        @Property(name = CacheConfigurationProps.CLUSTERING__ASYNC__REPL_QUEUE_MAX_ELEMENTS, intValue = 1000),
        @Property(name = CacheConfigurationProps.CLUSTERING__HASH__NUM_OWNERS, intValue = 2),
        @Property(name = CacheConfigurationProps.CLUSTERING__HASH__NUM_SEGMENTS, intValue = 60),
        @Property(name = CacheConfigurationProps.CLUSTERING__HASH__CAPACITY_FACTOR, floatValue = 1),
        @Property(name = CacheConfigurationProps.CLUSTERING__L1__ENABLED, boolValue = false),
        @Property(name = CacheConfigurationProps.CLUSTERING__L1__INVALIDATION_TRESHOLD, intValue = 0),
        @Property(name = CacheConfigurationProps.CLUSTERING__L1__LIFESPAN, longValue = 600000),
        @Property(name = CacheConfigurationProps.CLUSTERING__L1__ON_REHASH, boolValue = true),
        @Property(name = CacheConfigurationProps.CLUSTERING__L1__CLEANUP_TASK_FREQUENCY, longValue = 600000),
        @Property(name = CacheConfigurationProps.CLUSTERING__STATE_TRANSFER__FETCH_IN_MEMORY_STATE,
                value = CacheConfigurationProps.COMMON__BOOLEAN_OPT_DEFAULT,
                options = { @PropertyOption(name = CacheConfigurationProps.COMMON__BOOLEAN_OPT_DEFAULT,
                        value = CacheConfigurationProps.COMMON__BOOLEAN_OPT_DEFAULT),
                        @PropertyOption(name = CacheConfigurationProps.COMMON__BOOLEAN_OPT_FALSE,
                                value = CacheConfigurationProps.COMMON__BOOLEAN_OPT_FALSE),
                        @PropertyOption(name = CacheConfigurationProps.COMMON__BOOLEAN_OPT_TRUE,
                                value = CacheConfigurationProps.COMMON__BOOLEAN_OPT_TRUE) }),
        @Property(name = CacheConfigurationProps.CLUSTERING__STATE_TRANSFER__AWAIT_INITIAL_TRANSFER,
                value = CacheConfigurationProps.COMMON__BOOLEAN_OPT_DEFAULT,
                options = { @PropertyOption(name = CacheConfigurationProps.COMMON__BOOLEAN_OPT_DEFAULT,
                        value = CacheConfigurationProps.COMMON__BOOLEAN_OPT_DEFAULT),
                        @PropertyOption(name = CacheConfigurationProps.COMMON__BOOLEAN_OPT_FALSE,
                                value = CacheConfigurationProps.COMMON__BOOLEAN_OPT_FALSE),
                        @PropertyOption(name = CacheConfigurationProps.COMMON__BOOLEAN_OPT_TRUE,
                                value = CacheConfigurationProps.COMMON__BOOLEAN_OPT_TRUE) }),
        @Property(name = CacheConfigurationProps.CLUSTERING__STATE_TRANSFER__CHUNK_SIZE, intValue = 10000),
        @Property(name = CacheConfigurationProps.CLUSTERING__STATE_TRANSFER__TIMEOUT, longValue = 4 * 60 * 1000),
        @Property(name = CacheConfigurationProps.CLUSTERING__SYNC__REPL_TIMEOUT, longValue = 15000),
        @Property(name = CacheConfigurationProps.LOCKING__CONCURRENCY_LEVEL, intValue = 32),
        @Property(name = CacheConfigurationProps.LOCKING__ISOLATION_LEVEL, options = {
                @PropertyOption(name = CacheConfigurationProps.LOCKING__ISOLATION_LEVEL_OPT_READ_COMMITTED,
                        value = CacheConfigurationProps.LOCKING__ISOLATION_LEVEL_OPT_READ_COMMITTED),
                @PropertyOption(name = CacheConfigurationProps.LOCKING__ISOLATION_LEVEL_OPT_REPEATABLE_READ,
                        value = CacheConfigurationProps.LOCKING__ISOLATION_LEVEL_OPT_REPEATABLE_READ) }),
        @Property(name = CacheConfigurationProps.LOCKING__LOCK_ACQUISITION_TIMEOUT, longValue = 10000),
        @Property(name = CacheConfigurationProps.LOCKING__USE_LOCK_STRIPING, boolValue = false),
        @Property(name = CacheConfigurationProps.LOCKING_WRITE_SKEW_CHECK, boolValue = false),
        @Property(name = CacheConfigurationProps.DEADLOCK_DETECTION__ENABLED, boolValue = false),
        @Property(name = CacheConfigurationProps.DEADLOCKDETECTION__SPIN_DURATION, longValue = 100),
        @Property(name = CacheConfigurationProps.TRANSACTION__TRANSACTION_MODE,
                value = CacheConfigurationProps.TRANSACTION__TRANSACTION_MODE_OPT_DEFAULT, options = { @PropertyOption(
                        name = CacheConfigurationProps.TRANSACTION__TRANSACTION_MODE_OPT_DEFAULT,
                        value = CacheConfigurationProps.TRANSACTION__TRANSACTION_MODE_OPT_DEFAULT),
                        @PropertyOption(
                                name = CacheConfigurationProps.TRANSACTION__TRANSACTION_MODE_OPT_NON_TRANSACTIONAL,
                                value = CacheConfigurationProps.TRANSACTION__TRANSACTION_MODE_OPT_NON_TRANSACTIONAL),
                        @PropertyOption(
                                name = CacheConfigurationProps.TRANSACTION__TRANSACTION_MODE_OPT_TRANSACTIONAL,
                                value = CacheConfigurationProps.TRANSACTION__TRANSACTION_MODE_OPT_TRANSACTIONAL) }),
        @Property(name = CacheConfigurationProps.TRANSACTION__AUTO_COMMIT, boolValue = true),
        @Property(name = CacheConfigurationProps.TRANSACTION__CACHE_STOP_TIMEOUT, longValue = 30000),
        @Property(name = CacheConfigurationProps.TRANSACTION__LOCKING_MODE,
                value = CacheConfigurationProps.TRANSACTION__LOCKING_MODE_OPT_OPTIMISTIC, options = { @PropertyOption(
                        name = CacheConfigurationProps.TRANSACTION__LOCKING_MODE_OPT_OPTIMISTIC,
                        value = CacheConfigurationProps.TRANSACTION__LOCKING_MODE_OPT_OPTIMISTIC), @PropertyOption(
                        name = CacheConfigurationProps.TRANSACTION__LOCKING_MODE_OPT_PESSIMISTIC,
                        value = CacheConfigurationProps.TRANSACTION__LOCKING_MODE_OPT_PESSIMISTIC) }),
        @Property(name = CacheConfigurationProps.TRANSACTION__SYNC_COMMIT_PHASE, boolValue = true),
        @Property(name = CacheConfigurationProps.TRANSACTION__SYNC_ROLLBACK_PHASE, boolValue = false),
        @Property(name = CacheConfigurationProps.TRANSACTION__USE_SYNCHRONIZATION, boolValue = true),
        @Property(name = CacheConfigurationProps.TRANSACTION__RECOVERY__ENABLED, boolValue = true),
        // TODO be careful as getting a the recovery config builder automatically enables
        @Property(name = CacheConfigurationProps.TRANSACTION__RECOVERY__RECOVERY_INFO_CACHE_NAME),
        @Property(name = CacheConfigurationProps.TRANSACTION__USE_1PC_FOR_AUTO_COMMIT_TRANSACTIONS, boolValue = false),
        @Property(name = CacheConfigurationProps.TRANSACTION__REAPER_WAKE_UP_INTERVAL, longValue = 1000),
        @Property(name = CacheConfigurationProps.TRANSACTION__COMPLETED_TX_TIMEOUT, longValue = 15000),
        @Property(name = CacheConfigurationProps.TRANSACTION__TRANSACTION_PROTOCOL,
                value = CacheConfigurationProps.TRANSACTION__TRANSACTION_PROTOCOL_OPT_DEFAULT, options = {
                        @PropertyOption(
                                name = CacheConfigurationProps.TRANSACTION__TRANSACTION_PROTOCOL_OPT_DEFAULT,
                                value = CacheConfigurationProps.TRANSACTION__TRANSACTION_PROTOCOL_OPT_DEFAULT),
                        @PropertyOption(
                                name = CacheConfigurationProps.TRANSACTION__TRANSACTION_PROTOCOL_OPT_TOTAL_ORDER,
                                value = CacheConfigurationProps.TRANSACTION__TRANSACTION_PROTOCOL_OPT_TOTAL_ORDER) }),
        @Property(name = CacheConfigurationProps.VERSIONING__ENABLED, boolValue = false),
        @Property(name = CacheConfigurationProps.VERSIONING__SCHEME,
                value = CacheConfigurationProps.VERSIONING__SCHEME_OPT_NONE,
                options = { @PropertyOption(name = CacheConfigurationProps.VERSIONING__SCHEME_OPT_NONE,
                        value = CacheConfigurationProps.VERSIONING__SCHEME_OPT_NONE),
                        @PropertyOption(name = CacheConfigurationProps.VERSIONING__SCHEME_OPT_SIMPLE,
                                value = CacheConfigurationProps.VERSIONING__SCHEME_OPT_SIMPLE) }),
        // TODO support sites configuration
        // TODO support compatibility mode configuration
        @Property(name = CacheConfigurationProps.JMX_STATISTICS__ENABLED, boolValue = false),
        @Property(name = CacheConfigurationProps.TRANSACTION__TRANSACTION_MANAGER__TARGET),
        @Property(name = CacheConfigurationProps.TRANSACTION__TRANSACTION_SYNCHRONIZATION_REGISTRY__TARGET),
        @Property(name = Constants.SERVICE_DESCRIPTION)
})
public class CacheConfigurationComponent<K, V> implements ISPNCacheConfiguration<K, V> {

    private ServiceRegistration<?> serviceRegistration = null;

    private Configuration configuration = null;

    private String cacheName = null;

    @Reference
    private TransactionManager transactionManager;

    @Reference
    private TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    @Activate
    public void activate(final BundleContext context, final Map<String, Object> componentConfiguration) {
        Map<String, Object> serviceProperties = new HashMap<String, Object>();
        serviceProperties.put(Constants.SERVICE_PID, componentConfiguration.get(Constants.SERVICE_PID));

        ConfigurationBuilder builder = new ConfigurationBuilder();
        ReflectiveConfigurationBuilderHelper h = new ReflectiveConfigurationBuilderHelper(componentConfiguration,
                builder);

        ReflectiveComponentConfigurationHelper cch = h.getComponentConfigHelper();

        cacheName = cch.getPropValue(CacheConfigurationProps.CACHE_NAME, String.class, true);
        serviceProperties.put(CacheConfigurationProps.CACHE_NAME, cacheName);

        h.applyConfigOnBuilderValue(CacheConfigurationProps.EVICTION__MAX_ENTRIES, int.class, false);
        h.applyConfigOnBuilderValue(CacheConfigurationProps.EVICTION__STRATEGY, EvictionStrategy.class, false);
        h.applyConfigOnBuilderValue(CacheConfigurationProps.EVICTION__THREAD_POLICY, EvictionThreadPolicy.class, false);
        h.applyConfigOnBuilderValue(CacheConfigurationProps.EXPIRATION__LIFESPAN, long.class, false);
        h.applyConfigOnBuilderValue(CacheConfigurationProps.EXPIRATION__MAX_IDLE, long.class, false);
        h.applyConfigOnBuilderValue(CacheConfigurationProps.EXPIRATION__REAPER_ENABLED, boolean.class, false);
        h.applyConfigOnBuilderValue(CacheConfigurationProps.EXPIRATION__WAKE_UP_INTERVAL, long.class, false);
        h.applyConfigOnBuilderValue(CacheConfigurationProps.INVOCATION_BATCHING__ENABLE, boolean.class, false);
        CacheMode cacheMode = h.applyConfigOnBuilderValue(CacheConfigurationProps.CLUSTERING__CACHE_MODE,
                CacheMode.class, false);
        if ((cacheMode != null) && !CacheMode.LOCAL.equals(cacheMode)) {
            h.applyConfigOnBuilderValue(CacheConfigurationProps.CLUSTERING__ASYNC__ASYNC_MARSHALLING, boolean.class,
                    false);
            h.applyConfigOnBuilderValue(CacheConfigurationProps.CLUSTERING__ASYNC__USE_REPL_QUEUE, boolean.class, false);
            h.applyConfigOnBuilderValue(CacheConfigurationProps.CLUSTERING__ASYNC__REPL_QUEUE_INTERVAL, long.class,
                    false);
            h.applyConfigOnBuilderValue(CacheConfigurationProps.CLUSTERING__ASYNC__REPL_QUEUE_MAX_ELEMENTS, int.class,
                    false);
            h.applyConfigOnBuilderValue(CacheConfigurationProps.CLUSTERING__HASH__NUM_OWNERS, int.class, false);
            h.applyConfigOnBuilderValue(CacheConfigurationProps.CLUSTERING__HASH__NUM_SEGMENTS, int.class, false);
            h.applyConfigOnBuilderValue(CacheConfigurationProps.CLUSTERING__HASH__CAPACITY_FACTOR, Float.class, false);
            Boolean l1Enabled = h.applyConfigOnBuilderValue(CacheConfigurationProps.CLUSTERING__L1__ENABLED,
                    Boolean.class, false);

            if ((l1Enabled != null) && l1Enabled) {
                h.applyConfigOnBuilderValue(CacheConfigurationProps.CLUSTERING__L1__INVALIDATION_TRESHOLD, int.class,
                        false);
                h.applyConfigOnBuilderValue(CacheConfigurationProps.CLUSTERING__L1__LIFESPAN, long.class, false);
                h.applyConfigOnBuilderValue(CacheConfigurationProps.CLUSTERING__L1__ON_REHASH, boolean.class, false);
                h.applyConfigOnBuilderValue(CacheConfigurationProps.CLUSTERING__L1__CLEANUP_TASK_FREQUENCY, long.class,
                        false);
            }

            applyNullableBoolean(CacheConfigurationProps.CLUSTERING__STATE_TRANSFER__FETCH_IN_MEMORY_STATE, h);
            applyNullableBoolean(CacheConfigurationProps.CLUSTERING__STATE_TRANSFER__AWAIT_INITIAL_TRANSFER, h);

            h.applyConfigOnBuilderValue(CacheConfigurationProps.CLUSTERING__STATE_TRANSFER__CHUNK_SIZE, int.class,
                    false);
            h.applyConfigOnBuilderValue(CacheConfigurationProps.CLUSTERING__STATE_TRANSFER__TIMEOUT, long.class, false);
            h.applyConfigOnBuilderValue(CacheConfigurationProps.CLUSTERING__SYNC__REPL_TIMEOUT, long.class, false);
        }
        h.applyConfigOnBuilderValue(CacheConfigurationProps.LOCKING__CONCURRENCY_LEVEL, int.class, false);
        h.applyConfigOnBuilderValue(CacheConfigurationProps.LOCKING__ISOLATION_LEVEL, IsolationLevel.class, false);
        h.applyConfigOnBuilderValue(CacheConfigurationProps.LOCKING__LOCK_ACQUISITION_TIMEOUT, long.class, false);
        h.applyConfigOnBuilderValue(CacheConfigurationProps.LOCKING__USE_LOCK_STRIPING, boolean.class, false);
        h.applyConfigOnBuilderValue(CacheConfigurationProps.LOCKING_WRITE_SKEW_CHECK, boolean.class, false);
        Boolean deadlockDetectionEnabled = h.applyConfigOnBuilderValue(
                CacheConfigurationProps.DEADLOCK_DETECTION__ENABLED,
                boolean.class, false);
        if ((deadlockDetectionEnabled != null) && deadlockDetectionEnabled) {
            h.applyConfigOnBuilderValue(CacheConfigurationProps.DEADLOCKDETECTION__SPIN_DURATION, long.class, false);
        }

        String transactionModeString = cch.getPropValue(CacheConfigurationProps.TRANSACTION__TRANSACTION_MODE,
                String.class, false);
        if ((transactionModeString != null)
                && !CacheConfigurationProps.TRANSACTION__TRANSACTION_MODE_OPT_DEFAULT.equals(transactionModeString)) {
            h.applyConfigOnBuilderValue(CacheConfigurationProps.TRANSACTION__TRANSACTION_MODE, TransactionMode.class,
                    false);
        }
        h.applyConfigOnBuilderValue(CacheConfigurationProps.TRANSACTION__AUTO_COMMIT, boolean.class, false);
        h.applyConfigOnBuilderValue(CacheConfigurationProps.TRANSACTION__CACHE_STOP_TIMEOUT, long.class, false);
        h.applyConfigOnBuilderValue(CacheConfigurationProps.TRANSACTION__LOCKING_MODE, LockingMode.class, false);
        h.applyConfigOnBuilderValue(CacheConfigurationProps.TRANSACTION__SYNC_COMMIT_PHASE, boolean.class, false);
        h.applyConfigOnBuilderValue(CacheConfigurationProps.TRANSACTION__SYNC_ROLLBACK_PHASE, boolean.class, false);
        h.applyConfigOnBuilderValue(CacheConfigurationProps.TRANSACTION__USE_SYNCHRONIZATION, boolean.class, false);
        h.applyConfigOnBuilderValue(CacheConfigurationProps.TRANSACTION__RECOVERY__ENABLED, boolean.class, false);
        h.applyConfigOnBuilderValue(CacheConfigurationProps.TRANSACTION__RECOVERY__RECOVERY_INFO_CACHE_NAME,
                String.class, false);
        h.applyConfigOnBuilderValue(CacheConfigurationProps.TRANSACTION__USE_1PC_FOR_AUTO_COMMIT_TRANSACTIONS,
                boolean.class, false);
        h.applyConfigOnBuilderValue(CacheConfigurationProps.TRANSACTION__REAPER_WAKE_UP_INTERVAL, long.class, false);
        h.applyConfigOnBuilderValue(CacheConfigurationProps.TRANSACTION__COMPLETED_TX_TIMEOUT, long.class, false);
        h.applyConfigOnBuilderValue(CacheConfigurationProps.TRANSACTION__TRANSACTION_PROTOCOL,
                TransactionProtocol.class, false);

        builder.transaction().transactionManagerLookup(new TransactionManagerLookup() {

            @Override
            public TransactionManager getTransactionManager() throws Exception {
                return transactionManager;
            }
        });

        builder.transaction().transactionSynchronizationRegistryLookup(new TransactionSynchronizationRegistryLookup() {

            @Override
            public TransactionSynchronizationRegistry getTransactionSynchronizationRegistry() throws Exception {
                return transactionSynchronizationRegistry;
            }
        });

        Boolean versioning = h.applyConfigOnBuilderValue(CacheConfigurationProps.VERSIONING__ENABLED, boolean.class,
                false);
        if ((versioning != null) && versioning) {
            h.applyConfigOnBuilderValue(CacheConfigurationProps.VERSIONING__SCHEME, VersioningScheme.class, false);
        }
        h.applyConfigOnBuilderValue(CacheConfigurationProps.JMX_STATISTICS__ENABLED, boolean.class, false);

        configuration = builder.build(true);

        fillServiceProperties(serviceProperties);

        serviceRegistration = context.registerService(
                new String[] { CacheConfiguration.class.getName(), ISPNCacheConfiguration.class.getName() }, this,
                new Hashtable<String, Object>(serviceProperties));
    }

    private Boolean applyNullableBoolean(final String key, final ReflectiveConfigurationBuilderHelper helper) {
        ReflectiveComponentConfigurationHelper configHelper = helper.getComponentConfigHelper();
        String propValue = configHelper.getPropValue(key, String.class, false);
        if ((propValue == null) || CacheConfigurationProps.COMMON__BOOLEAN_OPT_DEFAULT.equals(propValue)) {
            return null;
        }
        if (CacheConfigurationProps.COMMON__BOOLEAN_OPT_TRUE.equals(propValue)) {
            helper.applyValue(key, true, boolean.class);
            return true;
        } else if (CacheConfigurationProps.COMMON__BOOLEAN_OPT_FALSE.equals(propValue)) {
            helper.applyValue(key, false, boolean.class);
            return false;
        } else {
            throw new ComponentException("The value '" + propValue + "' is not allowed for configuration property "
                    + key);
        }
    }

    @Deactivate
    public void deactivate() {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
    }

    private void fillServiceProperties(final Map<String, Object> serviceProperties) {
        ReflectiveConfigToServicePropsHelper h = new ReflectiveConfigToServicePropsHelper(configuration,
                serviceProperties);

        h.transferProperty(CacheConfigurationProps.EVICTION__MAX_ENTRIES);
        h.transferProperty(CacheConfigurationProps.EVICTION__STRATEGY);
        h.transferProperty(CacheConfigurationProps.EVICTION__THREAD_POLICY);
        h.transferProperty(CacheConfigurationProps.EXPIRATION__LIFESPAN);
        h.transferProperty(CacheConfigurationProps.EXPIRATION__MAX_IDLE);
        h.transferProperty(CacheConfigurationProps.EXPIRATION__REAPER_ENABLED);
        h.transferProperty(CacheConfigurationProps.EXPIRATION__WAKE_UP_INTERVAL);
        h.transferProperty(CacheConfigurationProps.INVOCATION_BATCHING__ENABLE + "d");
        h.transferProperty(CacheConfigurationProps.CLUSTERING__CACHE_MODE);
        h.transferProperty(CacheConfigurationProps.CLUSTERING__ASYNC__ASYNC_MARSHALLING);
        h.transferProperty(CacheConfigurationProps.CLUSTERING__ASYNC__USE_REPL_QUEUE);
        h.transferProperty(CacheConfigurationProps.CLUSTERING__ASYNC__REPL_QUEUE_INTERVAL);
        h.transferProperty(CacheConfigurationProps.CLUSTERING__ASYNC__REPL_QUEUE_MAX_ELEMENTS);
        h.transferProperty(CacheConfigurationProps.CLUSTERING__HASH__NUM_OWNERS);
        h.transferProperty(CacheConfigurationProps.CLUSTERING__HASH__NUM_SEGMENTS);
        h.transferProperty(CacheConfigurationProps.CLUSTERING__HASH__CAPACITY_FACTOR);
        h.transferProperty(CacheConfigurationProps.CLUSTERING__L1__ENABLED);
        h.transferProperty(CacheConfigurationProps.CLUSTERING__L1__INVALIDATION_TRESHOLD);
        h.transferProperty(CacheConfigurationProps.CLUSTERING__L1__LIFESPAN);
        h.transferProperty(CacheConfigurationProps.CLUSTERING__L1__ON_REHASH);
        h.transferProperty(CacheConfigurationProps.CLUSTERING__L1__CLEANUP_TASK_FREQUENCY);
        h.transferProperty(CacheConfigurationProps.CLUSTERING__STATE_TRANSFER__FETCH_IN_MEMORY_STATE);
        h.transferProperty(CacheConfigurationProps.CLUSTERING__STATE_TRANSFER__AWAIT_INITIAL_TRANSFER);
        h.transferProperty(CacheConfigurationProps.CLUSTERING__STATE_TRANSFER__CHUNK_SIZE);
        h.transferProperty(CacheConfigurationProps.CLUSTERING__STATE_TRANSFER__TIMEOUT);
        h.transferProperty(CacheConfigurationProps.CLUSTERING__SYNC__REPL_TIMEOUT);
        h.transferProperty(CacheConfigurationProps.LOCKING__CONCURRENCY_LEVEL);
        h.transferProperty(CacheConfigurationProps.LOCKING__ISOLATION_LEVEL);
        h.transferProperty(CacheConfigurationProps.LOCKING__LOCK_ACQUISITION_TIMEOUT);
        h.transferProperty(CacheConfigurationProps.LOCKING__USE_LOCK_STRIPING);
        h.transferProperty(CacheConfigurationProps.LOCKING_WRITE_SKEW_CHECK);
        h.transferProperty(CacheConfigurationProps.DEADLOCK_DETECTION__ENABLED);
        h.transferProperty(CacheConfigurationProps.DEADLOCKDETECTION__SPIN_DURATION);

        h.transferProperty(CacheConfigurationProps.TRANSACTION__TRANSACTION_MODE);
        h.transferProperty(CacheConfigurationProps.TRANSACTION__AUTO_COMMIT);
        h.transferProperty(CacheConfigurationProps.TRANSACTION__CACHE_STOP_TIMEOUT);
        h.transferProperty(CacheConfigurationProps.TRANSACTION__LOCKING_MODE);
        h.transferProperty(CacheConfigurationProps.TRANSACTION__SYNC_COMMIT_PHASE);
        h.transferProperty(CacheConfigurationProps.TRANSACTION__SYNC_ROLLBACK_PHASE);
        h.transferProperty(CacheConfigurationProps.TRANSACTION__USE_SYNCHRONIZATION);
        h.transferProperty(CacheConfigurationProps.TRANSACTION__RECOVERY__ENABLED);
        h.transferProperty(CacheConfigurationProps.TRANSACTION__RECOVERY__RECOVERY_INFO_CACHE_NAME);
        h.transferProperty(CacheConfigurationProps.TRANSACTION__USE_1PC_FOR_AUTO_COMMIT_TRANSACTIONS);
        h.transferProperty(CacheConfigurationProps.TRANSACTION__REAPER_WAKE_UP_INTERVAL);
        h.transferProperty(CacheConfigurationProps.TRANSACTION__COMPLETED_TX_TIMEOUT);
        h.transferProperty(CacheConfigurationProps.TRANSACTION__TRANSACTION_PROTOCOL);

        h.transferProperty(CacheConfigurationProps.VERSIONING__ENABLED);
        h.transferProperty(CacheConfigurationProps.VERSIONING__SCHEME);
        h.transferProperty(CacheConfigurationProps.JMX_STATISTICS__ENABLED);
    }

    @Override
    public String getCacheName() {
        return cacheName;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    public void setTransactionManager(final TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void setTransactionSynchronizationRegistry(
            final TransactionSynchronizationRegistry transactionSynchronizationRegistry) {
        this.transactionSynchronizationRegistry = transactionSynchronizationRegistry;
    }
}
