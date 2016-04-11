#!/bin/bash

content="$(cat index.object | tr -d '\n' | tr -d '\t')"

curl -XPOST localhost:9201/cimmyt -d '${content}'
echo "curl -XPOST localhost:9201/cimmyt -d '${content}'"
