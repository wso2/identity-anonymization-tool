UPDATE UM_ACCOUNT_MAPPING
SET UM_USER_NAME = `pseudonym`
WHERE UM_USER_NAME = `username`
	  AND UM_USER_STORE_DOMAIN = `user_store_domain`
      AND UM_TENANT_ID = `tenant_id`
