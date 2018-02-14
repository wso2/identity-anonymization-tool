# Anonymize the reference to a user name

## How to run

forget-me <options>

## Options

| Option        | Description           | Example  |
| ------------- |:-------------:| -----:|
| U     |User Name (mandatory)      |-U john.doe|
|d|Configuration Directory (mandatory)|-d /users/john/forgetme/config|
|T|Tenant Domain (optional)|Default = “carbon.super”||-T acme-company|
|TID|Tenant ID. You need to specify this parameter if you specify \<T tenant-domain\> option. |-TID 2346
|D|User Store Domain (optional)|Default = “PRIMARY”|-D Finance-Domain|
|pu |The pseudonym which the user name needs to be replaced with. (optional)    |Default = A random UUID value is generated     |-pu “123-343-435-545-dfd-4”|
|carbon |The CARBON HOME    |This is replaced with variables $CARBON_HOME in directories configured in main config file[1]  |-carbon “/usr/bin/wso2is/wso2is5.4.1”|



