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
import org.everit.osgi.cache.infinispan.config.CacheConfigurationConstants;
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

@Component(name = CacheConfigurationConstants.SERVICE_FACTORYPID_CACHE_CONFIGURATION, configurationFactory = true,
        immediate = true, policy = ConfigurationPolicy.REQUIRE, metatype = true)
@Properties({
        @Property(name = CacheConfigurationConstants.CACHE_NAME),
        @Property(name = CacheConfigurationConstants.EVICTION__MAX_ENTRIES, intValue = -1),
        @Property(name = CacheConfigurationConstants.EVICTION__STRATEGY, options = {
                @PropertyOption(name = CacheConfigurationConstants.EVICTION__STRATEGY_OPT_NONE,
                        value = CacheConfigurationConstants.EVICTION__STRATEGY_OPT_NONE),
                @PropertyOption(name = CacheConfigurationConstants.EVICTION__STRATEGY_OPT_UNORDERED,
                        value = CacheConfigurationConstants.EVICTION__STRATEGY_OPT_UNORDERED),
                @PropertyOption(name = CacheConfigurationConstants.EVICTION__STRATEGY_OPT_LRU,
                        value = CacheConfigurationConstants.EVICTION__STRATEGY_OPT_LRU),
                @PropertyOption(name = CacheConfigurationConstants.EVICTION__STRATEGY_OPT_LIRS,
                        value = CacheConfigurationConstants.EVICTION__STRATEGY_OPT_LIRS) }),
        @Property(name = CacheConfigurationConstants.EVICTION__THREAD_POLICY, options = {
                @PropertyOption(name = CacheConfigurationConstants.EVICTION__THREAD_POLICY_OPT_DEFAULT,
                        value = CacheConfigurationConstants.EVICTION__THREAD_POLICY_OPT_DEFAULT),
                @PropertyOption(name = CacheConfigurationConstants.EVICTION__THREAD_POLICY_OPT_PIGGYBACK,
                        value = CacheConfigurationConstants.EVICTION__THREAD_POLICY_OPT_PIGGYBACK) }),
        @Property(name = CacheConfigurationConstants.EXPIRATION__LIFESPAN, longValue = -1),
        @Property(name = CacheConfigurationConstants.EXPIRATION__MAX_IDLE, longValue = -1),
        @Property(name = CacheConfigurationConstants.EXPIRATION__REAPER_ENABLED, boolValue = true),
        @Property(name = CacheConfigurationConstants.EXPIRATION__WAKE_UP_INTERVAL, longValue = 60000),
        // TODO support persistence stores @Property(name = CacheProps.PERSISTENCE__PASSIVATION, boolValue = false),
        @Property(name = CacheConfigurationConstants.INVOCATION_BATCHING__ENABLE, boolValue = false),
        @Property(name = CacheConfigurationConstants.CLUSTERING__CACHE_MODE,
                value = CacheConfigurationConstants.CLUSTERING__CACHE_MODE_OPT_LOCAL,
                options = {
                        @PropertyOption(name = CacheConfigurationConstants.CLUSTERING__CACHE_MODE_OPT_LOCAL,
                                value = CacheConfigurationConstants.CLUSTERING__CACHE_MODE_OPT_LOCAL),
                        @PropertyOption(name = CacheConfigurationConstants.CLUSTERING__CACHEMODE_OPT_REPL_SYNC,
                                value = CacheConfigurationConstants.CLUSTERING__CACHEMODE_OPT_REPL_SYNC),
                        @PropertyOption(name = CacheConfigurationConstants.CLUSTERING__CACHE_MODE_OPT_REPL_ASYNC,
                                value = CacheConfigurationConstants.CLUSTERING__CACHE_MODE_OPT_REPL_ASYNC),
                        @PropertyOption(
                                name = CacheConfigurationConstants.CLUSTERING__CACHE_MODE_OPT_INVALIDATION_SYNC,
                                value = CacheConfigurationConstants.CLUSTERING__CACHE_MODE_OPT_INVALIDATION_SYNC),
                        @PropertyOption(
                                name = CacheConfigurationConstants.CLUSTERING__CACHE_MODE_OPT_INVALIDATION_ASYNC,
                                value = CacheConfigurationConstants.CLUSTERING__CACHE_MODE_OPT_INVALIDATION_ASYNC),
                        @PropertyOption(name = CacheConfigurationConstants.CLUSTERING__CACHE_MODE_OPT_DIST_SYNC,
                                value = CacheConfigurationConstants.CLUSTERING__CACHE_MODE_OPT_DIST_SYNC),
                        @PropertyOption(name = CacheConfigurationConstants.CLUSTERING__CACHE_MODE_OPT_DIST_ASYNC,
                                value = CacheConfigurationConstants.CLUSTERING__CACHE_MODE_OPT_DIST_ASYNC) }),
        @Property(name = CacheConfigurationConstants.CLUSTERING__ASYNC__ASYNC_MARSHALLING, boolValue = false),
        @Property(name = CacheConfigurationConstants.CLUSTERING__ASYNC__USE_REPL_QUEUE, boolValue = false),
        @Property(name = CacheConfigurationConstants.CLUSTERING__ASYNC__REPL_QUEUE_INTERVAL, longValue = 5000),
        @Property(name = CacheConfigurationConstants.CLUSTERING__ASYNC__REPL_QUEUE_MAX_ELEMENTS, intValue = 1000),
        @Property(name = CacheConfigurationConstants.CLUSTERING__HASH__NUM_OWNERS, intValue = 2),
        @Property(name = CacheConfigurationConstants.CLUSTERING__HASH__NUM_SEGMENTS, intValue = 60),
        @Property(name = CacheConfigurationConstants.CLUSTERING__HASH__CAPACITY_FACTOR, floatValue = 1),
        @Property(name = CacheConfigurationConstants.CLUSTERING__L1__ENABLED, boolValue = false),
        @Property(name = CacheConfigurationConstants.CLUSTERING__L1__INVALIDATION_TRESHOLD, intValue = 0),
        @Property(name = CacheConfigurationConstants.CLUSTERING__L1__LIFESPAN, longValue = 600000),
        @Property(name = CacheConfigurationConstants.CLUSTERING__L1__ON_REHASH, boolValue = true),
        @Property(name = CacheConfigurationConstants.CLUSTERING__L1__CLEANUP_TASK_FREQUENCY, longValue = 600000),
        @Property(name = CacheConfigurationConstants.CLUSTERING__STATE_TRANSFER__FETCH_IN_MEMORY_STATE,
                value = CacheConfigurationConstants.COMMON__BOOLEAN_OPT_DEFAULT,
                options = { @PropertyOption(name = CacheConfigurationConstants.COMMON__BOOLEAN_OPT_DEFAULT,
                        value = CacheConfigurationConstants.COMMON__BOOLEAN_OPT_DEFAULT),
                        @PropertyOption(name = CacheConfigurationConstants.COMMON__BOOLEAN_OPT_FALSE,
                                value = CacheConfigurationConstants.COMMON__BOOLEAN_OPT_FALSE),
                        @PropertyOption(name = CacheConfigurationConstants.COMMON__BOOLEAN_OPT_TRUE,
                                value = CacheConfigurationConstants.COMMON__BOOLEAN_OPT_TRUE) }),
        @Property(name = CacheConfigurationConstants.CLUSTERING__STATE_TRANSFER__AWAIT_INITIAL_TRANSFER,
                value = CacheConfigurationConstants.COMMON__BOOLEAN_OPT_DEFAULT,
                options = { @PropertyOption(name = CacheConfigurationConstants.COMMON__BOOLEAN_OPT_DEFAULT,
                        value = CacheConfigurationConstants.COMMON__BOOLEAN_OPT_DEFAULT),
                        @PropertyOption(name = CacheConfigurationConstants.COMMON__BOOLEAN_OPT_FALSE,
                                value = CacheConfigurationConstants.COMMON__BOOLEAN_OPT_FALSE),
                        @PropertyOption(name = CacheConfigurationConstants.COMMON__BOOLEAN_OPT_TRUE,
                                value = CacheConfigurationConstants.COMMON__BOOLEAN_OPT_TRUE) }),
        @Property(name = CacheConfigurationConstants.CLUSTERING__STATE_TRANSFER__CHUNK_SIZE, intValue = 10000),
        @Property(name = CacheConfigurationConstants.CLUSTERING__STATE_TRANSFER__TIMEOUT, longValue = 4 * 60 * 1000),
        @Property(name = CacheConfigurationConstants.CLUSTERING__SYNC__REPL_TIMEOUT, longValue = 15000),
        @Property(name = CacheConfigurationConstants.LOCKING__CONCURRENCY_LEVEL, intValue = 32),
        @Property(name = CacheConfigurationConstants.LOCKING__ISOLATION_LEVEL, options = {
                @PropertyOption(name = CacheConfigurationConstants.LOCKING__ISOLATION_LEVEL_OPT_READ_COMMITTED,
                        value = CacheConfigurationConstants.LOCKING__ISOLATION_LEVEL_OPT_READ_COMMITTED),
                @PropertyOption(name = CacheConfigurationConstants.LOCKING__ISOLATION_LEVEL_OPT_REPEATABLE_READ,
                        value = CacheConfigurationConstants.LOCKING__ISOLATION_LEVEL_OPT_REPEATABLE_READ) }),
        @Property(name = CacheConfigurationConstants.LOCKING__LOCK_ACQUISITION_TIMEOUT, longValue = 10000),
        @Property(name = CacheConfigurationConstants.LOCKING__USE_LOCK_STRIPING, boolValue = false),
        @Property(name = CacheConfigurationConstants.LOCKING_WRITE_SKEW_CHECK, boolValue = false),
        @Property(name = CacheConfigurationConstants.DEADLOCK_DETECTION__ENABLED, boolValue = false),
        @Property(name = CacheConfigurationConstants.DEADLOCKDETECTION__SPIN_DURATION, longValue = 100),
        @Property(
                name = CacheConfigurationConstants.TRANSACTION__TRANSACTION_MODE,
                value = CacheConfigurationConstants.TRANSACTION__TRANSACTION_MODE_OPT_DEFAULT,
                options = {
                        @PropertyOption(
                                name = CacheConfigurationConstants.TRANSACTION__TRANSACTION_MODE_OPT_DEFAULT,
                                value = CacheConfigurationConstants.TRANSACTION__TRANSACTION_MODE_OPT_DEFAULT),
                        @PropertyOption(
                                name = CacheConfigurationConstants.TRANSACTION__TRANSACTION_MODE_OPT_NON_TRANSACTIONAL,
                                value = CacheConfigurationConstants.TRANSACTION__TRANSACTION_MODE_OPT_NON_TRANSACTIONAL),
                        @PropertyOption(
                                name = CacheConfigurationConstants.TRANSACTION__TRANSACTION_MODE_OPT_TRANSACTIONAL,
                                value = CacheConfigurationConstants.TRANSACTION__TRANSACTION_MODE_OPT_TRANSACTIONAL) }),
        @Property(name = CacheConfigurationConstants.TRANSACTION__AUTO_COMMIT, boolValue = true),
        @Property(name = CacheConfigurationConstants.TRANSACTION__CACHE_STOP_TIMEOUT, longValue = 30000),
        @Property(name = CacheConfigurationConstants.TRANSACTION__LOCKING_MODE,
                value = CacheConfigurationConstants.TRANSACTION__LOCKING_MODE_OPT_OPTIMISTIC, options = {
                        @PropertyOption(
                                name = CacheConfigurationConstants.TRANSACTION__LOCKING_MODE_OPT_OPTIMISTIC,
                                value = CacheConfigurationConstants.TRANSACTION__LOCKING_MODE_OPT_OPTIMISTIC),
                        @PropertyOption(
                                name = CacheConfigurationConstants.TRANSACTION__LOCKING_MODE_OPT_PESSIMISTIC,
                                value = CacheConfigurationConstants.TRANSACTION__LOCKING_MODE_OPT_PESSIMISTIC) }),
        @Property(name = CacheConfigurationConstants.TRANSACTION__SYNC_COMMIT_PHASE, boolValue = true),
        @Property(name = CacheConfigurationConstants.TRANSACTION__SYNC_ROLLBACK_PHASE, boolValue = false),
        @Property(name = CacheConfigurationConstants.TRANSACTION__USE_SYNCHRONIZATION, boolValue = true),
        @Property(name = CacheConfigurationConstants.TRANSACTION__RECOVERY__ENABLED, boolValue = true),
        // TODO be careful as getting a the recovery config builder automatically enables
        @Property(name = CacheConfigurationConstants.TRANSACTION__RECOVERY__RECOVERY_INFO_CACHE_NAME),
        @Property(name = CacheConfigurationConstants.TRANSACTION__USE_1PC_FOR_AUTO_COMMIT_TRANSACTIONS,
                boolValue = false),
        @Property(name = CacheConfigurationConstants.TRANSACTION__REAPER_WAKE_UP_INTERVAL, longValue = 1000),
        @Property(name = CacheConfigurationConstants.TRANSACTION__COMPLETED_TX_TIMEOUT, longValue = 15000),
        @Property(
                name = CacheConfigurationConstants.TRANSACTION__TRANSACTION_PROTOCOL,
                value = CacheConfigurationConstants.TRANSACTION__TRANSACTION_PROTOCOL_OPT_DEFAULT,
                options = {
                        @PropertyOption(
                                name = CacheConfigurationConstants.TRANSACTION__TRANSACTION_PROTOCOL_OPT_DEFAULT,
                                value = CacheConfigurationConstants.TRANSACTION__TRANSACTION_PROTOCOL_OPT_DEFAULT),
                        @PropertyOption(
                                name = CacheConfigurationConstants.TRANSACTION__TRANSACTION_PROTOCOL_OPT_TOTAL_ORDER,
                                value = CacheConfigurationConstants.TRANSACTION__TRANSACTION_PROTOCOL_OPT_TOTAL_ORDER) }),
        @Property(name = CacheConfigurationConstants.VERSIONING__ENABLED, boolValue = false),
        @Property(name = CacheConfigurationConstants.VERSIONING__SCHEME,
                value = CacheConfigurationConstants.VERSIONING__SCHEME_OPT_NONE,
                options = { @PropertyOption(name = CacheConfigurationConstants.VERSIONING__SCHEME_OPT_NONE,
                        value = CacheConfigurationConstants.VERSIONING__SCHEME_OPT_NONE),
                        @PropertyOption(name = CacheConfigurationConstants.VERSIONING__SCHEME_OPT_SIMPLE,
                                value = CacheConfigurationConstants.VERSIONING__SCHEME_OPT_SIMPLE) }),
        // TODO support sites configuration
        // TODO support compatibility mode configuration
        @Property(name = CacheConfigurationConstants.JMX_STATISTICS__ENABLED, boolValue = false),
        @Property(name = CacheConfigurationConstants.TRANSACTION__TRANSACTION_MANAGER__TARGET),
        @Property(name = CacheConfigurationConstants.TRANSACTION__TRANSACTION_SYNCHRONIZATION_REGISTRY__TARGET),
        @Property(name = Constants.SERVICE_DESCRIPTION, propertyPrivate = false)
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

        cacheName = cch.getPropValue(CacheConfigurationConstants.CACHE_NAME, String.class, true);
        serviceProperties.put(CacheConfigurationConstants.CACHE_NAME, cacheName);

        h.applyConfigOnBuilderValue(CacheConfigurationConstants.EVICTION__MAX_ENTRIES, int.class, false);
        h.applyConfigOnBuilderValue(CacheConfigurationConstants.EVICTION__STRATEGY, EvictionStrategy.class, false);
        h.applyConfigOnBuilderValue(CacheConfigurationConstants.EVICTION__THREAD_POLICY,
                EvictionThreadPolicy.class, false);
        h.applyConfigOnBuilderValue(CacheConfigurationConstants.EXPIRATION__LIFESPAN, long.class, false);
        h.applyConfigOnBuilderValue(CacheConfigurationConstants.EXPIRATION__MAX_IDLE, long.class, false);
        h.applyConfigOnBuilderValue(CacheConfigurationConstants.EXPIRATION__REAPER_ENABLED, boolean.class, false);
        h.applyConfigOnBuilderValue(CacheConfigurationConstants.EXPIRATION__WAKE_UP_INTERVAL, long.class, false);
        h.applyConfigOnBuilderValue(CacheConfigurationConstants.INVOCATION_BATCHING__ENABLE, boolean.class, false);
        CacheMode cacheMode = h.applyConfigOnBuilderValue(CacheConfigurationConstants.CLUSTERING__CACHE_MODE,
                CacheMode.class, false);
        if ((cacheMode != null) && !CacheMode.LOCAL.equals(cacheMode)) {
            h.applyConfigOnBuilderValue(CacheConfigurationConstants.CLUSTERING__ASYNC__ASYNC_MARSHALLING,
                    boolean.class,
                    false);
            h.applyConfigOnBuilderValue(CacheConfigurationConstants.CLUSTERING__ASYNC__USE_REPL_QUEUE,
                    boolean.class, false);
            h.applyConfigOnBuilderValue(CacheConfigurationConstants.CLUSTERING__ASYNC__REPL_QUEUE_INTERVAL,
                    long.class,
                    false);
            h.applyConfigOnBuilderValue(CacheConfigurationConstants.CLUSTERING__ASYNC__REPL_QUEUE_MAX_ELEMENTS,
                    int.class,
                    false);
            h.applyConfigOnBuilderValue(CacheConfigurationConstants.CLUSTERING__HASH__NUM_OWNERS, int.class, false);
            h.applyConfigOnBuilderValue(CacheConfigurationConstants.CLUSTERING__HASH__NUM_SEGMENTS, int.class,
                    false);
            h.applyConfigOnBuilderValue(CacheConfigurationConstants.CLUSTERING__HASH__CAPACITY_FACTOR, Float.class,
                    false);
            Boolean l1Enabled = h.applyConfigOnBuilderValue(CacheConfigurationConstants.CLUSTERING__L1__ENABLED,
                    Boolean.class, false);

            if ((l1Enabled != null) && l1Enabled) {
                h.applyConfigOnBuilderValue(CacheConfigurationConstants.CLUSTERING__L1__INVALIDATION_TRESHOLD,
                        int.class,
                        false);
                h.applyConfigOnBuilderValue(CacheConfigurationConstants.CLUSTERING__L1__LIFESPAN, long.class, false);
                h.applyConfigOnBuilderValue(CacheConfigurationConstants.CLUSTERING__L1__ON_REHASH, boolean.class,
                        false);
                h.applyConfigOnBuilderValue(CacheConfigurationConstants.CLUSTERING__L1__CLEANUP_TASK_FREQUENCY,
                        long.class,
                        false);
            }

            applyNullableBoolean(CacheConfigurationConstants.CLUSTERING__STATE_TRANSFER__FETCH_IN_MEMORY_STATE, h);
            applyNullableBoolean(CacheConfigurationConstants.CLUSTERING__STATE_TRANSFER__AWAIT_INITIAL_TRANSFER, h);

            h.applyConfigOnBuilderValue(CacheConfigurationConstants.CLUSTERING__STATE_TRANSFER__CHUNK_SIZE,
                    int.class,
                    false);
            h.applyConfigOnBuilderValue(CacheConfigurationConstants.CLUSTERING__STATE_TRANSFER__TIMEOUT,
                    long.class, false);
            h.applyConfigOnBuilderValue(CacheConfigurationConstants.CLUSTERING__SYNC__REPL_TIMEOUT, long.class,
                    false);
        }
        h.applyConfigOnBuilderValue(CacheConfigurationConstants.LOCKING__CONCURRENCY_LEVEL, int.class, false);
        h.applyConfigOnBuilderValue(CacheConfigurationConstants.LOCKING__ISOLATION_LEVEL, IsolationLevel.class,
                false);
        h.applyConfigOnBuilderValue(CacheConfigurationConstants.LOCKING__LOCK_ACQUISITION_TIMEOUT, long.class,
                false);
        h.applyConfigOnBuilderValue(CacheConfigurationConstants.LOCKING__USE_LOCK_STRIPING, boolean.class, false);
        h.applyConfigOnBuilderValue(CacheConfigurationConstants.LOCKING_WRITE_SKEW_CHECK, boolean.class, false);
        Boolean deadlockDetectionEnabled = h.applyConfigOnBuilderValue(
                CacheConfigurationConstants.DEADLOCK_DETECTION__ENABLED,
                boolean.class, false);
        if ((deadlockDetectionEnabled != null) && deadlockDetectionEnabled) {
            h.applyConfigOnBuilderValue(CacheConfigurationConstants.DEADLOCKDETECTION__SPIN_DURATION, long.class,
                    false);
        }

        String transactionModeString = cch.getPropValue(CacheConfigurationConstants.TRANSACTION__TRANSACTION_MODE,
                String.class, false);
        if ((transactionModeString != null)
                && !CacheConfigurationConstants.TRANSACTION__TRANSACTION_MODE_OPT_DEFAULT
                        .equals(transactionModeString)) {
            h.applyConfigOnBuilderValue(CacheConfigurationConstants.TRANSACTION__TRANSACTION_MODE,
                    TransactionMode.class,
                    false);
        }
        h.applyConfigOnBuilderValue(CacheConfigurationConstants.TRANSACTION__AUTO_COMMIT, boolean.class, false);
        h.applyConfigOnBuilderValue(CacheConfigurationConstants.TRANSACTION__CACHE_STOP_TIMEOUT, long.class, false);
        h.applyConfigOnBuilderValue(CacheConfigurationConstants.TRANSACTION__LOCKING_MODE, LockingMode.class, false);
        h.applyConfigOnBuilderValue(CacheConfigurationConstants.TRANSACTION__SYNC_COMMIT_PHASE, boolean.class,
                false);
        h.applyConfigOnBuilderValue(CacheConfigurationConstants.TRANSACTION__SYNC_ROLLBACK_PHASE, boolean.class,
                false);
        h.applyConfigOnBuilderValue(CacheConfigurationConstants.TRANSACTION__USE_SYNCHRONIZATION, boolean.class,
                false);
        h.applyConfigOnBuilderValue(CacheConfigurationConstants.TRANSACTION__RECOVERY__ENABLED, boolean.class,
                false);
        h.applyConfigOnBuilderValue(CacheConfigurationConstants.TRANSACTION__RECOVERY__RECOVERY_INFO_CACHE_NAME,
                String.class, false);
        h.applyConfigOnBuilderValue(CacheConfigurationConstants.TRANSACTION__USE_1PC_FOR_AUTO_COMMIT_TRANSACTIONS,
                boolean.class, false);
        h.applyConfigOnBuilderValue(CacheConfigurationConstants.TRANSACTION__REAPER_WAKE_UP_INTERVAL, long.class,
                false);
        h.applyConfigOnBuilderValue(CacheConfigurationConstants.TRANSACTION__COMPLETED_TX_TIMEOUT, long.class,
                false);
        h.applyConfigOnBuilderValue(CacheConfigurationConstants.TRANSACTION__TRANSACTION_PROTOCOL,
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

        Boolean versioning = h.applyConfigOnBuilderValue(CacheConfigurationConstants.VERSIONING__ENABLED,
                boolean.class,
                false);
        if ((versioning != null) && versioning) {
            h.applyConfigOnBuilderValue(CacheConfigurationConstants.VERSIONING__SCHEME, VersioningScheme.class,
                    false);
        }
        h.applyConfigOnBuilderValue(CacheConfigurationConstants.JMX_STATISTICS__ENABLED, boolean.class, false);

        configuration = builder.build(true);

        fillServiceProperties(serviceProperties);

        Object serviceDescription = componentConfiguration.get(Constants.SERVICE_DESCRIPTION);
        if (serviceDescription != null) {
            serviceProperties.put(Constants.SERVICE_DESCRIPTION, serviceDescription);
        }

        serviceRegistration = context.registerService(
                new String[] { CacheConfiguration.class.getName(), ISPNCacheConfiguration.class.getName() }, this,
                new Hashtable<String, Object>(serviceProperties));
    }

    private Boolean applyNullableBoolean(final String key, final ReflectiveConfigurationBuilderHelper helper) {
        ReflectiveComponentConfigurationHelper configHelper = helper.getComponentConfigHelper();
        String propValue = configHelper.getPropValue(key, String.class, false);
        if ((propValue == null) || CacheConfigurationConstants.COMMON__BOOLEAN_OPT_DEFAULT.equals(propValue)) {
            return null;
        }
        if (CacheConfigurationConstants.COMMON__BOOLEAN_OPT_TRUE.equals(propValue)) {
            helper.applyValue(key, true, boolean.class);
            return true;
        } else if (CacheConfigurationConstants.COMMON__BOOLEAN_OPT_FALSE.equals(propValue)) {
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

        h.transferProperty(CacheConfigurationConstants.EVICTION__MAX_ENTRIES);
        h.transferProperty(CacheConfigurationConstants.EVICTION__STRATEGY);
        h.transferProperty(CacheConfigurationConstants.EVICTION__THREAD_POLICY);
        h.transferProperty(CacheConfigurationConstants.EXPIRATION__LIFESPAN);
        h.transferProperty(CacheConfigurationConstants.EXPIRATION__MAX_IDLE);
        h.transferProperty(CacheConfigurationConstants.EXPIRATION__REAPER_ENABLED);
        h.transferProperty(CacheConfigurationConstants.EXPIRATION__WAKE_UP_INTERVAL);
        h.transferProperty(CacheConfigurationConstants.INVOCATION_BATCHING__ENABLE + "d");
        h.transferProperty(CacheConfigurationConstants.CLUSTERING__CACHE_MODE);
        h.transferProperty(CacheConfigurationConstants.CLUSTERING__ASYNC__ASYNC_MARSHALLING);
        h.transferProperty(CacheConfigurationConstants.CLUSTERING__ASYNC__USE_REPL_QUEUE);
        h.transferProperty(CacheConfigurationConstants.CLUSTERING__ASYNC__REPL_QUEUE_INTERVAL);
        h.transferProperty(CacheConfigurationConstants.CLUSTERING__ASYNC__REPL_QUEUE_MAX_ELEMENTS);
        h.transferProperty(CacheConfigurationConstants.CLUSTERING__HASH__NUM_OWNERS);
        h.transferProperty(CacheConfigurationConstants.CLUSTERING__HASH__NUM_SEGMENTS);
        h.transferProperty(CacheConfigurationConstants.CLUSTERING__HASH__CAPACITY_FACTOR);
        h.transferProperty(CacheConfigurationConstants.CLUSTERING__L1__ENABLED);
        h.transferProperty(CacheConfigurationConstants.CLUSTERING__L1__INVALIDATION_TRESHOLD);
        h.transferProperty(CacheConfigurationConstants.CLUSTERING__L1__LIFESPAN);
        h.transferProperty(CacheConfigurationConstants.CLUSTERING__L1__ON_REHASH);
        h.transferProperty(CacheConfigurationConstants.CLUSTERING__L1__CLEANUP_TASK_FREQUENCY);
        h.transferProperty(CacheConfigurationConstants.CLUSTERING__STATE_TRANSFER__FETCH_IN_MEMORY_STATE);
        h.transferProperty(CacheConfigurationConstants.CLUSTERING__STATE_TRANSFER__AWAIT_INITIAL_TRANSFER);
        h.transferProperty(CacheConfigurationConstants.CLUSTERING__STATE_TRANSFER__CHUNK_SIZE);
        h.transferProperty(CacheConfigurationConstants.CLUSTERING__STATE_TRANSFER__TIMEOUT);
        h.transferProperty(CacheConfigurationConstants.CLUSTERING__SYNC__REPL_TIMEOUT);
        h.transferProperty(CacheConfigurationConstants.LOCKING__CONCURRENCY_LEVEL);
        h.transferProperty(CacheConfigurationConstants.LOCKING__ISOLATION_LEVEL);
        h.transferProperty(CacheConfigurationConstants.LOCKING__LOCK_ACQUISITION_TIMEOUT);
        h.transferProperty(CacheConfigurationConstants.LOCKING__USE_LOCK_STRIPING);
        h.transferProperty(CacheConfigurationConstants.LOCKING_WRITE_SKEW_CHECK);
        h.transferProperty(CacheConfigurationConstants.DEADLOCK_DETECTION__ENABLED);
        h.transferProperty(CacheConfigurationConstants.DEADLOCKDETECTION__SPIN_DURATION);

        h.transferProperty(CacheConfigurationConstants.TRANSACTION__TRANSACTION_MODE);
        h.transferProperty(CacheConfigurationConstants.TRANSACTION__AUTO_COMMIT);
        h.transferProperty(CacheConfigurationConstants.TRANSACTION__CACHE_STOP_TIMEOUT);
        h.transferProperty(CacheConfigurationConstants.TRANSACTION__LOCKING_MODE);
        h.transferProperty(CacheConfigurationConstants.TRANSACTION__SYNC_COMMIT_PHASE);
        h.transferProperty(CacheConfigurationConstants.TRANSACTION__SYNC_ROLLBACK_PHASE);
        h.transferProperty(CacheConfigurationConstants.TRANSACTION__USE_SYNCHRONIZATION);
        h.transferProperty(CacheConfigurationConstants.TRANSACTION__RECOVERY__ENABLED);
        h.transferProperty(CacheConfigurationConstants.TRANSACTION__RECOVERY__RECOVERY_INFO_CACHE_NAME);
        h.transferProperty(CacheConfigurationConstants.TRANSACTION__USE_1PC_FOR_AUTO_COMMIT_TRANSACTIONS);
        h.transferProperty(CacheConfigurationConstants.TRANSACTION__REAPER_WAKE_UP_INTERVAL);
        h.transferProperty(CacheConfigurationConstants.TRANSACTION__COMPLETED_TX_TIMEOUT);
        h.transferProperty(CacheConfigurationConstants.TRANSACTION__TRANSACTION_PROTOCOL);

        h.transferProperty(CacheConfigurationConstants.VERSIONING__ENABLED);
        h.transferProperty(CacheConfigurationConstants.VERSIONING__SCHEME);
        h.transferProperty(CacheConfigurationConstants.JMX_STATISTICS__ENABLED);
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
