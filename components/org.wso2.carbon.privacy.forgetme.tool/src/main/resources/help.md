# Anonymize the reference to a user name

## How to run

> forget-me \[options]>

## Options

###### Option U
User Name (mandatory)

example
>-U john.doe

###### Option d
Configuration Directory (mandatory)

example
> -d /users/john/forgetme/config

###### Option T
Tenant Domain (optional) Default = “carbon.super”

example
>-T acme-company

###### Option TID
Tenant ID. You need to specify this parameter
        if you specify \<T tenant-domain\> option.

example
> -TID 2346

###### Option D
User Store Domain (optional)|Default = “PRIMARY”

example
> -D Finance-Domain

###### Option sha256
To enable SHA256 hashing for anonymizing the given ID attribute (optional)
> -sha256

###### Option pu
The pseudonym which the user name needs to be
replaced with. (optional)  Default = A random UUID
value is generated

example
> -pu “123-343-435-545-dfd-4”

######Option carbon
The CARBON HOME. This is replaced with variables
$CARBON_HOME in directories configured in main
config file

example
> -carbon “/usr/bin/wso2is/wso2is5.4.1”



