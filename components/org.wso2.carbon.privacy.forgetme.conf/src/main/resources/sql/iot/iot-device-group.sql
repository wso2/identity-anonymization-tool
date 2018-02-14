UPDATE DM_GROUP
SET OWNER = `pseudonym`
WHERE OWNER = `username`
    AND TENANT_ID = `tenant_id`
