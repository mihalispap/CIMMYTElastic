#!/bin/bash

#curl -XPUT 'localhost:9201/cimmyt?pretty'
#curl -XPUT 'localhost:9201/cimmyt/collection/1?pretty' -d '{"name":"beta"}'
curl -XPOST 'localhost:9201/cimmyt/collection/1/_update/?pretty' -d '{"doc":{"fname":"addition"}}'
