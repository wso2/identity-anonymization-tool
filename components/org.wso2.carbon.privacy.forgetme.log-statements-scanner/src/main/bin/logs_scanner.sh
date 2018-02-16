#!/bin/bash

echo "Searching for logs in ${1}..."

# Possible variable names for the logger
loggerVariableNames=( "log" "LOG" "logger" "LOGGER" )

# Log message types
logTypes=( "info" "debug" "error" )

# Output file
outputFile="${1}-logs.txt"

# Find all the possible combinations of above and dump the results in to the output file.
for logType in ${logTypes[@]}
do
    for loggerVariableName in ${loggerVariableNames[@]}
    do
        grep -r "${loggerVariableName}.${logType}" $1 >> $outputFile
    done
    printf "\n\n" >> $outputFile
done

echo "Finished searching for logs in ${1}. For found logs please check ${outputFile} file."
