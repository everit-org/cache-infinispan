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

import org.osgi.service.component.ComponentException;

public class ReflectiveConfigurationBuilderHelper {

    private final Object builder;

    private final ReflectiveComponentConfigurationHelper componentConfigHelper;

    public ReflectiveConfigurationBuilderHelper(Map<String, ?> configuration, Object builder) {
        this.componentConfigHelper = new ReflectiveComponentConfigurationHelper(configuration);
        this.builder = builder;
    }

    public ReflectiveComponentConfigurationHelper getComponentConfigHelper() {
        return componentConfigHelper;
    }

    public void applyConfigOnBuilderValue(final String key,
            Class<?> valueType, boolean mandatory) {
        Object propValue = componentConfigHelper.getPropValue(key, valueType, mandatory);
        if (propValue == null) {
            return;
        }
        applyValue(key, propValue, valueType);
    }

    public void applyValue(String key, Object value, Class<?> valueType) {
        String[] keyParts = key.split("\\.");
        Object currentBuilderObject = builder;
        try {
            for (int i = 0, n = keyParts.length; i < n; i++) {
                if (i < n - 1) {
                    Method method = currentBuilderObject.getClass().getMethod(keyParts[i]);
                    currentBuilderObject = method.invoke(currentBuilderObject);

                } else {
                    Method method = currentBuilderObject.getClass().getMethod(keyParts[i], valueType);
                    method.invoke(currentBuilderObject, value);
                }
            }
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
