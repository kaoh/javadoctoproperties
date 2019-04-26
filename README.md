# Introduction

This library generates a properties file as string as output from Java source files' Javadocs. It is implemented as 
Doclet and to be used together with `javadoc`.

# Features

The library in its current state was created for getting a data model documentation of entities or DTOs for the 
ALPS or JSON schema documentation used in [Spring Data REST API](https://docs.spring.io/spring-data/rest/docs/current/reference/html/#metadata.alps.descriptions). It is also useful for the
[OpenAPI documentation (former Swagger)](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.0.md) using 
property files in the annotations, e.g. in ` ApiModelProperty` support by [SpringFox](https://springfox.github.io/springfox/).

It is using the [Doclet API](https://docs.oracle.com/javase/6/docs/jdk/api/javadoc/doclet/index.html) internally.

This is the first version and has the following limitations:

* No methods are scanned
* No tags are used

# Usage

```
        javadoc -doclet de.ohmesoftware.javadoctoproperties.Converter -docletpath target/classes -sourcepath src/main/java de.example.my.model
```

## Maven Dependency

```
    <groupId>de.ohmesoftware</groupId>
    <artifactId>javadoctoproperties</artifactId>
    <version>0.0.1-SNAPSHOT</version>
```

# Deployment + Release

See https://central.sonatype.org/pages/apache-maven.html


# For Snapshots

    mvn clean deploy

## For Releases

```
mvn release:clean release:prepare
mvn release:perform
```

Release the deployment using Nexus See https://central.sonatype.org/pages/releasing-the-deployment.html
Or alternatively do it with Maven:

```
cd target/checkout
mvn nexus-staging:release
```
