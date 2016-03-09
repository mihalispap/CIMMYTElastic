#!/bin/bash

wget https://download.elasticsearch.org/elasticsearch/elasticsearch/elasticsearch-1.5.2.deb

sudo dpkg -i elasticsearch-1.5.2.deb

#CONFIGURE ES
sudo pico /etc/elasticsearch/elasticsearch.yml

sudo /usr/share/elasticsearch/bin/plugin -install mobz/elasticsearch-head
sudo /usr/share/elasticsearch/bin/plugin -install lukas-vlcek/bigdesk
sudo /usr/share/elasticsearch/bin/plugin -install elasticsearch/elasticsearch-analysis-icu/2.5.0

sudo service elasticsearch start
