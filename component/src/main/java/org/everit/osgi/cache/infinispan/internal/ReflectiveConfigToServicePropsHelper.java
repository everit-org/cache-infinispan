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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.infinispan.configuration.cache.Configuration;
import org.osgi.service.component.ComponentException;

public class ReflectiveConfigToServicePropsHelper {

    private final Map<String, Object> serviceProperties;

    private final Configuration configuration;

    public ReflectiveConfigToServicePropsHelper(final Configuration configuration,
            final Map<String, Object> serviceProperties) {
        this.configuration = configuration;
        this.serviceProperties = serviceProperties;
    }

    public <V> V transferProperty(final String key) {
        String[] keyParts = key.split("\\.");
        Object currentConfigObject = configuration;
        V result = null;
        try {
            for (int i = 0, n = keyParts.length; i < n; i++) {
                if (i < (n - 1)) {
                    Method method = currentConfigObject.getClass().getMethod(keyParts[i]);
                    currentConfigObject = method.invoke(currentConfigObject);
                    if (currentConfigObject == null) {
                        return null;
                    }
                } else {
                    Method method = currentConfigObject.getClass().getMethod(keyParts[i]);
                    @SuppressWarnings("unchecked")
                    V value = (V) method.invoke(currentConfigObject);
                    if (value != null) {
                        serviceProperties.put(key, value);
                        result = value;
                    }
                }
            }
            return result;
        } catch (NoSuchMethodException e) {
            throw new ComponentException("Could not set configuration with key " + key, e);
        } catch (SecurityException e) {
            throw new ComponentException("Could not set configuration with key " + key, e);
        } catch (IllegalAccessException e) {
            throw new ComponentException("Could not set configuration with key " + key, e);
        } catch (IllegalArgumentException e) {
            throw new ComponentException("Could not set configuration with key " + key, e);
        } catch (InvocationTargetException e) {
            throw new ComponentException("Could not set configuration with key " + key, e);
        }

    }
}
