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

public class Hash {

    private Float capacityFactor;

    private Integer numOwners;

    private Integer numSegments;

    public Float getCapacityFactor() {
        return capacityFactor;
    }

    public void setCapacityFactor(Float capacityFactor) {
        this.capacityFactor = capacityFactor;
    }

    public Integer getNumOwners() {
        return numOwners;
    }

    public void setNumOwners(Integer numOwners) {
        this.numOwners = numOwners;
    }

    public Integer getNumSegments() {
        return numSegments;
    }

    public void setNumSegments(Integer numSegments) {
        this.numSegments = numSegments;
    }

}
