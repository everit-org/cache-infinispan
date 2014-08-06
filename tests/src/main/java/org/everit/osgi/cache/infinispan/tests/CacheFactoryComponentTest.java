/**
 * This file is part of Everit - Infinispan Cache Tests.
 *
 * Everit - Infinispan Cache Tests is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Infinispan Cache Tests is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Infinispan Cache Tests.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.osgi.cache.infinispan.tests;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.cache.CacheConfiguration;
import org.everit.osgi.cache.CacheFactory;
import org.everit.osgi.cache.CacheHolder;
import org.everit.osgi.cache.infinispan.config.CacheFactoryProps;
import org.everit.osgi.cache.infinispan.config.CacheProps;
import org.everit.osgi.dev.testrunner.TestDuringDevelopment;
import org.everit.osgi.transaction.helper.api.Callback;
import org.everit.osgi.transaction.helper.api.TransactionHelper;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.service.component.ComponentException;

/**
 * Unit test for CacheFactoryComponent.
 */
@Component(name = "CacheFactoryComponentTest", immediate = true)
@Service(value = CacheFactoryComponentTest.class)
@Properties({
        @Property(name = "eosgi.testId", value = "cachefactorycomponentTest"),
        @Property(name = "eosgi.testEngine", value = "junit4")
})
@TestDuringDevelopment
public class CacheFactoryComponentTest
{
    @Reference(target = "(" + CacheFactoryProps.GLOBAL_JMX_STATISTICS__ENABLED + "=true)")
    private CacheFactory jmxCacheFactory;

    @Reference(target = "(" + CacheProps.CACHE_NAME + "=simpleCache)")
    private CacheConfiguration<String, String> simpleCacheConfiguration;

    @Reference(target = "(" + CacheFactoryProps.GLOBAL_JMX_STATISTICS__ENABLED + "=false)")
    private CacheFactory simpleCacheFactory;

    @Reference
    private TransactionHelper t;

    @Reference(target = "(" + CacheProps.CACHE_NAME + "=transactionalCache)")
    private CacheConfiguration<String, String> transactionalCacheConfiguration;

    public void setCacheConfiguration(final CacheConfiguration<String, String> cacheConfiguration) {
        this.simpleCacheConfiguration = cacheConfiguration;
    }

    public void setCacheFactory(final CacheFactory cacheFactory) {
        this.simpleCacheFactory = cacheFactory;
    }

    @Test
    public void simpleTest() {
        CacheHolder<String, String> cacheHolder = simpleCacheFactory.createCache(
                simpleCacheConfiguration, this.getClass().getClassLoader());
        ConcurrentMap<String, String> cache = cacheHolder.getCache();
        cache.put("1", "1");
        Assert.assertEquals("1", cache.get("1"));
        cacheHolder.close();
    }

    @Test
    public void testDoubleCacheNameError() {
        CacheHolder<String, String> cacheHolder = simpleCacheFactory.createCache(simpleCacheConfiguration, this
                .getClass()
                .getClassLoader());

        try {
            simpleCacheFactory.createCache(simpleCacheConfiguration, this.getClass()
                    .getClassLoader());
            Assert.fail("Exception should have been thrown if a cache with the same cache name are requested twice");
        } catch (ComponentException e) {
            // good
        }

        cacheHolder.close();
    }

    @Test
    public void testTwoCacheFactoriesWithSameCacheName() {
        CacheHolder<String, String> cache1 = simpleCacheFactory.createCache(simpleCacheConfiguration, this.getClass()
                .getClassLoader());
        CacheHolder<String, String> cache2 = jmxCacheFactory.createCache(simpleCacheConfiguration, this.getClass()
                .getClassLoader());

        cache1.getCache().put("1", "one");
        Assert.assertNull(cache2.getCache().get("1"));
        cache1.close();
        cache2.close();
    }

    @Test
    public void transactionalWithCommitTest() {
        CacheHolder<String, String> cacheHolder = simpleCacheFactory.createCache(transactionalCacheConfiguration, this
                .getClass().getClassLoader());

        final ConcurrentMap<String, String> cache = cacheHolder.getCache();

        final Object mutex = new Object();
        final AtomicBoolean entryInserted = new AtomicBoolean(false);
        final AtomicBoolean entryChecked = new AtomicBoolean(false);
        final AtomicBoolean transactionCommited = new AtomicBoolean(false);

        new Thread(new Runnable() {

            public void run() {
                t.required(new Callback<Object>() {

                    public Object execute() {
                        synchronized (mutex) {
                            cache.put("1", "one");
                            entryInserted.set(true);
                            mutex.notify();
                        }
                        synchronized (mutex) {
                            if (!entryChecked.get()) {
                                try {
                                    mutex.wait();
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                            }
                        }
                        return null;
                    }
                });
                synchronized (mutex) {
                    transactionCommited.set(true);
                    mutex.notify();
                }
            }
        }).start();

        synchronized (mutex) {
            if (!entryInserted.get()) {
                try {
                    mutex.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        Assert.assertNull(cache.get("1"));
        synchronized (mutex) {
            entryChecked.set(true);
            mutex.notify();
        }
        synchronized (mutex) {
            if (!transactionCommited.get()) {
                try {
                    mutex.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        Assert.assertEquals(cache.get("1"), "one");
        cacheHolder.close();
    }

    @Test
    public void transactionWithRollbackTest() {
        CacheHolder<String, String> cacheHolder = simpleCacheFactory.createCache(transactionalCacheConfiguration, this
                .getClass().getClassLoader());

        final ConcurrentMap<String, String> cache = cacheHolder.getCache();
        try {
            t.required(new Callback<Object>() {
                public Object execute() {
                    cache.put("1", "one");
                    throw new RuntimeException();
                }
            });
            Assert.fail("Code should not be reachable.");
        } catch (RuntimeException e) {
            Assert.assertNull(cache.get("1"));
        }
        cacheHolder.close();
    }
}
