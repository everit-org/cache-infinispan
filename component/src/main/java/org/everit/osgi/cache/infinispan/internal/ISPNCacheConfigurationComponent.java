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
import org.everit.osgi.cache.api.CacheConfiguration;
import org.everit.osgi.cache.infinispan.config.CacheProps;
import org.everit.osgi.cache.infinispan.config.ISPNCacheConfiguration;
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

@Component(name = CacheProps.CACHE_CONFIGURATION_COMPONENT_NAME, configurationFactory = true,
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
        @Property(name = CacheProps.INVOCATION_BATCHING__ENABLE, boolValue = false),
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
        @Property(name = CacheProps.TRANSACTION__TRANSACTION_MODE,
                value = CacheProps.TRANSACTION__TRANSACTION_MODE_OPT_DEFAULT, options = { @PropertyOption(
                        name = CacheProps.TRANSACTION__TRANSACTION_MODE_OPT_DEFAULT,
                        value = CacheProps.TRANSACTION__TRANSACTION_MODE_OPT_DEFAULT),
                        @PropertyOption(
                                name = CacheProps.TRANSACTION__TRANSACTION_MODE_OPT_NON_TRANSACTIONAL,
                                value = CacheProps.TRANSACTION__TRANSACTION_MODE_OPT_NON_TRANSACTIONAL),
                        @PropertyOption(
                                name = CacheProps.TRANSACTION__TRANSACTION_MODE_OPT_TRANSACTIONAL,
                                value = CacheProps.TRANSACTION__TRANSACTION_MODE_OPT_TRANSACTIONAL) }),
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
        // TODO support sites configuration
        // TODO support compatibility mode configuration
        @Property(name = CacheProps.JMX_STATISTICS__ENABLED, boolValue = false),
        @Property(name = CacheProps.TRANSACTION__TRANSACTION_MANAGER__TARGET),
        @Property(name = CacheProps.TRANSACTION__TRANSACTION_SYNCHRONIZATION_REGISTRY__TARGET),
})
public class ISPNCacheConfigurationComponent<K, V> implements ISPNCacheConfiguration<K, V> {

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

        String cacheName = cch.getPropValue(CacheProps.CACHE_NAME, String.class, true);
        serviceProperties.put(CacheProps.CACHE_NAME, cacheName);

        h.applyConfigOnBuilderValue(CacheProps.EVICTION__MAX_ENTRIES, int.class, false);
        h.applyConfigOnBuilderValue(CacheProps.EVICTION__STRATEGY, EvictionStrategy.class, false);
        h.applyConfigOnBuilderValue(CacheProps.EVICTION__THREAD_POLICY, EvictionThreadPolicy.class, false);
        h.applyConfigOnBuilderValue(CacheProps.EXPIRATION__LIFESPAN, long.class, false);
        h.applyConfigOnBuilderValue(CacheProps.EXPIRATION__MAX_IDLE, long.class, false);
        h.applyConfigOnBuilderValue(CacheProps.EXPIRATION__REAPER_ENABLED, boolean.class, false);
        h.applyConfigOnBuilderValue(CacheProps.EXPIRATION__WAKE_UP_INTERVAL, long.class, false);
        h.applyConfigOnBuilderValue(CacheProps.INVOCATION_BATCHING__ENABLE, boolean.class, false);
        CacheMode cacheMode = h.applyConfigOnBuilderValue(CacheProps.CLUSTERING__CACHE_MODE, CacheMode.class, false);
        if (cacheMode != null && !CacheMode.LOCAL.equals(cacheMode)) {
            h.applyConfigOnBuilderValue(CacheProps.CLUSTERING__ASYNC__ASYNC_MARSHALLING, boolean.class, false);
            h.applyConfigOnBuilderValue(CacheProps.CLUSTERING__ASYNC__USE_REPL_QUEUE, boolean.class, false);
            h.applyConfigOnBuilderValue(CacheProps.CLUSTERING__ASYNC__REPL_QUEUE_INTERVAL, long.class, false);
            h.applyConfigOnBuilderValue(CacheProps.CLUSTERING__ASYNC__REPL_QUEUE_MAX_ELEMENTS, int.class, false);
            h.applyConfigOnBuilderValue(CacheProps.CLUSTERING__HASH__NUM_OWNERS, int.class, false);
            h.applyConfigOnBuilderValue(CacheProps.CLUSTERING__HASH__NUM_SEGMENTS, int.class, false);
            h.applyConfigOnBuilderValue(CacheProps.CLUSTERING__HASH__CAPACITY_FACTOR, Float.class, false);
            Boolean l1Enabled = h.applyConfigOnBuilderValue(CacheProps.CLUSTERING__L1__ENABLED, Boolean.class, false);

            if (l1Enabled != null && l1Enabled) {
                h.applyConfigOnBuilderValue(CacheProps.CLUSTERING__L1__INVALIDATION_TRESHOLD, int.class, false);
                h.applyConfigOnBuilderValue(CacheProps.CLUSTERING__L1__LIFESPAN, long.class, false);
                h.applyConfigOnBuilderValue(CacheProps.CLUSTERING__L1__ON_REHASH, boolean.class, false);
                h.applyConfigOnBuilderValue(CacheProps.CLUSTERING__L1__CLEANUP_TASK_FREQUENCY, long.class, false);
            }

            applyNullableBoolean(CacheProps.CLUSTERING__STATE_TRANSFER__FETCH_IN_MEMORY_STATE, h);
            applyNullableBoolean(CacheProps.CLUSTERING__STATE_TRANSFER__AWAIT_INITIAL_TRANSFER, h);

            h.applyConfigOnBuilderValue(CacheProps.CLUSTERING__STATE_TRANSFER__CHUNK_SIZE, int.class, false);
            h.applyConfigOnBuilderValue(CacheProps.CLUSTERING__STATE_TRANSFER__TIMEOUT, long.class, false);
            h.applyConfigOnBuilderValue(CacheProps.CLUSTERING__SYNC__REPL_TIMEOUT, long.class, false);
        }
        h.applyConfigOnBuilderValue(CacheProps.LOCKING__CONCURRENCY_LEVEL, int.class, false);
        h.applyConfigOnBuilderValue(CacheProps.LOCKING__ISOLATION_LEVEL, IsolationLevel.class, false);
        h.applyConfigOnBuilderValue(CacheProps.LOCKING__LOCK_ACQUISITION_TIMEOUT, long.class, false);
        h.applyConfigOnBuilderValue(CacheProps.LOCKING__USE_LOCK_STRIPING, boolean.class, false);
        h.applyConfigOnBuilderValue(CacheProps.LOCKING_WRITE_SKEW_CHECK, boolean.class, false);
        Boolean deadlockDetectionEnabled = h.applyConfigOnBuilderValue(CacheProps.DEADLOCK_DETECTION__ENABLED,
                boolean.class, false);
        if (deadlockDetectionEnabled != null && deadlockDetectionEnabled) {
            h.applyConfigOnBuilderValue(CacheProps.DEADLOCKDETECTION__SPIN_DURATION, long.class, false);
        }

        String transactionModeString = cch.getPropValue(CacheProps.TRANSACTION__TRANSACTION_MODE, String.class, false);
        if (transactionModeString != null && !CacheProps.TRANSACTION__TRANSACTION_MODE_OPT_DEFAULT.equals(transactionModeString)) {
            h.applyConfigOnBuilderValue(CacheProps.TRANSACTION__TRANSACTION_MODE, TransactionMode.class, false);    
        }
        h.applyConfigOnBuilderValue(CacheProps.TRANSACTION__AUTO_COMMIT, boolean.class, false);
        h.applyConfigOnBuilderValue(CacheProps.TRANSACTION__CACHE_STOP_TIMEOUT, long.class, false);
        h.applyConfigOnBuilderValue(CacheProps.TRANSACTION__LOCKING_MODE, LockingMode.class, false);
        h.applyConfigOnBuilderValue(CacheProps.TRANSACTION__SYNC_COMMIT_PHASE, boolean.class, false);
        h.applyConfigOnBuilderValue(CacheProps.TRANSACTION__SYNC_ROLLBACK_PHASE, boolean.class, false);
        h.applyConfigOnBuilderValue(CacheProps.TRANSACTION__USE_SYNCHRONIZATION, boolean.class, false);
        h.applyConfigOnBuilderValue(CacheProps.TRANSACTION__RECOVERY__ENABLED, boolean.class, false);
        h.applyConfigOnBuilderValue(CacheProps.TRANSACTION__RECOVERY__RECOVERY_INFO_CACHE_NAME, String.class, false);
        h.applyConfigOnBuilderValue(CacheProps.TRANSACTION__USE_1PC_FOR_AUTO_COMMIT_TRANSACTIONS, boolean.class, false);
        h.applyConfigOnBuilderValue(CacheProps.TRANSACTION__REAPER_WAKE_UP_INTERVAL, long.class, false);
        h.applyConfigOnBuilderValue(CacheProps.TRANSACTION__COMPLETED_TX_TIMEOUT, long.class, false);
        h.applyConfigOnBuilderValue(CacheProps.TRANSACTION__TRANSACTION_PROTOCOL, TransactionProtocol.class, false);
        
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
        
        
        Boolean versioning = h.applyConfigOnBuilderValue(CacheProps.VERSIONING__ENABLED, boolean.class, false);
        if (versioning != null && versioning) {
            h.applyConfigOnBuilderValue(CacheProps.VERSIONING__SCHEME, VersioningScheme.class, false);
        }
        h.applyConfigOnBuilderValue(CacheProps.JMX_STATISTICS__ENABLED, boolean.class, false);

        configuration = builder.build(true);
        
        fillServiceProperties(serviceProperties);

        serviceRegistration = context.registerService(
                new String[] { CacheConfiguration.class.getName(), ISPNCacheConfiguration.class.getName() }, this,
                new Hashtable<String, Object>(serviceProperties));
    }

    private void fillServiceProperties(Map<String, Object> serviceProperties) {
        ReflectiveConfigToServicePropsHelper h = new ReflectiveConfigToServicePropsHelper(configuration,
                serviceProperties);
        
        h.transferProperty(CacheProps.EVICTION__MAX_ENTRIES);
        h.transferProperty(CacheProps.EVICTION__STRATEGY);
        h.transferProperty(CacheProps.EVICTION__THREAD_POLICY);
        h.transferProperty(CacheProps.EXPIRATION__LIFESPAN);
        h.transferProperty(CacheProps.EXPIRATION__MAX_IDLE);
        h.transferProperty(CacheProps.EXPIRATION__REAPER_ENABLED);
        h.transferProperty(CacheProps.EXPIRATION__WAKE_UP_INTERVAL);
        h.transferProperty(CacheProps.INVOCATION_BATCHING__ENABLE + "d");
        h.transferProperty(CacheProps.CLUSTERING__CACHE_MODE);
        h.transferProperty(CacheProps.CLUSTERING__ASYNC__ASYNC_MARSHALLING);
        h.transferProperty(CacheProps.CLUSTERING__ASYNC__USE_REPL_QUEUE);
        h.transferProperty(CacheProps.CLUSTERING__ASYNC__REPL_QUEUE_INTERVAL);
        h.transferProperty(CacheProps.CLUSTERING__ASYNC__REPL_QUEUE_MAX_ELEMENTS);
        h.transferProperty(CacheProps.CLUSTERING__HASH__NUM_OWNERS);
        h.transferProperty(CacheProps.CLUSTERING__HASH__NUM_SEGMENTS);
        h.transferProperty(CacheProps.CLUSTERING__HASH__CAPACITY_FACTOR);
        h.transferProperty(CacheProps.CLUSTERING__L1__ENABLED);
        h.transferProperty(CacheProps.CLUSTERING__L1__INVALIDATION_TRESHOLD);
        h.transferProperty(CacheProps.CLUSTERING__L1__LIFESPAN);
        h.transferProperty(CacheProps.CLUSTERING__L1__ON_REHASH);
        h.transferProperty(CacheProps.CLUSTERING__L1__CLEANUP_TASK_FREQUENCY);
        h.transferProperty(CacheProps.CLUSTERING__STATE_TRANSFER__FETCH_IN_MEMORY_STATE);
        h.transferProperty(CacheProps.CLUSTERING__STATE_TRANSFER__AWAIT_INITIAL_TRANSFER);
        h.transferProperty(CacheProps.CLUSTERING__STATE_TRANSFER__CHUNK_SIZE);
        h.transferProperty(CacheProps.CLUSTERING__STATE_TRANSFER__TIMEOUT);
        h.transferProperty(CacheProps.CLUSTERING__SYNC__REPL_TIMEOUT);
        h.transferProperty(CacheProps.LOCKING__CONCURRENCY_LEVEL);
        h.transferProperty(CacheProps.LOCKING__ISOLATION_LEVEL);
        h.transferProperty(CacheProps.LOCKING__LOCK_ACQUISITION_TIMEOUT);
        h.transferProperty(CacheProps.LOCKING__USE_LOCK_STRIPING);
        h.transferProperty(CacheProps.LOCKING_WRITE_SKEW_CHECK);
        h.transferProperty(CacheProps.DEADLOCK_DETECTION__ENABLED);
        h.transferProperty(CacheProps.DEADLOCKDETECTION__SPIN_DURATION);
        
        h.transferProperty(CacheProps.TRANSACTION__TRANSACTION_MODE);
        h.transferProperty(CacheProps.TRANSACTION__AUTO_COMMIT);
        h.transferProperty(CacheProps.TRANSACTION__CACHE_STOP_TIMEOUT);
        h.transferProperty(CacheProps.TRANSACTION__LOCKING_MODE);
        h.transferProperty(CacheProps.TRANSACTION__SYNC_COMMIT_PHASE);
        h.transferProperty(CacheProps.TRANSACTION__SYNC_ROLLBACK_PHASE);
        h.transferProperty(CacheProps.TRANSACTION__USE_SYNCHRONIZATION);
        h.transferProperty(CacheProps.TRANSACTION__RECOVERY__ENABLED);
        h.transferProperty(CacheProps.TRANSACTION__RECOVERY__RECOVERY_INFO_CACHE_NAME);
        h.transferProperty(CacheProps.TRANSACTION__USE_1PC_FOR_AUTO_COMMIT_TRANSACTIONS);
        h.transferProperty(CacheProps.TRANSACTION__REAPER_WAKE_UP_INTERVAL);
        h.transferProperty(CacheProps.TRANSACTION__COMPLETED_TX_TIMEOUT);
        h.transferProperty(CacheProps.TRANSACTION__TRANSACTION_PROTOCOL);
        
        h.transferProperty(CacheProps.VERSIONING__ENABLED);
        h.transferProperty(CacheProps.VERSIONING__SCHEME);
        h.transferProperty(CacheProps.JMX_STATISTICS__ENABLED);
    }

    private Boolean applyNullableBoolean(String key, ReflectiveConfigurationBuilderHelper helper) {
        ReflectiveComponentConfigurationHelper configHelper = helper.getComponentConfigHelper();
        String propValue = configHelper.getPropValue(key, String.class, false);
        if (propValue == null || CacheProps.COMMON__BOOLEAN_OPT_DEFAULT.equals(propValue)) {
            return null;
        }
        if (CacheProps.COMMON__BOOLEAN_OPT_TRUE.equals(propValue)) {
            helper.applyValue(key, true, boolean.class);
            return true;
        } else if (CacheProps.COMMON__BOOLEAN_OPT_FALSE.equals(propValue)) {
            helper.applyValue(key, false, boolean.class);
            return false;
        } else {
            throw new ComponentException("The value '" + propValue + "' is not allowed for configuration property "
                    + key);
        }
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Deactivate
    public void deactivate() {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
    }
    
    @Override
    public String getCacheName() {
        return cacheName;
    }

}
