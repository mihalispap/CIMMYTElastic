#!/bin/bash

content="$(cat data/cimmyt_resource3664NOLB.json)"

#TODO: think about given id, perhaps strip that from handle.net? seems like a good choice
echo "curl -XPUT 'http://localhost:9201/cimmyt/collection/4592' -d '${content}'"
