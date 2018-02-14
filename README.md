# Tool for removing/Replacing all identifiers matching given criteria

## How to Build

```
mvn clean install
mvn package
```

## How to run
```
cd components/org.wso2.carbon.privacy.forgetme.tool
cd target/dist/bin
./forget-m -d <config-dir> -U <userName> [-D domainName] [-T tenantDomain]

```

[For more information please refer the help document](components/org.wso2.carbon.privacy.forgetme.tool/src/main/resources/help.md)

