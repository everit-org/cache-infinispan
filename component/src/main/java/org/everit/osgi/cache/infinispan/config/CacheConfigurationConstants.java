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
package org.everit.osgi.cache.infinispan.config;

import java.util.Map;

import javax.transaction.Synchronization;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.infinispan.configuration.cache.RecoveryConfiguration;

/**
 * Defines standard names for infinispan cache factory component configuration
 */
public final class CacheConfigurationConstants {

    public static final String SERVICE_FACTORYPID_CACHE_CONFIGURATION =
            "org.everit.osgi.cache.infinispan.CacheConfiguration";

    /**
     * Enable asynchronous marshalling. This allows the caller to return even quicker, but it can suffer from reordering
     * of operations. You can find more information at <a
     * href="https://docs.jboss.org/author/display/ISPN/Asynchronous+Options"
     * >https://docs.jboss.org/author/display/ISPN/Asynchronous+Options</a>.
     */
    public static final String CLUSTERING__ASYNC__ASYNC_MARSHALLING = "clustering.async.asyncMarshalling";

    /**
     * If useReplQueue is set to true, this attribute controls how often the asynchronous thread used to flush the
     * replication queue runs.
     */
    public static final String CLUSTERING__ASYNC__REPL_QUEUE_INTERVAL = "clustering.async.replQueueInterval";

    /**
     * If useReplQueue is set to true, this attribute can be used to trigger flushing of the queue when it reaches a
     * specific threshold.
     */
    public static final String CLUSTERING__ASYNC__REPL_QUEUE_MAX_ELEMENTS = "clustering.async.replQueueMaxElements";

    /**
     * If true, forces all async communications to be queued up and sent out periodically as a batch.
     */
    public static final String CLUSTERING__ASYNC__USE_REPL_QUEUE = "clustering.async.useReplQueue";

    /**
     * Cache replication mode.
     */
    public static final String CLUSTERING__CACHE_MODE = "clustering.cacheMode";

    /**
     * Async DIST
     */
    public static final String CLUSTERING__CACHE_MODE_OPT_DIST_ASYNC = "DIST_ASYNC";

    /**
     * Synchronous DIST
     */
    public static final String CLUSTERING__CACHE_MODE_OPT_DIST_SYNC = "DIST_SYNC";

    /**
     * Data invalidated asynchronously.
     */
    public static final String CLUSTERING__CACHE_MODE_OPT_INVALIDATION_ASYNC = "INVALIDATION_ASYNC";

    /**
     * Data invalidated synchronously.
     */
    public static final String CLUSTERING__CACHE_MODE_OPT_INVALIDATION_SYNC = "INVALIDATION_SYNC";

    /**
     * Data is not replicated.
     */
    public static final String CLUSTERING__CACHE_MODE_OPT_LOCAL = "LOCAL";

    /**
     * Data replicated synchronously.
     */
    public static final String CLUSTERING__CACHE_MODE_OPT_REPL_ASYNC = "REPL_ASYNC";

    /**
     * Data replicated asynchronously.
     */
    public static final String CLUSTERING__CACHEMODE_OPT_REPL_SYNC = "REPL_SYNC";

    /**
     * Controls the proportion of entries that will reside on the local node, compared to the other nodes in the
     * cluster. This is just a suggestion, there is no guarantee that a node with a capacity factor of {@code 2} will
     * have twice as many entries as a node with a capacity factor of {@code 1}.
     * <p>
     * Must be positive.
     */
    public static final String CLUSTERING__HASH__CAPACITY_FACTOR = "clustering.hash.capacityFactor";

    /**
     * Number of cluster-wide replicas for each cache entry.
     */
    public static final String CLUSTERING__HASH__NUM_OWNERS = "clustering.hash.numOwners";

    /**
     * Controls the total number of hash space segments (per cluster).
     * 
     * <p>
     * A hash space segment is the granularity for key distribution in the cluster: a node can own (or primary-own) one
     * or more full segments, but not a fraction of a segment. As such, larger {@code numSegments} values will mean a
     * more even distribution of keys between nodes.
     * <p>
     * On the other hand, the memory/bandwidth usage of the new consistent hash grows linearly with {@code numSegments}.
     * So we recommend keeping {@code numSegments <= 10 * clusterSize}.
     * <p>
     * Must be strictly positive.
     */
    public static final String CLUSTERING__HASH__NUM_SEGMENTS = "clustering.hash.numSegments";

    /**
     * How often the L1 requestors map is cleaned up of stale items
     */
    public static final String CLUSTERING__L1__CLEANUP_TASK_FREQUENCY = "clustering.l1.cleanupTaskFrequency";
    /**
     * Enables L1 cache. Used in 'distributed' caches instances. In any other cache modes, this is ignored.
     */
    public static final String CLUSTERING__L1__ENABLED = "clustering.l1.enabled";
    /**
     * <p>
     * Determines whether a multicast or a web of unicasts are used when performing L1 invalidations.
     * </p>
     * 
     * <p>
     * By default multicast will be used.
     * </p>
     * 
     * <p>
     * If the threshold is set to -1, then unicasts will always be used. If the threshold is set to 0, then multicast
     * will be always be used.
     * </p>
     */
    public static final String CLUSTERING__L1__INVALIDATION_TRESHOLD = "clustering.l1.invalidationThreshold";

    /**
     * Maximum lifespan of an entry placed in the L1 cache.
     */
    public static final String CLUSTERING__L1__LIFESPAN = "clustering.l1.lifespan";

    /**
     * Entries removed due to a rehash will be moved to L1 rather than being removed altogether.
     */
    public static final String CLUSTERING__L1__ON_REHASH = "clustering.l1.onRehash";

    /**
     * If {@code true}, this will cause the first call to method {@code CacheManager.getCache()} on the joiner node to
     * block and wait until the joining is complete and the cache has finished receiving state from neighboring caches
     * (if fetchInMemoryState is enabled). This option applies to distributed and replicated caches only and is enabled
     * by default. Please note that setting this to {@code false} will make the cache object available immediately but
     * any access to keys that should be available locally but are not yet transferred will actually cause a
     * (transparent) remote access. While this will not have any impact on the logic of your application it might impact
     * performance.
     */
    public static final String CLUSTERING__STATE_TRANSFER__AWAIT_INITIAL_TRANSFER = "clustering.stateTransfer.awaitInitialTransfer";

    /**
     * If &gt; 0, the state will be transferred in batches of {@code chunkSize} cache entries. If &lt;= 0, the state
     * will be transferred in all at once. Not recommended.
     */
    public static final String CLUSTERING__STATE_TRANSFER__CHUNK_SIZE = "clustering.stateTransfer.chunkSize";

    /**
     * If {@code true}, the cache will fetch data from the neighboring caches when it starts up, so the cache starts
     * 'warm', although it will impact startup time.
     * <p>
     * In distributed mode, state is transferred between running caches as well, as the ownership of keys changes (e.g.
     * because a cache left the cluster). Disabling this setting means a key will sometimes have less than
     * {@code numOwner} owners.
     */
    public static final String CLUSTERING__STATE_TRANSFER__FETCH_IN_MEMORY_STATE = "clustering.stateTransfer.fetchInMemoryState";

    /**
     * This is the maximum amount of time - in milliseconds - to wait for state from neighboring caches, before throwing
     * an exception and aborting startup.
     */
    public static final String CLUSTERING__STATE_TRANSFER__TIMEOUT = "clustering.stateTransfer.timeout";

    /**
     * This is the timeout used to wait for an acknowledgment when making a remote call, after which the call is aborted
     * and an exception is thrown.
     */
    public static final String CLUSTERING__SYNC__REPL_TIMEOUT = "clustering.sync.replTimeout";

    /**
     * Enable or disable deadlock detection.
     */
    public static final String DEADLOCK_DETECTION__ENABLED = "deadlockDetection.enabled";

    /**
     * Time period that determines how often is lock acquisition attempted within maximum time allowed to acquire a
     * particular lock.
     */
    public static final String DEADLOCKDETECTION__SPIN_DURATION = "deadlockDetection.spinDuration";

    /**
     * Maximum number of entries in a cache instance. Cache size is guaranteed not to exceed upper limit specified by
     * max entries. However, due to the nature of eviction it is unlikely to ever be exactly maximum number of entries
     * specified here.
     */
    public static final String EVICTION__MAX_ENTRIES = "eviction.maxEntries";

    /**
     * Eviction strategy. Available options are 'UNORDERED', 'LRU', 'LIRS' and 'NONE' (to disable eviction).
     */
    public static final String EVICTION__STRATEGY = "eviction.strategy";

    public static final String EVICTION__STRATEGY_OPT_LIRS = "LIRS";

    public static final String EVICTION__STRATEGY_OPT_LRU = "LRU";

    public static final String EVICTION__STRATEGY_OPT_NONE = "NONE";

    public static final String EVICTION__STRATEGY_OPT_UNORDERED = "UNORDERED";

    /**
     * Threading policy for eviction.
     */
    public static final String EVICTION__THREAD_POLICY = "eviction.threadPolicy";

    public static final String EVICTION__THREAD_POLICY_OPT_DEFAULT = "DEFAULT";

    public static final String EVICTION__THREAD_POLICY_OPT_PIGGYBACK = "PIGGYBACK";

    /**
     * Maximum lifespan of a cache entry, after which the entry is expired cluster-wide, in milliseconds. -1 means the
     * entries never expire.
     * 
     * Note that this can be overridden on a per-entry basis by using the Cache API.
     */
    public static final String EXPIRATION__LIFESPAN = "expiration.lifespan";

    /**
     * Maximum idle time a cache entry will be maintained in the cache, in milliseconds. If the idle time is exceeded,
     * the entry will be expired cluster-wide. -1 means the entries never expire.
     * 
     * Note that this can be overridden on a per-entry basis by using the Cache API.
     */
    public static final String EXPIRATION__MAX_IDLE = "expiration.maxIdle";

    /**
     * Enable the background reaper to test entries for expiration. Regardless of whether a reaper is used, entries are
     * tested for expiration lazily when they are touched.
     */
    public static final String EXPIRATION__REAPER_ENABLED = "expiration.reaperEnabled";

    /**
     * Interval (in milliseconds) between subsequent runs to purge expired entries from memory and any cache stores. If
     * you wish to disable the periodic eviction process altogether, set wakeupInterval to -1.
     */
    public static final String EXPIRATION__WAKE_UP_INTERVAL = "expiration.wakeUpInterval";

    public static final String INVOCATION_BATCHING__ENABLE = "invocationBatching.enable";

    /**
     * Determines whether statistics are gather and reported.
     */
    public static final String JMX_STATISTICS__ENABLED = "jmxStatistics.enabled";

    /**
     * Concurrency level for lock containers. Adjust this value according to the number of concurrent threads
     * interacting with Infinispan. Similar to the concurrencyLevel tuning parameter seen in the JDK's
     * ConcurrentHashMap.
     */
    public static final String LOCKING__CONCURRENCY_LEVEL = "locking.concurrencyLevel";

    /**
     * Cache isolation level. Infinispan only supports READ_COMMITTED or REPEATABLE_READ isolation levels. See <a href=
     * 'http://en.wikipedia.org/wiki/Isolation_level'>http://en.wikipedia.org/wiki/Isolation_level</a > for a discussion
     * on isolation levels.
     */
    public static final String LOCKING__ISOLATION_LEVEL = "locking.isolationLevel";

    public static final String LOCKING__ISOLATION_LEVEL_OPT_READ_COMMITTED = "READ_COMMITTED";

    public static final String LOCKING__ISOLATION_LEVEL_OPT_REPEATABLE_READ = "REPEATABLE_READ";

    /**
     * Maximum time to attempt a particular lock acquisition
     */
    public static final String LOCKING__LOCK_ACQUISITION_TIMEOUT = "locking.lockAcquisitionTimeout";

    /**
     * If true, a pool of shared locks is maintained for all entries that need to be locked. Otherwise, a lock is
     * created per entry in the cache. Lock striping helps control memory footprint but may reduce concurrency in the
     * system.
     */
    public static final String LOCKING__USE_LOCK_STRIPING = "locking.useLockStriping";

    /**
     * This setting is only applicable in the case of REPEATABLE_READ. When write skew check is set to false, if the
     * writer at commit time discovers that the working entry and the underlying entry have different versions, the
     * working entry will overwrite the underlying entry. If true, such version conflict - known as a write-skew - will
     * throw an Exception.
     */
    public static final String LOCKING_WRITE_SKEW_CHECK = "locking.writeSkewCheck";

    /**
     * If true, data is only written to the cache store when it is evicted from memory, a phenomenon known as
     * 'passivation'. Next time the data is requested, it will be 'activated' which means that data will be brought back
     * to memory and removed from the persistent store. This gives you the ability to 'overflow' to disk, similar to
     * swapping in an operating system. <br>
     * <br>
     * If false, the cache store contains a copy of the contents in memory, so writes to cache result in cache store
     * writes. This essentially gives you a 'write-through' configuration.
     */
    public static final String PERSISTENCE__PASSIVATION = "persistence.passivation";

    /**
     * Whether the cache is transactional or not.
     */
    public static final String TRANSACTION__TRANSACTION_MODE = "transaction.transactionMode";

    public static final String TRANSACTION__TRANSACTION_MODE_OPT_DEFAULT = "DEFAULT";

    public static final String TRANSACTION__TRANSACTION_MODE_OPT_TRANSACTIONAL = "TRANSACTIONAL";

    public static final String TRANSACTION__TRANSACTION_MODE_OPT_NON_TRANSACTIONAL = "NON_TRANSACTIONAL";

    /**
     * If true, data is only written to the cache store when it is evicted from memory, a phenomenon known as
     * 'passivation'. Next time the data is requested, it will be 'activated' which means that data will be brought back
     * to memory and removed from the persistent store. This gives you the ability to 'overflow' to disk, similar to
     * swapping in an operating system. <br>
     * <br>
     * If false, the cache store contains a copy of the contents in memory, so writes to cache result in cache store
     * writes. This essentially gives you a 'write-through' configuration.
     */
    public static final String TRANSACTION__AUTO_COMMIT = "transaction.autoCommit";

    /**
     * If there are any ongoing transactions when a cache is stopped, Infinispan waits for ongoing remote and local
     * transactions to finish. The amount of time to wait for is defined by the cache stop timeout. It is recommended
     * that this value does not exceed the transaction timeout because even if a new transaction was started just before
     * the cache was stopped, this could only last as long as the transaction timeout allows it. <br>
     * This configuration property may be adjusted at runtime
     */
    public static final String TRANSACTION__CACHE_STOP_TIMEOUT = "transaction.cacheStopTimeout";

    /**
     * The duration (millis) in which to keep information about the completion of a transaction. Defaults to 15000.
     */
    public static final String TRANSACTION__COMPLETED_TX_TIMEOUT = "transaction.completedTxTimeout";

    /**
     * Configures whether the cache uses optimistic or pessimistic locking. If the cache is not transactional then the
     * locking mode is ignored.
     * 
     * <a href="http://community.jboss.org/wiki/OptimisticLockingInInfinispan">OPTIMISTIC</a> or PESSIMISTIC.
     */
    public static final String TRANSACTION__LOCKING_MODE = "transaction.lockingMode";

    public static final String TRANSACTION__LOCKING_MODE_OPT_OPTIMISTIC = "OPTIMISTIC";

    public static final String TRANSACTION__LOCKING_MODE_OPT_PESSIMISTIC = "PESSIMISTIC";

    /**
     * The time interval (millis) at which the thread that cleans up transaction completion information kicks in.
     * Defaults to 1000.
     */
    public static final String TRANSACTION__REAPER_WAKE_UP_INTERVAL = "transaction.reaperWakeUpInterval";

    /**
     * Enable recovery for this cache
     */
    public static final String TRANSACTION__RECOVERY__ENABLED = "transaction.recovery.enabled";

    /**
     * Sets the name of the cache where recovery related information is held. If not specified defaults to a cache named
     * {@link RecoveryConfiguration#DEFAULT_RECOVERY_INFO_CACHE}
     */
    public static final String TRANSACTION__RECOVERY__RECOVERY_INFO_CACHE_NAME =
            "transaction.recovery.recoveryInfoCacheName";

    /**
     * If true, the cluster-wide commit phase in two-phase commit (2PC) transactions will be synchronous, so Infinispan
     * will wait for responses from all nodes to which the commit was sent. Otherwise, the commit phase will be
     * asynchronous. Keeping it as false improves performance of 2PC transactions, since any remote failures are trapped
     * during the prepare phase anyway and appropriate rollbacks are issued. <br>
     * This configuration property may be adjusted at runtime
     */
    public static final String TRANSACTION__SYNC_COMMIT_PHASE = "transaction.syncCommitPhase";

    /**
     * If true, the cluster-wide rollback phase in two-phase commit (2PC) transactions will be synchronous, so
     * Infinispan will wait for responses from all nodes to which the rollback was sent. Otherwise, the rollback phase
     * will be asynchronous. Keeping it as false improves performance of 2PC transactions. <br>
     * 
     * This configuration property may be adjusted at runtime.
     */
    public static final String TRANSACTION__SYNC_ROLLBACK_PHASE = "transaction.syncRollbackPhase";

    /**
     * OSGi filter expression for the transaction manager service.
     */
    public static final String TRANSACTION__TRANSACTION_MANAGER__TARGET = "transactionManager.target";

    /**
     * DEFAULT: uses the 2PC protocol, TOTAL_ORDER: uses the total order protocol
     */
    public static final String TRANSACTION__TRANSACTION_PROTOCOL = "transaction.transactionProtocol";

    public static final String TRANSACTION__TRANSACTION_PROTOCOL_OPT_DEFAULT = "DEFAULT";

    public static final String TRANSACTION__TRANSACTION_PROTOCOL_OPT_TOTAL_ORDER = "TOTAL_ORDER";

    /**
     * OSGi filter expression for the transaction synchronization registry service.
     */
    public static final String TRANSACTION__TRANSACTION_SYNCHRONIZATION_REGISTRY__TARGET =
            "transactionSynchronizationRegistry.target";

    /**
     * Before Infinispan 5.1 you could access the cache both transactionally and non-transactionally. Naturally the
     * non-transactional access is faster and offers less consistency guarantees. From Infinispan 5.1 onwards, mixed
     * access is no longer supported, so if you wanna speed up transactional caches and you're ready to trade some
     * consistency guarantees, you can enable use1PcForAutoCommitTransactions. <br>
     * 
     * What this configuration option does is force an induced transaction, that has been started by Infinispan as a
     * result of enabling autoCommit, to commit in a single phase. So only 1 RPC instead of 2RPCs as in the case of a
     * full 2 Phase Commit (2PC).
     */
    public static final String TRANSACTION__USE_1PC_FOR_AUTO_COMMIT_TRANSACTIONS =
            "transaction.use1PcForAutoCommitTransactions";

    /**
     * Configures whether the cache registers a synchronization with the transaction manager, or registers itself as an
     * XA resource. It is often unnecessary to register as a full XA resource unless you intend to make use of recovery
     * as well, and registering a synchronization is significantly more efficient. <br>
     * If true, {@link Synchronization}s are used rather than {@link XAResource}s when communicating with a
     * {@link TransactionManager}.
     */
    public static final String TRANSACTION__USE_SYNCHRONIZATION = "transaction.useSynchronization";

    /**
     * Specify whether Infinispan is allowed to disregard the {@link Map} contract when providing return values for
     * {@link org.infinispan.Cache#put(Object, Object)} and {@link org.infinispan.Cache#remove(Object)} methods. <br>
     * Providing return values can be expensive as they may entail a read from disk or across a network, and if the
     * usage of these methods never make use of these return values, allowing unreliable return values helps Infinispan
     * optimize away these remote calls or disk reads. <br>
     * If true, return values for the methods described above should not be relied on.
     */
    public static final String UNSAFE__UNRELIABLE_RETURN_VALUES = "unsafe.unreliableReturnValues";

    public static final String VERSIONING__ENABLED = "versioning.enabled";

    public static final String VERSIONING__SCHEME = "versioning.scheme";

    public static final String VERSIONING__SCHEME_OPT_NONE = "NONE";

    public static final String VERSIONING__SCHEME_OPT_SIMPLE = "SIMPLE";

    public static final String COMMON__BOOLEAN_OPT_DEFAULT = "default";

    public static final String COMMON__BOOLEAN_OPT_TRUE = "true";

    public static final String COMMON__BOOLEAN_OPT_FALSE = "false";

    private CacheConfigurationConstants() {
    }
}
