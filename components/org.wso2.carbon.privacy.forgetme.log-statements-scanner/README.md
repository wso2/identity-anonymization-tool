# Log Statements Scanner Tool

Log statements scanner tool is a shell script which can be used to get a dump of log statements in a source directory.

## How to Run

1. Go to the parent directory of the source code directory and issue the following command in the terminal.

```bash
sh <PATH_TO_IDENTITY_ANONYMIZATION_TOOL>/components/org.wso2.carbon.privacy.forgetme.log-statements-scanner/src/main/bin/logs_scanner.sh <SOURCE_DIR>
```

Ex:
```bash
sh ./identity-anonymization-tool/components/org.wso2.carbon.privacy.forgetme.log-statements-scanner/src/main/bin/logs_scanner.sh carbon-analytics-common
```

* This will create a file named as `<SOURCE_DIR>-logs.txt` in the current working directory which contains all the log statements of the directory.

