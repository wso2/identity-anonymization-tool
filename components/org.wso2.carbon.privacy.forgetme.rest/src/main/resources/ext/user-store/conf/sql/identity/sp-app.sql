UPDATE SP_APP
SET USERNAME = `pseudonym`
WHERE USERNAME = `username`
      AND USER_STORE = `user_store_domain`
      AND TENANT_ID = `tenant_id`