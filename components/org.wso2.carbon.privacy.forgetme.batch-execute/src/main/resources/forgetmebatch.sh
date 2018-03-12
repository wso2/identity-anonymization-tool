#
# Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
#
# WSO2 Inc. licenses this file to you under the Apache License,
# Version 2.0 (the "License"); you may not use this file except
# in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.
#

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