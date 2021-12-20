# Introduction

This library generates a properties file from the Java source files' Javadocs. It is implemented as 
Doclet and to be used together with `javadoc`.

This version is supporting Java 8 and also the the new `jdk.javadoc.doclet` API from JDK 9 
depending on the used Java version.

# Features

The library in its current state was created for getting a data model documentation of entities or DTOs for the 
ALPS or JSON schema documentation used in [Spring Data REST API](https://docs.spring.io/spring-data/rest/docs/current/reference/html/#metadata.alps.descriptions). It is also useful for the
[OpenAPI documentation (former Swagger)](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.0.md) using 
property files in the annotations, e.g. in ` ApiModelProperty` support by [SpringFox](https://springfox.github.io/springfox/).

The project is using the [Doclet API](https://docs.oracle.com/javase/6/docs/jdk/api/javadoc/doclet/index.html) 
and the [Doclet API](https://docs.oracle.com/javase/9/docs/api/jdk/javadoc/doclet/package-summary.html) internally.

Supported:
* Prefix for property keys
* Output file naming
* Excludes
* Includes

This is the first version and has the following limitations:

* No methods are scanned
* No tags are used

# Usage

## Cmd Line:

```
        javadoc -doclet de.ohmesoftware.javadoctoproperties.Converter -docletpath target/classes -sourcepath src/main/java de.example.my.model
```

## Maven

```xml
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-javadoc-plugin</artifactId>
                <version>3.2.0</version>
                <executions>
                    <execution>
                        <id>javadoc-test</id>
                        <goals>
                            <goal>jar</goal>
                        </goals>
                        <phase>test</phase>
                        <configuration>
                            <!-- use for Java 8 -->
                            <!--doclet>de.ohmesoftware.javadoctoproperties.Converter</doclet-->
                            <doclet>de.ohmesoftware.javadoctoproperties.Converter9</doclet>
                            <additionalOptions>-prefix rest.description -output src/main/resources/mydocs.properties</additionalOptions>
                            <additionalOptions>-i *Foo* -e *model.Bar*</additionalOptions>
                            <debug>true</debug>
                            <docletPath>${project.build.outputDirectory}</docletPath>
                            <sourcepath>${project.basedir}/src/main/java/foo/bar/model</sourcepath>
                        </configuration>
                    </execution>
                </executions>
                <dependencies>
                    <dependency>
                        <groupId>de.ohmesoftware</groupId>
                        <artifactId>javadoctoproperties</artifactId>
                        <version>0.0.5</version>
                    </dependency>
                </dependencies>
            </plugin>
```

# Build

For the compilation the environment property `JAVA_8_HOME` must bet set point to a Java 8 JDK.
Java9+ is needed for the compilation.

# Deployment + Release

See https://central.sonatype.org/pages/apache-maven.html

The login page of Nexus is: https://oss.sonatype.org/

## Preparations

* Set up a PGP key for signing the jar
* In the Maven `settings.xml` include:

```
 <servers>
        <server>
            <id>ossrh</id>
            <username>your username</username>
            <password>your password</password>
        </server>
    </servers>

    <profiles>
        <profile>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <properties>
                <gpg.javadocproperties.keyname>pgp signign identity (= email address)</gpg.javadocproperties.keyname>
            </properties>
        </profile>
    </profiles>
```

# For Snapshots

    mvn clean deploy

## For Releases

```
mvn release:clean release:prepare
mvn release:perform
```

Release the deployment using Nexus See https://central.sonatype.org/pages/releasing-the-deployment.html
Or do it with Maven:

```
cd target/checkout
mvn nexus-staging:release
```
