#!/bin/bash

content="$(cat data/11529_10201.entity.json)"

#TODO: think about given id, perhaps strip that from handle.net? seems like a good choice
echo "curl -XPUT 'http://localhost:9201/cimmyt/beta/12' -d '${content}'"

curl -XPUT 'http://localhost:9201/cimmyt/collection/12' -d "${content}"

#cat data/11529_10201.entity.json | curl -XPUT 'http://localhost:9201/cimmyt/collection/12' -d
