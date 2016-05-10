#!/bin/bash

content="$(head -n 1 $1)"

type="$(echo ${content} | sed 's/{\"\(.*\)\": {/\1/')"
content="$(cat $1 | sed 's/{\"$type\": {/{/' | sed 's/}}/}/')"
#echo "cat $1 | sed 's/{\"$type\": {/{/' | sed 's/}}/}/'"

cat $1 | sed "s/{\"$type\": {/{/" | sed 's/}}/}/' > tmp
content="$(cat tmp)"

id="$(cat $1 | grep \"appid\": | sed 's/\"appid\":\s\{1,\}//' | sed 's/\"//' | sed 's/\s\{1,\}//' | sed 's/,//' | sed 's/"//')"

#appuri="$(cat $1 | grep \"appuri\": | sed 's/\"appuri\":\s\{1,\}//' | sed 's/\"//' | sed 's/\s\{1,\}//' | sed 's/,//' | sed 's/"//')"

#echo '|'$id'|'

#exit

#TODO: think about given id, perhaps strip that from handle.net? seems like a good choice
#echo "curl -XPUT 'http://localhost:9201/cimmyt/beta/12' -d '${content}'"

echo 'http://localhost:9201/cimmyt/'$type'/'$id
#exit

#curl -XPUT 'http://localhost:9201'$appuri -d "${content}"

if [ "$type" == "object" ]; then
	curl -XPUT 'http://localhost:9201/cimmyt/'$type'/'${id} -d "${content}"
	exit
fi

curl -XPUT 'http://localhost:9201/cimmyt/'$type'/'${id}'?parent='${id} -d "${content}"

#cat data/11529_10201.entity.json | curl -XPUT 'http://localhost:9201/cimmyt/collection/12' -d
