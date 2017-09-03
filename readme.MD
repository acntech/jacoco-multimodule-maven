# Jacoco coverage in multi-module maven projects

Simple dummy-project that demonstrates how to configure the jacoco code coverage 
library in a multi-module maven project with integration-tests in order to get
a complete coverage-report.

### Problem
One of the problems with jacoco and maven is that coverage is only 
reported for code in the module in which the tests are located and not for
the entire project as a whole. This means that the coverage-percent will
be under-reported in many cases. 

### Solution
In this example class A in module-a calls class B in module-B which calls class C
in module-c. All the tests are located in module-a. With standard configuration class A
is the only class that will get coverage in the jacoco report. 

By using report-appending in jacoco we can fix this and get coverage for all the classes
in the whole project and not just for the classes in the module where the tests resides.

##### Root pom.xml

Properties:
```xml
<properties>
    <jacoco.version>0.7.9</jacoco.version>
    <sonar.jacoco.reportPaths>${project.basedir}/../target/jacoco.exec</sonar.jacoco.reportPaths>
    <sonar.jacoco.itReportPath>${project.basedir}/../target/jacoco-it.exec</sonar.jacoco.itReportPath>
    <sonar.language>java</sonar.language>
    <sonar.java.coveragePlugin>jacoco</sonar.java.coveragePlugin>
</properties>
````

Configuration of the jacoco maven plugin:
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>${jacoco.version}</version>
    <executions>
        <!-- Unit-tests -->
        <execution>
            <id>agent-for-ut</id>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
            <configuration>
                <append>true</append>
                <destFile>${sonar.jacoco.reportPaths}</destFile>
            </configuration>
        </execution>
        <!-- Integration-tests executed with failsafe-plugin -->
        <execution>
            <id>agent-for-it</id>
            <phase>package</phase>
            <goals>
                <goal>prepare-agent-integration</goal>
            </goals>
            <configuration>
                <append>true</append>
                <destFile>${sonar.jacoco.itReportPath}</destFile>
            </configuration>
        </execution>
    </executions>
</plugin>
```

##### Nested submodules
No further configuration is required if you only have one level of submodules.
In our example we have two levels of submodules for module-c.
```
root
 - module-a
 - module-b
 - module-c
     - module-c1
```

To get nested submodules to append to the report we have to override the jacoco properties
we specified in the root pom:
```
<sonar.jacoco.reportPaths>${project.basedir}/../../target/jacoco.exec</sonar.jacoco.reportPaths>
<sonar.jacoco.itReportPath>${project.basedir}/../../target/jacoco-it.exec</sonar.jacoco.itReportPath>
```

Notice that we need to traverse up to the folder of the root module in the file path.

### Testing
Run sonar in a docker container
`docker run -d --name sonarqube -p 9000:9000 -p 9092:9092 sonarqube`

Build the project with integration-tests and and publish to sonar
`mvn clean install failsafe:integration-test sonar:sonar`

You should now have jacoco.exec and jacoco-it.exec in the target-folder of the root-module.

Open localhost:9000 and verify that the coverage is 100%.