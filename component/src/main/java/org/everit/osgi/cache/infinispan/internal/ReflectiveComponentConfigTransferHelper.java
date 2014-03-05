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

public class ReflectiveComponentConfigTransferHelper {

    private final Map<String, Object> targetConfig;

    private final ReflectiveComponentConfigurationHelper configHelper;

    public ReflectiveComponentConfigTransferHelper(Map<String, Object> componentConfig, Map<String, Object> targetConfig) {
        this.configHelper = new ReflectiveComponentConfigurationHelper(componentConfig);
        this.targetConfig = targetConfig;
    }

    /**
     * Transfer an entry from the component configuration to the target configuration if it exists in the component
     * configuration. If the value type is an enum, the component configuration will be queried as string and it will be
     * converted to the enum type in the target configuration.
     * 
     * @param key
     *            The key of the entry.
     * @param valueType
     *            The type of the entry. If type does not match, a {@link ComponentException} will be thrown.
     * @param mandatory
     *            If mandatory and the entry does not exist in the component configuration, a {@link ComponentException}
     *            will be thrown.
     * @return The value of the entry or null if not mandatory and the entry did not exist with the specified key in the
     *         component configuration.
     */
    public <V> V transferEntry(String key, Class<V> valueType, boolean mandatory) {
        Object value = null;
        if (Enum.class.isAssignableFrom(valueType)) {
            String stringValue = configHelper.getPropValue(key, String.class, mandatory);
            if (stringValue != null) {

                @SuppressWarnings({ "rawtypes", "unchecked" })
                Class<Enum> enumType = (Class<Enum>) valueType;

                @SuppressWarnings("unchecked")
                Enum<?> enumValue = Enum.valueOf(enumType, stringValue);
                value = enumValue;
            }
        } else {
            value = configHelper.getPropValue(key, valueType, mandatory);
        }

        if (mandatory || value != null) {
            targetConfig.put(key, value);
        }
        V typedValue = (V) value;
        return typedValue;
    }

    public ReflectiveComponentConfigurationHelper getConfigHelper() {
        return configHelper;
    }

}
