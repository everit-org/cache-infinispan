cache-infinispan-component
==========================

An Infinispan based component which implements the `org.everit.osgi.cache.api`
so it can configure and return a ready to use cache instance. 


# Introduction

The aim of the cache-infinispan-component is to provide a simple way to create 
and customize `java.util.concurrent.ConcurrentMap` instances.
With the help of these components you can obtain a cache instance with 
Distribution/Invalidation/Replication clustering or Local data storage modes
just in a few lines of code.


# Usage

In order to use the component you need to add this dependency to the pom.xml:

    <dependency>
    	<groupId>org.everit.osgi</groupId>
    	<artifactId>org.everit.osgi.cache.infinispan</artifactId>
    	<version>1.0.0</version>
    </dependency>

At next, it is required to define a configuration with [ConfigurationAdmin]
(http://blog.osgi.org/2010/06/how-to-use-config-admin.html), for example 
with the [Felix Web Console](http://felix.apache.org/site/apache-felix-web-console.html).

First, a new instance of CacheFactory component must be configured. Secondly,
a CacheConfiguration component should be configured. Both components will
register OSGi services.

Having those services, the factory can be used in the following way:

```java
    CacheHolder<String, String> cacheHolder = cacheFactory.createCache(
            cacheConfiguration, this.getClass().getClassLoader());
    ConcurrentMap<String, String> cache = cacheHolder.getCache();
    cache.put("1", "1");
       ...
    cacheHolder.close();
```
