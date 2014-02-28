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

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.dev.testrunner.TestDuringDevelopment;
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

    @Test
    public void simpleTest() {

    }
}
