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

import javax.cache.Cache;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.cache.api.CacheConfiguration;
import org.everit.osgi.cache.api.CacheFactory;
import org.everit.osgi.dev.testrunner.TestDuringDevelopment;
import org.junit.Assert;
import org.junit.Test;

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
    @Reference(bind = "setCacheConfiguration")
    private CacheConfiguration<String, String> cacheConfiguration;

    @Reference(bind = "setCacheFactory")
    private CacheFactory cacheFactory;

    public void setCacheConfiguration(CacheConfiguration<String, String> cacheConfiguration) {
        this.cacheConfiguration = cacheConfiguration;
    }

    public void setCacheFactory(CacheFactory cacheFactory) {
        this.cacheFactory = cacheFactory;
    }

    @Test
    public void simpleTest() {
        Cache<String, String> cache = cacheFactory.createCache(cacheConfiguration, this.getClass().getClassLoader());
        cache.put("1", "1");
        Assert.assertEquals("1", cache.get("1"));

        cache.close();
    }
}
