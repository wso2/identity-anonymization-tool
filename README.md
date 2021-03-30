# Tool for removing/Replacing all identifiers matching given criteria

## How to Build

**Make sure you have JDK 8 (JDK 11 not supported)**

```
mvn clean install
mvn package
```

## How to run
```
cd components/org.wso2.carbon.privacy.forgetme.tool
cd target/dist/bin
./forget-me -U <userName> [-D domainName] [-TID tenantId]
```

[For more information please refer the help document](components/org.wso2.carbon.privacy.forgetme.tool/src/main/resources/help.md)

## Components
- [Log Statements Scanner Tool](components/org.wso2.carbon.privacy.forgetme.log-statements-scanner)
- [Persisted Streams in DAS 3.1.0](components/org.wso2.carbon.privacy.forgetme.analytics.streams)

## How to debug the tool

To debug the tool remotely do the following.

####Linux:

Execute following commands in the shell that the tool in running. 

 * ```JAVA_OPTS="-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=5005,suspend=y"```
 * ```export JAVA_OPTS```
 * ```./forgetme.sh <arguments>```

Use IDEs remote debugging feature to connect to port 5005. 

####Windows
