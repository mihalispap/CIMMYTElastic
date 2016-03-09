#!/bin/bash

content="$(cat data/4592.json)"

#TODO: think about given id, perhaps strip that from handle.net? seems like a good choice
echo "curl -XPUT 'http://localhost:9201/cimmyt/collection/4592' -d '${content}'"
