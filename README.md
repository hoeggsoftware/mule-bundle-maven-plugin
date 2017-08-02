Mule Bundle Maven Plugin
====================

The Mule Bundle Maven Plugin allows you to package your Mule applications together into a single application bundle for
deployment.

Maven Configuration
----------------------------------------
To generate a mule application bundle, create a new maven project. This project will serve as the application bundle,
and will include each of the intended Mule applications as maven dependencies.

To make this work, configure the project to use this plugin, set the project's packaging to `mule-bundle`, and add
dependencies for each of the applications you want included in the bundle.

#### Packaging
The bundle project must use `mule-bundle` as its packaging, which means it needs to generate a Mule Bundle artifact.
Do this by including the `packaging` element in your `pom.xml`:

```xml
    <project ... > 
        <groupId>com.smartcompany.mule</groupId>
        <artifactId>our-mule-bundle</artifactId>
        <version>1.0.2-SNAPSHOT</version>
        <packaging>mule-bundle</packaging>
```

#### Plugin Configuration
Your bundle project must also explicitly include this plugin. Add it to the `<plugins>` section of your pom: 
```xml
<build>
    <plugins>
        <plugin>
            <groupId>software.hoegg.mule</groupId>
            <artifactId>mule-bundle-maven-plugin</artifactId>
            <version>1.0.0</version>
            <extensions>true</extensions>
        </plugin>
    </plugins>
</build>
```
The `extensions` element is essential, as it allows the plugin to let Maven know it can support the packaging type
`mule-bundle` we just configured in the last step.

#### Dependencies
Now that we've got most of the paperwork done, we can actually start putting things into our bundle. We do this
using Maven's built-in dependency support, which means we need to make sure we've released each of the applications
we want to bundle. This means each of them is available in our maven repository, using a respectable, immutable version
like 2.3.1, and without any suffix such as `SNAPSHOT`. 

Our bundle dependencies might look something like this:

```xml
<dependencies>
    <dependency>
        <groupId>com.smartcompany.mule.microservice</groupId>
        <artifactId>widget-inventory-system-api</artifactId>
        <version>1.1.9</version>
        <type>zip</type>
    </dependency>
    <dependency>
        <groupId>com.smartcompany.mule.microservice</groupId>
        <artifactId>widget-restocking-system-api</artifactId>
        <version>1.0.0</version>
        <type>zip</type>
    </dependency>
    <dependency>
        <groupId>com.smartcompany.mule.microservice</groupId>
        <artifactId>widget-replenishment-process-api</artifactId>
        <version>1.0.3</version>
        <type>zip</type>
    </dependency>
</dependencies>
```

We hope that you will be able to make use of this plugin. Please feel free to open isssues, or email us about any
trouble you have getting it to work for you. 