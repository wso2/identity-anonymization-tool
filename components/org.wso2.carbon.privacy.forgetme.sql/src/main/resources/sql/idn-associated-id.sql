UPDATE IDN_ASSOCIATED_ID
SET USER_NAME = `pseudonym`
WHERE USER_NAME = `username`
      AND DOMAIN_NAME = `user_store_domain`
      AND TENANT_ID = (SELECT UM_ID
                       FROM UM_TENANT
                       WHERE UM_DOMAIN_NAME = `tenant_domain`
                       UNION (SELECT '-1234')
                       ORDER BY UM_ID DESC LIMIT 1)