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

    /**
     * Copies a value from a configuration to the builder if the value is not null.
     * 
     * @param key
     *            The key of the configuration entry.
     * @param valueType
     *            The target type of the value.
     * @param mandatory
     *            Whether the configuration entry is mandatory or not.
     */
    public <T> T applyConfigOnBuilderValue(final String key,
            Class<T> valueType, boolean mandatory) {
        Object value = null;
        if (Enum.class.isAssignableFrom(valueType)) {
            String stringValue = componentConfigHelper.getPropValue(key, String.class, mandatory);
            if (stringValue != null) {

                @SuppressWarnings({ "rawtypes", "unchecked" })
                Class<Enum> enumType = (Class<Enum>) valueType;

                @SuppressWarnings("unchecked")
                Enum<?> enumValue = Enum.valueOf(enumType, stringValue);
                value = enumValue;
            }
        } else {
            value = componentConfigHelper.getPropValue(key, valueType, mandatory);
        }

        @SuppressWarnings("unchecked")
        T propValue = (T) value;

        if (propValue == null) {
            return null;
        }
        applyValue(key, propValue, valueType);
        return propValue;
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
