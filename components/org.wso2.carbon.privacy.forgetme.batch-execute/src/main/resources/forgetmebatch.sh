#!/usr/bin/env bash

# resolve links - $0 may be a softlink
PRG="$0"

# Get the file name from command line arguments.
file_name=$1

# Validate file name since it is mandatory.
if [ -z "$file_name" ]; then
    echo "Please provide file path as the first command line argument."
    exit 0
fi

while [ -h "$PRG" ]; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '.*/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`/"$link"
    fi
done

# Get standard environment variables.
PRGDIR=`dirname "$PRG"`

[ -z "$HOME_DIR" ] && HOME_DIR=`cd "$PRGDIR/.." ; pwd`

# 'col5' will read all the junk after col4. So we have to keep and ignore it.
while IFS=, read -r col1 col2 col3 col4 col5
do
    echo "Executing for $col1,$col2,$col3,$col4"
    sh ${HOME_DIR}/bin/forget-me -U $col1 -D $col2 -T $col3 -TID $col4
done < ${file_name}