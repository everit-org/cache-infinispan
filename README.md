cache-infinispan-component
==========================

An Infinispan based component which implements the `org.everit.osgi.cache.api`
so it can configure and return a ready to use `javax.cache.Cache` instance. 


# Introduction

The aim of the cache-infinispan-component is to provide a simple way to create 
and customize `javax.Cache` instances.
With the help of this component you can obtain a Cache instance with 
Distribution/Invalidation/Replication or Local data storage modes just in a 
few lines of code.


# Usage

In order to use the component you need to add this dependency to the pom.xml:

    <dependency>
    	<groupId>org.everit.osgi.cache.infinispan</groupId>
    	<artifactId>org.everit.osgi.cache.infinispan.component</artifactId>
    	<version>0.1.0-SNAPSHOT</version>
    </dependency>

At next, it is required to define a configuration with [ConfigurationAdmin]
(http://blog.osgi.org/2010/06/how-to-use-config-admin.html), for example 
with the [Felix Web Console]
(http://felix.apache.org/site/apache-felix-web-console.html),
containing some of the following properties:

 - Clustername: The name of the created cluster
 - UDP multicast address: An IP address which will be used by the underlying 
 jgroups component, represented in a string.
 - UDP multicast port: A Port number, also used by the jgroups component,
  represented in a string.
  
This component is a Service Factory, which means that with each configuration 
defined, a matching CacheFactoryComponent will be created.

After the bundle has started, the simplest way to obtain a cache instance when 
cacheFactory is an instance of org.everit.osgi.cache.api.CacheFactory is:

    Cache<String, Object> cache = cacheFactory.createCache(10, null);
    
Available arguments of the crateCache function:
 - maxEntries: An Integer which defines the maximum number of entries in a
   cache instance.
 - options: Additional settings for the cache. Could be null or a Map.
 	The available additional settings are:
 	 - PARAM_CACHEMODE
 	 - PARAM_WAKEUPINTERVAL
 	 - PARAM_MAXIDLE
 	 - PARAM_NUMOWNERS

Now we have our cache instance, so we can start using it.

    cache.put("key", "value");
    cache.get("key") == "value"; //true

And so on...


# Example

The additional options can be defined in a way like this:

```java
Map<String, Object> config = new HashMap<String, Object>();
config.put(Constants.PARAM_MAXIDLE, 1000L);
config.put(Constants.PARAM_CACHEMODE, Constants.CACHEMODE_DIST_SYNC);
Cache<String, Object> cache = cacheFactory.createCache(30, config);
cache.put("testkey", "testvalue");
```
