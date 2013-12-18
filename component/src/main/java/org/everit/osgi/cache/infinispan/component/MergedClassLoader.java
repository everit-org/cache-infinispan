package org.everit.osgi.cache.infinispan.component;

/*
 * Copyright (c) 2011, Everit Kft.
 *
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

public class MergedClassLoader extends ClassLoader {

    private final List<ClassLoader> wrapped;

    public MergedClassLoader(final ClassLoader[] wrapped) {
        this.wrapped = new ArrayList<ClassLoader>(Arrays.asList(wrapped));
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        Iterator<ClassLoader> iterator = wrapped.iterator();
        Class<?> result = null;
        while ((result == null) && iterator.hasNext()) {
            ClassLoader classLoader = iterator.next();
            try {
                result = classLoader.loadClass(name);
            } catch (ClassNotFoundException e) {
                // Do nothing
            }
        }
        if (result == null) {
            throw new ClassNotFoundException(name);
        } else {
            return result;
        }
    }

    @Override
    protected URL findResource(final String name) {
        Iterator<ClassLoader> iterator = wrapped.iterator();
        URL result = null;
        while ((result == null) && iterator.hasNext()) {
            ClassLoader classLoader = iterator.next();
            result = classLoader.getResource(name);
        }

        return result;
    }

    @Override
    protected Enumeration<URL> findResources(final String name) {
        Iterator<ClassLoader> iterator = wrapped.iterator();
        List<URL> result = new ArrayList<URL>();
        // Class<?> result = null;
        while (iterator.hasNext()) {
            ClassLoader classLoader = iterator.next();
            URL currentUrl = classLoader.getResource(name);
            if (currentUrl != null) {
                result.add(currentUrl);
            }
        }

        return Collections.enumeration(result);
    }
}
