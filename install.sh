#!/bin/bash

wget https://download.elasticsearch.org/elasticsearch/elasticsearch/elasticsearch-1.5.2.deb

sudo dpkg -i elasticsearch-1.5.2.deb

#CONFIGURE ES

#cluster.name: cimmyt
#node.name: cimmyt_node
#node.rack: local
#node.zone: main
#http.port: 9201

sudo pico /etc/elasticsearch/elasticsearch.yml

sudo /usr/share/elasticsearch/bin/plugin -install mobz/elasticsearch-head
sudo /usr/share/elasticsearch/bin/plugin -install lukas-vlcek/bigdesk
sudo /usr/share/elasticsearch/bin/plugin -install elasticsearch/elasticsearch-analysis-icu/2.5.0

sudo service elasticsearch start
