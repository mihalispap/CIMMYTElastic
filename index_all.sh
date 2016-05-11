#!/bin/bash


for f in json/*.json;
#for f in beta/*.json;
do
	sed -i 's/"date": 2\(.*\),/"date": "2\1",/g' $f
	sed -i 's/"date": 1\(.*\),/"date": "1\1",/g' $f
	./index_json.sh $f
done
