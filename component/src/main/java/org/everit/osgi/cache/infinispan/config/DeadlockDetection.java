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

public class DeadlockDetection {

    private Boolean enabled = null;

    private Long spinDuration = null;

    public Boolean getEnabled() {
        return enabled;
    }

    public Long getSpinDuration() {
        return spinDuration;
    }

    public void setEnabled(final Boolean enabled) {
        this.enabled = enabled;
    }

    public void setSpinDuration(final Long spinDuration) {
        this.spinDuration = spinDuration;
    }

}
