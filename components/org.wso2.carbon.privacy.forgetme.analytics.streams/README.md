# Persisted Strams in DAS 3.1.0

This component can be used to anonymize PII in Persisted streams in WSO2 Data Analytics Server 3.1.0.

## How to Run?

* Users need to define the streams and each attribute which contains PII in tool's extension configuration directory. 

##### streams.json

````json
{
    "streams": [
        {
            "streamName": "org.wso2.gdpr.students",
            "attributes": ["username", "email", "dateOfBirth"],
            "id": "username"
        },
        {
            "streamName": "org.wso2.gdpr.students.marks",
            "attributes": ["username"],
            "id": "username"
        }
    ]
}
````
- **streamName**: Name of the stream
- **attributes**: List of attributes which contains PII
- **id**: ID attribute which needs to be replaced by the value of pseudonym argument when executing the tool

* Then the `analytics-stream` processor needs to be added into the tool's configuration file.

##### config.json
````json
{
    "processors": [
        "analytics-streams"
    ],
    "directories": [
        {
            "dir": "analytics-streams",
            "type": "analytics-streams",
            "processor": "analytics-streams"
        }
    ]
}
````

* Then the user can execute the forget-me tool pointing to the `DAS_HOME` directory.