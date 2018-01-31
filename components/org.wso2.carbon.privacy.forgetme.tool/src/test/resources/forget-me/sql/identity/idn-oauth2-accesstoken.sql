UPDATE IDN_OAUTH2_ACCESS_TOKEN SET AUTHZ_USER = `pseudonym`, SUBJECT_IDENTIFIER = `pseudonym`
WHERE SUBJECT_IDENTIFIER = `username` AND USER_DOMAIN = `user_store_domain` AND TENANT_ID = `tenant_domain`
