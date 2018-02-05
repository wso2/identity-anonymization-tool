# Tool for removing/Replacing all identifieres matching given criteria

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
