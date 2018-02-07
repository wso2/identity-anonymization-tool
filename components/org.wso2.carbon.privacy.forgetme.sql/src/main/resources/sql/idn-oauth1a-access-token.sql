UPDATE IDN_OAUTH1A_ACCESS_TOKEN
SET AUTHZ_USER = `pseudonym`
WHERE AUTHZ_USER =  `username`
      AND TENANT_ID = (SELECT UM_ID
                       FROM UM_TENANT
                       WHERE UM_DOMAIN_NAME = `tenant_domain`
                       UNION (SELECT '-1234')
                       ORDER BY UM_ID DESC LIMIT 1)