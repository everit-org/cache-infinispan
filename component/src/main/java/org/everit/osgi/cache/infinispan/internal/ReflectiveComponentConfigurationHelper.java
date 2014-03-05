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

import java.util.Map;

import org.osgi.service.component.ComponentException;

public class ReflectiveComponentConfigurationHelper {

    private final Map<String, ?> configuration;

    public ReflectiveComponentConfigurationHelper(final Map<String, ?> configuration) {
        this.configuration = configuration;
    }

    public Class<?> classify(final Class<?> potentiallyTypeOfPrimitive) {
        if (potentiallyTypeOfPrimitive.isAssignableFrom(boolean.class)) {
            return Boolean.class;
        }
        if (potentiallyTypeOfPrimitive.isAssignableFrom(int.class)) {
            return Integer.class;
        }
        if (potentiallyTypeOfPrimitive.isAssignableFrom(long.class)) {
            return Long.class;
        }
        return potentiallyTypeOfPrimitive;
    }

    public Object getObjectValue(final String key, final boolean mandatory) {
        Object value = configuration.get(key);
        if ((value == null) && mandatory) {
            throw new ComponentException("The value of the mandatory configuration property '" + key
                    + "' is not defined.");
        }
        return value;
    }

    public <V> V getPropValue(final String key, final Class<V> valueType,
            final boolean mandatory) {
        Object value = getObjectValue(key, mandatory);
        if ((value == null) || (String.class.isInstance(value) && ((String) value).trim().equals(""))) {
            System.out.println(" VALUEEEEEEEEE NULL " + key);
            if (mandatory) {
                System.out.println("AND MANDATORY");
                throw new ComponentException("Value of mandatory configuration " + key + " is not specified.");
            } else {
                return null;
            }
        }
        if (!classify(valueType).isInstance(value)) {
            throw new ComponentException("Type of configuration property " + key + " must be " + valueType.toString()
                    + ". Current type is " + value.getClass().toString());
        }
        return (V) value;
    }
}
