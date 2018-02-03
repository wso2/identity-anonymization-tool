UPDATE IDN_OAUTH2_AUTHORIZATION_CODE
SET AUTHZ_USER = `pseudonym`, SUBJECT_IDENTIFIER = `pseudonym`
WHERE AUTHZ_USER = `username`
      AND USER_DOMAIN = `user_store_domain`
      AND TENANT_ID = (SELECT UM_ID
                       FROM UM_TENANT
                       WHERE UM_DOMAIN_NAME = `tenant_domain`
                       UNION (SELECT '-1234')
                       ORDER BY UM_ID DESC LIMIT 1)