#!/bin/bash

wget -O file http://www.cbi.cnptia.embrapa.br/cgi-bin/SMS/xlsFile/excelFile.pl?$1

sed -i '15,$ d' file
sed -i '1,13 d' file
sed -i 's/<a href="//' file
sed -i 's/".*//' file
link=`cat file`
IFS="," read -ra array <<< "$1"
wget "http://www.cbi.cnptia.embrapa.br"$link -O "${array[0]}-${array[1]}.xls"
