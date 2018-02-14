UPDATE DM_ENROLMENT
SET OWNER = `pseudonym`
WHERE OWNER = `username`
    AND TENANT_ID = `tenant_id`
