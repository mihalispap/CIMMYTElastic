#!/bin/bash


for f in json/*.json;
do
	./index_json.sh $f
done
