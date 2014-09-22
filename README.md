cache-infinispan-component
==========================

An Infinispan based components that implement `org.everit.osgi.cache.api`.

# Usage

In order to use the component you need to add this dependency to the pom.xml:

    <dependency>
    	<groupId>org.everit.osgi</groupId>
    	<artifactId>org.everit.osgi.cache.infinispan</artifactId>
    	<version>1.0.0</version>
    </dependency>

Two configurations must be defined. One for CacheConfiguration and one for
CacheFactory. See the metatype information about configuration possibilities.

To see an example about the usage of the API, please see the [README file][1]
of the API project.

[![Analytics](https://ga-beacon.appspot.com/UA-15041869-4/everit-org/cache-infinispan)](https://github.com/igrigorik/ga-beacon)

[1]: https://github.com/everit-org/cache-api/blob/master/README.md
